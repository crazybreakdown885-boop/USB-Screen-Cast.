package com.nightcity.capriai;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original cyberpunk anime-style portrait for Capri's in-app avatar. */
public class CapriFaceView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean speaking = false;
    private boolean mouthOpen = false;
    private final int pink = Color.rgb(255, 79, 163);
    public CapriFaceView(Context c) { super(c); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    public void setSpeaking(boolean value) { speaking=value; mouthOpen=value; invalidate(); }
    public void animateMouth(boolean open) { mouthOpen=open; invalidate(); }
    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w=getWidth(), h=getHeight(), cx=w/2f, cy=h/2f;
        c.save();
        p.setShader(new RadialGradient(cx,cy,Math.max(w,h)*.7f,new int[]{0xff352039,0xff11121d,0xff080910},null,Shader.TileMode.CLAMP));
        c.drawRoundRect(0,0,w,h,dp(22),dp(22),p); p.setShader(null);
        p.setColor(0x66ff4fa3); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); c.drawRoundRect(dp(4),dp(4),w-dp(4),h-dp(4),dp(20),dp(20),p); p.setStyle(Paint.Style.FILL);
        // Hair silhouette
        p.setColor(0xff171321); c.drawOval(cx-dp(66),cy-dp(83),cx+dp(66),cy+dp(87),p);
        p.setColor(0xffe8b9ad); c.drawOval(cx-dp(48),cy-dp(54),cx+dp(48),cy+dp(66),p);
        // Neck and jacket
        p.setColor(0xff171b2b); c.drawRoundRect(cx-dp(17),cy+dp(48),cx+dp(17),cy+dp(91),dp(9),dp(9),p);
        p.setColor(pink); Path jacket=new Path(); jacket.moveTo(cx-dp(17),cy+dp(66)); jacket.lineTo(cx-dp(58),cy+dp(96)); jacket.lineTo(cx+dp(58),cy+dp(96)); jacket.lineTo(cx+dp(17),cy+dp(66)); jacket.close(); c.drawPath(jacket,p);
        // Fringe and side locks
        p.setColor(0xff21172b); Path hair=new Path(); hair.moveTo(cx-dp(52),cy-dp(15)); hair.cubicTo(cx-dp(67),cy-dp(89),cx+dp(38),cy-dp(103),cx+dp(57),cy-dp(27)); hair.lineTo(cx+dp(31),cy-dp(5)); hair.lineTo(cx+dp(12),cy-dp(32)); hair.lineTo(cx-dp(7),cy-dp(5)); hair.lineTo(cx-dp(25),cy-dp(34)); hair.close(); c.drawPath(hair,p);
        c.drawRoundRect(cx-dp(57),cy-dp(30),cx-dp(43),cy+dp(65),dp(7),dp(7),p);
        c.drawRoundRect(cx+dp(43),cy-dp(30),cx+dp(57),cy+dp(65),dp(7),dp(7),p);
        // Eyes
        p.setColor(0xff24202c); c.drawOval(cx-dp(31),cy+dp(4),cx-dp(8),cy+dp(17),p); c.drawOval(cx+dp(8),cy+dp(4),cx+dp(31),cy+dp(17),p);
        p.setColor(0xff75e6e8); c.drawCircle(cx-dp(19),cy+dp(10),dp(4),p); c.drawCircle(cx+dp(19),cy+dp(10),dp(4),p);
        p.setColor(Color.WHITE); c.drawCircle(cx-dp(20),cy+dp(8),dp(1.5f),p); c.drawCircle(cx+dp(18),cy+dp(8),dp(1.5f),p);
        // Nose and mouth
        p.setColor(0xffb77e7c); p.setStrokeWidth(dp(1.4f)); p.setStyle(Paint.Style.STROKE); c.drawLine(cx,cy+dp(18),cx-dp(2),cy+dp(29),p); p.setStyle(Paint.Style.FILL);
        p.setColor(0xff8f344e);
        if(speaking && mouthOpen) c.drawOval(cx-dp(7),cy+dp(36),cx+dp(7),cy+dp(49),p);
        else { p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); c.drawArc(cx-dp(8),cy+dp(33),cx+dp(8),cy+dp(45),15,150,false,p); p.setStyle(Paint.Style.FILL); }
        // Neon hair streak
        p.setColor(pink); p.setStrokeWidth(dp(3)); p.setStrokeCap(Paint.Cap.ROUND); p.setStyle(Paint.Style.STROKE); Path streak=new Path(); streak.moveTo(cx+dp(27),cy-dp(77)); streak.quadTo(cx+dp(62),cy-dp(47),cx+dp(50),cy-dp(8)); c.drawPath(streak,p); p.setStyle(Paint.Style.FILL);
        c.restore();
    }
    private float dp(float v){return v*getResources().getDisplayMetrics().density;}
}