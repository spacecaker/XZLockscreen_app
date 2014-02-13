package com.spacecaker.xzlockscreen;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class XZLockscreenFCKU extends View {

	public static final int BLINDS_MAX_NUMBER = 1;
	public static final int FINGERS_MAX_NUMBER = 10;
	public static final int BLINDS_TIMEOUT_MILLIS = 2000;
	public static final int BLINDS_DISABLE_SPEED_MILLIS = 300;
	private static final boolean LOG_ON = false;
	private static final String LOG_TAG = null;

	private static final float CONFIG_MAX_ROTATIONX = 45f;
	private static final float CONFIG_MAX_ROTATIONY = 15f;
	private static final float CONFIG_MIN_SCALING = 0.97f;
	private static final float CONFIG_MAX_YOFFSET = 16;
	private static float mMaxAffectRadius;
	private static final int CONFIG_BLINDSTROKE_BASECOLOR = Color.DKGRAY;
	private static final int CONFIG_BLINDSTROKE_ALPHA = 175;
	private static final int CONFIG_BLINDSTROKE_BEVEL_ANGLE = 45;
	private static float mConfigStrokeWidth;
	
	private boolean mIsInBlindMode = false;
	
	private Bitmap mUndistortedBitmap ;         
	@SuppressWarnings("unused")
	private Canvas mUndistortedCanvas;
	private Drawable mBgDrawable ;
	private Paint mBlindPaint1, mBlindStrokePaint;
	private final Camera mCamera = new Camera();
	
	private int mRequiredRadius;
	private float mDensity;

	private Blind[] mBlinds;
	private int[] mFingersTrack;

	private OnActionListener mOnActionListener;

	public void setOnActionListener(OnActionListener onActionListener) {
		mOnActionListener = onActionListener;
	}
	 
	private void drawCustomStuff(Canvas screenCanvas) {
		if (LOG_ON) {
			Log.d(LOG_TAG,
					"drawCustomStuff  (doing the custom drawing of this ViewGroup)");
		}

		final boolean initBmpAndCanvas = (mIsInBlindMode && (!(mUndistortedBitmap != null && !mUndistortedBitmap
				.isRecycled())));

		if (!mIsInBlindMode || (mIsInBlindMode && initBmpAndCanvas)) {
			// Draw normally
			if (mIsInBlindMode && initBmpAndCanvas) {
				mUndistortedBitmap = Bitmap.createBitmap(getWidth(),
						getHeight(), Bitmap.Config.ARGB_8888);
				mUndistortedCanvas = new Canvas(mUndistortedBitmap);
			}

			Canvas canvasToDrawTo = mIsInBlindMode ? mUndistortedCanvas
					: screenCanvas;

			drawUndistorted(canvasToDrawTo);
		}
		if (mIsInBlindMode) {
			// Draw blinds version
			drawBlinds(screenCanvas);
		}
	}
	private void drawUndistorted(Canvas canvas) {
	Log. d( LOG_TAG, "Performing undistorted draw" );
	        if (mBgDrawable != null) {
	               mBgDrawable.draw(canvas);
	}
	super.dispatchDraw(canvas);
	}
	private void drawBlinds(Canvas canvas) {
	        // FIXME: Draw transformed, not undistorted!
	Log. d( LOG_TAG, "Performing draw in blinds mode (well, not really, but it will be!)" );
	        drawUndistorted(canvas);
	}

	private ArrayList<SpaceInfo> mBlindSet = null;
	private void setupBlinds (int blindHeight) {
	        if (blindHeight == 0) {
	                throw new IllegalArgumentException("blindHeight must be >0");
	        }
	        ArrayList<SpaceInfo> bi = new ArrayList<SpaceInfo>();
	        int accumulatedHeight = 0;
	        do {
	                bi.add( new SpaceInfo(0, accumulatedHeight, getWidth(),
	                                accumulatedHeight + blindHeight));
	                accumulatedHeight += blindHeight;
	        } while (accumulatedHeight < getHeight());
	        mBlindSet = bi;
	}
	
	protected void onSizeChanged1(int w, int h, int oldw, int oldh) {
	        super.onSizeChanged(w, h, oldw, oldh);
	 
	        setupBlinds((int) getResources().getDimension(R.dimen.blindHeight));
	        if (LOG_ON ) {
	                Log. d(LOG_TAG,
	                        "onLayout. Layout properties changed - blinds set rebuilt. New set contains "
	                        + mBlindSet.size() + " blinds");
	                }
	}

	public class SpaceInfo {
        private final Rect mBounds;
    	private float mScale = 1f;
    	private float mYoffset = 0f;
    	private boolean mDrawStroke = false;
        public SpaceInfo(int l, int t, int r, int b) {
                mBounds = new Rect(l, t, r, b);
        }
    	public int getHeight() {
    		return mBounds.height();
    	}

    	public int getWidth() {
    		return mBounds.width();
    	}

    	public int getLeft() {
    		return mBounds.left;
    	}

    	public int getRight() {
    		return mBounds.right;
    	}

    	public int getTop() {
    		return mBounds.top;
    	}

    	public int getBottom() {
    		return mBounds.bottom;
    	}

    	public void setRotations(float xRotation, float yRotation, float zRotation) {
    		mRotationX = xRotation;
    		mRotationY = yRotation;
    		mRotationZ = zRotation;
    	}

    	public float getRotationX() {
    		return mRotationX;
    	}

    	public float getRotationY() {
    		return mRotationY;
    	}

    	public float getRotationZ() {
    		return mRotationZ;
    	}

    	public void setScale(float s) {
    		mScale = s;
    	}

    	public float getScale() {
    		return mScale;
    	}

    	public void setYoffset(float offset) {
    		mYoffset = offset;
    	}

    	public float getYoffset() {
    		return mYoffset;
    	}

    	public void setDrawStroke(boolean d) {
    		mDrawStroke = d;
    	}

    	public boolean getDrawStroke() {
    		return mDrawStroke;
    	}
	}

	private void drawBlind(SpaceInfo info, Canvas canvas) {
		// Read params
		final int width = info.getWidth();
		final int height = info.getHeight();
		final int coordX = info.getLeft();
		final int coordY = info.getTop();
		final float xRotation = info.getRotationX();
		final float yRotation = info.getRotationY();
		final float zRotation = info.getRotationZ();
		final float scale = info.getScale();
		final float yOffset = info.getYoffset();
		final boolean drawBottomStroke = info.getDrawStroke();

		// Prepare Canvas and Camera
		canvas.save();
		mCamera.save();
		canvas.translate((coordX + (width / 2f)), (coordY + (height / 2f)));

		// Apply transformations
		mCamera.rotateY(yRotation);
		mCamera.rotateX(xRotation);
		canvas.scale(scale, scale, 0f, 0f);
		canvas.translate(0f, yOffset);

		Matrix cameraMatrix = new Matrix();
		mCamera.getMatrix(cameraMatrix);
		canvas.concat(cameraMatrix);

		mBlindPaint1.setColorFilter(calculateLight(xRotation));

		// Draw
		final Rect src = new Rect(coordX, coordY, (coordX + width),
				(coordY + height));
		final RectF dst = new RectF(-(width / 2f), -(height / 2f), width / 2f,
				height / 2f);
		canvas.drawBitmap(mUndistortedBitmap, src, dst, mBlindPaint1);
		if (drawBottomStroke) {
			mBlindStrokePaint.setColorFilter(calculateLight(xRotation
					+ CONFIG_BLINDSTROKE_BEVEL_ANGLE));
			canvas.drawLine(dst.left, (dst.bottom - mConfigStrokeWidth / 2f),
					dst.right, (dst.bottom - mConfigStrokeWidth / 2f),
					mBlindStrokePaint);
		}

		// Restore Canvas and Camera
		mCamera.restore();
		canvas.restore();

		if (LOG_ON) {
			Log.d(LOG_TAG, "Drew blind with size " + width + " by " + height
					+ " px with rotation (" + xRotation + ", " + yRotation
					+ ", " + zRotation + ") (x,y,z) at coordinates " + coordX
					+ ", " + coordY);
		}
	}
	
	private Paint mBlindPaint;

	public interface OnActionListener {
		public void onAction();

		public void onTouchDown(float x, float y);

		public void onTouchUp();
	};

	public XZLockscreenFCKU(Context context, OnActionListener onActionListener) {
		super(context);
		init(context);

		setOnActionListener(onActionListener);
	}

	public XZLockscreenFCKU(Context context) {
		super(context);
		init(context);
	}

	public XZLockscreenFCKU(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public XZLockscreenFCKU(Context context, AttributeSet attrs, int styles) {
		super(context, attrs, styles);
		init(context);
	}

	private void init(Context context) {
        mBlindPaint1 = new Paint();
        mBlindPaint1.setStyle(Paint.Style.FILL);
        mBlindPaint1.setAntiAlias(true);
        mBlindPaint1.setFilterBitmap(true);
		mBlinds = new Blind[BLINDS_MAX_NUMBER];
		mFingersTrack = new int[FINGERS_MAX_NUMBER];
		for (int i = 0; i < BLINDS_MAX_NUMBER; i++) {
			mBlinds[i] = new Blind();
			mFingersTrack[i] = -1;
		}

		mDensity = (float) getResources().getDisplayMetrics().density;
	}

	private float mRotationX, mRotationY , mRotationZ;
	 
	public void setRotations(float xRotation, float yRotation, float zRotation) {
	        mRotationX = xRotation;
	        mRotationY = yRotation;
	        mRotationZ = zRotation;
	}
	 
	public float getRotationX() {
	        return mRotationX ;
	}
	 
	public float getRotationY() {
	        return mRotationY ;
	}
	 
	public float getRotationZ() {
	        return mRotationZ ;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		// Calculate the size of required ring radius
		mRequiredRadius = (int) (Math.sqrt(w * w + h * h) / 3);
	}

	@Override
	public void onDraw(Canvas canvas) {
		for (int i = 0; i < mBlinds.length; i++) {
			final Blind blind = mBlinds[i];
			if (!blind.inUse())
				continue;

			blind.draw(canvas);
		}
	}

	private static final int AMBIENT_LIGHT = 55;

	/** Diffuse light intensity */
	private static final int DIFFUSE_LIGHT = 255;

	/** Specular light intensity */
	private static final float SPECULAR_LIGHT = 70;

	/** Shininess constant */
	private static final float SHININESS = 255;

	/** The max intensity of the light */
	private static final int MAX_INTENSITY = 0xFF;

	/** Light source angular offset */
	private static final float LIGHT_SOURCE_ANGLE = 38f;

	private LightingColorFilter calculateLight(float rotation) {
		rotation -= LIGHT_SOURCE_ANGLE;
		final double cosRotation = Math.cos(Math.PI * rotation / 180);
		int intensity = AMBIENT_LIGHT + (int) (DIFFUSE_LIGHT * cosRotation);
		int highlightIntensity = (int) (SPECULAR_LIGHT * Math.pow(cosRotation,
				SHININESS));

		if (intensity > MAX_INTENSITY) {
			intensity = MAX_INTENSITY;
		}
		if (highlightIntensity > MAX_INTENSITY) {
			highlightIntensity = MAX_INTENSITY;
		}

		final int light = Color.rgb(intensity, intensity, intensity);
		final int highlight = Color.rgb(highlightIntensity, highlightIntensity,
				highlightIntensity);

		return new LightingColorFilter(light, highlight);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int i = 0;
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mIsInBlindMode = true;
		case MotionEvent.ACTION_POINTER_DOWN:
			mIsInBlindMode = true;
			for (; i < mBlinds.length; i++)
				if (!mBlinds[i].inUse())
					break;
			if (i == mBlinds.length)
				return false;

			int index = event.getActionIndex();
			mFingersTrack[index] = i;
			mBlinds[i].reInit(event.getX(index), event.getY(index));
			break;
		case MotionEvent.ACTION_MOVE:
			mIsInBlindMode = true;			
			int fingers = event.getPointerCount();
			if (fingers > FINGERS_MAX_NUMBER)
				fingers = FINGERS_MAX_NUMBER;
			for (; i < fingers; i++) {
				int blindId = mFingersTrack[i];
				if (blindId < 0)
					continue;
				mBlinds[blindId].setTouchMoveCoords(event.getX(i), event.getY(i));
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			index = event.getActionIndex();
			disableBlind(mFingersTrack[index]);
			mFingersTrack[index] = -1;
			mIsInBlindMode = true;
			break;
		case MotionEvent.ACTION_UP:
			mIsInBlindMode = false;
		case MotionEvent.ACTION_CANCEL:
			for (; i < mBlinds.length; i++) {
				if (!mBlinds[i].isInteractive())
					continue;
				disableBlind(i);
			}
			for (i = 0; i < mFingersTrack.length; i++)
				mFingersTrack[i] = -1;
			break;
		}

		invalidate();
		return true;
	}

	private void disableBlind(int i) {
		mBlinds[i].disableBlind();
	}

	private synchronized void calculateBlindRotations(float xPos, float yPos) {

		float currentBlindPivotY;
		float normalizedVerticalDistanceFromTouch;

		for (SpaceInfo currentBlind : mBlindSet) {
			currentBlindPivotY = currentBlind.getTop()
					+ (float) currentBlind.getHeight() / 2f;

			normalizedVerticalDistanceFromTouch = Math
					.abs((yPos - currentBlindPivotY) / mMaxAffectRadius);

			float xRotation = 0;
			float yRotation = 0;
			float scaling = 1f;
			float yOffset = 0f;
			boolean drawStroke = false;

			if (normalizedVerticalDistanceFromTouch <= 1f) {

				final double normalizedRotationX = Math
						.max(0d,
								(-Math.pow(
										((normalizedVerticalDistanceFromTouch - 0.55f) * 2f),
										2) + 1));

				if ((currentBlindPivotY < yPos)) {
					xRotation = (float) -(CONFIG_MAX_ROTATIONX * normalizedRotationX);
				} else {
					xRotation = (float) (CONFIG_MAX_ROTATIONX * normalizedRotationX);
				}

				final float normalizedHorizontalDistanceFromPivot = ((xPos / getWidth()) - 0.5f) / 0.5f;
				final float linearDeclineFactor = 1 - normalizedVerticalDistanceFromTouch;
				yRotation = CONFIG_MAX_ROTATIONY
						* normalizedHorizontalDistanceFromPivot
						* linearDeclineFactor;

				scaling = 1f
						- (1f - normalizedVerticalDistanceFromTouch
								* normalizedVerticalDistanceFromTouch)
						* (1f - CONFIG_MIN_SCALING);

				yOffset = ((1f - normalizedVerticalDistanceFromTouch
						* normalizedVerticalDistanceFromTouch))
						* CONFIG_MAX_YOFFSET;

				drawStroke = true;

			}
			currentBlind.setRotations(xRotation, yRotation, 0f);
			currentBlind.setScale(scaling);
			currentBlind.setYoffset(yOffset);
			currentBlind.setDrawStroke(drawStroke);
		}

	}
	
	private class Blind {

		private double mRadius;
		private int mAlpha;
		private boolean mInteractive;
		private boolean mUsing;
		private long mInitTime;

		private float mTouchMoveX;
		private float mTouchMoveY;
		private float mTouchDownX;
		private float mTouchDownY;

		private final Paint mPaint;

		public Blind() {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
		}

		public void draw(Canvas canvas) {
			if (mRadius < 0 || !mUsing)
				return;

			final double deltaRadius = Math.sqrt(8l / (double) mRequiredRadius)
					* mRadius;
			final int deltaAlpha = (int) Math
					.round(270 * deltaRadius / mRadius);

			int alpha = (int) (mAlpha * mRadius / mRequiredRadius - deltaAlpha);
			float radius = (float) mRadius;
			while (radius > 0 && alpha > 0) {				

				radius -= deltaRadius;
				alpha -= deltaAlpha;
			}
		}

		public void disableBlind() {
			if (mOnActionListener != null)
				mOnActionListener.onTouchUp();

			// We're shouldn't see touch point
			mInteractive = false;

			final double radius = mRadius;
			final long duration = Math.round(BLINDS_DISABLE_SPEED_MILLIS
					* mRadius / mRequiredRadius);
			final long endTime = SystemClock.uptimeMillis() + duration;

			final Handler handler = new Handler();
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					if (mInteractive)
						return;

					long deltaTime = endTime - SystemClock.uptimeMillis();
					if (deltaTime > 0) {
						float progress = 1f - (float) (duration - deltaTime)
								/ duration;
						mRadius = radius * progress;
						mAlpha = (int) (0f * progress);

						handler.postDelayed(this, 10);
					} else {
						mUsing = false;
					}
					postInvalidate();
				}
			};
			handler.post(runnable);
		}

		public void setTouchMoveCoords(float x, float y) {
			if (!mInteractive)
				return;

			if (System.currentTimeMillis() - mInitTime > BLINDS_TIMEOUT_MILLIS) {
				disableBlind();
				return;
			}

			float a = mTouchDownX - x;
			float b = mTouchDownY - y;
			double radius = Math.sqrt(a * a + b * b);
			if (radius > mRequiredRadius) {
				// Send message to level-up-class
				if (mOnActionListener != null)
					mOnActionListener.onAction();

				disableBlind();
			} else {
				mRadius = radius;

				mTouchMoveX = x;
				mTouchMoveY = y;
			}
		}

		public boolean inUse() {
			return mUsing;
		}

		public boolean isInteractive() {
			return mInteractive;
		}

		public void reInit(float x, float y) {
			mAlpha = 0;
			mInteractive = true;
			mRadius = -1;
			mUsing = true;
			mInitTime = System.currentTimeMillis();

			// Remember the center of circle
			mTouchDownX = x;
			mTouchDownY = y;

			// Say activity that we began
			if (mOnActionListener != null)
				mOnActionListener.onTouchDown(x, y);
		};
	}

}
