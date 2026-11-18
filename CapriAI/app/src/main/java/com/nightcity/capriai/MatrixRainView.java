package com.nightcity.capriai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import java.util.Random;

/** Lightweight animated terminal rain; throttled for entry-level Android devices. */
public final class MatrixRainView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random=new Random(17);
    private float[] y=new float[0],speed=new float[0];
    private int[] glyph=new int[0],tint=new int[0];
    private final int columnWidth;
    private final int[] palette={0xfff2154c,0xff8a3dff,0xff22f27a,0xff28dff4};
    public MatrixRainView(Context context){super(context);paint.setTypeface(Typeface.MONOSPACE);paint.setTextSize(dp(13));columnWidth=dp(17);setWillNotDraw(false);}
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private void prepare(int width,int height){int count=Math.max(8,width/Math.max(1,columnWidth)+1);y=new float[count];speed=new float[count];glyph=new int[count];tint=new int[count];for(int i=0;i<count;i++){y[i]=-random.nextInt(Math.max(1,height));speed[i]=dp(1.2f+random.nextFloat()*2.4f);glyph[i]=random.nextInt(2);tint[i]=palette[random.nextInt(palette.length)];}}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){prepare(w,h);}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);canvas.drawColor(0xff070910);for(int i=0;i<y.length;i++){float x=i*columnWidth;paint.setColor(tint[i]);paint.setAlpha(70+(i%4)*22);canvas.drawText(glyph[i]==0?"0":"1",x,y[i],paint);if(i%3==0){paint.setAlpha(28);canvas.drawText(glyph[i]==0?"1":"0",x,y[i]-dp(18),paint);}y[i]+=speed[i];if(y[i]>getHeight()+dp(25)){y[i]=-random.nextInt(Math.max(dp(40),getHeight()/2));speed[i]=dp(1.2f+random.nextFloat()*2.4f);glyph[i]=random.nextInt(2);tint[i]=palette[random.nextInt(palette.length)];}}postInvalidateDelayed(60);}
}