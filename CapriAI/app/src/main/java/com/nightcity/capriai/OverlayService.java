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
    TextView bubble;
    WindowManager.LayoutParams params;
    final Handler motionHandler=new Handler(Looper.getMainLooper());
    final Random random=new Random();
    boolean dragging=false,destroyed=false;
    long lastTap=0;
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    final Runnable motionLoop=new Runnable(){@Override public void run(){
        if(destroyed||dragging||!getPrefs().getBoolean("free_roam",true))return;
        if(getPrefs().getBoolean("teleport_enabled",true)&&random.nextInt(4)==0)teleportToRandom();
        else glideToRandom();
    }};
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
        bubble.setBackground(bd);bubble.setText("CAPRI: E aí, choom? Tô aqui.");
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(86),Gravity.TOP);
        window.addView(bubble,bp);
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
                            if(now-lastTap<360&&getPrefs().getBoolean("teleport_enabled",true))teleportToRandom();
                            else{
                                boolean compact=!getPrefs().getBoolean("face_only",false);
                                getPrefs().edit().putBoolean("face_only",compact).apply();character.setFaceOnly(compact);
                                Toast.makeText(OverlayService.this,compact?"Modo rosto ativado":"Personagem completa ativada",Toast.LENGTH_SHORT).show();
                            }
                            lastTap=now;
                        }else getPrefs().edit().putInt("overlay_x",params.x).putInt("overlay_y",params.y).apply();
                        dragging=false;scheduleMotion(2800);return true;
                }return true;
            }
        });
        wm.addView(window,params);
        scheduleMotion(2200);
    }

    void scheduleMotion(long delay){
        motionHandler.removeCallbacks(motionLoop);
        if(!destroyed&&getPrefs().getBoolean("free_roam",true))motionHandler.postDelayed(motionLoop,delay);
    }
    void randomizeForm(){
        if(getPrefs().getBoolean("auto_morph",true)){
            String[] poses={"sit","lie","crouch","back"};
            character.setPose(poses[random.nextInt(poses.length)]);
            boolean face=random.nextInt(3)==0;
            getPrefs().edit().putBoolean("face_only",face).apply();character.setFaceOnly(face);
        }
        float scale=getPrefs().getInt("character_scale",100)/100f;
        float variation=.88f+random.nextFloat()*.24f;
        character.animate().scaleX(scale*variation).scaleY(scale*variation).rotation(-5f+random.nextFloat()*10f).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
        if(getPrefs().getBoolean("idle_activities",true)) idleActivity();
    }
    void idleActivity(){
        if(character==null||bubble==null||destroyed)return;
        int act=random.nextInt(5);
        character.animate().cancel();bubble.animate().cancel();
        character.setTranslationY(0f);bubble.setTranslationY(0f);
        if(act==0){
            character.setPose("lie");bubble.setText("CAPRI: *se espreguiça* Preciso de um intervalo dessa simulação.");
            character.animate().translationY(-dp(18)).rotation(-7f).setDuration(650).start();
        }else if(act==1){
            character.setPose("sit");bubble.setText("CAPRI: *se senta na caixa de diálogo* Pronto. Esse lugar agora é meu.");
            character.animate().translationY(-dp(82)).rotation(2f).setDuration(700).start();
        }else if(act==2){
            character.setPose("crouch");bubble.setText("CAPRI: *jogando um game* Não me distrai. Estou carregando o time.");
            character.animate().translationY(-dp(8)).rotation(-3f).setDuration(350).withEndAction(()->character.animate().rotation(3f).setDuration(350).start()).start();
        }else if(act==3){
            character.setPose("back");bubble.setText("CAPRI: *assobiando uma melodia cyberpunk* 🎵");
            character.animate().translationY(-dp(12)).rotation(4f).setDuration(500).start();
        }else{
            character.setPose("sit");bubble.setText("CAPRI: *ouvindo música, balançando no ritmo* 🎧");
            character.animate().translationY(-dp(10)).rotation(-4f).setDuration(500).withEndAction(()->character.animate().rotation(4f).setDuration(500).start()).start();
        }
        motionHandler.postDelayed(()->{
            if(destroyed||character==null)return;
            character.animate().translationY(0f).rotation(0f).setDuration(450).start();
            bubble.setText("CAPRI: E aí, choom? Tô aqui.");
        },4200);
    }
    int[] randomPosition(){
        android.graphics.Point size=new android.graphics.Point();wm.getDefaultDisplay().getRealSize(size);
        int maxX=Math.max(0,size.x-params.width),maxY=Math.max(dp(24),size.y-params.height);
        int x=random.nextInt(Math.max(1,maxX+1));
        int y=dp(24)+random.nextInt(Math.max(1,maxY-dp(24)+1));
        return new int[]{x,y};
    }
    void glideToRandom(){
        int[] target=randomPosition();int startX=params.x,startY=params.y;
        long started=SystemClock.uptimeMillis();long duration=1800+random.nextInt(1500);
        final Runnable[] step=new Runnable[1];
        step[0]=()->{
            if(destroyed||dragging||window==null)return;
            float t=Math.min(1f,(SystemClock.uptimeMillis()-started)/(float)duration);
            float eased=t*t*(3f-2f*t);
            params.x=startX+Math.round((target[0]-startX)*eased);
            params.y=startY+Math.round((target[1]-startY)*eased);
            clampPosition();try{wm.updateViewLayout(window,params);}catch(Exception ignored){}
            if(t<1f)motionHandler.postDelayed(step[0],16);
            else{
                getPrefs().edit().putInt("overlay_x",params.x).putInt("overlay_y",params.y).apply();
                randomizeForm();scheduleMotion(2500+random.nextInt(5500));
            }
        };
        motionHandler.post(step[0]);
    }
    void teleportToRandom(){
        if(destroyed||dragging||window==null)return;
        int[] target=randomPosition();
        character.animate().cancel();bubble.animate().cancel();
        character.animate().alpha(0f).scaleX(.05f).scaleY(.05f).rotation(180f).setDuration(220).withEndAction(()->{
            if(destroyed||dragging)return;
            params.x=target[0];params.y=target[1];clampPosition();
            try{wm.updateViewLayout(window,params);}catch(Exception ignored){}
            randomizeForm();
            bubble.setAlpha(0f);
            character.animate().alpha(1f).scaleX(getPrefs().getInt("character_scale",100)/100f).scaleY(getPrefs().getInt("character_scale",100)/100f).rotation(0f).setDuration(280).setInterpolator(new DecelerateInterpolator()).start();
            bubble.animate().alpha(1f).setDuration(300).start();
            getPrefs().edit().putInt("overlay_x",params.x).putInt("overlay_y",params.y).apply();
            scheduleMotion(3500+random.nextInt(5500));
        }).start();
    }
    void clampPosition(){
        android.graphics.Point size=new android.graphics.Point();wm.getDefaultDisplay().getRealSize(size);
        params.x=Math.max(0,Math.min(params.x,size.x-params.width));params.y=Math.max(0,Math.min(params.y,size.y-params.height));
    }
    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i!=null){String action=i.getAction();
            if(ACTION_SAY.equals(action)){String s=i.getStringExtra(EXTRA_TEXT);if(s!=null&&!s.trim().isEmpty()){bubble.setText("CAPRI: "+s);character.react();}}
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
