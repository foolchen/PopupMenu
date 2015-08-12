package com.foolchen.popmenu.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.foolchen.popmenu.BuildConfig;
import com.foolchen.popmenu.R;

/**
 * 自定义的PopupMenu
 *
 * @author chenchong
 *         15/8/12
 *         下午3:36
 */
public class PopupMenu extends FrameLayout {
    private static final String TAG = PopupMenu.class.getSimpleName();
    /** 位置-左侧 */
    public static final int LEFT = 0;
    /** 位置-顶部 */
    public static final int TOP = 1;
    /** 位置-右侧 */
    public static final int RIGHT = 2;
    /** 位置-底部 */
    public static final int BOTTOM = 3;
    /** 默认动画执行时间 */
    private static final int DEFAULT_DURATION = 200;

    private static final int DEFAULT_SCRIM_COLOR = 0xB2000000;
    private static final int DEFAULT_TRANSPARENT = 0x00000000;

    /** 子View的位置 */
    private int mGravity;
    /** 子View,用于菜单显示 */
    private View mChild;
    /** 用于标识子View是否正在现实 */
    private boolean isChildDisplaying;
    /** 用于标识动画是否正在进行 */
    private boolean onGoing;
    /** 动画执行时间 */
    private int mTransitionDuration = DEFAULT_DURATION;
    /** 菜单的出现/消失动画 */
    private ValueAnimator mDisplayAnimator, mDismissAnimator;
    /** 背景色渐变Drawable */
    private TransitionDrawable mTransitionDrawable;
    /** 点击菜单外部区域的{@link android.view.View.OnClickListener} */
    private OnClickListener mOutsideClickListener;

    public PopupMenu(Context context) {
        super(context);
        init(context, null);
    }

