package com.nightcity.capriai;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;

public class OverlayService extends Service {
    public static boolean running=false;
    public static final String ACTION_SAY="com.nightcity.capriai.OVERLAY_SAY";
    public static final String ACTION_SPEAK="com.nightcity.capriai.OVERLAY_SPEAK";
    public static final String EXTRA_TEXT="text";
    WindowManager wm;
    FrameLayout window;
    CapriSpriteView sprite;
    TextView bubble;
    WindowManager.LayoutParams params;
    Handler handler=new Handler();
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override public IBinder onBind(Intent i){return null;}

    @Override public void onCreate(){
        super.onCreate(); running=true;
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        window=new FrameLayout(this);
        window.setClipChildren(false); window.setClipToPadding(false);
        sprite=new CapriSpriteView(this);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(228),Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        window.addView(sprite,sp);
        bubble=new TextView(this);
        bubble.setTextColor(0xffeafff3); bubble.setTextSize(11);
        bubble.setTypeface(android.graphics.Typeface.MONOSPACE,android.graphics.Typeface.BOLD);
        bubble.setMaxLines(4); bubble.setEllipsize(TextUtils.TruncateAt.END);
        bubble.setPadding(dp(10),dp(8),dp(10),dp(12));
        GradientDrawable bd=new GradientDrawable(); bd.setColor(0xee080d17); bd.setCornerRadius(dp(12)); bd.setStroke(dp(1),0xff25dff4);
        bubble.setBackground(bd); bubble.setText("E aí, choom? To aqui.");
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(66),Gravity.TOP);
        window.addView(bubble,bp);
        int type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params=new WindowManager.LayoutParams(dp(190),dp(300),type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START; params.x=dp(8); params.y=dp(150);
        window.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY; int sx,sy; boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        downX=e.getRawX();downY=e.getRawY();sx=params.x;sy=params.y;moved=false;return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx=(int)(e.getRawX()-downX),dy=(int)(e.getRawY()-downY);
                        if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved=true;
                        if(moved){params.x=sx+dx;params.y=sy+dy;try{wm.updateViewLayout(window,params);}catch(Exception ignored){}}
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!moved){Intent a=new Intent(OverlayService.this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);}
                        return true;
                } return true;
            }
        });
        wm.addView(window,params);
    }

    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i!=null){
            String action=i.getAction();
            if(ACTION_SAY.equals(action)){
                String text=i.getStringExtra(EXTRA_TEXT);
                if(text!=null&&!text.trim().isEmpty()){bubble.setText(text);bubble.setVisibility(View.VISIBLE);sprite.react();}
            } else if(ACTION_SPEAK.equals(action)){
                boolean on=i.getBooleanExtra("speaking",false);sprite.setSpeaking(on);
            }
        }
        return START_STICKY;
    }
    @Override public void onDestroy(){
        running=false;handler.removeCallbacksAndMessages(null);
        if(window!=null&&wm!=null)try{wm.removeView(window);}catch(Exception ignored){}
        super.onDestroy();
    }
}