package com.sky.drovik.player.bitmapfun;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

public class ScrollLayout extends FrameLayout {
	private static final int INVALID_SCREEN = -999;
	private static final int SNAP_VELOCITY = 600;
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	
	private VelocityTracker mVelocityTracker;
	
	private static final String TAG = "ScrollLayout";
	
	private Scroller mScroller;

	private OnViewChangeListener mOnViewChangeListener;

	private int mCurScreen;
	private int mNextScreen = INVALID_SCREEN;
	
	private int mDefaultScreen = 0;
	
	private int mTouchSlop;
	
	private float mLastMotionX;
	
	private float mLastMotionY;
	
	private static final int INVALID_POINTER = -1;
	private int mActivePointerId = INVALID_POINTER;
	
	private boolean isScroll = true;

	private int mTouchState = TOUCH_STATE_REST;
	
	private static final float BASELINE_FLING_VELOCITY = 2500.f;
	private static final float FLING_VELOCITY_INFLUENCE = 0.4f;

	private static final float NANOTIME_DIV = 1000000000.0f;
	private static final float SMOOTHING_SPEED = 0.75f;
	private static final float SMOOTHING_CONSTANT = (float) (0.016 / Math.log(SMOOTHING_SPEED));
	private float mSmoothingTime;
	private float mTouchX;
	private boolean mFirstLayout = true;
	
	private int[] arr = null;
	
	private int preSelectIndex = 0;
	
	private ImageView[] imageArr = null;
	
	private int imageAdapterIndex = 0;
	
	public void setIsScroll(boolean b) {
		this.isScroll = b;
	}

