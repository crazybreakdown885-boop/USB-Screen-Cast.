package com.nightcity.capriai;
import android.app.Service;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class OverlayService extends Service {
 WindowManager wm; View root; WindowManager.LayoutParams params; EditText input;
 @Override public IBinder onBind(Intent i){return null;}
 GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(22);d.setStroke(2,0xffff4fa3);return d;}
 TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(8,5,8,5);return t;}
 Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(10);b.setAllCaps(false);b.setTextColor(Color.BLACK);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xffff4fa3));return b;}
 @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);
 LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(12,10,12,10);card.setBackground(shape(0xee10121c));root=card;
 LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
 TextView title=text("C  CAPRI  //  ONLINE",16,0xffff80ba);title.setTypeface(null,Typeface.BOLD);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
 Button close=btn("×");top.addView(close,new LinearLayout.LayoutParams(44,42));close.setOnClickListener(v->stopSelf());card.addView(top);
 TextView reply=text("E aí, choom. Estou aqui por cima de tudo.",13,Color.WHITE);reply.setBackground(shape(0xff24202e));card.addView(reply);
 input=new EditText(this);input.setSingleLine(true);input.setTextColor(Color.WHITE);input.setHintTextColor(0xff999999);input.setHint("Escreva para Capri...");input.setBackground(shape(0xff202330));card.addView(input);
 LinearLayout row=new LinearLayout(this);
 Button send=btn("ENVIAR"), read=btn("LER TELA"), open=btn("APP");
 row.addView(send,new LinearLayout.LayoutParams(0,44,1));row.addView(read,new LinearLayout.LayoutParams(0,44,1));row.addView(open,new LinearLayout.LayoutParams(0,44,1));card.addView(row);
 send.setOnClickListener(v->{String q=input.getText().toString().trim();if(!q.isEmpty()){Intent a=new Intent(this,MainActivity.class);a.putExtra("capri_prompt",q);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);input.setText("");}});
 open.setOnClickListener(v->{Intent a=new Intent(this,MainActivity.class);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(a);});
 read.setOnClickListener(v->{Intent a=new Intent(this,MainActivity.class);a.putExtra("capri_capture_screen",true);a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(a);});
 params=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
 params.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;params.x=0;params.y=100;
 top.setOnTouchListener(new View.OnTouchListener(){float sy;int oy;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){sy=e.getRawY();oy=params.y;return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){params.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(root,params);return true;}return true;}});
 wm.addView(root,params);
 }
 @Override public void onDestroy(){if(root!=null&&wm!=null)wm.removeView(root);super.onDestroy();}
}