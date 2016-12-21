package com.jeremy.android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;


import com.jeremy.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 2016/11/30.
 */
public class InterestView extends RelativeLayout {

    private final static int ACTION_CLICK_FOR_LEFT = 0;
    private final static int ACTION_CLICK_FOR_RIGHT = 1;
    private final static int ACTION_TOUCH_MOVE = 2;

    private final static int MAX_VISIBLE_COUNT = 4;

    private ImageView imgLeft, imgRight;
    private FrameLayout rlList;

    private float density = getResources().getDisplayMetrics().density;
    private int mTouchSlop;

    private ListAdapter mAdapter;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            showAll();
        }
    };

    public InterestView(Context context) {
        this(context, null);
    }

    public InterestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InterestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);

        LayoutInflater.from(context).inflate(R.layout.layout_interest_view, this);
        rlList = (FrameLayout) findViewById(R.id.rl_list);
        imgLeft = (ImageView) findViewById(R.id.img_left);
        imgRight = (ImageView) findViewById(R.id.img_right);

        imgLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlList.getChildCount() > 0) {
                    View child = rlList.getChildAt(0);
                    new AutoMoveAnimator(child, ACTION_CLICK_FOR_LEFT).start();
                }
                if (iLeftBtnOnclickListener != null) {
                    iLeftBtnOnclickListener.onclick();
                }
            }
        });

        imgRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlList.getChildCount() > 0) {
                    View child = rlList.getChildAt(0);
                    new AutoMoveAnimator(child, ACTION_CLICK_FOR_RIGHT).start();
                }
                if (iRightBtnOnclickListener != null) {
                    iRightBtnOnclickListener.onclick();
                }
            }
        });
    }

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(ListAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
        showAll();
    }

    private void showAll() {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return;
        }
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View child = mAdapter.getView(i, null, this);
            if (child.getLayoutParams() == null) {
                child.setLayoutParams(generateDefaultLayoutParams());
            }
            rlList.addView(child);
        }

        if (getMeasuredWidth() > 0) {
            resetListUI(false);
            return;
        }

        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                resetListUI(false);
                removeOnLayoutChangeListener(this);
            }
        });
    }

    List<Animator> animators = new ArrayList<>();

    void resetListUI(boolean isFromRemove) {
        int count = rlList.getChildCount();
        int offsetX = 15;
        int offsetY = 32;
        animators.clear();
        for (int i = 0; i < count; i++) {
            float scale = (rlList.getMeasuredWidth() - dp2px(i * offsetX)) / (float) rlList.getMeasuredWidth();
            CardView child = (CardView) rlList.getChildAt(i);
            float toTranY, toTranZ;
            if (i < MAX_VISIBLE_COUNT) {
                toTranY = dp2px(i * offsetY);
                toTranZ = child.getCardElevation() - dp2px(i);
            } else {
                toTranY = dp2px((MAX_VISIBLE_COUNT - 1) * offsetY);
                toTranZ = child.getCardElevation() - dp2px(MAX_VISIBLE_COUNT - 1);
            }
            if (!isFromRemove) {
                child.setPivotY(0);
                child.setPivotX(getMeasuredWidth() / 2);
                child.setScaleX(scale);
                child.setScaleY(scale);
                ViewCompat.setTranslationY(child, toTranY);
                ViewCompat.setTranslationZ(child, toTranZ);
            } else {
                if (i < MAX_VISIBLE_COUNT) {
                    Animator animator = new AutoFixAnimator(child, child.getScaleX(), scale, ViewCompat.getTranslationY(child), toTranY, ViewCompat.getTranslationZ(child), toTranZ);
                    animators.add(animator);
                } else {
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                    ViewCompat.setTranslationY(child, toTranY);
                    ViewCompat.setTranslationZ(child, toTranZ);
                }
            }
        }
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animators);
        set.setDuration(200);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }
        });
        set.start();
    }

    int dp2px(int dp) {
        return (int) (dp * density);
    }

    private int mDownX;
    private int mDownY;
    private int mLastMoveX;
    private int mLastMoveY;
    private float originCardViewTranX;
    private float originCardViewTranY;
    private float originCardViewRawX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !(mIsAnimating || mAdapter == null || rlList.getChildCount() == 0) && super.dispatchTouchEvent(ev);
    }


    private boolean isMultiTouch = false;//是否多指触摸

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int moveX = (int) ev.getRawX();
        int moveY = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMultiTouch = false;
                mDownX = moveX;
                mDownY = moveY;
                View child = rlList.getChildAt(0);
                child.getLocationInWindow(pos);
                originCardViewRawX = pos[0];
                originCardViewTranX = ViewCompat.getTranslationX(child);
                originCardViewTranY = ViewCompat.getTranslationY(child);
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveX = moveX;
                mLastMoveY = moveY;
                // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
                int distanceXY = (int) Math.sqrt(Math.pow((moveX - mDownX), 2) + Math.pow((moveY - mDownY), 2));
                if (distanceXY > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int moveX = (int) event.getRawX();
        int moveY = (int) event.getRawY();

        View child = rlList.getChildAt(0);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isPosInside(child, mDownX, mDownY)) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMultiTouch) {
                    int disX = moveX - mLastMoveX;
                    int disY = moveY - mLastMoveY;
                    ViewCompat.setTranslationX(child, ViewCompat.getTranslationX(child) + disX);
                    ViewCompat.setTranslationY(child, ViewCompat.getTranslationY(child) + disY);
                    mLastMoveX = moveX;
                    mLastMoveY = moveY;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                isMultiTouch = event.getPointerCount() > 0;
                new AutoMoveAnimator(child, ACTION_TOUCH_MOVE).start();
                break;
        }
        return true;
    }

    int pos[] = new int[2];

    private boolean isPosInside(View v, int posX, int posY) {
        v.getLocationInWindow(pos);
        return posX >= pos[0] && posX <= (pos[0] + v.getMeasuredWidth()) && posY >= pos[1] && posY <= (pos[1] + v.getMeasuredHeight());
    }

    boolean mIsAnimating = false;

    class AutoFixAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

        private View photoView;

        AutoFixAnimator(View v, float fromScale, float toScale, float fromTranY, float toTranY, float fromTranZ, float toTranZ) {
            photoView = v;
            addUpdateListener(this);

            PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", fromScale, toScale);
            PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", fromScale, toScale);
            PropertyValuesHolder pvhTranY = PropertyValuesHolder.ofFloat("tranY", fromTranY, toTranY);
            PropertyValuesHolder pvhTranZ = PropertyValuesHolder.ofFloat("tranZ", fromTranZ, toTranZ);
            setValues(pvhScaleX, pvhScaleY, pvhTranY, pvhTranZ);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float scaleX = (float) animation.getAnimatedValue("scaleX");
            float scaleY = (float) animation.getAnimatedValue("scaleY");
            float tranY = (float) animation.getAnimatedValue("tranY");
            float tranZ = (float) animation.getAnimatedValue("tranZ");
            photoView.setScaleX(scaleX);
            photoView.setScaleY(scaleY);
            ViewCompat.setTranslationY(photoView, tranY);
            ViewCompat.setTranslationZ(photoView, tranZ);
        }
    }

    class AutoMoveAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

        private float slope;//直线斜率
        private View photoView;

        AutoMoveAnimator(final View v, int action) {
            photoView = v;
            boolean isRemove = true;
            boolean isLeft = true;

            int leftBorder = 0;
            int rightBorder = getMeasuredWidth();

            float endTranX, endTranY;

            float lastTranX = ViewCompat.getTranslationX(v);
            float lastTranY = ViewCompat.getTranslationY(v);

            if (action == ACTION_TOUCH_MOVE) {
                int distanceXY = (int) Math.sqrt(Math.pow((lastTranX - originCardViewTranX), 2) + Math.pow((lastTranY - originCardViewTranY), 2));//滑动距离
                if (distanceXY > v.getMeasuredWidth() / 3) {
                    slope = (lastTranY - originCardViewTranY) / (lastTranX - originCardViewTranX);
                    float disX = lastTranX - originCardViewTranX;
                    if (disX <= 0) {
                        endTranX = -originCardViewRawX - v.getMeasuredWidth();
                    } else {
                        isLeft = false;
                        endTranX = getMeasuredWidth() - originCardViewRawX;
                    }
                    endTranY = (int) (slope * (endTranX - originCardViewTranX) + originCardViewTranY);
                } else {
                    isRemove = false;
                    endTranX = originCardViewTranX;
                    endTranY = originCardViewTranY;
                }
            } else if (action == ACTION_CLICK_FOR_LEFT) {
                slope = (float) -Math.tan(60);
                endTranX = -originCardViewRawX - v.getMeasuredWidth();
                endTranY = (int) (slope * (endTranX - originCardViewTranX) + originCardViewTranY);
            } else {
                isLeft = false;
                slope = (float) Math.tan(60);
                endTranX = getMeasuredWidth() - originCardViewRawX;
                endTranY = (int) (slope * (endTranX - originCardViewTranX) + originCardViewTranY);
            }

            PropertyValuesHolder pvhTranX = PropertyValuesHolder.ofFloat("tranX", ViewCompat.getTranslationX(v), endTranX);
            PropertyValuesHolder pvhTranY = PropertyValuesHolder.ofFloat("tranY", ViewCompat.getTranslationY(v), endTranY);

            setValues(pvhTranX, pvhTranY);
            setInterpolator(new AccelerateInterpolator());
            setDuration(300);
            addUpdateListener(this);
            final boolean finalIsLike = isLeft;
            final boolean finalIsRemove = isRemove;
            addListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                    imgLeft.setClickable(false);
                    imgRight.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    if (finalIsRemove) {
                        rlList.removeView(photoView);
                        if (iDirectionListener != null) {
                            iDirectionListener.onDirection(finalIsLike);
                        }
                        resetListUI(true);
                    }
                    imgLeft.setClickable(true);
                    imgRight.setClickable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float tranX = (float) animation.getAnimatedValue("tranX");
            float tranY = (float) animation.getAnimatedValue("tranY");
            ViewCompat.setTranslationX(photoView, tranX);
            ViewCompat.setTranslationY(photoView, tranY);
        }

    }

    public interface IDirectionListener {
        void onDirection(boolean isLeft);
    }

    private IDirectionListener iDirectionListener;

    public void setiDirectionListener(IDirectionListener iDirectionListener) {
        this.iDirectionListener = iDirectionListener;
    }

    public interface ILeftBtnOnclickListener {
        void onclick();
    }

    private ILeftBtnOnclickListener iLeftBtnOnclickListener;

    public void setiLeftBtnOnclickListener(ILeftBtnOnclickListener iLeftBtnOnclickListener) {
        this.iLeftBtnOnclickListener = iLeftBtnOnclickListener;
    }

    public interface IRightBtnOnclickListener {
        void onclick();
    }

    private IRightBtnOnclickListener iRightBtnOnclickListener;

    public void setiRightBtnOnclickListener(IRightBtnOnclickListener iRightBtnOnclickListener) {
        this.iRightBtnOnclickListener = iRightBtnOnclickListener;
    }

}
