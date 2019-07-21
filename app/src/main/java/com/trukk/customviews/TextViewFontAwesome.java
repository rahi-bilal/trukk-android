package com.trukk.customviews;

import android.support.v7.widget.AppCompatTextView;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TextViewFontAwesome extends AppCompatTextView {

    public TextViewFontAwesome(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public TextViewFontAwesome(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init();
    }

    public TextViewFontAwesome(Context context){
        super(context);
        init();
    }

    public void init(){
        Typeface openSansRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/FontAwesome_5_solid.otf");
        setTypeface(openSansRegular);
    }
}

