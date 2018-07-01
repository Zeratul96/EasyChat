package com.bs.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;

import com.bs.easy_chat.R;


public class SelectPicturePopupWindow extends PopupWindow implements View.OnClickListener {

    private Context context;
    private View mMenuView;
    private PopupWindow popupWindow;
    private OnSelectedListener mOnSelectedListener;

    public SelectPicturePopupWindow(Context context) {
        super(context);
        this.context = context;
        mMenuView = LayoutInflater.from(context).inflate(R.layout.picture_selector, null);

        mMenuView.findViewById(R.id.take_photo).setOnClickListener(this);
        mMenuView.findViewById(R.id.pick_photo).setOnClickListener(this);
        mMenuView.findViewById(R.id.cancel).setOnClickListener(this);
    }

    public void resetBackGroundView(){
        mMenuView = LayoutInflater.from(context).inflate(R.layout.background_selector, null);

        mMenuView.findViewById(R.id.take_photo).setOnClickListener(this);
        mMenuView.findViewById(R.id.pick_photo).setOnClickListener(this);
        mMenuView.findViewById(R.id.cancel).setOnClickListener(this);

    }

    /**
     * 把一个View控件添加到PopupWindow上并且显示
     *
     * @param activity:显示在当前Activity上
     */
    public void showPopupWindow(final Activity activity) {

        //收起软键盘
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(),0);
        }

        popupWindow = new PopupWindow(mMenuView,ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER | Gravity.BOTTOM, 0, 0);
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popupWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1;
                activity.getWindow().setAttributes(lp);
            }
        });

        //点击返回键只是让PopupWindow 消失 不是退出Activity
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = 0.7f;
        activity.getWindow().setAttributes(lp);

        popupWindow.update();
    }

    /**
     * 移除PopupWindow
     */
    public void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo:
                if(null != mOnSelectedListener) {
                    mOnSelectedListener.OnSelected(v, 0);
                }
                break;
            case R.id.pick_photo:
                if(null != mOnSelectedListener) {
                    mOnSelectedListener.OnSelected(v, 1);
                }
                break;
            case R.id.cancel:
                if(null != mOnSelectedListener) {
                    mOnSelectedListener.OnSelected(v, 2);
                }
                break;
        }
    }

    /**
     * 设置选择监听
     * @param l:接口对象
     */
    public void setOnSelectedListener(OnSelectedListener l) {
        this.mOnSelectedListener = l;
    }

    /**
     * 选择监听接口
     */
    public interface OnSelectedListener {
        void OnSelected(View v, int position);
    }

}