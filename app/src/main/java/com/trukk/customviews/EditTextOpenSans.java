package com.trukk.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.trukk.R;

public class EditTextOpenSans extends AppCompatEditText {

    public EditTextOpenSans(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }
    public EditTextOpenSans(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init();
    }
    public EditTextOpenSans(Context context){
        super(context);
        init();
    }

    public void init() {
        Typeface openSansRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans_Regular.ttf");
        setTypeface(openSansRegular);
    }
}

