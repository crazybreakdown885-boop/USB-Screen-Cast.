package com.nightcity.capriai;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Uses a real Capri close-up sourced from a documented fan reference archive. */
public class CapriFaceView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap portrait;
    private boolean speaking = false;
    private boolean mouthOpen = false;

    public CapriFaceView(Context context) {
        super(context);
        portrait = BitmapFactory.decodeResource(getResources(), R.drawable.capri_portrait);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
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

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        canvas.drawColor(0xff05090d);

        if (portrait != null) {
            float pulse = speaking && mouthOpen ? 1.012f : 1.0f;
            float targetW=w*0.60f; float targetH=h*0.98f; float scale=Math.min(targetW/portrait.getWidth(),targetH/portrait.getHeight())*pulse;
            float dw = portrait.getWidth() * scale;
            float dh = portrait.getHeight() * scale;
            float left = w - dw - 8f * getResources().getDisplayMetrics().density;
            float top = (h - dh) / 2f;

            Path clip = new Path();
            clip.addRoundRect(0, 0, w, h, 16f * getResources().getDisplayMetrics().density,
                    16f * getResources().getDisplayMetrics().density, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(clip);
            paint.setColor(Color.WHITE);
            canvas.drawBitmap(portrait, null, new RectF(left, top, left + dw, top + dh), paint);
            canvas.restore();
        }

        float density = getResources().getDisplayMetrics().density;
        paint.setColor(0xff35d9f5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f * density);
        canvas.drawRoundRect(0.7f * density, 0.7f * density,
                w - 0.7f * density, h - 0.7f * density,
                16f * density, 16f * density, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xffff315f);
        canvas.drawRect(w * 0.36f, h - 2.5f * density, w * 0.64f, h - 1.2f * density, paint);
    }
}
