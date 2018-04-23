package com.geefox.geefoxptrlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GeeFox
 * 2018/4/20
 */

public class GeeFoxPullToRefreshFrame extends FrameLayout {


    /**
     * 滑动状态
     */
    private static int SCROLL_IDLE = 0;//停止
    private static int SCROLL_UP = 1;//向上
    private static int SCROLL_DOWN = 2;//向下

    /**
     * 滑动摩擦系数系数
     */
    private static final float DEFAULT_FRICTION = 0.5f;


    //  默认刷新时间
    private static final int DEFAULT_REFRESH_DURATION = 300;

    //  自动刷新时间
    private static final int DEFAULT_AUTO_REFRESH_DURATION = 100;
    //   刷新完成时，默认平滑滚动单位距离
    private static final int DEFAULT_SMOOTH_SCROLL_DISTANCE = 50;
    //   刷新完成时，默认平滑滚动单位时间
    private static final int DEFAULT_SMOOTH_SCROLL_DURATION = 3;


    //摩擦系数
    private float mFriction = DEFAULT_FRICTION;
    public long mDuration = DEFAULT_REFRESH_DURATION;
    //平滑滚动单位距离
    private int mSmoothDistance = DEFAULT_SMOOTH_SCROLL_DISTANCE;
    //平滑滚动单位时间
    private int mSmoothDuration = DEFAULT_SMOOTH_SCROLL_DURATION;
    //是否可下拉
    private boolean mRefreshEnabled = true;
    //是否可上拉
    private boolean mLoadMoreEnabled = true;
    //下拉监听
    protected OnRefreshListener mOnRefreshListener;
    //上拉监听
    protected OnLoadMoreListener mOnLoadMoreListener;

    private View mHeaderView;//头部视图
    private View mFooterView;//底部视图
    private View mContentView;//内容视图


    //    头部高度
    protected int mHeaderHeight;
    //    底部高度
    protected int mFooterHeight;

    private boolean isSetHeaderHeight;
    private boolean isSetFooterHeight;

    private boolean isOnRefresh;//是否正在刷新
    private boolean isOnLoadMore;//是否正在加载更多


    //mIsCoo=true时有效
    private View mScrollView;
    private AppBarLayout mAppBarLayout;
    private int mRefreshBackgroundResource;
    private int mLoadMoreBackgroundResource;
    private boolean mIsCooLayout;
    private boolean isDependentOpen = true;

    private Scroller mScroller;

    //不可滑动view的滑动方向
    private int isUpOrDown = SCROLL_IDLE;

    //判断y轴方向的存储值
    float directionX;
    //判断x轴方向存储值
    float directionY;
    //下拉偏移
    private int mHeadOffY;
    //上拉偏移
    private int mFootOffY;
    //内容偏移
    private int mContentOffY;
    //最后一次触摸的位置
    private float lastY;
    //偏移
    private int currentOffSetY;
    //触摸移动的位置
    private int offsetSum;
    //触摸移动的位置之和
    private int scrollSum;
    //一个缓存值
    private int tempY;

    public GeeFoxPullToRefreshFrame(@NonNull Context context) {
        this(context, null);
    }

    public GeeFoxPullToRefreshFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public GeeFoxPullToRefreshFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mScroller = new Scroller(context);

//            mScrollView = findViewById(R.id.can_scroll_view);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GeeFoxPullToRefreshFrame, defStyleAttr, 0);
        try {
            final int N = a.getIndexCount();
            for (int i = 0; i < N; i++) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.GeeFoxPullToRefreshFrame_canRefresh) {
                    setRefreshEnabled(a.getBoolean(attr, true));
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_canLoadMore) {
                    setLoadMoreEnabled(a.getBoolean(attr, true));
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrFriction) {
                    setFriction(a.getFloat(attr, DEFAULT_FRICTION));
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrDuration) {
                    mDuration = a.getInt(attr, DEFAULT_REFRESH_DURATION);
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrSmooth_duration) {
                    mSmoothDuration = a.getInt(attr, DEFAULT_SMOOTH_SCROLL_DURATION);
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrSmoothDistance) {
                    mSmoothDistance = a.getInt(attr, DEFAULT_SMOOTH_SCROLL_DISTANCE);
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrRefreshBg) {
                    mRefreshBackgroundResource = a.getResourceId(attr, android.R.color.transparent);
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrLoadMoreBg) {
                    mLoadMoreBackgroundResource = a.getResourceId(attr, android.R.color.transparent);
                } else if (attr == R.styleable.GeeFoxPullToRefreshFrame_ptrIsCoo) {
                    mIsCooLayout = a.getBoolean(attr, false);
                }
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        final int childCount = getChildCount();
        if (childCount > 0) {
            mContentView = getChildAt(0);
        }
        mHeaderView = new RotateRefreshView(getContext());
        mHeaderView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mFooterView = new RotateRefreshView(getContext());
        mFooterView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.addView(mHeaderView);
        this.addView(mFooterView);
        if (mContentView == null) {
            throw new IllegalStateException("mContentView is null");
        }
        if (mIsCooLayout) {
            if (mContentView instanceof CoordinatorLayout) {
                CoordinatorLayout coo = (CoordinatorLayout) mContentView;
                mAppBarLayout = (AppBarLayout) coo.getChildAt(0);
                setAppBarListener();
                //find mScrollView
                int coordChildCount = coo.getChildCount();
                for (int i = 0; i < coordChildCount; i++) {
                    View child = coo.getChildAt(i);
                    if (child instanceof NestedScrollingChild) {
                        mScrollView = child;
                        break;
                    }
                }
            } else {
                throw new IllegalStateException("mContentView is not CoordinatorLayout");
            }
            if (mScrollView == null) {
                throw new IllegalStateException("mScrollView is null");
            }
            if (!(mScrollView instanceof NestedScrollingChild)) {
                throw new IllegalStateException("mScrollView is not NestedScrollingChild");
            }
        }
        if (mHeaderView != null && !(mHeaderView instanceof IRefreshView)) {
            throw new IllegalStateException("mHeaderView  error");
        }
        if (mFooterView != null && !(mFooterView instanceof IRefreshView)) {
            throw new IllegalStateException("mFooterView error");
        }
        if (mHeaderView != null) {
            getHeaderInterface().setIsHeaderOrFooter(true);
        }
        if (mFooterView != null) {
            getFooterInterface().setIsHeaderOrFooter(false);
        }

