package com.bs.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bs.easy_chat.R;


/**
 自定义view的几个步骤：
 * 1、首先需要写一个类来继承自View
 * 2、需要得到view的对象，那么需要重写构造方法，其中一参的构造方法用于new，二参的构造方法用于xml布局文件使用，三参的构造方法可以传入一个样式
 * 3、需要设置view的大小，那么需要重写onMeasure方法
 * 4、需要设置view的位置，那么需要重写onLayout方法，但是这个方法在自定义view的时候用的不多，原因主要在于view的位置主要是由父控件来决定
 * 5、需要绘制出所需要显示的view，那么需要重写onDraw方法
 * 6、当控件状态改变的时候，需要重绘view，那么调用invalidate();方法，这个方法实际上会重新调用onDraw方法
 * 7、在这其中，如果需要对view设置点击事件，可以直接调用setOnClickListener方法
 */

public class MyToggleButton extends View {

    //开关按钮的背景
    private Bitmap backgroundBitmap;
    //开关按钮的滑动部分
    private Bitmap slideButton;
    //滑动按钮的左边界
    private float slideBtn_left;
    //当前开关的状态
    private boolean currentState = false;
    private int MAX_DISTANCE;
    private int firstX;
    private int secondX;
    private boolean isDrag;
    boolean isToggleListenerOn =false;
    public OnToggleListener onToggleListener;

    /**
     * 在代码里面创建对象的时候，使用此构造方法
     */
    public MyToggleButton(Context context) {
        super(context);
    }

    /**
     * 在布局文件中声明的view，创建时由系统自动调用
     *
     * @param context:
     * @param attrs:
     */
    public MyToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * 测量尺寸时的回调方法
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置当前view的大小 width:view的宽，单位都是像素值 height:view的高，单位都是像素值
        setMeasuredDimension(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight());
    }

    // 这个方法对于自定义view的时候帮助不大，因为view的位置一般由父组件来决定的
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * 画view的方法,绘制当前view的内容
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        // 打开抗锯齿
        paint.setAntiAlias(true);

        // 画背景
        canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
        // 画滑块
        canvas.drawBitmap(slideButton, slideBtn_left, 0, paint);
    }

    /**
     * 更换按钮背景色
     */
    private void changeBackgroundColor()
    {
        if(currentState)
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        else
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_off);

        System.gc();
    }

    /**
     * 初始化view
     */
    private void initView() {

        changeBackgroundColor();

        slideButton = BitmapFactory.decodeResource(getResources(),
                R.drawable.silderbtn);

        MAX_DISTANCE = backgroundBitmap.getWidth() - slideButton.getWidth();
    }

    /**
     * 刷新视图
     */
    protected void flushView() {
        invalidate();//调用此方法可以回调OnDraw
    }

    /**
     * 刷新当前的状态
     */
    protected void flushState() {
        if (currentState) {
            slideBtn_left = backgroundBitmap.getWidth() - slideButton.getWidth();
        } else {
            slideBtn_left = 0;
        }
    }


    public interface OnToggleListener
    {
        void onToggleSate(boolean state);
    }
    public void setOnToggleStateListener(OnToggleListener listener)
    {
        isToggleListenerOn  = true;
        onToggleListener = listener;

    }
    public void setToggleState(boolean state)
    {
        currentState = state;
        //疑问：为什么卸载OnDraw中无法触发滑动事件
        if(currentState)
        {
            slideBtn_left = backgroundBitmap.getWidth()-slideButton.getWidth();
        }else
        {
            slideBtn_left=0;
        }

        changeBackgroundColor();
        invalidate();
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 当按下的时候
                isDrag = false;
                firstX = secondX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                // 当移动的时候
                isDrag = true;
                // 计算手指在屏幕上移动的距离
                int disX = (int) (event.getX() - secondX);
                secondX = (int) event.getX();

                // 更新slideBtn_left
                slideBtn_left = slideBtn_left + disX;

                // 做一个判断，防止滑块划出边界，滑块的范围应该是在[0,MAX_LEFT_DISTANCE];
                if (slideBtn_left < 0) {
                    slideBtn_left = 0;
                } else {
                    if (slideBtn_left > MAX_DISTANCE) {
                        slideBtn_left = MAX_DISTANCE;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 抬起的时候，判断松开的位置是哪里，来由此来决定开关的状态是打开还是关闭
                if(slideBtn_left==0||slideBtn_left==MAX_DISTANCE)
                    currentState = (int) event.getX()>=MAX_DISTANCE/2;
                else
                    currentState = slideBtn_left>=MAX_DISTANCE/2;


                if(isToggleListenerOn)
                    onToggleListener.onToggleSate(currentState);

                // 由开关的状态标志，确定应该是打开还是关闭状态
                flushState();
                break;
        }

        changeBackgroundColor();
        flushView();
        return true;
    }

    public boolean getCurrentState(){
        return currentState;
    }
}
