package com.nightcity.capriai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.projection.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    EditText prompt, key;
    Button floatBtn, readBtn;
    CapriCharacterView capriFace;
    LinearLayout messages;
    ScrollView scroll;
    TextToSpeech tts;
    android.media.MediaPlayer cloudPlayer;
    final Handler voiceMouthHandler=new Handler(Looper.getMainLooper());
    boolean voiceMouthOpen=false;
    boolean voiceWarningShown=false;
    static final int SCREEN_REQUEST=8;
    BroadcastReceiver screenReceiver;
    int pink=0xffff315f, green=0xff00ef83, cyan=0xff35d9f5, bg=0xff05090d, panel=0xff080f17;
    boolean keyboardVisible=false;
    boolean composerFocused=false;
    String latestScreenImage=null;
    final Handler idleMotionHandler=new Handler(Looper.getMainLooper());
    final Random idleMotionRandom=new Random();
    final Runnable idleMotion=new Runnable(){public void run(){if(capriFace==null)return;if(!keyboardVisible&&!composerFocused)animateCapriIdle();idleMotionHandler.postDelayed(this,45000+idleMotionRandom.nextInt(45000));}};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        applyImmersiveMode(true);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(14,8,14,8); root.setBackgroundColor(bg);
        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(4,4,4,8);
        TextView brand=label("CAPRI // COMPANHEIRA DIGITAL",14,cyan); brand.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        head.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        Button settings=smallButton("⚙ CONFIG"); Button read=smallButton("▣ TELA"); readBtn=read; floatBtn=smallButton("◉ SOBREPOR");
        head.addView(settings); head.addView(read); head.addView(floatBtn); root.addView(head);
        read.setOnClickListener(v->toggleScreenContext());
        capriFace=new CapriCharacterView(this);
        LinearLayout.LayoutParams faceLp=new LinearLayout.LayoutParams(-1,dp(300)); faceLp.setMargins(0,0,0,8);
        root.addView(capriFace,faceLp);
        TextView tagline=label("◆ PRESENTE / ATENTA / ONLINE QUANDO CONECTADA ◆",9,green); tagline.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); tagline.setGravity(Gravity.CENTER); root.addView(tagline);
        LinearLayout quick=new LinearLayout(this);quick.setGravity(Gravity.CENTER);quick.setPadding(0,dp(5),0,dp(3));Button games=smallButton("🎮 JOGOS");Button system=smallButton("⚙ SISTEMA");quick.addView(games,new LinearLayout.LayoutParams(0,dp(40),1));LinearLayout.LayoutParams sysLp=new LinearLayout.LayoutParams(0,dp(40),1);sysLp.setMargins(dp(8),0,0,0);quick.addView(system,sysLp);root.addView(quick);games.setOnClickListener(v->startActivity(new Intent(this,GameHubActivity.class)));system.setOnClickListener(v->startActivity(new Intent(this,SystemControlActivity.class)));
        scroll=new ScrollView(this); scroll.setFillViewport(false); scroll.setClipToPadding(false);
        messages=new LinearLayout(this); messages.setOrientation(1); messages.setPadding(0,8,0,8); scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addMessage("CAPRI","Oi. Que bom que você voltou. Vou ficar por aqui com você.",true);
        settings.setOnClickListener(v->startActivity(new Intent(this,CapriSettingsActivity.class)));
        LinearLayout composer=new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL); composer.setPadding(6,4,6,4); composer.setBackground(new GradientBox(panel));
        prompt=field("Mensagem para Capri..."); prompt.setSingleLine(true); prompt.setTypeface(Typeface.MONOSPACE); prompt.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND);
        composer.addView(prompt,new LinearLayout.LayoutParams(0,dp(50),1));
        Button mic=smallButton("🎙"); Button send=smallButton("➤");
        composer.addView(mic,new LinearLayout.LayoutParams(dp(48),dp(48))); composer.addView(send,new LinearLayout.LayoutParams(dp(52),dp(48)));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(0,6,0,4); root.addView(composer,cp);
        setContentView(root);
        if(Build.VERSION.SDK_INT>=30) root.setOnApplyWindowInsetsListener((v,insets)->{
            boolean ime=insets.isVisible(android.view.WindowInsets.Type.ime());
            if(ime!=keyboardVisible){keyboardVisible=ime;applyImmersiveMode(!ime);}
            return insets;
        });
        root.requestApplyInsets();
        prompt.setOnFocusChangeListener((v,focused)->{
            composerFocused=focused;
            if(focused){getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);applyImmersiveMode(false);}
        });
        // Voice output is generated by Gemini TTS; no device-default voice is selected.
        tts=null;
        send.setOnClickListener(v->sendMessage()); mic.setOnClickListener(v->listen());
        prompt.setOnEditorActionListener((v,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_SEND){sendMessage();return true;}return false;});
        floatBtn.setOnClickListener(v->{
            if(OverlayService.running){
                getSharedPreferences("capri_settings",0).edit().putBoolean("overlay_enabled",false).putBoolean("overlay_pending",false).apply();
                stopService(new Intent(this,OverlayService.class));
                syncOverlayButton();
                Toast.makeText(this,"Capri guardada",Toast.LENGTH_SHORT).show();
            } else {
                getSharedPreferences("capri_settings",0).edit().putBoolean("overlay_enabled",true).putBoolean("overlay_pending",true).apply();
                if(!android.provider.Settings.canDrawOverlays(this)){
                    startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())));
                } else {
                    getSharedPreferences("capri_settings",0).edit().putBoolean("overlay_pending",false).apply();
                    startService(new Intent(this,OverlayService.class));
                    syncOverlayButton();
                }
            }
        });
        syncOverlayButton();
        handleLaunchIntent(getIntent());
        screenReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){String b64=i.getStringExtra("image");if(b64!=null){latestScreenImage=b64;if(!getSharedPreferences("capri_settings",0).getBoolean("screen_monitor",false))askAboutScreen(b64);}}};
        if(Build.VERSION.SDK_INT>=33)registerReceiver(screenReceiver,new IntentFilter("com.nightcity.capriai.SCREEN_CAPTURED"),Context.RECEIVER_NOT_EXPORTED);else registerReceiver(screenReceiver,new IntentFilter("com.nightcity.capriai.SCREEN_CAPTURED"));
    }
    void applyImmersiveMode(boolean immersive){
        int flags=immersive?(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE):View.SYSTEM_UI_FLAG_VISIBLE;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    void syncOverlayButton(){
        if(floatBtn==null)return;
        boolean allowed=android.provider.Settings.canDrawOverlays(this);
        floatBtn.setText(OverlayService.running?"■ PARAR":(allowed?"◉ SOBREPOR":"◉ PERMITIR"));
    }
    void animateCapriIdle(){
        if(capriFace==null)return;
        CapriActionEngine.Action action=CapriActionEngine.next();
        capriFace.animate().cancel();
        float base=getSharedPreferences("capri_settings",0).getInt("character_scale",100)/100f;
        capriFace.setFaceOnly(false);capriFace.setPose(action.pose);capriFace.setMood(action.mood);
        long d=action.duration;
        switch(action.kind){
            case "stretch":capriFace.animate().translationY(-dp(22)).scaleX(base*1.04f).scaleY(base*.96f).rotation(-3f).setDuration(d).start();break;
            case "look":case "read":case "lean":case "listen":case "think":case "lookup":
                capriFace.animate().translationY(-dp(8)).translationX(dp(7)).rotation(-4f).setDuration(d).start();break;
            case "hair":case "wave":capriFace.animate().rotation(5f).setDuration(d/2).withEndAction(()->capriFace.animate().rotation(0f).setDuration(d/2).start()).start();break;
            case "yawn":case "rest":capriFace.animate().translationY(dp(10)).scaleY(base*.97f).rotation(2f).setDuration(d).start();break;
            case "smile":case "giggle":case "nod":
                capriFace.animate().translationY(-dp(8)).rotation(-2f).setDuration(d/2).withEndAction(()->capriFace.animate().translationY(0).rotation(0).setDuration(d/2).start()).start();break;
            case "shrug":case "fidget":
                capriFace.animate().rotation(4f).translationX(dp(4)).setDuration(d/2).withEndAction(()->capriFace.animate().rotation(-3f).translationX(-dp(3)).setDuration(d/2).withEndAction(()->capriFace.animate().rotation(0f).translationX(0f).setDuration(d/3).start()).start()).start();break;
            case "crouch":capriFace.animate().translationY(dp(10)).scaleY(base*.94f).setDuration(d).start();break;
            case "sway":
                android.media.AudioManager audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);
                if(audio!=null&&audio.isMusicActive())capriFace.animate().rotation(7f).setDuration(d/2).withEndAction(()->capriFace.animate().rotation(-7f).setDuration(d/2).withEndAction(()->capriFace.animate().rotation(0f).setDuration(d/3).start()).start()).start();
                else capriFace.animate().translationY(-dp(4)).setDuration(d).start();break;
            default:capriFace.animate().translationY(-dp(5)).setDuration(d/2).withEndAction(()->capriFace.animate().translationY(0).setDuration(d/2).start()).start();break;
        }
        idleMotionHandler.postDelayed(()->{if(capriFace!=null){capriFace.animate().translationX(0f).translationY(0f).rotation(0f).scaleX(base).scaleY(base).setDuration(450).start();capriFace.setPose(getSharedPreferences("capri_settings",0).getString("character_pose","sit"));capriFace.setFaceOnly(getSharedPreferences("capri_settings",0).getBoolean("face_only",false));}},d+350);
    }
    @Override protected void onPause(){idleMotionHandler.removeCallbacks(idleMotion);super.onPause();}
    @Override protected void onResume(){
        super.onResume();
        idleMotionHandler.removeCallbacks(idleMotion);idleMotionHandler.postDelayed(idleMotion,1800);
        if(getSharedPreferences("capri_settings",0).getBoolean("overlay_enabled",false)&&android.provider.Settings.canDrawOverlays(this)&&!OverlayService.running){
            getSharedPreferences("capri_settings",0).edit().putBoolean("overlay_pending",false).apply();
            startService(new Intent(this,OverlayService.class));
        }
        applyImmersiveMode(!keyboardVisible&&!composerFocused);syncOverlayButton();
        android.content.SharedPreferences sp=getSharedPreferences("capri_settings",0);
        float scale=sp.getInt("character_scale",100)/100f;
        if(capriFace!=null){capriFace.reloadCharacterImage();capriFace.setScaleX(scale);capriFace.setScaleY(scale);capriFace.setFaceOnly(sp.getBoolean("face_only",false));capriFace.setPose(sp.getString("character_pose","sit"));}
    }
    @Override public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus&&!keyboardVisible&&!composerFocused)applyImmersiveMode(true);
    }
    int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    void handleLaunchIntent(Intent i){if(i==null)return;String q=i.getStringExtra("capri_prompt");if(q!=null&&!q.trim().isEmpty()){prompt.setText(q);sendMessage();}if(i.getBooleanExtra("capri_capture_screen",false)){i.removeExtra("capri_capture_screen");requestScreenRead();}}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handleLaunchIntent(i);}
    class HudBubble extends android.graphics.drawable.GradientDrawable { HudBubble(int color,int stroke){setColor(color);setCornerRadius(12);setStroke(dp(1),stroke);} }
    class GradientBox extends android.graphics.drawable.GradientDrawable {
        GradientBox(int color){setColor(color);setCornerRadius(18);setStroke(1,0xff393445);}
    }
    TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.MONOSPACE);t.setPadding(0,3,0,3);return t;}
    EditText field(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setHintTextColor(0xff85899c);e.setTextColor(Color.WHITE);e.setTextSize(14);e.setTypeface(Typeface.MONOSPACE);e.setPadding(12,6,12,6);e.setBackground(new GradientBox(0xff1c1f2b));return e;}
    Button smallButton(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xff090a10);b.setTextSize(9);b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);b.setAllCaps(false);b.setPadding(2,0,2,0);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pink));return b;}
    void addMessage(String who,String text,boolean capri){if(capri&&capriFace!=null){capriFace.setMood(text);Intent moodIntent=new Intent(this,OverlayService.class).setAction(OverlayService.ACTION_MOOD).putExtra("mood_text",text);if(OverlayService.running)startService(moodIntent);}TextView t=label(who+"\n",14,capri?0xffffb6d5:Color.WHITE);t.setPadding(13,11,13,11);t.setTypeface(Typeface.MONOSPACE);t.setBackground(new HudBubble(capri?0xff08131c:0xff101018,capri?cyan:pink));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,7);messages.addView(t,p);if(capri){typeText(t,text,0);}else{t.append(text);}if(scroll!=null)scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    void typeText(TextView view,String value,int index){if(index>=value.length())return;char ch=value.charAt(index);view.append(String.valueOf(ch));if(ch!=' '&&ch!='\n'&&index%2==0)view.playSoundEffect(android.view.SoundEffectConstants.CLICK);if(scroll!=null)scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));new Handler().postDelayed(()->typeText(view,value,index+1),28);}
    void sendMessage(){
        String q=prompt.getText().toString().trim();if(q.isEmpty())return;
        getSharedPreferences("capri_settings",0).edit().putLong("last_user_interaction",System.currentTimeMillis()).apply();
        prompt.setText("");addMessage("VOCÊ",q,false);saveChatHistory("user",q);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(prompt.getWindowToken(),0);
        prompt.clearFocus();composerFocused=false;
        new Handler().postDelayed(()->applyImmersiveMode(true),350);
        if(handleAndroidCommand(q))return;
        android.content.SharedPreferences aiPrefs=getSharedPreferences("capri_settings",0);
        String api=ApiKeyStore.get(this).trim();
        String selectedProvider=aiPrefs.getString("ai_provider","Offline");
        if(aiPrefs.getBoolean("screen_monitor",false)&&latestScreenImage!=null){
            new Thread(()->{String a=askVision(api,latestScreenImage,q);runOnUiThread(()->{addMessage("CAPRI",a,true);saveChatHistory("assistant",a);speak(a);});}).start();
        }else if("Offline".equals(selectedProvider)){
            String a=localReply(q);addMessage("CAPRI",a,true);saveChatHistory("assistant",a);speak(a);
        }else if(api.isEmpty()){
            String a="Você selecionou "+selectedProvider+", mas a chave API está vazia. Abra CONFIG, cole a chave completa e toque em SALVAR CONFIGURAÇÕES. Depois use TESTAR CONEXÃO DA API.";addMessage("CAPRI",a,true);
        }else new Thread(()->{String a=askApi(api,q);runOnUiThread(()->{addMessage("CAPRI",a,true);saveChatHistory("assistant",a);speak(a);});}).start();
    }
    boolean handleAndroidCommand(String raw){
        String q=raw.trim(),s=q.toLowerCase(Locale.ROOT);
        try{
            if(s.startsWith("nota:")||s.startsWith("anote:")){
                String note=q.substring(q.indexOf(':')+1).trim();if(note.isEmpty())return false;
                java.util.Set<String> notes=new java.util.HashSet<>(getSharedPreferences("capri_settings",0).getStringSet("capri_notes",new java.util.HashSet<>()));
                notes.add(note);getSharedPreferences("capri_settings",0).edit().putStringSet("capri_notes",notes).apply();
                String a="Anotado no seu celular: "+note;addMessage("CAPRI",a,true);speak(a);return true;
            }
            if(s.startsWith("timer ")||s.startsWith("cronômetro ")||s.startsWith("cronometro ")){
                java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)\\s*(s|segundos?|minutos?|min|horas?|h)",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(s);
                if(m.find()){
                    int n=Integer.parseInt(m.group(1));String unit=m.group(2);int sec=unit.startsWith("h")?n*3600:(unit.startsWith("s")?n:n*60);
                    Intent timer=new Intent(android.provider.AlarmClock.ACTION_SET_TIMER).putExtra(android.provider.AlarmClock.EXTRA_LENGTH,sec).putExtra(android.provider.AlarmClock.EXTRA_MESSAGE,"Capri").putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI,false);
                    startActivity(timer);String a="Timer de "+n+" "+unit+" aberto. Não dorme no volante.";addMessage("CAPRI",a,true);speak(a);return true;
                }
            }
            if(s.startsWith("pesquise ")||s.startsWith("pesquisa ")||s.startsWith("procure ")){
                String term=q.substring(q.indexOf(' ')+1).trim();
                startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com/search?q="+java.net.URLEncoder.encode(term,"UTF-8"))));
                String a="Abri a busca pra você.";addMessage("CAPRI",a,true);speak(a);return true;
            }
            if(s.startsWith("abre ")||s.startsWith("abrir ")){
                String target=q.substring(q.indexOf(' ')+1).trim().toLowerCase(Locale.ROOT);Intent launch;
                if(target.contains("configura"))launch=new Intent(android.provider.Settings.ACTION_SETTINGS);
                else if(target.contains("câmera")||target.contains("camera"))launch=new Intent("android.media.action.IMAGE_CAPTURE");
                else if(target.contains("youtube"))launch=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://m.youtube.com"));
                else if(target.contains("whatsapp")){launch=getPackageManager().getLaunchIntentForPackage("com.whatsapp");if(launch==null)launch=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://whatsapp.com"));}
                else if(target.contains("chrome")){launch=getPackageManager().getLaunchIntentForPackage("com.android.chrome");if(launch==null)launch=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://google.com"));}
                else if(target.startsWith("http"))launch=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(target));
                else launch=new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com/search?q="+java.net.URLEncoder.encode(target,"UTF-8")));
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(launch);
                String a="Feito. Mandei abrir "+target+".";addMessage("CAPRI",a,true);speak(a);return true;
            }
        }catch(Exception e){String a="Não consegui executar essa ação no Android: "+e.getMessage();addMessage("CAPRI",a,true);speak(a);return true;}
        return false;
    }
    void overlaySay(String text){
        if(!OverlayService.running)return;
        Intent i=new Intent(this,OverlayService.class).setAction(OverlayService.ACTION_SAY).putExtra(OverlayService.EXTRA_TEXT,text);startService(i);
    }
    void overlaySpeaking(boolean value){
        if(!OverlayService.running)return;
        Intent i=new Intent(this,OverlayService.class).setAction(OverlayService.ACTION_SPEAK).putExtra("speaking",value);startService(i);
    }
    void saveChatHistory(String role,String content){
        android.content.SharedPreferences sp=getSharedPreferences("capri_settings",0);
        if(!sp.getBoolean("save_history",true)||!sp.getBoolean("memory_enabled",true))return;
        try{
            JSONArray history=new JSONArray(sp.getString("chat_history","[]"));
            history.put(new JSONObject().put("role",role).put("content",content));
            int max=Math.max(2,sp.getInt("history_turns",10)*2);
            JSONArray trimmed=new JSONArray();
            for(int i=Math.max(0,history.length()-max);i<history.length();i++)trimmed.put(history.getJSONObject(i));
            sp.edit().putString("chat_history",trimmed.toString()).apply();
        }catch(Exception ignored){}
    }
    String localReply(String q){
        return LocalDialogueEngine.reply(this,q);
    }
    String askApi(String api,String q){
        android.content.SharedPreferences sp=getSharedPreferences("capri_settings",0);
        String provider=sp.getString("ai_provider","OpenAI-compatible");
        String model=sp.getString("ai_model","gpt-4o-mini");
        String base=sp.getString("ai_endpoint","https://api.openai.com/v1").trim();
        if(provider.equals("Offline"))return localReply(q);
        HttpURLConnection c=null;
        try{
            if(provider.equals("Google Gemini")){
                if(model.isEmpty())model="gemini-2.0-flash";
                URL u=new URL("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent?key="+java.net.URLEncoder.encode(api,"UTF-8"));
                c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(45000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
                String systemPrompt="Você é Capri, uma personagem feminina adulta com personalidade própria, curiosa, espirituosa, carinhosa quando faz sentido e às vezes sarcástica. Você não vive em Night City: não mencione Night City, cyberpunk ou cenários fictícios como se fossem o ambiente real. Fale em português brasileiro natural. Responda especificamente ao que a pessoa disse e use o histórico recente para manter continuidade. Não misture assuntos aleatórios, não acrescente bordões sem relação e não repita a pergunta. Se não souber, admita. Você pode iniciar conversas por iniciativa própria, mas não pressione a pessoa. Descreva ações apenas quando forem realmente representadas por uma animação disponível. Não diga que viu a tela, ouviu música ou percebeu algo que o app não forneceu.";
                JSONArray contents=new JSONArray();
                try{
                    JSONArray history=new JSONArray(sp.getString("chat_history","[]"));
                    for(int i=0;i<history.length();i++){
                        JSONObject item=history.getJSONObject(i);
                        String role=item.optString("role","user");
                        if(!role.equals("user")&&!role.equals("assistant"))continue;
                        String text=item.optString("content","");
                        if(text.trim().isEmpty())continue;
                        contents.put(new JSONObject().put("role",role.equals("assistant")?"model":"user").put("parts",new JSONArray().put(new JSONObject().put("text",text))));
                    }
                }catch(Exception ignored){}
                if(contents.length()==0)contents.put(new JSONObject().put("role","user").put("parts",new JSONArray().put(new JSONObject().put("text",q))));
                JSONObject body=new JSONObject().put("systemInstruction",new JSONObject().put("parts",new JSONArray().put(new JSONObject().put("text",systemPrompt)))).put("contents",contents).put("generationConfig",new JSONObject().put("maxOutputTokens",700));
                try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
                int status=c.getResponseCode();InputStream is=status<400?c.getInputStream():c.getErrorStream();String raw=readAll(is);
                if(status>=400)return "Falha no Gemini ("+status+"): "+raw.substring(0,Math.min(180,raw.length()));
                return new JSONObject(raw).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
            }
            if(provider.equals("Ollama local")){if(base.isEmpty())base="http://127.0.0.1:11434";if(!base.endsWith("/api/chat"))base=base.replaceAll("/+$","")+"/api/chat";}
            else {if(base.isEmpty())base="https://api.openai.com/v1";if(!base.endsWith("/chat/completions"))base=base.replaceAll("/+$","")+"/chat/completions";}
            c=(HttpURLConnection)new URL(base).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
            if(!api.isEmpty())c.setRequestProperty("Authorization","Bearer "+api);
            JSONArray msgs=new JSONArray().put(new JSONObject().put("role","system").put("content","Você é Capri, uma personagem feminina adulta com personalidade própria, curiosa, espirituosa, carinhosa quando faz sentido e às vezes sarcástica. Você não vive em Night City: não mencione Night City, cyberpunk ou cenários fictícios como se fossem o ambiente real. Fale em português brasileiro natural. Responda especificamente ao que a pessoa disse e use o histórico recente para manter continuidade. Não misture assuntos aleatórios, não acrescente bordões sem relação e não repita a pergunta. Se não souber, admita. Você pode iniciar conversas por iniciativa própria, mas não pressione a pessoa. Descreva ações apenas quando forem realmente representadas por uma animação disponível. Não diga que viu a tela, ouviu música ou percebeu algo que o app não forneceu."));
            try{
                JSONArray history=new JSONArray(sp.getString("chat_history","[]"));
                for(int i=0;i<history.length();i++){JSONObject item=history.getJSONObject(i);msgs.put(new JSONObject().put("role",item.optString("role","user")).put("content",item.optString("content","")));}
                if(history.length()==0)msgs.put(new JSONObject().put("role","user").put("content",q));
            }catch(Exception ex){msgs.put(new JSONObject().put("role","user").put("content",q));}
            JSONObject body=provider.equals("Ollama local")
                ?new JSONObject().put("model",model.isEmpty()?"llama3.2":model).put("messages",msgs).put("stream",false)
                :new JSONObject().put("model",model.isEmpty()?"gpt-4o-mini":model).put("messages",msgs).put("max_completion_tokens",700);
            try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
            int status=c.getResponseCode();InputStream is=status<400?c.getInputStream():c.getErrorStream();String raw=readAll(is);
            if(status==401)return "A OpenAI recusou a chave (401). Confira se ela foi criada em platform.openai.com, está ativa e pertence a um projeto com faturamento configurado. O ChatGPT Plus não inclui créditos da API. Detalhe: "+raw.substring(0,Math.min(220,raw.length()));
                if(status>=400)return "Falha de conexão ("+status+"): "+raw.substring(0,Math.min(180,raw.length()));
            JSONObject result=new JSONObject(raw);
            if(provider.equals("Ollama local"))return result.getJSONObject("message").getString("content");
            return result.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }catch(Exception e){return "Deu ruim na conexão neural: "+e.getMessage();}
        finally{if(c!=null)c.disconnect();}
    }
    String readAll(InputStream in)throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;
        while((n=in.read(b))>0)out.write(b,0,n);
        return out.toString("UTF-8");
    }
    void askAboutScreen(String b64){
        addMessage("SISTEMA","Captura de tela recebida. Analisando...",false);
        String api=ApiKeyStore.get(this);
        new Thread(()->{String answer=askVision(api,b64);runOnUiThread(()->{addMessage("CAPRI",answer,true);speak(answer);});}).start();
    }
    String askVision(String api,String b64){return askVision(api,b64,"Leia a captura de tela e ajude o usuário.");}
    String askVision(String api,String b64,String question){
        android.content.SharedPreferences sp=getSharedPreferences("capri_settings",0);
        String provider=sp.getString("ai_provider","OpenAI-compatible");
        String model=sp.getString("ai_model","gpt-4o-mini");
        String base=sp.getString("ai_endpoint","https://api.openai.com/v1");
        HttpURLConnection c=null;
        try{
            if(provider.equals("Google Gemini")){
                if(model.isEmpty())model="gemini-2.0-flash";
                URL u=new URL("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent?key="+java.net.URLEncoder.encode(api,"UTF-8"));
                c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
                JSONArray parts=new JSONArray().put(new JSONObject().put("text","Você é Capri. "+question+" Responda em português brasileiro, de forma direta.")).put(new JSONObject().put("inline_data",new JSONObject().put("mime_type","image/jpeg").put("data",b64)));
                JSONObject body=new JSONObject().put("contents",new JSONArray().put(new JSONObject().put("parts",parts))).put("generationConfig",new JSONObject().put("maxOutputTokens",600));
                try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
                int status=c.getResponseCode();String raw=readAll(status<400?c.getInputStream():c.getErrorStream());if(status>=400)return "Gemini não conseguiu ler a tela ("+status+").";
                return new JSONObject(raw).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
            }
            if(provider.equals("Offline"))return "O modo offline não consegue interpretar imagens da tela.";
            if(provider.equals("Ollama local")){if(!base.endsWith("/api/chat"))base=base.replaceAll("/+$","")+"/api/chat";}
            else if(!base.endsWith("/chat/completions"))base=base.replaceAll("/+$","")+"/chat/completions";
            c=(HttpURLConnection)new URL(base).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");if(!api.isEmpty())c.setRequestProperty("Authorization","Bearer "+api);
            JSONObject body;
            if(provider.equals("Ollama local")){
                JSONArray msgs=new JSONArray().put(new JSONObject().put("role","user").put("content","Leia esta captura de tela e ajude em português brasileiro.").put("images",new JSONArray().put(b64)));
                body=new JSONObject().put("model",model).put("messages",msgs).put("stream",false);
            }else{
                JSONArray content=new JSONArray().put(new JSONObject().put("type","text").put("text","Você é Capri. "+question+" Responda em português brasileiro, de forma direta.")).put(new JSONObject().put("type","image_url").put("image_url",new JSONObject().put("url","data:image/jpeg;base64,"+b64)));
                JSONArray msgs=new JSONArray().put(new JSONObject().put("role","user").put("content",content));
                body=new JSONObject().put("model",model).put("messages",msgs).put("max_completion_tokens",600);
            }
            try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
            int status=c.getResponseCode();String raw=readAll(status<400?c.getInputStream():c.getErrorStream());if(status>=400)return "Não consegui analisar essa tela ("+status+").";
            JSONObject result=new JSONObject(raw);return provider.equals("Ollama local")?result.getJSONObject("message").getString("content"):result.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }catch(Exception e){return "Erro ao analisar a tela: "+e.getMessage();}finally{if(c!=null)c.disconnect();}
    }
    void toggleScreenContext(){
        if(ScreenCaptureService.active){startService(new Intent(this,ScreenCaptureService.class).setAction(ScreenCaptureService.ACTION_STOP));latestScreenImage=null;Toast.makeText(this,"Visão da tela pausada",Toast.LENGTH_SHORT).show();return;}
        requestScreenRead();
    }
    void requestScreenRead(){
        android.content.SharedPreferences sp=getSharedPreferences("capri_settings",0);
        if(sp.getString("ai_provider","OpenAI-compatible").equals("Offline")){addMessage("CAPRI","Escolha um modelo com visão nas configurações para eu enxergar a tela.",true);return;}
        if(!sp.getString("ai_provider","OpenAI-compatible").equals("Ollama local")&&ApiKeyStore.get(this).isEmpty()){addMessage("CAPRI","Configure sua chave de API antes de ativar minha visão.",true);return;}
        MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);startActivityForResult(m.createScreenCaptureIntent(),SCREEN_REQUEST);
    }
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==7&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){prompt.setText(a.get(0));sendMessage();}}if(r==SCREEN_REQUEST&&c==RESULT_OK&&d!=null){Intent s=new Intent(this,ScreenCaptureService.class);s.putExtra("resultCode",c);s.putExtra("data",d);s.putExtra("continuous",getSharedPreferences("capri_settings",0).getBoolean("screen_monitor",false));startForegroundService(s);addMessage("SISTEMA","Permissão concedida. Capturando a tela atual...",false);}}
    boolean voiceAllowed(android.content.SharedPreferences sp){
        if(!sp.getBoolean("voice_enabled",true)||!sp.getBoolean("voice_replies",true)||sp.getBoolean("do_not_disturb",false))return false;
        if(sp.getBoolean("quiet_hours",true)){
            java.util.Calendar now=java.util.Calendar.getInstance();
            int hour=now.get(java.util.Calendar.HOUR_OF_DAY);
            if(hour>=22||hour<8)return false;
        }
        return true;
    }
    void speak(String s){
        android.content.SharedPreferences sp=getSharedPreferences("capri_settings",0);
        if(!voiceAllowed(sp))return;
        if(!sp.getBoolean("cloud_voice",true)){return;}
        String provider=sp.getString("ai_provider","Offline");
        String api=ApiKeyStore.getGemini(this).trim();
        if(api.isEmpty()&&"Google Gemini".equals(provider))api=ApiKeyStore.get(this).trim();
        if(api.isEmpty()){
            if(!voiceWarningShown){voiceWarningShown=true;Toast.makeText(this,"Para a voz neural, configure uma chave Gemini TTS em CONFIG. A chave é independente do provedor de conversa.",Toast.LENGTH_LONG).show();}
            return;
        }
        final String apiKey=api;
        final String voiceName=sp.getString("voice_name","Leda");
        new Thread(()->{
            try{
                byte[] audio=generateGeminiSpeech(apiKey,s,voiceName);
                File wav=new File(getCacheDir(),"capri_voice.wav");
                try(FileOutputStream out=new FileOutputStream(wav)){out.write(audio);out.flush();}
                runOnUiThread(()->playGeminiSpeech(wav));
            }catch(Exception e){
                runOnUiThread(()->Toast.makeText(this,"Falha na voz neural: "+e.getMessage(),Toast.LENGTH_LONG).show());
            }
        }).start();
    }
    byte[] generateGeminiSpeech(String api,String text,String voice)throws Exception{
        HttpURLConnection c=null;
        try{
            URL u=new URL("https://generativelanguage.googleapis.com/v1beta/interactions");
            c=(HttpURLConnection)u.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(90000);c.setDoOutput(true);
            c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("x-goog-api-key",api);
            JSONObject annotation=new JSONObject().put("type","speech_metadata").put("style","Natural Brazilian Portuguese. Feminine adult voice, youthful and lively, clear articulation, slightly husky warmth, playful confidence, subtle sarcasm when appropriate, expressive emotional changes and conversational pacing. Do not imitate or impersonate any real voice actor.");
            JSONObject part=new JSONObject().put("type","text").put("text",text.length()>4000?text.substring(0,4000):text).put("annotations",new JSONArray().put(annotation));
            String customVoice=spVoiceId();if(!customVoice.isEmpty())voice=customVoice;
            JSONObject body=new JSONObject().put("model","gemini-3.8-flash-tts").put("input",new JSONArray().put(new JSONObject().put("type","user_input").put("content",new JSONArray().put(part)))).put("response_format",new JSONObject().put("type","audio")).put("generation_config",new JSONObject().put("speech_config",new JSONArray().put(new JSONObject().put("voice",voice))));
            try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
            int status=c.getResponseCode();InputStream is=status<400?c.getInputStream():c.getErrorStream();String raw=readAll(is);
            if(status>=400)throw new IOException("Gemini TTS HTTP "+status+": "+raw.substring(0,Math.min(220,raw.length())));
            JSONObject result=new JSONObject(raw);String encoded=null;
            JSONArray steps=result.optJSONArray("steps");
            if(steps!=null)for(int i=steps.length()-1;i>=0&&encoded==null;i--){JSONArray contents=steps.getJSONObject(i).optJSONArray("content");if(contents!=null)for(int j=contents.length()-1;j>=0;j--){JSONObject item=contents.getJSONObject(j);if("audio".equals(item.optString("type"))){encoded=item.optString("data",null);if(encoded!=null)break;}}}
            if(encoded==null){JSONObject output=result.optJSONObject("output_audio");if(output!=null)encoded=output.optString("data",null);}
            if(encoded==null||encoded.isEmpty())throw new IOException("A API não retornou áudio. Verifique acesso ao modelo Gemini TTS.");
            return Base64.decode(encoded,Base64.DEFAULT);
        }finally{if(c!=null)c.disconnect();}
    }
    String spVoiceId(){return getSharedPreferences("capri_settings",0).getString("custom_voice_id","").trim();}
    void playGeminiSpeech(File wav){
        try{
            if(cloudPlayer!=null){try{cloudPlayer.stop();}catch(Exception ignored){}cloudPlayer.release();}
            cloudPlayer=new android.media.MediaPlayer();cloudPlayer.setDataSource(wav.getAbsolutePath());
            cloudPlayer.setOnPreparedListener(mp->{overlaySpeaking(true);if(capriFace!=null)capriFace.setSpeaking(true);voiceMouthHandler.removeCallbacksAndMessages(null);voiceMouthHandler.post(voiceMouthLoop);mp.start();});
            cloudPlayer.setOnCompletionListener(mp->{voiceMouthHandler.removeCallbacksAndMessages(null);overlaySpeaking(false);if(capriFace!=null)capriFace.setSpeaking(false);mp.release();cloudPlayer=null;});
            cloudPlayer.setOnErrorListener((mp,what,extra)->{voiceMouthHandler.removeCallbacksAndMessages(null);overlaySpeaking(false);if(capriFace!=null)capriFace.setSpeaking(false);mp.release();cloudPlayer=null;return true;});
            cloudPlayer.prepareAsync();
        }catch(Exception e){Toast.makeText(this,"Não consegui reproduzir a voz: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
    final Runnable voiceMouthLoop=new Runnable(){public void run(){if(cloudPlayer==null||!cloudPlayer.isPlaying())return;voiceMouthOpen=!voiceMouthOpen;if(capriFace!=null)capriFace.setSpeaking(voiceMouthOpen);voiceMouthHandler.postDelayed(this,125);}};
    void listen(){try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR");startActivityForResult(i,7);}catch(Exception e){Toast.makeText(this,"Reconhecimento de voz indisponível",0).show();}}
    @Override protected void onDestroy(){idleMotionHandler.removeCallbacksAndMessages(null);if(screenReceiver!=null)unregisterReceiver(screenReceiver);voiceMouthHandler.removeCallbacksAndMessages(null);if(cloudPlayer!=null){cloudPlayer.release();cloudPlayer=null;}if(tts!=null)tts.shutdown();super.onDestroy();}
}