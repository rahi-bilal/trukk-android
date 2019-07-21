package com.trukk.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class TextViewOpenSans extends AppCompatTextView {

    public TextViewOpenSans(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public TextViewOpenSans(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init();
    }

    public TextViewOpenSans(Context context){
        super(context);
        init();
    }

    public void init(){
        Typeface openSansRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans_Regular.ttf");
        setTypeface(openSansRegular);
    }
}
