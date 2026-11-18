package com.nightcity.capriai;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import org.json.*;
import java.io.*;

public class CapriCharacterView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private JSONObject model;
    private Bitmap customArt;
    private String pose="sit";
    private String mood="neutra";
    private boolean speaking=false, blink=false, faceOnly=false;
    private int expressionVariant=0;
    private long start=SystemClock.uptimeMillis();
    private final int crimson=0xfff2154c, violet=0xff8a3dff, ink=0xff0a0710, green=0xff22f27a, blue=0xff1d6cff, white=0xffffffff;
    private final HandlerTick tick=new HandlerTick();

    public CapriCharacterView(Context c){super(c);setLayerType(View.LAYER_TYPE_HARDWARE,null);load(c);}
    private void load(Context c){
        try(InputStream in=c.getAssets().open("capri_model.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);
            model=new JSONObject(out.toString("UTF-8"));
        }catch(Exception ignored){model=null;}
        reloadCharacterImage();
    }
    public void reloadCharacterImage(){
        customArt=null;
        try{String saved=getContext().getSharedPreferences("capri_settings",0).getString("character_image_uri","");if(saved!=null&&!saved.isEmpty()){try(InputStream art=getContext().getContentResolver().openInputStream(android.net.Uri.parse(saved))){customArt=BitmapFactory.decodeStream(art);}}}catch(Exception ignored){customArt=null;}
        invalidate();
    }
    public void setSpeaking(boolean value){speaking=value;invalidate();}
    public void setFaceOnly(boolean value){faceOnly=value;invalidate();}
    public void setMood(String value){if(value==null)return;String m=value.toLowerCase(java.util.Locale.ROOT);if(m.contains("alegr")||m.contains("feliz")||m.contains("ótim")||m.contains("haha")||m.contains("obrigad")||m.contains("sorr"))mood="feliz";else if(m.contains("trist")||m.contains("pena")||m.contains("desculp"))mood="triste";else if(m.contains("surpres")||m.contains("uau")||m.contains("espant"))mood="surpresa";else if(m.contains("raiv")||m.contains("irrit")||m.contains("droga")||m.contains("merda")||m.contains("impacient"))mood="irritada";else if(m.contains("curios")||m.contains("pergunt")||m.contains("pens")||m.contains("atent")||m.contains("olha")||m.contains("escut"))mood="curiosa";else mood="neutra";expressionVariant=Math.floorMod(m.hashCode(),120);start=SystemClock.uptimeMillis();invalidate();}
    public String getMood(){return mood;}
    public void setPose(String value){if(value!=null&&model!=null){JSONObject poses=model.optJSONObject("poses");if(poses!=null&&poses.has(value)){pose=value;invalidate();}}}
    public void react(){start=SystemClock.uptimeMillis();invalidate();}
    public void setBlink(boolean value){blink=value;invalidate();}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float d=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight();
        c.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
        if(customArt!=null&&!faceOnly){float t=(SystemClock.uptimeMillis()-start)/1000f;float bob=(float)Math.sin(t*1.6f)*(speaking?2.4f:1.2f)*d;float scale=Math.min(w/customArt.getWidth(),h/customArt.getHeight())*.94f;float dw=customArt.getWidth()*scale,dh=customArt.getHeight()*scale;paint.setAlpha(255);c.drawBitmap(customArt,null,new RectF((w-dw)/2f,(h-dh)/2f+bob,(w+dw)/2f,(h+dh)/2f+bob),paint);postInvalidateDelayed(speaking?55:120);return;}
        if(faceOnly){drawMinimalFace(c,w,h,d);postInvalidateDelayed(speaking?55:100);return;}
        if(model==null){drawFallback(c,w,h,d);postInvalidateDelayed(40);return;}
        try{
            JSONObject p=model.getJSONObject("poses").getJSONObject(pose);
            JSONArray b=p.getJSONArray("bounds");float minX=(float)b.getDouble(0),minY=(float)b.getDouble(1),maxX=(float)b.getDouble(2),maxY=(float)b.getDouble(3);
            float t=(SystemClock.uptimeMillis()-start)/1000f;
            float scale=Math.min(w/(maxX-minX),h/(maxY-minY))*.92f;
            float bob=(float)Math.sin(t*1.7f)*(speaking?2.6f:1.4f)*d;
            c.save();c.translate((w-(maxX-minX)*scale)/2f-minX*scale,(h-(maxY-minY)*scale)/2f-minY*scale+bob);
            c.scale(scale,scale);
            JSONObject layers=p.getJSONObject("layers");
            drawLayer(c,layers,"violet",violet);
            drawLayer(c,layers,"hair",crimson);
            drawLayer(c,layers,"crimson",crimson);
            drawLayer(c,layers,"shade",0xffc80f3f);
            drawLayer(c,layers,"ink",ink);
            drawLayer(c,layers,"green",green);
            JSONObject face=p.optJSONObject("face");
            if(face!=null)drawFace(c,(float)face.optDouble("x"),(float)face.optDouble("y"),(float)face.optDouble("angle"),t);
            c.restore();
            if(speaking)postInvalidateDelayed(55);else postInvalidateDelayed(120);
        }catch(Exception ignored){drawFallback(c,w,h,d);}
    }
    private void drawLayer(Canvas c,JSONObject layers,String name,int color)throws Exception{
        JSONArray polys=layers.optJSONArray(name);if(polys==null)return;paint.setColor(color);paint.setStyle(Paint.Style.FILL);
        for(int i=0;i<polys.length();i++){JSONArray pts=polys.getJSONArray(i);if(pts.length()<3)continue;Path path=new Path();
            JSONArray first=pts.getJSONArray(0);path.moveTo((float)first.getDouble(0),(float)first.getDouble(1));
            for(int j=1;j<pts.length();j++){JSONArray q=pts.getJSONArray(j);path.lineTo((float)q.getDouble(0),(float)q.getDouble(1));}
            path.close();c.drawPath(path,paint);
        }
    }
    private void drawFace(Canvas c,float x,float y,float angle,float t){
        c.save();c.translate(x,y);c.rotate((float)Math.toDegrees(angle));
        Path head=new Path();head.moveTo(-22,-25);head.lineTo(-27,-9);head.lineTo(-24,8);head.lineTo(-15,21);head.lineTo(-5,30);head.lineTo(1,33);head.lineTo(6,29);head.lineTo(16,19);head.lineTo(24,6);head.lineTo(27,-10);head.lineTo(21,-26);head.lineTo(8,-32);head.lineTo(-9,-31);head.close();
        paint.setColor(violet);c.save();c.translate(-1.6f,2);c.drawPath(head,paint);c.restore();paint.setColor(ink);c.drawPath(head,paint);
        Path l=new Path();l.moveTo(-28,-15);l.lineTo(-3,-20);l.lineTo(-2,4);l.lineTo(-8,9);l.lineTo(-23,8);l.lineTo(-29,-2);l.close();
        Path r=new Path();r.moveTo(3,-21);r.lineTo(28,-17);r.lineTo(30,-3);r.lineTo(23,7);r.lineTo(7,9);r.lineTo(2,4);r.close();
        paint.setColor(green);c.drawPath(l,paint);c.drawPath(r,paint);
        int intensity=expressionVariant%5;
        int gaze=(expressionVariant/5)%4;
        float eyeY=mood.equals("triste")? -3f : (mood.equals("surpresa")?-7.5f:-5.8f);
        float eyeScale=(mood.equals("surpresa")?1.12f:(mood.equals("irritada")?.88f:1.0f))+(intensity-2)*.045f;
        float gazeX=gaze==1?2.2f:(gaze==2?-2.2f:0f);
        float gazeY=gaze==3?1.8f:0f;
        drawEye(c,-13.5f,eyeY,eyeScale,-.05f,gazeX,gazeY);drawEye(c,13.5f,eyeY+2.2f,eyeScale*.92f,.1f,gazeX,gazeY);
        paint.setColor(ink);paint.setStrokeWidth(2.1f);paint.setStyle(Paint.Style.STROKE);
        Path brow=new Path();if(mood.equals("irritada")){brow.moveTo(-23,-18);brow.lineTo(-7,-14);brow.moveTo(7,-14);brow.lineTo(23,-19);}else if(mood.equals("surpresa")){brow.moveTo(-23,-23);brow.quadTo(-14,-29,-6,-22);brow.moveTo(6,-22);brow.quadTo(15,-29,23,-23);}else if(mood.equals("triste")){brow.moveTo(-23,-17);brow.lineTo(-8,-22);brow.moveTo(8,-22);brow.lineTo(23,-17);}else{brow.moveTo(-22,-20);brow.quadTo(-14,-24,-7,-20);brow.moveTo(7,-20);brow.quadTo(15,-24,22,-20);}c.drawPath(brow,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(ink);Path bang=new Path();bang.moveTo(-17,-32);bang.lineTo(-3,-32);bang.lineTo(-8,-14);bang.close();c.drawPath(bang,paint);
        bang.reset();bang.moveTo(3,-32);bang.lineTo(17,-30);bang.lineTo(7,-16);bang.close();c.drawPath(bang,paint);
        if(speaking){float open=.45f+(float)Math.abs(Math.sin(t*12))*.55f;paint.setColor(0xff2a0f3a);c.drawRoundRect(-6,17,6,17+9*open,2,2,paint);paint.setColor(white);Path fang=new Path();fang.moveTo(-6,17);fang.lineTo(-2,19);fang.lineTo(0,18);fang.lineTo(2,19);fang.lineTo(6,17);fang.lineTo(2,27);fang.lineTo(0,29);fang.lineTo(-3,26);fang.close();c.drawPath(fang,paint);}
        else{paint.setColor(white);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.4f);Path mouth=new Path();if(mood.equals("feliz")){mouth.moveTo(-7,18);mouth.quadTo(0,29,7,18);}else if(mood.equals("triste")){mouth.moveTo(-7,24);mouth.quadTo(0,16,7,24);}else if(mood.equals("surpresa")){paint.setStyle(Paint.Style.FILL);c.drawOval(-3,17,3,26,paint);}else if(mood.equals("irritada")){mouth.moveTo(-7,21);mouth.lineTo(7,21);}else if(mood.equals("curiosa")){mouth.moveTo(-5,20);mouth.quadTo(1,24,6,18);}else{mouth.moveTo(-7,19);mouth.lineTo(-3,18);mouth.lineTo(0,20);mouth.lineTo(3,18);mouth.lineTo(7,19);}if(!mood.equals("surpresa"))c.drawPath(mouth,paint);paint.setStyle(Paint.Style.FILL);}
        c.restore();
    }
    private void drawEye(Canvas c,float x,float y,float sx,float rot,float gazeX,float gazeY){
        c.save();c.translate(x,y);c.rotate(rot*57.3f);c.scale(sx,1);
        Path eye=new Path();eye.moveTo(-12,-1);eye.quadTo(0,-10,12,-1);eye.quadTo(0,8,-12,-1);eye.close();
        paint.setColor(ink);c.drawPath(eye,paint);c.save();c.clipPath(eye);
        paint.setColor(green);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.4f);c.drawCircle(0,0,6.8f,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(blue);c.drawCircle(gazeX,gazeY,5,paint);paint.setColor(white);c.drawCircle(gazeX-1.4f,gazeY-1.5f,1.8f,paint);c.restore();c.restore();
    }
    private void drawMinimalFace(Canvas c,float w,float h,float d){
        c.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
        float t=(SystemClock.uptimeMillis()-start)/1000f;
        float s=Math.min(w/104f,h/78f)*.92f;
        c.save();c.translate(w/2f,h/2f+(float)Math.sin(t*1.8f)*2*d);c.scale(s,s);
        // Faixa angular serrilhada: recorte neon inspirado no desenho da personagem.
        Path band=new Path();band.moveTo(-48,-16);band.lineTo(-37,-22);band.lineTo(-30,-17);band.lineTo(-23,-23);band.lineTo(-14,-18);band.lineTo(-5,-24);band.lineTo(4,-18);band.lineTo(14,-23);band.lineTo(23,-17);band.lineTo(32,-22);band.lineTo(47,-15);band.lineTo(39,-5);band.lineTo(47,5);band.lineTo(34,12);band.lineTo(25,7);band.lineTo(15,14);band.lineTo(5,8);band.lineTo(-5,15);band.lineTo(-15,8);band.lineTo(-25,14);band.lineTo(-34,7);band.lineTo(-46,12);band.lineTo(-39,1);band.close();
        paint.setColor(violet);c.drawPath(band,paint);paint.setColor(ink);c.drawPath(band,paint);
        paint.setColor(green);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.1f);
        Path eye=new Path();eye.moveTo(-34,-5);eye.lineTo(-25,-12);eye.lineTo(-8,-10);eye.lineTo(-3,-4);eye.lineTo(-11,1);eye.lineTo(-27,1);eye.close();
        Path eye2=new Path();eye2.moveTo(4,-4);eye2.lineTo(10,-10);eye2.lineTo(27,-12);eye2.lineTo(35,-5);eye2.lineTo(28,1);eye2.lineTo(12,1);eye2.close();
        c.drawPath(eye,paint);c.drawPath(eye2,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(blue);c.drawOval(-23,-8,-13,-1,paint);c.drawOval(14,-8,24,-1,paint);
        paint.setColor(white);c.drawCircle(-17,-5,1.8f,paint);c.drawCircle(20,-5,1.8f,paint);
        paint.setColor(crimson);paint.setStrokeWidth(2.5f);paint.setStyle(Paint.Style.STROKE);
        Path mouth=new Path();if(speaking){float open=3+(float)Math.abs(Math.sin(t*13))*5;mouth.moveTo(-8,24);mouth.lineTo(-3,24+open);mouth.lineTo(2,24);mouth.lineTo(7,24+open*.7f);}else{mouth.moveTo(-8,24);mouth.lineTo(-3,21);mouth.lineTo(1,25);mouth.lineTo(5,21);mouth.lineTo(9,24);}c.drawPath(mouth,paint);paint.setStyle(Paint.Style.FILL);
        c.restore();
    }
    private void drawFallback(Canvas c,float w,float h,float d){
        paint.setColor(crimson);c.drawCircle(w/2,h/2,Math.min(w,h)*.3f,paint);
        paint.setColor(green);c.drawCircle(w*.42f,h*.42f,5*d,paint);c.drawCircle(w*.58f,h*.42f,5*d,paint);
    }
    private static class HandlerTick {}
}
