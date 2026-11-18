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
    Button floatBtn;
    CapriFaceView capriFace;
    LinearLayout messages;
    ScrollView scroll;
    TextToSpeech tts;
    static final int SCREEN_REQUEST=8;
    BroadcastReceiver screenReceiver;
    int pink=0xffff315f, green=0xff00ef83, cyan=0xff35d9f5, bg=0xff05090d, panel=0xff080f17;
    boolean keyboardVisible=false;
    boolean composerFocused=false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        applyImmersiveMode(true);
        LinearLayout root=new LinearLayout(this); root.setOrientation(1); root.setPadding(14,8,14,8); root.setBackgroundColor(bg);
        LinearLayout head=new LinearLayout(this); head.setGravity(Gravity.CENTER_VERTICAL); head.setPadding(4,4,4,8);
        TextView brand=label("CAPRI // NIGHT CITY",16,cyan); brand.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        head.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        Button settings=smallButton("⚙ API"); Button read=smallButton("▣ TELA"); floatBtn=smallButton("◉ FLUTUAR");
        head.addView(settings); head.addView(read); head.addView(floatBtn); root.addView(head);
        read.setOnClickListener(v->requestScreenRead());
        capriFace=new CapriFaceView(this);
        LinearLayout.LayoutParams faceLp=new LinearLayout.LayoutParams(-1,dp(270)); faceLp.setMargins(0,0,0,8);
        root.addView(capriFace,faceLp);
        TextView tagline=label("◆ ENGRAM INTERFACE / ONLINE ◆",10,green); tagline.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); tagline.setGravity(Gravity.CENTER); root.addView(tagline);
        scroll=new ScrollView(this); scroll.setFillViewport(false); scroll.setClipToPadding(false);
        messages=new LinearLayout(this); messages.setOrientation(1); messages.setPadding(0,8,0,8); scroll.addView(messages);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        addMessage("CAPRI","Finalmente. Achei que você tinha me deixado presa num shard velho. E aí, choom?",true);
        LinearLayout keyPanel=new LinearLayout(this); keyPanel.setOrientation(1); keyPanel.setPadding(10,6,10,6); keyPanel.setBackground(new GradientBox(panel));
        key=field("Chave da API OpenAI (opcional)"); key.setInputType(129); key.setText(getPreferences(0).getString("api_key","")); keyPanel.addView(key);
        Button save=smallButton("SALVAR CHAVE"); keyPanel.addView(save);
        save.setOnClickListener(v->{getPreferences(0).edit().putString("api_key",key.getText().toString().trim()).apply();Toast.makeText(this,"Chave salva neste aparelho",0).show();});
        keyPanel.setVisibility(View.GONE); root.addView(keyPanel);
        settings.setOnClickListener(v->keyPanel.setVisibility(keyPanel.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE));
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
        tts=new TextToSpeech(this,status->{if(status==TextToSpeech.SUCCESS){tts.setLanguage(new Locale("pt","BR"));tts.setSpeechRate(0.94f);}});
        send.setOnClickListener(v->sendMessage()); mic.setOnClickListener(v->listen());
        prompt.setOnEditorActionListener((v,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_SEND){sendMessage();return true;}return false;});
        floatBtn.setOnClickListener(v->{
            if(OverlayService.running){
                getPreferences(0).edit().putBoolean("overlay_enabled",false).putBoolean("overlay_pending",false).apply();
                stopService(new Intent(this,OverlayService.class));
                syncOverlayButton();
                Toast.makeText(this,"Capri guardada",Toast.LENGTH_SHORT).show();
            } else {
                getPreferences(0).edit().putBoolean("overlay_enabled",true).putBoolean("overlay_pending",true).apply();
                if(!android.provider.Settings.canDrawOverlays(this)){
                    startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())));
                } else {
                    getPreferences(0).edit().putBoolean("overlay_pending",false).apply();
                    startService(new Intent(this,OverlayService.class));
                    syncOverlayButton();
                }
            }
        });
        syncOverlayButton();
        handleLaunchIntent(getIntent());
        screenReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){String b64=i.getStringExtra("image");if(b64!=null)askAboutScreen(b64);}};
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
    @Override protected void onResume(){
        super.onResume();
        if(getPreferences(0).getBoolean("overlay_enabled",false)&&android.provider.Settings.canDrawOverlays(this)&&!OverlayService.running){
            getPreferences(0).edit().putBoolean("overlay_pending",false).apply();
            startService(new Intent(this,OverlayService.class));
        }
        applyImmersiveMode(!keyboardVisible&&!composerFocused);syncOverlayButton();
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
    void addMessage(String who,String text,boolean capri){TextView t=label(who+"\n",14,capri?0xffffb6d5:Color.WHITE);t.setPadding(13,11,13,11);t.setTypeface(Typeface.MONOSPACE);t.setBackground(new HudBubble(capri?0xff08131c:0xff101018,capri?cyan:pink));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,5,0,7);messages.addView(t,p);if(capri){typeText(t,text,0);}else{t.append(text);}if(scroll!=null)scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
    void typeText(TextView view,String value,int index){if(index>=value.length())return;char ch=value.charAt(index);view.append(String.valueOf(ch));if(ch!=' '&&ch!='\n'&&index%2==0)view.playSoundEffect(android.view.SoundEffectConstants.CLICK);if(scroll!=null)scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));new Handler().postDelayed(()->typeText(view,value,index+1),28);}
    void sendMessage(){
        String q=prompt.getText().toString().trim();if(q.isEmpty())return;
        prompt.setText("");addMessage("VOCÊ",q,false);
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(prompt.getWindowToken(),0);
        prompt.clearFocus();composerFocused=false;
        new Handler().postDelayed(()->applyImmersiveMode(true),350);
        if(handleAndroidCommand(q))return;
        String api=getPreferences(0).getString("api_key","");
        if(api.isEmpty()){String a=localReply(q);addMessage("CAPRI",a,true);speak(a);}
        else new Thread(()->{String a=askApi(api,q);runOnUiThread(()->{addMessage("CAPRI",a,true);speak(a);});}).start();
    }
    boolean handleAndroidCommand(String raw){
        String q=raw.trim(),s=q.toLowerCase(Locale.ROOT);
        try{
            if(s.startsWith("nota:")||s.startsWith("anote:")){
                String note=q.substring(q.indexOf(':')+1).trim();if(note.isEmpty())return false;
                java.util.Set<String> notes=new java.util.HashSet<>(getPreferences(0).getStringSet("capri_notes",new java.util.HashSet<>()));
                notes.add(note);getPreferences(0).edit().putStringSet("capri_notes",notes).apply();
                String a="Anotado no seu celular, choom: "+note;addMessage("CAPRI",a,true);speak(a);return true;
            }
            if(s.startsWith("timer ")||s.startsWith("cronômetro ")||s.startsWith("cronometro ")){
                java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)\\s*(s|segundos?|minutos?|min|horas?|h)",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(s);
                if(m.find()){
                    int n=Integer.parseInt(m.group(1));String unit=m.group(2);int sec=unit.startsWith("h")?n*3600:(unit.startsWith("s")?n:n*60);
                    Intent timer=new Intent(android.provider.AlarmClock.ACTION_SET_TIMER).putExtra(android.provider.AlarmClock.EXTRA_LENGTH,sec).putExtra(android.provider.AlarmClock.EXTRA_MESSAGE,"Capri").putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI,false);
                    startActivity(timer);String a="Timer de "+n+" "+unit+" aberto. Não dorme no volante, choom.";addMessage("CAPRI",a,true);speak(a);return true;
                }
            }
            if(s.startsWith("pesquise ")||s.startsWith("pesquisa ")||s.startsWith("procure ")){
                String term=q.substring(q.indexOf(' ')+1).trim();
                startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com/search?q="+java.net.URLEncoder.encode(term,"UTF-8"))));
                String a="Abri a busca pra você, choom.";addMessage("CAPRI",a,true);speak(a);return true;
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
                String a="Feito. Mandei abrir "+target+", choom.";addMessage("CAPRI",a,true);speak(a);return true;
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
    String localReply(String q){String s=q.toLowerCase(Locale.ROOT);if(s.contains("quem")||s.contains("capri"))return "Capri. Engram do Projeto Capricorn. Não sou exatamente uma garota comum, choom. Sou um monte de dados perigosamente interessantes com uma péssima paciência para corporações.";if(s.contains("oi")||s.contains("olá")||s.contains("eai"))return "E aí, choom! Já estava ficando entediada no ciberespaço. Manda a boa.";return "Minha conexão neural está no modo local. Configure sua chave de API para conversas completas, choom.";}
    String askApi(String api,String q){HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://api.openai.com/v1/chat/completions").openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+api);c.setRequestProperty("Content-Type","application/json");JSONObject sys=new JSONObject().put("role","system").put("content","Você é Capri (Capricorn), um engrama digital do universo Cyberpunk: Edgerunners 2. Fale em português brasileiro, com humor ácido, inteligência, curiosidade, atitude provocadora e gírias leves de Night City como choom. Seja útil e concisa. Não finja ser oficial.");JSONArray msgs=new JSONArray().put(sys).put(new JSONObject().put("role","user").put("content",q));JSONObject body=new JSONObject().put("model","gpt-4o-mini").put("messages",msgs);try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=is.read(buf))>0)out.write(buf,0,n);if(c.getResponseCode()>=400)return "Falha de conexão ("+c.getResponseCode()+"). Confere a chave, choom.";return new JSONObject(out.toString("UTF-8")).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");}catch(Exception e){return "Deu ruim na conexão neural: "+e.getMessage();}finally{if(c!=null)c.disconnect();}}
    void askAboutScreen(String b64){String api=getPreferences(0).getString("api_key","");if(api.isEmpty()){addMessage("CAPRI","A captura chegou, mas preciso da chave de API para analisar o conteúdo visual.",true);return;}addMessage("SISTEMA","Captura de tela recebida. Analisando...",false);new Thread(()->{String a=askVision(api,b64);runOnUiThread(()->{addMessage("CAPRI",a,true);speak(a);});}).start();}
    String askVision(String api,String b64){HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://api.openai.com/v1/chat/completions").openConnection();c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(60000);c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+api);c.setRequestProperty("Content-Type","application/json");JSONArray content=new JSONArray().put(new JSONObject().put("type","text").put("text","Descreva o que está visível na captura de tela e ajude o usuário com base nela. Responda em português brasileiro como Capri, de forma objetiva." )).put(new JSONObject().put("type","image_url").put("image_url",new JSONObject().put("url","data:image/jpeg;base64,"+b64)));JSONArray msgs=new JSONArray().put(new JSONObject().put("role","system").put("content","Você é Capri, assistente útil em português brasileiro.")).put(new JSONObject().put("role","user").put("content",content));JSONObject body=new JSONObject().put("model","gpt-4o-mini").put("messages",msgs).put("max_tokens",500);try(OutputStream os=c.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}InputStream is=c.getResponseCode()<400?c.getInputStream():c.getErrorStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=is.read(buf))>0)out.write(buf,0,n);if(c.getResponseCode()>=400)return "Não consegui analisar essa tela ("+c.getResponseCode()+").";return new JSONObject(out.toString("UTF-8")).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");}catch(Exception e){return "Erro ao analisar a tela: "+e.getMessage();}finally{if(c!=null)c.disconnect();}}
    void requestScreenRead(){if(getPreferences(0).getString("api_key","").isEmpty()){addMessage("CAPRI","Primeiro configure sua chave de API. Depois toque em LER TELA e autorize a captura do Android.",true);return;}MediaProjectionManager m=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);startActivityForResult(m.createScreenCaptureIntent(),SCREEN_REQUEST);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==7&&c==RESULT_OK&&d!=null){ArrayList<String>a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(a!=null&&!a.isEmpty()){prompt.setText(a.get(0));sendMessage();}}if(r==SCREEN_REQUEST&&c==RESULT_OK&&d!=null){Intent s=new Intent(this,ScreenCaptureService.class);s.putExtra("resultCode",c);s.putExtra("data",d);startForegroundService(s);addMessage("SISTEMA","Permissão concedida. Capturando a tela atual...",false);}}
    void speak(String s){
        overlaySay(s);
        if(tts==null)return;
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener(){
            final Handler h=new Handler();
            boolean open=false;
            Runnable mouth=new Runnable(){public void run(){if(capriFace!=null){open=!open;capriFace.animateMouth(open);h.postDelayed(this,145);}}};
            @Override public void onStart(String id){runOnUiThread(()->{overlaySpeaking(true);if(capriFace!=null)capriFace.setSpeaking(true);h.post(mouth);});}
            @Override public void onDone(String id){h.removeCallbacks(mouth);runOnUiThread(()->{overlaySpeaking(false);if(capriFace!=null)capriFace.setSpeaking(false);});}
            @Override public void onError(String id){h.removeCallbacks(mouth);runOnUiThread(()->{overlaySpeaking(false);if(capriFace!=null)capriFace.setSpeaking(false);});}
        });
        tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"capri");
    }
    void listen(){try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"pt-BR");startActivityForResult(i,7);}catch(Exception e){Toast.makeText(this,"Reconhecimento de voz indisponível",0).show();}}
    @Override protected void onDestroy(){if(screenReceiver!=null)unregisterReceiver(screenReceiver);if(tts!=null)tts.shutdown();super.onDestroy();}
}