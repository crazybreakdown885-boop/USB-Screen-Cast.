package com.nightcity.capriai;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;

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
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override public IBinder onBind(Intent i){return null;}

    @Override public void onCreate(){
        super.onCreate();running=true;wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        window=new FrameLayout(this);window.setClipChildren(false);window.setClipToPadding(false);
        character=new CapriCharacterView(this);
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
        int type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params=new WindowManager.LayoutParams(dp(205),dp(335),type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;params.x=dp(8);params.y=dp(115);
        window.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY;int sx,sy;boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:downX=e.getRawX();downY=e.getRawY();sx=params.x;sy=params.y;moved=false;return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);
                        if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved=true;
                        if(moved){params.x=sx+dx;params.y=sy+dy;clampPosition();try{wm.updateViewLayout(window,params);}catch(Exception ignored){}}
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!moved){Intent a=new Intent(OverlayService.this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);}
                        return true;
                }return true;
            }
        });
        wm.addView(window,params);
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
    @Override public void onDestroy(){running=false;if(window!=null&&wm!=null)try{wm.removeView(window);}catch(Exception ignored){}super.onDestroy();}
}