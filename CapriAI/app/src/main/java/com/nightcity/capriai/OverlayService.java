package com.nightcity.capriai;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import android.text.TextUtils;
import org.json.*;
import java.net.*;
import java.io.*;
import java.util.Locale;
import java.util.Random;

public class OverlayService extends Service {
    public static boolean running=false;
    public static final String ACTION_SAY="com.nightcity.capriai.OVERLAY_SAY";
    public static final String ACTION_SPEAK="com.nightcity.capriai.OVERLAY_SPEAK";
    public static final String ACTION_POSE="com.nightcity.capriai.OVERLAY_POSE";
    public static final String ACTION_MOOD="com.nightcity.capriai.OVERLAY_MOOD";
    public static final String EXTRA_TEXT="text";
    WindowManager wm;
    FrameLayout window;
    CapriCharacterView character;
    TextView bubble,gameToken;
    int gameScore=0;
    WindowManager.LayoutParams params;
    final Handler motionHandler=new Handler(Looper.getMainLooper());
    final Random random=new Random();
    boolean dragging=false,destroyed=false;
    final Runnable activityLoop=new Runnable(){public void run(){if(destroyed)return;if(!dragging&&getPrefs().getBoolean("idle_activities",true))idleActivity();motionHandler.postDelayed(this,22000+random.nextInt(18000));}};
    long lastTap=0;
    final Runnable autonomyLoop=new Runnable(){public void run(){if(destroyed)return;maybeSpeakSpontaneously();motionHandler.postDelayed(this,150000+random.nextInt(150000));}};
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    final Runnable motionLoop=new Runnable(){@Override public void run(){ /* Sem deslocamento aleatório: a personagem só se move como parte de uma ação contextual. */ }};
    android.content.SharedPreferences getPrefs(){return getSharedPreferences("capri_settings",0);}
    @Override public IBinder onBind(Intent i){return null;}