	public ScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	
	public void init(Context context) {
		mScroller = new Scroller(context);
		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	public void setAdapter(int[] arr) {
		this.arr = arr;
	}
	
	public void initImageArr(ImageView[] imageArr) {
		this.imageArr = imageArr;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			if (childView.getVisibility() != View.GONE) {
				final int childWidth = childView.getMeasuredWidth();
				childView.layout(childLeft, 0, childLeft + childWidth,
						childView.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//Log.e(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!");
		}
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
		}

		// The children are given the same width and height as the scrollLayout
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		// Log.e(TAG, "moving to screen "+mCurScreen);
		if (mFirstLayout) {
			scrollTo(mCurScreen * width, 0);
			mFirstLayout = false;
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING
				&& mNextScreen == INVALID_SCREEN;
		if (fastDraw) {
			drawChild(canvas, getChildAt(mCurScreen), getDrawingTime());
			if(preSelectIndex != mCurScreen) {
				//TODO
				System.out.println(preSelectIndex + " mCurScreen=" + mCurScreen);
				preSelectIndex = mCurScreen;
			}
		} else {
			long drawingTime = getDrawingTime();
			int width = getWidth();
			float scrollPos = (float) getScrollX() / width;
			boolean endlessScrolling = true;

			int leftScreen;
			int rightScreen;
			boolean isScrollToRight = false;
			int childCount = getChildCount();
			if (scrollPos < 0 && endlessScrolling) {
				leftScreen = childCount - 1;
				rightScreen = 0;
			} else {
				leftScreen = Math.min((int) scrollPos, childCount - 1);
				rightScreen = leftScreen + 1;
				if (endlessScrolling) {
					rightScreen = rightScreen % childCount;
					isScrollToRight = true;
				}
			}
			if (isScreenNoValid(leftScreen)) {
				if (rightScreen == 0 && !isScrollToRight) {
					int offset = childCount * width;
					canvas.translate(-offset, 0);
					drawChild(canvas, getChildAt(leftScreen), drawingTime);
					canvas.translate(+offset, 0);
				} else {
					drawChild(canvas, getChildAt(leftScreen), drawingTime);
				}
			}
			if (scrollPos != leftScreen && isScreenNoValid(rightScreen)) {
				if (endlessScrolling && rightScreen == 0 && isScrollToRight) {
					int offset = childCount * width;
					canvas.translate(+offset, 0);
					drawChild(canvas, getChildAt(rightScreen), drawingTime);
					canvas.translate(-offset, 0);
				} else {
					drawChild(canvas, getChildAt(rightScreen), drawingTime);
				}
			}
		}
	}

	private boolean isScreenNoValid(int screen) {
		return screen >= 0 && screen < getChildCount();
	}
	
	/*public void snapToScreen(int whichScreen) {
		// �Ƿ�ɻ���
		if (!isScroll) {
			this.setToScreen(whichScreen);
			return;
		}
		scrollToScreen(whichScreen);
	}

	void snapToScreen(int whichScreen) {
		snapToScreen(whichScreen, 0, false);
	}*/
	
	private void snapToScreen(int whichScreen, int velocity, boolean settle) {
		whichScreen = Math.max((true ? -1 : 0), Math.min(whichScreen, getChildCount() - (true ? 0 : 1)));
//TODO
		mNextScreen = whichScreen;
		if(mNextScreen>mCurScreen) {
			imageAdapterIndex= nextAdapterPos(imageAdapterIndex);
			System.out.println("next = " + imageAdapterIndex);
		}else if(mNextScreen<mCurScreen) {
			imageAdapterIndex= preAdapterPos(imageAdapterIndex);
			System.out.println("pre = " + imageAdapterIndex);
		}
System.out.println("mNextScreen=" + mNextScreen);
		View focusedChild = getFocusedChild();
		if (focusedChild != null && whichScreen != mCurScreen
				&& focusedChild == getChildAt(mCurScreen)) {
			focusedChild.clearFocus();
		}
		final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurScreen));
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		int duration = (screenDelta + 1) * 100;

		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		velocity = Math.abs(velocity);
		if (velocity > 0) {
			duration += (duration / (velocity / BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
		} else {
			duration += 100;
		}
		awakenScrollBars(duration);
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();
	}
	
	/*public void scrollToScreen(int whichScreen) {		
		// get the valid layout page
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (getScrollX() != (whichScreen * getWidth())) {
			final int delta = whichScreen * getWidth() - getScrollX();
			mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 1);//��������ʱ�� �Ժ���Ϊ��λ
			mCurScreen = whichScreen;
			invalidate(); // Redraw the layout
			if (mOnViewChangeListener != null) {
            	mOnViewChangeListener.OnViewChange(mCurScreen);
            }
		}
	}

	public void setToScreen(int whichScreen) {
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mCurScreen = whichScreen;
		scrollTo(whichScreen * getWidth(), 0);
        if (mOnViewChangeListener != null) {
        	mOnViewChangeListener.OnViewChange(mCurScreen);
        }
	}*/

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		acquireVelocityTrackerAndAddMovement(ev);
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);

			final int touchSlop = mTouchSlop;
			boolean xMoved = xDiff > touchSlop;
			boolean yMoved = yDiff > touchSlop;

			if (xMoved || yMoved) {

				if (xMoved) {
					// Scroll if the user moved far enough along the X axis
					mTouchState = TOUCH_STATE_SCROLLING;
					mLastMotionX = x;
					mTouchX = getScrollX();
					mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
				}
			}
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			// Remember location of down touch
			mLastMotionX = x;
			mLastMotionY = y;
			mActivePointerId = ev.getPointerId(0);
			/*
			 * If being flinged and user touches the screen, initiate drag;
			 * otherwise don't. mScroller.isFinished should be false when being
			 * flinged.
			 */
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST	: TOUCH_STATE_SCROLLING;
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// Release the drag
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
			releaseVelocityTracker();
			break;

		case MotionEvent.ACTION_POINTER_UP:
			break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			// Remember where the motion event started
			mLastMotionX = ev.getX();
			mActivePointerId = ev.getPointerId(0);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int pointerIndex = ev.findPointerIndex(mActivePointerId);
				final float x = ev.getX(pointerIndex);
				final float deltaX = mLastMotionX - x;
				mLastMotionX = x;
				if (deltaX < 0) { //
					if (mTouchX > 0) {
						mTouchX += Math.max(-mTouchX, deltaX);
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						invalidate();
					} else if (true && mTouchX > -getWidth()) {
						mTouchX += deltaX;
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						invalidate();
					}
				} else if (deltaX > 0) {
					final float availableToScroll = getChildAt(	getChildCount() - 1).getRight()
							- mTouchX - (true ? 0 : getWidth());
					if (availableToScroll > 0) {
						mTouchX += Math.min(availableToScroll, deltaX);
						mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						invalidate();
					}
				} else {
					awakenScrollBars();
				}

			}
			break;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				final int velocityX = (int) velocityTracker.getXVelocity();

