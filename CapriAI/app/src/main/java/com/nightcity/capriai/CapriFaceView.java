package com.nightcity.capriai;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/**
 * Stylized, hand-drawn Capri-inspired portrait.
 * The original show artwork is not embedded; this keeps the character's
 * recognizable black/neon-green/pink palette and jagged pink silhouette.
 */
public class CapriFaceView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean speaking = false;
    private boolean mouthOpen = false;
    private final int pink = Color.rgb(255, 35, 145);
    private final int green = Color.rgb(185, 255, 35);
    private final int blue = Color.rgb(80, 190, 255);

    public CapriFaceView(Context c) {
        super(c);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setSpeaking(boolean value) {
        speaking = value;
        mouthOpen = value;
        invalidate();
    }

    public void animateMouth(boolean open) {
        mouthOpen = open;
        invalidate();
    }

    private void fill(Canvas c, int color, Path path) {
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        p.setShader(null);
        c.drawPath(path, p);
    }

    private Path polygon(float... xy) {
        Path path = new Path();
        path.moveTo(xy[0], xy[1]);
        for (int i = 2; i < xy.length; i += 2) path.lineTo(xy[i], xy[i + 1]);
        path.close();
        return path;
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight(), cx = w / 2f, cy = h / 2f;
        float s = Math.min(w / dp(260), h / dp(184));
        c.save();
        c.translate(cx, cy);
        c.scale(s, s);
        c.translate(-130, -92);

        // Dark red / black character-card backdrop.
        p.setShader(new LinearGradient(0, 0, 260, 184,
                new int[]{0xff6e102f, 0xff210b20, 0xff080910},
                null, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, 260, 184, p);
        p.setShader(null);
        p.setColor(0x44ff287f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.2f);
        c.drawRect(5, 5, 255, 179, p);
        p.setStyle(Paint.Style.FILL);

        // Neck and angular shoulders, kept mostly in shadow.
        fill(c, 0xff080a12, polygon(105, 132, 155, 132, 166, 184, 92, 184));
        fill(c, 0xff15131e, polygon(102, 145, 48, 184, 212, 184, 158, 145));
        fill(c, pink, polygon(52, 181, 82, 158, 96, 170, 87, 184));
        fill(c, green, polygon(173, 171, 188, 158, 207, 181, 212, 184));

        // Huge, jagged neon-pink hair silhouette.
        Path hair = new Path();
        hair.moveTo(130, 8);
        hair.lineTo(111, 27); hair.lineTo(89, 0); hair.lineTo(88, 34);
        hair.lineTo(52, 13); hair.lineTo(70, 53); hair.lineTo(34, 45);
        hair.lineTo(67, 75); hair.lineTo(42, 94); hair.lineTo(78, 105);
        hair.lineTo(65, 145); hair.lineTo(103, 128); hair.lineTo(130, 153);
        hair.lineTo(155, 127); hair.lineTo(198, 146); hair.lineTo(185, 105);
        hair.lineTo(226, 88); hair.lineTo(190, 68); hair.lineTo(218, 36);
        hair.lineTo(177, 47); hair.lineTo(178, 8); hair.lineTo(148, 29);
        hair.close();
        fill(c, 0xfff21b91, hair);

        // Black inner hair and side locks create the sharp graphic silhouette.
        fill(c, 0xff090a12, polygon(82, 34, 111, 21, 130, 34, 151, 21, 181, 42,
                187, 94, 169, 137, 130, 151, 91, 137, 73, 93));
        fill(c, 0xfff82a9b, polygon(72, 54, 91, 66, 79, 112, 60, 130, 67, 91));
        fill(c, 0xfff82a9b, polygon(177, 49, 196, 37, 203, 84, 216, 102, 190, 113));

        // Angular black face, not a human skin-tone oval.
        fill(c, 0xff05070c, polygon(91, 54, 111, 42, 151, 42, 174, 58,
                169, 112, 150, 137, 129, 145, 106, 131, 88, 103));
        // Neon green cheek / brow markings.
        fill(c, green, polygon(83, 65, 108, 55, 119, 62, 98, 76, 86, 91));
        fill(c, green, polygon(143, 61, 161, 53, 180, 68, 174, 91, 161, 76));
        fill(c, 0xff72ff4a, polygon(91, 105, 108, 112, 119, 126, 100, 120));
        fill(c, 0xff72ff4a, polygon(151, 119, 171, 104, 165, 126, 145, 135));

        // Large bright-blue eyes with sharp eyeliner.
        fill(c, 0xff020308, polygon(89, 77, 119, 69, 126, 84, 111, 96, 92, 91));
        fill(c, 0xff020308, polygon(137, 83, 148, 68, 176, 77, 171, 91, 151, 96));
        p.setColor(blue); c.drawOval(99, 77, 117, 91, p); c.drawOval(148, 77, 166, 91, p);
        p.setColor(0xff07162b); c.drawOval(105, 77, 113, 92, p); c.drawOval(154, 77, 162, 92, p);
        p.setColor(Color.WHITE); c.drawCircle(108, 80, 2.2f, p); c.drawCircle(157, 80, 2.2f, p);

        // Nose and animated mouth.
        p.setColor(green);
        p.setStrokeWidth(1.5f);
        p.setStyle(Paint.Style.STROKE);
        c.drawLine(130, 91, 125, 105, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(pink);
        if (speaking && mouthOpen) {
            c.drawOval(119, 111, 141, 132, p);
            p.setColor(0xfff4d7e8);
            c.drawRect(123, 112, 137, 116, p);
        } else {
            p.setColor(green);
            p.setStrokeWidth(2.2f);
            p.setStyle(Paint.Style.STROKE);
            c.drawArc(117, 108, 143, 128, 15, 150, false, p);
            p.setStyle(Paint.Style.FILL);
        }

        // Extra bright spikes and highlights for the iconic silhouette.
        fill(c, 0xffff83c5, polygon(88, 1, 101, 28, 91, 24));
        fill(c, 0xffff83c5, polygon(217, 36, 189, 52, 197, 38));
        fill(c, 0xffa9ff38, polygon(50, 45, 72, 57, 59, 58));
        fill(c, 0xffa9ff38, polygon(207, 101, 185, 108, 197, 94));

        c.restore();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
