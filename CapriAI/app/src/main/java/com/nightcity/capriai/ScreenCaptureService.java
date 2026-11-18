package com.nightcity.capriai;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.ByteArrayOutputStream;

public class ScreenCaptureService extends Service {
 public static volatile boolean active=false;
 public static final String ACTION_STOP="com.nightcity.capriai.STOP_CAPTURE";
 MediaProjection projection;ImageReader reader;VirtualDisplay display;Handler handler;boolean continuous=false;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public void onCreate(){super.onCreate();handler=new Handler(Looper.getMainLooper());active=true;}
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(intent==null)return START_NOT_STICKY;
  if(ACTION_STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
  continuous=intent.getBooleanExtra("continuous",false);
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("capri_capture","Capri screen context",NotificationManager.IMPORTANCE_LOW));
  Notification n=new Notification.Builder(this,"capri_capture").setContentTitle(continuous?"Capri está acompanhando sua tela":"Capri está lendo a tela").setContentText("Captura autorizada por você. Toque em parar no app para encerrar.").setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(continuous).build();
  if(Build.VERSION.SDK_INT>=29)startForeground(41,n,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);else startForeground(41,n);
  try{
   int result=intent.getIntExtra("resultCode",0);Intent data=(Intent)intent.getParcelableExtra("data");
   MediaProjectionManager mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);projection=mgr.getMediaProjection(result,data);
   DisplayMetrics dm=new DisplayMetrics();((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
   reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,android.graphics.PixelFormat.RGBA_8888,3);
   projection.registerCallback(new MediaProjection.Callback(){@Override public void onStop(){stopSelf();}},handler);
   display=projection.createVirtualDisplay("CapriScreen",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
   handler.postDelayed(this::captureOnce,700);
  }catch(Exception e){stopSelf();}
  return START_NOT_STICKY;
 }
 void captureOnce(){
  Image image=null;
  try{
   image=reader.acquireLatestImage();
   if(image==null){if(continuous)handler.postDelayed(this::captureOnce,600);else stopSelf();return;}
   Image.Plane p=image.getPlanes()[0];int pixel=p.getPixelStride(),row=p.getRowStride();
   Bitmap full=Bitmap.createBitmap(row/pixel,image.getHeight(),Bitmap.Config.ARGB_8888);full.copyPixelsFromBuffer(p.getBuffer());
   Bitmap crop=Bitmap.createBitmap(full,0,0,image.getWidth(),image.getHeight());
   int targetW=Math.min(1280,crop.getWidth());int targetH=(int)(crop.getHeight()*(targetW/(float)crop.getWidth()));
   Bitmap scaled=Bitmap.createScaledBitmap(crop,targetW,targetH,true);ByteArrayOutputStream out=new ByteArrayOutputStream();scaled.compress(Bitmap.CompressFormat.JPEG,55,out);
   Intent result=new Intent("com.nightcity.capriai.SCREEN_CAPTURED").setPackage(getPackageName()).putExtra("image",Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP));sendBroadcast(result);
   full.recycle();crop.recycle();scaled.recycle();
  }catch(Exception ignored){}finally{if(image!=null)image.close();}
  if(continuous)handler.postDelayed(this::captureOnce,5000);else stopSelf();
 }
 @Override public void onDestroy(){active=false;handler.removeCallbacksAndMessages(null);if(display!=null)display.release();if(reader!=null)reader.close();if(projection!=null)projection.stop();stopForeground(true);super.onDestroy();}
}