				final int screenWidth = getWidth();
				// final int whichScreen = (mScrollX + (screenWidth / 2)) /
				// screenWidth;
				final int whichScreen = (int) Math.floor((getScrollX() + (screenWidth / 2.0))
								/ screenWidth);
				final float scrolledPos = (float) getScrollX() / screenWidth;

				if (velocityX > SNAP_VELOCITY && mCurScreen > (true ? -1 : 0)) {
					// Fling hard enough to move left.
					// Don't fling across more than one screen at a time.
					final int bound = scrolledPos < whichScreen ? mCurScreen - 1 : mCurScreen;
					snapToScreen(Math.min(whichScreen, bound), velocityX, true);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurScreen < getChildCount() - (true ? 0 : 1)) {
					// Fling hard enough to move right
					// Don't fling across more than one screen at a time.
					final int bound = scrolledPos > whichScreen ? mCurScreen + 1 : mCurScreen;
					snapToScreen(Math.max(whichScreen, bound), velocityX, true);
				} else {
					snapToScreen(whichScreen, 0, true);
				}
			}
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
			releaseVelocityTracker();
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final int screenWidth = getWidth();
				final int whichScreen = (getScrollX() + (screenWidth / 2))
						/ screenWidth;
				snapToScreen(whichScreen, 0, true);
			}
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER;
			releaseVelocityTracker();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			break;
		}
		return true;
	}


	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			mTouchX = mScroller.getCurrX();
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			if (mNextScreen == -1 && true) {
				mCurScreen = getChildCount() - 1;
				scrollTo(mCurScreen * getWidth(), getScrollY());
			} else if (mNextScreen == getChildCount() && true) {
				mCurScreen = 0;
				scrollTo(0, getScrollY());
			} else {
				mCurScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
			}
			mNextScreen = INVALID_SCREEN;
		} else if (mTouchState == TOUCH_STATE_SCROLLING) {
			final float now = System.nanoTime() / NANOTIME_DIV;
			final float e = (float) Math.exp((now - mSmoothingTime)
					/ SMOOTHING_CONSTANT);
			final float dx = mTouchX - getScrollX();
			// mScrollX += dx * e;
			final int scrolltoX = getScrollX() + (int) (dx * e);
			super.scrollTo(scrolltoX, getScrollY());
		}
	}
	
	/*public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		snapToScreen(destScreen);
	}*/

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	private int nextAdapterPos(int relativePos) {
		int nextPos = relativePos + 1;
		if (nextPos > getLastAdpaterIndex()) {
			nextPos = getFirstAdapterIndex();
		}
		return nextPos;
	}
	
	private int preAdapterPos(int relativePos) {
		int nextPos = relativePos - 1;
		if (nextPos < getFirstAdapterIndex()) {
			nextPos = getLastAdpaterIndex();
		}
		return nextPos;
	}
	
	private int getFirstAdapterIndex() {
		return 0;
	}
	
	private int getLastAdpaterIndex() {
		return getAdapterCount() == 0 ? 0 : getAdapterCount() - 1;
	}
	
	private int getAdapterCount() {
		return arr == null? 0 : arr.length;
	}
	
	/**
	 * ������Ļ�л�������
	 * 
	 * @param listener
	 */
	public void SetOnViewChangeListener(OnViewChangeListener listener) {
		mOnViewChangeListener = listener;
	}

	/**
	 * ��Ļ�л�������
	 * 
	 * @author liux
	 */
	public interface OnViewChangeListener {
		public void OnViewChange(int view);
	}
}