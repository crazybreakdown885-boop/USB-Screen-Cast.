package com.nightcity.capriai;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.*;
import android.widget.TextView;
public class OverlayService extends Service {
 WindowManager wm; View bubble;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);
 TextView v=new TextView(this);v.setText("CAPRI\n✦");v.setTextColor(Color.WHITE);v.setTextSize(16);v.setGravity(Gravity.CENTER);
 GradientDrawable bg=new GradientDrawable();bg.setColor(0xee251522);bg.setCornerRadius(80);bg.setStroke(4,0xffff4fa3);v.setBackground(bg);bubble=v;
 WindowManager.LayoutParams p=new WindowManager.LayoutParams(78,78,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);
 p.gravity=Gravity.TOP|Gravity.LEFT;p.x=24;p.y=180;
 v.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;
 public boolean onTouch(View view,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=p.x;oy=p.y;return true;}
 if(e.getAction()==MotionEvent.ACTION_MOVE){p.x=ox+(int)(e.getRawX()-sx);p.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(bubble,p);return true;}
 if(e.getAction()==MotionEvent.ACTION_UP){if(Math.abs(e.getRawX()-sx)<12&&Math.abs(e.getRawY()-sy)<12){Intent a=new Intent(OverlayService.this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(a);}return true;}return true;}});
 wm.addView(v,p);}
 @Override public void onDestroy(){if(bubble!=null&&wm!=null)wm.removeView(bubble);super.onDestroy();}
}