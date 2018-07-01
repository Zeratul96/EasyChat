package com.bs.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by 13273 on 2017/9/22.
 *
 * 该控件使用时描述文件必须加DrawableRight属性
 */

public class ClearEditText extends EditText implements View.OnFocusChangeListener,TextWatcher{

    private Drawable mClearDrawable;
    private boolean hasFocus;
    private OnClearBtnDownListener listener;

    public ClearEditText(Context context){
        this(context, null);
    }

   public ClearEditText(Context context, AttributeSet attrs){
        super(context, attrs);
       init();
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mClearDrawable = getCompoundDrawables()[2];

        mClearDrawable.setBounds(0,0,mClearDrawable.getIntrinsicWidth(),mClearDrawable.getIntrinsicHeight());
        setOnFocusChangeListener(this);
        addTextChangedListener(this);

        setDrawableVisible(false);
    }


    /**
     * getWidth 控件总宽
     * getTotalPaddingRight():图片左边缘到控件右边缘距离
     * getPaddingRight():图片右边缘到控件右边缘距离
     * @param event:事件
     * @return 是否消费此次事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        requestFocus();
        if(event.getAction() == MotionEvent.ACTION_UP)
            if(getCompoundDrawables()[2] != null)
            {
                int start = getWidth() - getTotalPaddingRight();
                int end = getWidth() - getPaddingRight();
                boolean available = (event.getX() > start) && (event.getX() < end);
                if(available){
                    this.setText("");
                    if(listener != null)
                        listener.onClearBtnDown();
                }
            }
        return super.onTouchEvent(event);
    }


    @Override
    public void onFocusChange(View view, boolean b) {
        this.hasFocus = b;
        setDrawableVisible(hasFocus && getText().length() > 0);
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if(hasFocus)
            setDrawableVisible(text.length() > 0);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {}

    private void setDrawableVisible(boolean visible)
    {
        Drawable right = visible? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],right,getCompoundDrawables()[3]);
    }

    /**
     * 设置按取消键监听器
     */

    public interface OnClearBtnDownListener{
        void onClearBtnDown();
    }

    public void setOnClearBtnDownListener(OnClearBtnDownListener listener){
        this.listener = listener;
    }
}
