package com.nightcity.capriai;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.*;
import android.widget.*;

public class OverlayService extends Service {
    public static boolean running=false;
    WindowManager wm;
    ImageView avatar;
    WindowManager.LayoutParams params;
    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override public IBinder onBind(Intent i){return null;}
    @Override public void onCreate(){
        super.onCreate();
        running=true;
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        avatar=new ImageView(this);
        avatar.setImageResource(R.drawable.ic_capri);
        avatar.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        avatar.setPadding(dp(12),dp(12),dp(12),dp(12));
        avatar.setContentDescription("Abrir Capri AI");
        GradientDrawable bg=new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xee10121c);
        bg.setStroke(dp(2),0xffff4fa3);
        avatar.setBackground(bg);
        avatar.setElevation(dp(8));
        int type=WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params=new WindowManager.LayoutParams(dp(76),dp(76),type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.START;
        params.x=dp(18); params.y=dp(180);
        avatar.setOnTouchListener(new View.OnTouchListener(){
            float downX,downY; int startX,startY; boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                switch(e.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        downX=e.getRawX(); downY=e.getRawY(); startX=params.x; startY=params.y; moved=false; return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx=(int)(e.getRawX()-downX), dy=(int)(e.getRawY()-downY);
                        if(Math.abs(dx)>dp(5)||Math.abs(dy)>dp(5))moved=true;
                        if(moved){params.x=startX+dx;params.y=startY+dy;wm.updateViewLayout(avatar,params);} return true;
                    case MotionEvent.ACTION_UP:
                        if(!moved){Intent a=new Intent(OverlayService.this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);}
                        return true;
                }
                return true;
            }
        });
        wm.addView(avatar,params);
    }
    @Override public void onDestroy(){running=false;if(avatar!=null&&wm!=null)wm.removeView(avatar);super.onDestroy();}
}