        super.onFinishInflate();
        steView();

    }


    private void setAppBarListener() {
        if (mAppBarLayout != null) {
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                    int miniH = mAppBarLayout.getMeasuredHeight() / 2;

                    if (verticalOffset == 0) {
                        isDependentOpen = true;
                    } else if (Math.abs(verticalOffset) >= miniH) {
                        isDependentOpen = false;
                    }
                }
            });
        }
    }


    /**
     * 设置风格
     */
    public void steView() {
        bringChildToFront(mContentView);
        if (mHeaderView != null) {
            bringChildToFront(mHeaderView);
        }
        if (mFooterView != null) {
            bringChildToFront(mFooterView);
        }
        int count = getChildCount();

        List<View> list = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            View v = getChildAt(i);

            if (v != mHeaderView && v != mFooterView && v != mContentView) {

                list.add(v);
            }
        }
        for (View v : list) {
            bringChildToFront(v);
        }
    }

    public void setFooterView(View footerView) {
        if (null == footerView) return;
        if (!(footerView instanceof IRefreshView))
            throw new IllegalStateException("footerView is not IRefreshView");
        final int index = indexOfChild(mFooterView);
        if (index >= 0) {
            removeViewAt(index);
        }
        mFooterView = footerView;
        addView(mFooterView);
        steView();
        requestLayout();
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            if (!isSetHeaderHeight) {
                mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
        }

        if (mFooterView != null) {
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            if (!isSetFooterHeight) {
                mFooterHeight = mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
        }

        if (mContentView != null) {
            measureChildWithMargins(mContentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v != mHeaderView && v != mFooterView && v != mContentView) {
                measureChildWithMargins(v, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin - mHeaderHeight + mHeadOffY;
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);
        }

        if (mFooterView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = getMeasuredHeight() + paddingTop + lp.topMargin - mFootOffY;
            final int right = left + mFooterView.getMeasuredWidth();
            final int bottom = top + mFooterView.getMeasuredHeight();
            mFooterView.layout(left, top, right, bottom);
        }

        if (mContentView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + mContentOffY;
            final int right = left + mContentView.getMeasuredWidth();
            final int bottom = top + mContentView.getMeasuredHeight();
            mContentView.layout(left, top, right, bottom);
        }

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v != mHeaderView && v != mFooterView && v != mContentView) {
                MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
                int left = paddingLeft + lp.leftMargin;
                int top = paddingTop + lp.topMargin + mContentOffY;
                int right = left + v.getMeasuredWidth();
                int bottom = top + v.getMeasuredHeight();
                v.layout(left, top, right, bottom);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //     当是不可滑动的view里进入
        Log.e("blh", "isChildCanPullDown:" + isChildCanPullDown() + "\tisChildCanPullUp:" + isChildCanPullUp());
        if (!isChildCanPullDown() && !isChildCanPullUp()) {

            if (isUpOrDown == SCROLL_DOWN) {
                if (canRefresh()) {
                    return onTouch(e, true);
                }
            } else if (isUpOrDown == SCROLL_UP) {
                if (canLoadMore()) {
                    return onTouch(e, false);
                }
            } else {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        directionY = e.getY();
                        directionX = e.getX();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (directionY <= 0 || directionX <= 0) {

                            break;
                        }

                        float eventY = e.getY();
                        float eventX = e.getX();

                        float offY = eventY - directionY;
                        float offX = eventX - directionX;

                        directionY = eventY;
                        directionX = eventX;

                        boolean moved = Math.abs(offY) > Math.abs(offX);
                        if (offY > 0 && moved && canRefresh()) {
                            isUpOrDown = SCROLL_DOWN;
                        } else if (offY < 0 && moved && canLoadMore()) {
                            isUpOrDown = SCROLL_UP;
                        } else {
                            isUpOrDown = SCROLL_IDLE;
                        }
                        break;
                }
                return true;
            }
        } else {
            if (canRefresh()) {
                return onTouch(e, true);
            } else if (canLoadMore()) {
                return onTouch(e, false);
            }
        }
        return super.onTouchEvent(e);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                directionY = e.getY();
                directionX = e.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (directionY <= 0 || directionX <= 0) {
                    break;
                }
                float eventY = e.getY();
                float eventX = e.getX();
                float offY = eventY - directionY;
                float offX = eventX - directionX;
                directionY = eventY;
                directionX = eventX;
                boolean moved = Math.abs(offY) > Math.abs(offX);
                if (offY > 0 && moved && canRefresh()) {
                    isUpOrDown = SCROLL_DOWN;
                } else if (offY < 0 && moved && canLoadMore()) {
                    isUpOrDown = SCROLL_UP;
                } else {
                    isUpOrDown = SCROLL_IDLE;
                }
                if (isUpOrDown == SCROLL_UP || isUpOrDown == SCROLL_DOWN) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(e);
    }


    /**
     * 触摸滑动处理
     *
     * @param e
     * @param isHead
     * @return
     */
    private boolean onTouch(MotionEvent e, boolean isHead) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = e.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (lastY > 0) {
                    currentOffSetY = (int) (e.getY() - lastY);
                    offsetSum += currentOffSetY;
                }
                lastY = e.getY();
                boolean isCanMove;
                if (isHead) {
                    isCanMove = offsetSum > 0;
                } else {
                    isCanMove = offsetSum < 0;
                }
                if (isCanMove) {
                    float ratio = getRatio();
                    if (ratio < 0) {
                        ratio = 0;
                    }
                    int scrollNum = -((int) (currentOffSetY * ratio));
                    scrollSum += scrollNum;
                    if (isHead) {
                        setBackgroundResource(mRefreshBackgroundResource);
                        smoothMove(true, true, scrollNum);
                        if (Math.abs(scrollSum) > mHeaderHeight) {
                            getHeaderInterface().onPrepare();
                        }
                        getHeaderInterface().onPositionChange(Math.abs(scrollSum) / (float) mHeaderHeight);
                    } else {
                        setBackgroundResource(mLoadMoreBackgroundResource);
                        smoothMove(false, true, scrollNum);
                        if (Math.abs(scrollSum) > mFooterHeight) {
                            getFooterInterface().onPrepare();
                        }
                        getFooterInterface().onPositionChange(Math.abs(scrollSum) / (float) mFooterHeight);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isHead) {
                    if (Math.abs(scrollSum) > mHeaderHeight) {
                        smoothMove(true, false, -mHeaderHeight);
                        getHeaderInterface().onRelease();
                        doRefreshing();
                    } else {
                        smoothMove(true, false, 0);
                    }
                } else {
                    if (Math.abs(scrollSum) > mFooterHeight) {
                        smoothMove(false, false, mContentView.getMeasuredHeight() - getMeasuredHeight() + mFooterHeight);
                        getFooterInterface().onRelease();
                        doLoadingMore();
                    } else {
                        smoothMove(false, false, mContentView.getMeasuredHeight() - getMeasuredHeight());
                    }
                }
                resetParameter();
                break;
        }


        return super.onTouchEvent(e);


    }


    /**
     * * 滚动布局的方法
     *
     * @param isHeader
     * @param isMove      手指在移动还是已经抬起
     * @param moveScrollY
     */
    private void smoothMove(boolean isHeader, boolean isMove, int moveScrollY) {
//        moveY = Math.abs(moveY);
        if (isHeader) {
            if (isMove) {
                smoothScrollBy(0, moveScrollY);
            } else {
                smoothScrollTo(0, moveScrollY);
            }
        } else {
            if (isMove) {
                smoothScrollBy(0, moveScrollY);
            } else {
                smoothScrollTo(0, moveScrollY);
            }
        }
    }


    /**
     * 滑动系数
     *
     * @return
     */
    private float getRatio() {

        return 1 - (Math.abs(offsetSum) / (float) getMeasuredHeight()) - 0.3f * mFriction;

    }


    /**
     * 刷新完成
     */
    public void refreshComplete() {

        if (!isOnRefresh) {
            return;
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                smoothMove(true, false, 0);
                isOnRefresh = false;
                getHeaderInterface().onComplete();
                getHeaderInterface().onReset();

            }
        }, mDuration);


    }


    /**
     * 滚动到目标位置
     *
     * @param fx
     * @param fy
     */
    public void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    /**
     * 滚动相对偏移位置
     *
     * @param dx
     * @param dy
     */
    public void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy);
        invalidate();
    }

    /**
     * 刷新
     */
    private void doRefreshing() {
        isOnRefresh = true;
        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }
    }

    /**
     * 加载更多
     */
    private void doLoadingMore() {
        isOnLoadMore = true;
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onLoadMore();
        }
    }


    /**
     * 加载更多完成
     */
    public void loadMoreComplete() {
        if (!isOnLoadMore) {
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                smoothMove(false, false, mContentView.getMeasuredHeight() - getMeasuredHeight());
                isOnLoadMore = false;
                getFooterInterface().onComplete();
                getFooterInterface().onReset();

            }
        }, mDuration);

    }


    /**
     * 重置参数
     */
    private void resetParameter() {
        directionX = 0;
        directionY = 0;
        isUpOrDown = SCROLL_IDLE;
        lastY = 0;
        offsetSum = 0;
        scrollSum = 0;
    }

    /**
     * 是否可下来刷新
     *
     * @param refreshEnabled
     */
    public void setRefreshEnabled(boolean refreshEnabled) {
        this.mRefreshEnabled = refreshEnabled;
    }

    /**
     * 是否可以上拉加载
     *
     * @param loadMoreEnabled
     */
    public void setLoadMoreEnabled(boolean loadMoreEnabled) {
        this.mLoadMoreEnabled = loadMoreEnabled;
    }

    /**
     * 设置滑动摩擦系数
     *
     * @param mFriction
     */
    public void setFriction(float mFriction) {
        this.mFriction = mFriction;
    }

    /**
     * 能否刷新
     *
     * @return
     */
    private boolean canRefresh() {
        return !isOnLoadMore && !isOnRefresh && mRefreshEnabled && mHeaderView != null && !isChildCanPullDown();
    }

    /**
     * 能否加载更多
     *
     * @return
     */
    private boolean canLoadMore() {
        return !isOnRefresh && !isOnLoadMore && mLoadMoreEnabled && mFooterView != null && !isChildCanPullUp();
    }

    /**
     * 是否能下拉
     *
     * @return
     */
    protected boolean isChildCanPullDown() {
        if (mIsCooLayout) {
            return !isDependentOpen;
        }
        return isChildCanPullDown(mContentView);
    }


    /**
     * 子View是否还能下拉
     *
     * @param view
     * @return
     */
    private boolean isChildCanPullDown(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(view, -1) || view.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, -1);
        }
    }


    /**
     * 是否能上拉
     *
     * @return
     */
    protected boolean isChildCanPullUp() {
        if (mIsCooLayout) {
            return isDependentOpen || isChildCanPullUp(mScrollView);
        }
        return isChildCanPullUp(mContentView);
    }

    private boolean isChildCanPullUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(view, 1) || view.getScrollY() < 0;
            }
        } else {
            return ViewCompat.canScrollVertically(view, 1);
        }
    }


    private IRefreshView getHeaderInterface() {
        return (IRefreshView) mHeaderView;
    }

    private IRefreshView getFooterInterface() {
        return (IRefreshView) mFooterView;
    }

    /**
     * 设置刷新监听
     *
     * @param mOnRefreshListener
     */
    public void setOnRefreshListener(@NonNull OnRefreshListener mOnRefreshListener) {
        this.mOnRefreshListener = mOnRefreshListener;
    }


    /**
     * 设置加载更多监听
     *
     * @param mOnLoadMoreListener
     */
    public void setOnLoadMoreListener(@NonNull OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    /**
     * 设置下拉刷新时背景
     *
     * @param mRefreshBackgroundResource
     */
    public void setRefreshBackgroundResource(int mRefreshBackgroundResource) {
        this.mRefreshBackgroundResource = mRefreshBackgroundResource;
    }

    /**
     * 设置加载更多时背景
     *
     * @param mLoadMoreBackgroundResource
     */
    public void setLoadMoreBackgroundResource(int mLoadMoreBackgroundResource) {
        this.mLoadMoreBackgroundResource = mLoadMoreBackgroundResource;
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }

        super.computeScroll();
    }

    /**
     * refresh listener
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
