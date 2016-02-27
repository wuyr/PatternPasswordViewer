package com.demo.patternpasswordviewer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static com.demo.patternpasswordviewer.R.drawable;
import static com.demo.patternpasswordviewer.R.id.ic_1;
import static com.demo.patternpasswordviewer.R.id.ic_2;
import static com.demo.patternpasswordviewer.R.id.ic_3;
import static com.demo.patternpasswordviewer.R.id.ic_4;
import static com.demo.patternpasswordviewer.R.id.ic_5;
import static com.demo.patternpasswordviewer.R.id.ic_6;
import static com.demo.patternpasswordviewer.R.id.ic_7;
import static com.demo.patternpasswordviewer.R.id.ic_8;
import static com.demo.patternpasswordviewer.R.id.ic_9;

/**
 * Created by wuyr on 2/24/16 5:31 PM.
 */
public class PatternView extends LinearLayout {

    private Context mContext;
    private ImageView[] mImageViews;
    private List<Integer> mPic;
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    public PatternView(Context context) {
        super(context);
        init(context);
    }

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PatternView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.pattern_view, this);
        List<Integer> ids = new ArrayList<>();
        ids.add(ic_1);
        ids.add(ic_2);
        ids.add(ic_3);
        ids.add(ic_4);
        ids.add(ic_5);
        ids.add(ic_6);
        ids.add(ic_7);
        ids.add(ic_8);
        ids.add(ic_9);
        mImageViews = new ImageView[9];
        for (int i = 0; i < 9; i++)
            mImageViews[i] = (ImageView) rootView.findViewById(ids.get(i));

        mPic = new ArrayList<>();
        mPic.add(drawable.ic_1);
        mPic.add(drawable.ic_2);
        mPic.add(drawable.ic_3);
        mPic.add(drawable.ic_4);
        mPic.add(drawable.ic_5);
        mPic.add(drawable.ic_6);
        mPic.add(drawable.ic_7);
        mPic.add(drawable.ic_8);
        mPic.add(drawable.ic_9);

        mPaint = new Paint();
        mPaint.setStrokeWidth(8);
        mPaint.setColor(getResources().getColor(R.color.colorAccent));
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(128);
    }

    public void draw(String s) {
        resetIcon();
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        byte[] data = parseString(s);
        for (int i = 0; i < data.length; i++) {
            mImageViews[data[i]].setImageResource(mPic.get(i));
            if (i + 1 < data.length)
                drawLine(mImageViews[data[i]], mImageViews[data[i + 1]]);
        }
    }

    private void resetIcon(){
        for (ImageView iv : mImageViews)
            iv.setImageResource(drawable.ic_selected);
    }

    private void drawLine(View start, View end) {
        mCanvas.drawLine(start.getX() + start.getWidth() / 2, start.getY() + start.getHeight() / 2,
                end.getX() + end.getWidth() / 2, end.getY() + end.getHeight() / 2, mPaint);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(new BitmapDrawable(getResources(), mBitmap));
        } else
            setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), mBitmap));
    }

    private byte[] parseString(String s) {
        int length = s.length();
        byte[] result = new byte[length];
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++)
            result[i] = Byte.parseByte(handleString(String.valueOf(chars[i])));
        return result;
    }

    private String handleString(String s) {
        for (int i = 1; i < 10; i++)
            s = s.replace(i + "", i - 1 + "");
        return s;
    }
}
