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
 MediaProjection projection; ImageReader reader; Handler handler;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public void onCreate(){super.onCreate();handler=new Handler(Looper.getMainLooper());}
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(intent==null)return START_NOT_STICKY;
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  if(android.os.Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("capri_capture","Capri screen capture",NotificationManager.IMPORTANCE_LOW));
  Notification n=new Notification.Builder(this,"capri_capture").setContentTitle("Capri está lendo a tela").setContentText("A captura foi autorizada por você.").setSmallIcon(android.R.drawable.ic_menu_view).build();
  startForeground(41,n);
  try{
   int result=intent.getIntExtra("resultCode",0);Intent data=(Intent)intent.getParcelableExtra("data");
   MediaProjectionManager mgr=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);projection=mgr.getMediaProjection(result,data);
   DisplayMetrics dm=new DisplayMetrics();((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
   reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,android.graphics.PixelFormat.RGBA_8888,2);
   projection.createVirtualDisplay("CapriScreen",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader.getSurface(),null,handler);
   handler.postDelayed(()->captureOnce(),650);
  }catch(Exception e){stopSelf();}
  return START_NOT_STICKY;
 }
 void captureOnce(){Image image=null;try{
  image=reader.acquireLatestImage();if(image==null){handler.postDelayed(()->captureOnce(),300);return;}
  Image.Plane p=image.getPlanes()[0];int pixel=p.getPixelStride(),row=p.getRowStride();Bitmap full=Bitmap.createBitmap(row/pixel,image.getHeight(),Bitmap.Config.ARGB_8888);full.copyPixelsFromBuffer(p.getBuffer());Bitmap crop=Bitmap.createBitmap(full,0,0,image.getWidth(),image.getHeight());ByteArrayOutputStream out=new ByteArrayOutputStream();crop.compress(Bitmap.CompressFormat.JPEG,65,out);
  Intent result=new Intent("com.nightcity.capriai.SCREEN_CAPTURED");result.setPackage(getPackageName());result.putExtra("image",Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP));sendBroadcast(result);
  full.recycle();crop.recycle();
 }catch(Exception ignored){}finally{if(image!=null)image.close();stopSelf();}}
 @Override public void onDestroy(){if(reader!=null)reader.close();if(projection!=null)projection.stop();stopForeground(true);super.onDestroy();}
}