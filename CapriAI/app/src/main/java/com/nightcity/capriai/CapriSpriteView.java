package com.nightcity.capriai;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;

/** Lightweight animated companion renderer for the Android overlay. */
public class CapriSpriteView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap art;
    private long start = SystemClock.uptimeMillis();
    private boolean speaking = false;
    private boolean excited = false;

    public CapriSpriteView(Context c) {
        super(c);
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.capri_portrait);
        art = b;
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
    public void setSpeaking(boolean v) { speaking=v; invalidate(); }
    public void react() { excited=true; postDelayed(()->{excited=false;invalidate();},850); invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float d=getResources().getDisplayMetrics().density;
        float w=getWidth(), h=getHeight();
        float t=(SystemClock.uptimeMillis()-start)/1000f;
        float bob=(float)Math.sin(t*1.65f)*4*d;
        float sway=(float)Math.sin(t*.72f)*(speaking?2.8f:1.4f)*d;
        float pulse=speaking?1.025f+(float)Math.sin(t*13f)*.012f:1f+(float)Math.sin(t*1.8f)*.008f;
        if(excited)pulse+=.025f;
        if(art!=null){
            float maxW=w*.94f, maxH=h*.96f;
            float s=Math.min(maxW/art.getWidth(),maxH/art.getHeight())*pulse;
            float dw=art.getWidth()*s, dh=art.getHeight()*s;
            float x=(w-dw)/2f+sway, y=(h-dh)/2f+bob;
            c.save();
            c.translate(w/2f,h/2f);
            c.rotate((float)Math.sin(t*.9f)*.7f);
            c.translate(-w/2f,-h/2f);
            p.setAlpha(255);
            c.drawBitmap(art,null,new RectF(x,y,x+dw,y+dh),p);
            c.restore();
        }
        // Neon edge and small scanline glints keep the companion alive without a heavy 3D engine.
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(1.5f*d);
        p.setColor(speaking?0xff22f27a:0xff28dff4); p.setAlpha(185);
        c.drawRoundRect(d, d, w-d, h-d, 18*d,18*d,p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xffff2458); p.setAlpha(210);
        c.drawCircle(w*.84f,h*.12f,(speaking?3.4f:2.2f)*d,p);
        p.setAlpha(255);
        postInvalidateDelayed(33);
    }
}