    @Override public void onCreate(){
        super.onCreate();running=true;destroyed=false;wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        window=new FrameLayout(this);window.setClipChildren(false);window.setClipToPadding(false);
        character=new CapriCharacterView(this);
        android.content.SharedPreferences saved=getPrefs();
        character.setFaceOnly(saved.getBoolean("face_only",false));
        character.setPose(saved.getString("character_pose","sit"));
        float scale=saved.getInt("character_scale",100)/100f;
        character.setScaleX(scale);character.setScaleY(scale);
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,dp(245),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        window.addView(character,cp);
        bubble=new TextView(this);bubble.setTextColor(0xffd5efff);bubble.setTextSize(11);
        bubble.setTypeface(android.graphics.Typeface.MONOSPACE,android.graphics.Typeface.BOLD);
        bubble.setMaxLines(5);bubble.setEllipsize(TextUtils.TruncateAt.END);
        bubble.setPadding(dp(11),dp(9),dp(11),dp(12));
        GradientDrawable bd=new GradientDrawable();bd.setColor(0xf20a0b12);bd.setCornerRadius(dp(7));bd.setStroke(dp(2),0xff28dff4);
        bubble.setBackground(bd);bubble.setText("CAPRI: Oi. Estou por aqui.");
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(86),Gravity.TOP);
        window.addView(bubble,bp);
        gameToken=new TextView(this);gameToken.setText("◆");gameToken.setTextSize(27);gameToken.setTextColor(0xff28dff4);gameToken.setGravity(Gravity.CENTER);gameToken.setVisibility(View.GONE);gameToken.setShadowLayer(dp(8),0,0,0xff28dff4);
        FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(dp(44),dp(44),Gravity.CENTER);
        window.addView(gameToken,gp);
        gameToken.setOnClickListener(v->{gameScore++;bubble.setText("CAPRI: Boa! Pontuação: "+gameScore+" ◆");gameToken.animate().cancel();gameToken.setTranslationX(random.nextInt(dp(110)+1)-dp(55));gameToken.setTranslationY(random.nextInt(dp(120)+1)-dp(60));gameToken.animate().scaleX(1.5f).scaleY(1.5f).setDuration(160).withEndAction(()->gameToken.animate().scaleX(1f).scaleY(1f).setDuration(160).start()).start();});
        bubble.setOnClickListener(v->{Intent a=new Intent(OverlayService.this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);});
        int type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params=new WindowManager.LayoutParams(dp(205),dp(335),type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;params.x=saved.getInt("overlay_x",dp(8));params.y=saved.getInt("overlay_y",dp(115));
        clampPosition();
        window.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY;int sx,sy;boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        dragging=true;motionHandler.removeCallbacks(motionLoop);
                        character.animate().cancel();bubble.animate().cancel();
                        character.setAlpha(1f);bubble.setAlpha(1f);character.setRotation(0f);
                        float base=getPrefs().getInt("character_scale",100)/100f;character.setScaleX(base);character.setScaleY(base);
                        downX=e.getRawX();downY=e.getRawY();sx=params.x;sy=params.y;moved=false;return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);
                        if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved=true;
                        if(moved){params.x=sx+dx;params.y=sy+dy;clampPosition();try{wm.updateViewLayout(window,params);}catch(Exception ignored){}}
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!moved){
                            long now=SystemClock.uptimeMillis();
                            character.react();
                            character.setMood("curiosa");
                            character.animate().translationY(-dp(12)).rotation(-2f).setDuration(240).withEndAction(()->character.animate().translationY(0).rotation(0).setDuration(300).start()).start();
                            bubble.setText("CAPRI: Opa, chamou minha atenção.");
                            lastTap=now;
                        }else getPrefs().edit().putInt("overlay_x",params.x).putInt("overlay_y",params.y).apply();
                        dragging=false;return true;
                }return true;
            }
        });
        wm.addView(window,params);

        motionHandler.postDelayed(activityLoop,18000);
        motionHandler.postDelayed(autonomyLoop,150000);
    }

    void idleActivity(){
        if(character==null||bubble==null||destroyed||dragging)return;
        CapriActionEngine.Action action=CapriActionEngine.next();
        character.animate().cancel();bubble.animate().cancel();
        character.setTranslationX(0f);character.setTranslationY(0f);character.setRotation(0f);
        bubble.setTranslationY(0f);bubble.setScaleX(1f);bubble.setScaleY(1f);
        float base=getPrefs().getInt("character_scale",100)/100f;
        character.setFaceOnly(false);character.setPose(action.pose);character.setMood(action.mood);
        bubble.setText("CAPRI: "+action.caption);
        long d=action.duration;
        switch(action.kind){
            case "stretch":
                character.animate().translationY(-dp(25)).scaleX(base*1.04f).scaleY(base*.96f).rotation(-3f).setDuration(d).start();break;
            case "look": case "read": case "lean":
                character.animate().translationY(-dp(9)).translationX(dp(7)).rotation(-4f).setDuration(d).start();
                bubble.animate().scaleX(1.02f).setDuration(d/2).start();break;
            case "hair": case "wave":
                character.animate().rotation(5f).translationY(-dp(5)).setDuration(d/2).withEndAction(()->character.animate().rotation(0f).setDuration(d/2).start()).start();break;
            case "yawn": case "rest":
                character.animate().translationY(dp(8)).scaleX(base*1.03f).scaleY(base*.97f).rotation(2f).setDuration(d).start();break;
            case "smile": case "giggle":
                character.animate().translationY(-dp(8)).rotation(-2f).setDuration(d/2).withEndAction(()->character.animate().translationY(0).rotation(0).setDuration(d/2).start()).start();break;
            case "listen": case "think": case "lookup":
                character.animate().rotation(-5f).translationX(-dp(4)).setDuration(d/2).withEndAction(()->character.animate().rotation(0f).translationX(0f).setDuration(d/2).start()).start();break;
            case "shrug": case "fidget":
                character.animate().rotation(4f).translationX(dp(4)).setDuration(d/2).withEndAction(()->character.animate().rotation(-3f).translationX(-dp(3)).setDuration(d/2).withEndAction(()->character.animate().rotation(0f).translationX(0f).setDuration(d/3).start()).start()).start();break;
            case "nod":
                character.animate().translationY(dp(4)).setDuration(d/3).withEndAction(()->character.animate().translationY(-dp(4)).setDuration(d/3).withEndAction(()->character.animate().translationY(0).setDuration(d/3).start()).start()).start();break;
            case "crouch":
                character.animate().translationY(dp(10)).scaleY(base*.94f).setDuration(d).start();break;
            case "sway":
                android.media.AudioManager audio=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);
                if(audio!=null&&audio.isMusicActive()){
                    character.animate().rotation(7f).setDuration(d/2).withEndAction(()->character.animate().rotation(-7f).setDuration(d/2).withEndAction(()->character.animate().rotation(0f).setDuration(d/3).start()).start()).start();
                }else character.animate().translationY(-dp(4)).setDuration(d).start();
                break;
            case "dance":character.animate().rotation(7f).translationY(-dp(9)).setDuration(d/2).withEndAction(()->character.animate().rotation(-7f).translationY(0).setDuration(d/2).start()).start();break;
            case "twirl":character.animate().rotationBy(360f).setDuration(d+500).start();break;
            case "bounce":character.animate().translationY(-dp(18)).setDuration(d/2).withEndAction(()->character.animate().translationY(0).setDuration(d/2).start()).start();break;
            case "curtsy":character.animate().translationY(dp(12)).scaleY(base*.92f).rotation(3f).setDuration(d).start();break;
            case "peek":character.animate().translationX(-dp(12)).rotation(-6f).setDuration(d/2).withEndAction(()->character.animate().translationX(0).rotation(0).setDuration(d/2).start()).start();break;
            default:
                character.animate().translationY(-dp(5)).setDuration(d/2).withEndAction(()->character.animate().translationY(0).setDuration(d/2).start()).start();break;
        }
        motionHandler.postDelayed(()->{
            if(destroyed||character==null||dragging)return;
            character.animate().translationX(0f).translationY(0f).rotation(0f).scaleX(base).scaleY(base).setDuration(450).start();
            bubble.animate().translationY(0f).scaleX(1f).scaleY(1f).setDuration(350).start();
            character.setPose(getPrefs().getString("character_pose","sit"));
            character.setFaceOnly(getPrefs().getBoolean("face_only",false));
        },d+350);
    }
    boolean canSpeakAutonomously(){
        if(!getPrefs().getBoolean("autonomous_speech",true)||!getPrefs().getBoolean("voice_enabled",true)||!getPrefs().getBoolean("voice_replies",true)||getPrefs().getBoolean("do_not_disturb",false))return false;
        if(getPrefs().getBoolean("quiet_hours",true)){
            java.util.Calendar now=java.util.Calendar.getInstance();int hour=now.get(java.util.Calendar.HOUR_OF_DAY);
            if(hour>=22||hour<8)return false;
        }
        long last=getPrefs().getLong("last_user_interaction",0L);
        return System.currentTimeMillis()-last>=180000L;
    }
    void maybeSpeakSpontaneously(){
        if(destroyed||dragging||!canSpeakAutonomously())return;
        String provider=getPrefs().getString("ai_provider","Offline");
        String key=ApiKeyStore.get(this);
        if(!key.isEmpty()&&("OpenAI-compatible".equals(provider)||"Ollama local".equals(provider))){
            new Thread(()->{
                String thought=requestOnlineThought(key,provider);
                if(thought==null||thought.trim().isEmpty())thought=LocalDialogueEngine.idleThought(this);
                final String line=thought.trim();
                motionHandler.post(()->showAutonomousLine(line));
            }).start();
        }else showAutonomousLine(LocalDialogueEngine.idleThought(this));
    }
    String requestOnlineThought(String key,String provider){
        HttpURLConnection conn=null;
        try{
            String base=getPrefs().getString("ai_endpoint","https://api.openai.com/v1").trim();
            if(provider.equals("Ollama local")){if(base.isEmpty())base="http://127.0.0.1:11434";if(!base.endsWith("/api/chat"))base=base.replaceAll("/+$","")+"/api/chat";}
            else{if(base.isEmpty())base="https://api.openai.com/v1";if(!base.endsWith("/chat/completions"))base=base.replaceAll("/+$","")+"/chat/completions";}
            conn=(HttpURLConnection)new URL(base).openConnection();conn.setRequestMethod("POST");conn.setConnectTimeout(9000);conn.setReadTimeout(18000);conn.setDoOutput(true);conn.setRequestProperty("Content-Type","application/json");
            if(!key.isEmpty())conn.setRequestProperty("Authorization","Bearer "+key);
            JSONArray messages=new JSONArray().put(new JSONObject().put("role","system").put("content","Você é Capri, personagem feminina adulta, espontânea e coerente. Fale em português brasileiro. Não mencione Night City nem invente que está vendo ou ouvindo algo. Gere UMA fala espontânea curta, natural e não repetitiva para iniciar conversa após um período de silêncio. Pode comentar uma ideia, fazer uma pergunta leve ou dizer como está se sentindo. Não pressione o usuário."));
            try{
                JSONArray history=new JSONArray(getPrefs().getString("chat_history","[]"));
                for(int i=Math.max(0,history.length()-6);i<history.length();i++){JSONObject h=history.getJSONObject(i);messages.put(new JSONObject().put("role",h.optString("role","user")).put("content",h.optString("content","")));}
            }catch(Exception ignored){}
            String model=getPrefs().getString("ai_model",provider.equals("Ollama local")?"llama3.2":"gpt-4o-mini");
            JSONObject body=provider.equals("Ollama local")
                ?new JSONObject().put("model",model).put("messages",messages).put("stream",false)
                :new JSONObject().put("model",model).put("messages",messages).put("max_completion_tokens",90);
            try(OutputStream os=conn.getOutputStream()){os.write(body.toString().getBytes("UTF-8"));}
            int status=conn.getResponseCode();InputStream in=status<400?conn.getInputStream():conn.getErrorStream();
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[2048];int n;while(in!=null&&(n=in.read(buf))>0)out.write(buf,0,n);
            if(status>=400)return null;
            JSONObject result=new JSONObject(out.toString("UTF-8"));
            return provider.equals("Ollama local")?result.getJSONObject("message").getString("content"):result.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }catch(Exception ignored){return null;}finally{if(conn!=null)conn.disconnect();}
    }
    void showAutonomousLine(String line){
        if(destroyed||dragging||line==null||line.trim().isEmpty()||!canSpeakAutonomously())return;
        String clean=line.trim();if(clean.length()>220)clean=clean.substring(0,220);
        bubble.setText("CAPRI: "+clean);character.setMood(clean);character.setPose("sit");character.react();
        getPrefs().edit().putLong("last_user_interaction",System.currentTimeMillis()).apply();
        if(getPrefs().getBoolean("offline_voice_enabled",false)&&PiperOfflineVoice.isReady(this)){
            PiperOfflineVoice.speak(this,clean,new PiperOfflineVoice.PlaybackListener(){
                public void onStart(){if(character!=null)character.setSpeaking(true);}
                public void onFinish(String error){if(character!=null)character.setSpeaking(false);}
            });
        }
    }
    void clampPosition(){
        android.graphics.Point size=new android.graphics.Point();wm.getDefaultDisplay().getRealSize(size);
        params.x=Math.max(0,Math.min(params.x,size.x-params.width));params.y=Math.max(0,Math.min(params.y,size.y-params.height));
    }
    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i!=null){String action=i.getAction();
            if(ACTION_SAY.equals(action)){String s=i.getStringExtra(EXTRA_TEXT);if(s!=null&&!s.trim().isEmpty()){bubble.setText("CAPRI: "+s);character.react();character.setMood(s);}}
            else if(ACTION_SPEAK.equals(action))character.setSpeaking(i.getBooleanExtra("speaking",false));
            else if(ACTION_POSE.equals(action))character.setPose(i.getStringExtra("pose"));
            else if(ACTION_MOOD.equals(action))character.setMood(i.getStringExtra("mood_text"));
        }return START_STICKY;
    }
    @Override public void onDestroy(){
        destroyed=true;motionHandler.removeCallbacksAndMessages(null);running=false;
        if(window!=null&&wm!=null)try{window.animate().cancel();character.animate().cancel();bubble.animate().cancel();wm.removeView(window);}catch(Exception ignored){}
        super.onDestroy();
    }
}