    public PopupMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PopupMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PopupMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = getChildCount();
        if (childCount != 1) {
            throw new ExceptionInInitializerError("该控件需要且只能有一个子View");
        }
        mChild = getChildAt(0);
    }

    /** 初始化 */
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PopupMenu);
            mGravity = ta.getInt(R.styleable.PopupMenu_pGravity, LEFT);
            mTransitionDuration = ta.getInt(R.styleable.PopupMenu_pDuration, DEFAULT_DURATION);
            final int scrimColor = ta.getColor(R.styleable.PopupMenu_pScrimColor, DEFAULT_SCRIM_COLOR);
            Drawable[] drawables = {new ColorDrawable(DEFAULT_TRANSPARENT), new ColorDrawable(scrimColor)};
            mTransitionDrawable = new TransitionDrawable(drawables);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                setBackground(mTransitionDrawable);
            } else {
                //noinspection deprecation
                setBackgroundDrawable(mTransitionDrawable);
            }
            final boolean outsideTouchable = ta.getBoolean(R.styleable.PopupMenu_pOutsideTouchable, true);
            setOutsideTouchable(outsideTouchable);
            ta.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildWithMargins(mChild, widthMeasureSpec, 0, heightMeasureSpec, 0);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "子View的宽度:" + mChild.getMeasuredWidth() + ",height:" + mChild.getMeasuredHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childWidth = mChild.getMeasuredWidth();
        final int childHeight = mChild.getMeasuredHeight();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("onLayout(changed = %1$s,left = %2$d,top = %3$d,right = %4$d,bottom = %5$d)"
                    , String.valueOf(changed), left, top, right, bottom));
        }
        switch (mGravity) {
            case LEFT:
                mChild.layout(-childWidth, 0, 0, childHeight);
                break;
            case TOP:
                mChild.layout(0, -childHeight, childWidth, 0);
                break;
            case RIGHT:
                mChild.layout(right - left, 0, right - left + childWidth, childHeight);
                break;
            case BOTTOM:
                mChild.layout(0, bottom - top, right, bottom - top + childHeight);
                break;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "left = " + mChild.getLeft() + ",right = " + mChild.getRight() + ",top = " + mChild.getTop() + ",bottom = " + mChild.getBottom());
        }
    }

    /** 显示菜单 */
    public void show() {
        if (onGoing) {
            return;
        }
        final int childWidth = mChild.getMeasuredWidth();
        final int childHeight = mChild.getMeasuredHeight();
        int startX = -1, startY = -1, endX = -1, endY = -1;
        switch (mGravity) {
            case LEFT:
                startX = 0;
                endX = childWidth;
                break;
            case RIGHT:
                startX = 0;
                endX = -childWidth;
                break;
            case TOP:
                startY = 0;
                endY = childHeight;
                break;
            case BOTTOM:
                startY = 0;
                endY = -childHeight;
                break;
        }

        if (mGravity == LEFT || mGravity == RIGHT) {
            mDisplayAnimator = ValueAnimator.ofInt(startX, endX);
            mDisplayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    mChild.setTranslationX(value);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "show, value = " + value);
                    }
                }
            });
        } else {
            mDisplayAnimator = ValueAnimator.ofInt(startY, endY);
            mDisplayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    mChild.setTranslationY(value);
                }
            });
        }
        mDisplayAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onGoing = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isChildDisplaying = true;
                onGoing = false;
                setOnClickListener(mOutsideClickListener);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onGoing = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mDisplayAnimator.setDuration(mTransitionDuration);
        mDisplayAnimator.start();
        mTransitionDrawable.startTransition(mTransitionDuration);
    }

    public void dismiss() {
        if (onGoing) {
            return;
        }
        final int childWidth = mChild.getMeasuredWidth();
        final int childHeight = mChild.getMeasuredHeight();
        int startX = -1, startY = -1, endX = -1, endY = -1;
        switch (mGravity) {
            case LEFT:
                startX = childWidth;
                endX = 0;
                break;
            case RIGHT:
                startX = -childWidth;
                endX = 0;
                break;
            case TOP:
                startY = childHeight;
                endY = 0;
                break;
            case BOTTOM:
                startY = -childHeight;
                endY = 0;
                break;
        }

        if (mGravity == LEFT || mGravity == RIGHT) {
            mDismissAnimator = ValueAnimator.ofInt(startX, endX);
            mDismissAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    mChild.setTranslationX(value);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "dismiss, value = " + value);
                    }
                }
            });
        } else {
            mDismissAnimator = ValueAnimator.ofInt(startY, endY);
            mDismissAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    mChild.setTranslationY(value);
                }
            });
        }
        mDismissAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                onGoing = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isChildDisplaying = false;
                onGoing = false;
                setOnClickListener(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onGoing = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mDismissAnimator.setDuration(mTransitionDuration);
        mDismissAnimator.start();
        mTransitionDrawable.reverseTransition(mTransitionDuration);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDisplayAnimator != null && mDisplayAnimator.isRunning()) {
            mDisplayAnimator.cancel();
        }
        if (mDismissAnimator != null && mDismissAnimator.isRunning()) {
            mDismissAnimator.cancel();
        }
    }

    public boolean isChildDisplaying() {
        return isChildDisplaying;
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
        if (mGravity != LEFT && mGravity != TOP && mGravity != RIGHT && mGravity != BOTTOM) {
            throw new IllegalArgumentException("只能指定LEFT/TOP/RIGHT/BOTTOM四种类型");
        }
        reset();
    }

    public void reset() {
        mChild.setTranslationX(0);
        mChild.setTranslationY(0);
        mTransitionDrawable.resetTransition();
        requestLayout();
        isChildDisplaying = false;
        onGoing = false;
    }

    public int getGravity() {
        return mGravity;
    }

    public void setOutsideTouchable(boolean touchable) {
        if (touchable) {
            mOutsideClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isChildDisplaying) {
                        dismiss();
                    }
                }
            };
        } else {
            mOutsideClickListener = null;
        }
    }
}
