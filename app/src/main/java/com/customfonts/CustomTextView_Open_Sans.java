package com.customfonts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


public class CustomTextView_Open_Sans extends android.support.v7.widget.AppCompatTextView {

    public CustomTextView_Open_Sans(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CustomTextView_Open_Sans(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTextView_Open_Sans(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
            setTypeface(tf);
        }
    }

}