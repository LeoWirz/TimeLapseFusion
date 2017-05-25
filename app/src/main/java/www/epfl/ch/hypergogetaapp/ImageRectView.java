package www.epfl.ch.hypergogetaapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by WinLeo on 24.05.2017.
 */

public class ImageRectView extends android.support.v7.widget.AppCompatImageView {

    private Paint mRectPaint;

    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    private boolean mDrawRect = false;
    private TextPaint mTextPaint = null;

    private ImageRectView.OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onRectFinished(Rect rect);
    }

    public ImageRectView(final Context context) {
        super(context);
        init();
    }

    public ImageRectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageRectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setBorders(int left, int top, int right, int bottom) {
        mStartX = left;
        mStartY = top;
        mEndX = right;
        mEndY = bottom;
        mDrawRect = true;
    }

    public ArrayList<Integer> getBorders() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(mStartX);
        list.add(mStartY);
        list.add(mEndX);
        list.add(mEndY);
        return list;
    }

    /**
     * Sets callback for up
     *
     * @param callback {@link ImageRectView.OnUpCallback}
     */
    public void setOnUpCallback(ImageRectView.OnUpCallback callback) {
        mCallback = callback;
    }

    /**
     * Inits internal data
     */
    private void init() {
        mRectPaint = new Paint();
        mRectPaint.setColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(15);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        mTextPaint.setTextSize(20);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        // TODO: be aware of multi-touches
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawRect = false;
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (!mDrawRect || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    if (x > this.getRight()) {
                        mEndX = this.getRight();
                    } else if (x < 0) {
                        mEndX = 0;
                    } else {
                        mEndX = x;
                    }

                    if (y > this.getBottom()) {
                        mEndY = this.getBottom();
                    } else if (y < 0) {
                        mEndY = 0;
                    } else {
                        mEndY = y;
                    }

                    invalidate();
                }

                mDrawRect = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onRectFinished(new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.max(mEndX, mStartX), Math.max(mEndY, mStartX)));
                }
                invalidate();
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        int left = Math.min(mStartX, mEndX);
        int top = Math.min(mStartY, mEndY);
        int right = Math.max(mEndX, mStartX);
        int bottom = Math.max(mEndY, mStartY);

        if (mDrawRect) {
            canvas.drawRect(left, top, right, bottom, mRectPaint);
            //canvas.drawText("  (" + Math.abs(mStartX - mEndX) + ", " + Math.abs(mStartY - mEndY) + ")",
            //        Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mTextPaint);
        }
    }

}
