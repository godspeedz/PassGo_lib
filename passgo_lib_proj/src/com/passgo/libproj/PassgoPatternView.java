/*
 * Copyright (C) 2015 PassGo Technology, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.passgo.libproj;


import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.passgo.libproj.PassgoGlobalData;
import com.passgo.libproj.R;



/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 *
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 */
public class PassgoPatternView extends View {
	private boolean mHold=false;
	private String mPassword = "";
	private int coord[];
	private int last_x;
	private int last_y;
	private passwd passwd_tmp = new passwd("");
	private static final String TAG = "PassgoPatternView";
    // Aspect to use when rendering this view
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will be minimum of (w,h)
    
    private static int mGridLineFactor=1;

    //custom configuration
    private boolean mIsOfficialPattern = true;
    private int mPatternRow = 5;
    private int mPatternCol = 5;
    private boolean mIsVibrate = true;
    private boolean hidePassGoLineDot = false;
    private int mPatternRetryCount = 0;
    
    private int mPatternLineColor = PassgoGlobalData.KEY_COLOR_WHITE;
    private int mPatternDotColor = PassgoGlobalData.KEY_COLOR_WHITE;
    
    private int mPassGoLineThickness = 1;
    private int mPassGoLineThickness_factor = 1;
    private int mPatternOutlineColor = PassgoGlobalData.KEY_COLOR_WHITE;    
    
    private static final boolean PROFILE_DRAWING = false;
    private boolean mDrawingProfilingStarted = false;

    private Paint mPathPaint = new Paint();
    
    private Paint Grid_line_paint = new Paint();
    private Paint Go_line_paint = new Paint();
    private Paint Go_dot_paint = new Paint();

    // TODO: make this common with PhoneWindow
    static final int STATUS_BAR_HEIGHT = 25;

    private OnPatternListener mOnPatternListener;
    private ArrayList<Cell> mPattern = null;
    private Cell[][] mCells = null;
    
    private PassgoPatternUtils mLockPatternUtils = null;

    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private boolean[][] mPatternDrawLookup = null;

    /**
     * the in progress point:
     * - during interaction: where the user's finger is
     * - during animation: the current tip of the animating line
     */

    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mEnableHapticFeedback = true;

    private float mSquareWidth;
    private float mSquareHeight;

    private Bitmap mBitmapBtnDefault;
    private Bitmap mBitmapBtnTouched;
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleGreen;

    private int mBitmapWidth;
    private int mBitmapHeight;

    private int mAspect;
    
    private String ScreenLockerActivity = "ScreenLockerActivity";
    private String ApplicationLockActivity = "ApplicationLockActivity";


    /**
     * How to display the current pattern.
     */
    public enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * Animate the pattern (for demo, and help).
         */
        Animate,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong
    }

    /**
     * The call back interface for detecting patterns entered by the user.
     */
    public static interface OnPatternListener {

        /**
         * A new pattern has begun.
         */
        void onPatternStart();

        /**
         * The pattern was cleared.
         */
        void onPatternCleared();

        /**
         * The user extended the pattern currently being drawn by one cell.
         * @param pattern The pattern with newly added cell.
         */
        void onPatternCellAdded(List<Cell> pattern);

        /**
         * A pattern was detected from the user.
         * @param pattern The pattern.
         */
        void onPatternDetected(List<Cell> pattern);

		void onPatternDetected(String mPassword);
    }

    public PassgoPatternView(Context context) {
        this(context, null);
    }

    public PassgoPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        getAndSetCustomConfig(context);
        
        //init variabal according to row and col
        mPattern = new ArrayList<Cell>(mPatternRow * mPatternCol);
        mPatternDrawLookup = new boolean[mPatternRow][mPatternCol];
        mCells = new Cell[mPatternRow][mPatternCol];
        for (int i = 0; i < mPatternRow; i++) {
            for (int j = 0; j < mPatternCol; j++) {
            	mCells[i][j] = new Cell(i, j);
            }
        }
        
        mLockPatternUtils = new PassgoPatternUtils(context, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);

        final String aspect = a.getString(R.styleable.LockPatternView_aspect);

        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        setClickable(true);

        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);

        // lot's of bitmaps!
        mBitmapBtnDefault = getBitmapFor(R.drawable.btn_code_lock_default_holo);
        mBitmapBtnTouched = getBitmapFor(R.drawable.btn_code_lock_touched_holo);
        mBitmapCircleDefault = getBitmapFor(R.drawable.btn_code_lock_default_holo);
        
        switch (mPatternOutlineColor) {
	        case PassgoGlobalData.KEY_COLOR_BLACK:
	        	mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#000000"));
				break;
	        case PassgoGlobalData.KEY_COLOR_GRAY:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#808080"));
				break;
	        case PassgoGlobalData.KEY_COLOR_NAVY:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#000080"));
				break;
	        case PassgoGlobalData.KEY_COLOR_RED:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#FF0000"));
				break;
	        case PassgoGlobalData.KEY_COLOR_MAROON:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#800000"));
				break;
	        case PassgoGlobalData.KEY_COLOR_FUCHSIA:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#FF00FF"));
				break;
			
			case PassgoGlobalData.KEY_COLOR_ORANGE:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#66FFA500"));
				break;
			case PassgoGlobalData.KEY_COLOR_YELLOW:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.YELLOW);
				break;
			case PassgoGlobalData.KEY_COLOR_GREEN:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.GREEN);
				break;
			case PassgoGlobalData.KEY_COLOR_DARK_GREEN:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#66006400"));
				break;
			case PassgoGlobalData.KEY_COLOR_SKY_BLUE:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.BLUE);
				break;
			case PassgoGlobalData.KEY_COLOR_BLUE:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.BLUE);
				break;
			case PassgoGlobalData.KEY_COLOR_DARK_BLUE:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#6600008B"));
				break;
			case PassgoGlobalData.KEY_COLOR_PURPLE:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#800080"));
				break;
			case PassgoGlobalData.KEY_COLOR_PINK:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.parseColor("#66FFC0CB"));
				break;
			case PassgoGlobalData.KEY_COLOR_WHITE:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.WHITE);
				break;
			default:
				mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_ics);
				Grid_line_paint.setColor(Color.WHITE);
				break;
		}
        

        // bitmaps have the size of the largest bitmap in this group
        final Bitmap bitmaps[] = { mBitmapBtnDefault, mBitmapBtnTouched, mBitmapCircleDefault,
                mBitmapCircleGreen }; //mBitmapCircleGreen, mBitmapCircleRed };

        for (Bitmap bitmap : bitmaps) {
            mBitmapWidth = Math.max(mBitmapWidth, bitmap.getWidth());
            mBitmapHeight = Math.max(mBitmapHeight, bitmap.getHeight());
        }

    }
    
    private void getAndSetCustomConfig(Context context) {
		// TODO Auto-generated method stub
    	//row and col
    	
    	
    	String className = context.getClass().toString();
  	
        mIsOfficialPattern = PassgoGlobalData.getDataBool(context, PassgoGlobalData.KEY_IS_OFFICIAL_PATTERN, true);
        if (mIsOfficialPattern || className.toLowerCase().contains(ScreenLockerActivity.toLowerCase())
        		|| className.toLowerCase().contains(ApplicationLockActivity.toLowerCase())) {
        	mPatternRow = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_OFFICIAL_PATTERN_NUM_ROW, 3);
        	mPatternCol = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_OFFICIAL_PATTERN_NUM_COL, 3);
        }
       else {
       	mPatternRow = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_ROW, 3);
       	mPatternCol = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_COL, 3);
        }
        
        //Vibrate
        mIsVibrate = PassgoGlobalData.getDataBool(context, PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_VIBRATE, true);
        setTactileFeedbackEnabled(mIsVibrate);
        

         //Pattern Retry Setting
        mPatternRetryCount = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_RETRY_COUNT, 0);
        
        //PassGo Line and Dot Color Setting
        hidePassGoLineDot = PassgoGlobalData.getDataBool(context, PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_HIDE_LINE, false);
        if(hidePassGoLineDot && PassgoGlobalData.Changing_PassGo==false)
        {
            mPatternLineColor = Color.TRANSPARENT;
            mPatternDotColor = Color.TRANSPARENT;
            mPathPaint.setColor(Color.TRANSPARENT);

        }
        else{

        	mPatternLineColor = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_LOCK_SCREEN_PASSGO_LINE_COLOR, Color.RED);
        	mPatternDotColor = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_LOCK_SCREEN_PASSGO_DOT_COLOR, Color.RED);
        	mPathPaint.setColor(mPatternLineColor);
        }
        
 
        //PassGo's layout paint style
        Go_dot_paint = new Paint();
        Go_dot_paint.setColor(mPatternDotColor);
        Go_dot_paint.setStrokeJoin(Paint.Join.ROUND);   
        Go_dot_paint.setStrokeCap(Paint.Cap.ROUND);   
        Go_dot_paint.setStrokeWidth(mPassGoLineThickness); 
        
        Go_line_paint = new Paint();
        Go_line_paint.setColor(mPatternLineColor);
        Go_line_paint.setStrokeJoin(Paint.Join.ROUND);   
        Go_line_paint.setStrokeCap(Paint.Cap.ROUND);   
        Go_line_paint.setStrokeWidth(mPassGoLineThickness); 
        

        //Grid Outline Color Setting
        mPatternOutlineColor = PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_OUTLINE_COLOR, Color.WHITE);
        
        //Grid's layout paint style
        Grid_line_paint = new Paint();
        //Go_line_paint.setColor(Color.YELLOW);   //adding two more digits in front of this "number" will represent the transparency, ex: #66424242. (66 in this example)

        Grid_line_paint.setStrokeJoin(Paint.Join.ROUND);   
        Grid_line_paint.setStrokeCap(Paint.Cap.ROUND); 
        mGridLineFactor=PassgoGlobalData.getDataInt(context, PassgoGlobalData.KEY_LOCK_SCREEN_GRID_LINE_THICKNESS, 1);
        Grid_line_paint.setStrokeWidth(3*mGridLineFactor); 
        if (Math.max(mPatternCol, mPatternRow)>8){
        	Grid_line_paint.setStrokeWidth(2*mGridLineFactor); 
        }
        
	}

	/**
     * get current pattern cells
     * @param row
     * @param column
     * @return
     */
    public Cell getCell(int row, int column) {
        return mCells[row][column];
    }
    
    /**
     * @return how many cells in row
     */
    public int getRow() {
        return mPatternRow;
    }

    /**
     * @return how many cells in column
     */
    public int getColumn() {
        return mPatternCol;
    }
    
    public boolean isOfficialPattern() {
    	return mIsOfficialPattern;
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    /**
     * @return Whether the view is in stealth mode.
     */
    public boolean isInStealthMode() {
        return mInStealthMode;
    }

    /**
     * @return Whether the view has tactile feedback enabled.
     */
    public boolean isTactileFeedbackEnabled() {
        return mEnableHapticFeedback;
    }

    /**
     * Set whether the view is in stealth mode.  If true, there will be no
     * visible feedback as the user enters the pattern.
     *
     * @param inStealthMode Whether in stealth mode.
     */
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }

    /**
     * Set whether the view will use tactile feedback.  If true, there will be
     * tactile feedback as the user enters the pattern.
     *
     * @param tactileFeedbackEnabled Whether tactile feedback is enabled
     */
    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mEnableHapticFeedback = tactileFeedbackEnabled;
    }

    /**
     * Set the call back for pattern detection.
     * @param onPatternListener The call back.
     */
    public void setOnPatternListener(
            OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }

    /**
     * Set the pattern explicitely (rather than waiting for the user to input
     * a pattern).
     * @param displayMode How to display the pattern.
     * @param pattern The pattern.
     */
    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }

        setDisplayMode(displayMode);
    }

    /**
     * Set the display mode of the current pattern.  This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     * @param displayMode The display mode.
     */
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to "
                        + "animate if you want to set the display mode to animate");
            }

            
        } 
        invalidate();
    }


    private void notifyPatternDetected() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternDetected(mPassword);
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
        sendAccessEvent(R.string.lockscreen_access_pattern_detected);
    }

    private void notifyPatternCleared() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
        sendAccessEvent(R.string.lockscreen_access_pattern_cleared);
    }

    /**
     * Clear the pattern.
     */
    public void clearPattern() {
        resetPattern();
    }

    /**
     * Reset all pattern state.
     */
    private void resetPattern() {
        mPassword="";
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }
    
    public void clearLastStroke() {
        clearlaststroke();
    }

    /**
     * Reset all pattern state.
     */
    private void clearlaststroke() {
    	String last_4_number;
    	if (mPassword.length()==8){
    		mPassword="";
    	}else {
	    	mPassword=mPassword.substring(0,mPassword.length()-4);
	    	while (mPassword.length()>0 ) {
	    		last_4_number = mPassword.substring(mPassword.length()-4, mPassword.length());
	    		if (last_4_number.equals("0000")) {
//	    			Log.i(TAG,"breaking mPassword is now"+mPassword);
	    			break;
	    		} else {
	    			mPassword=mPassword.substring(0,mPassword.length()-4);
	    		}
				
			}
    	}
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }
    
    

    /**
     * Disable input (for instance when displaying a message that will
     * timeout so user doesn't get view into messy state).
     */
    public void disableInput() {
        mInputEnabled = false;
    }

    /**
     * Enable input.
     */
    public void enableInput() {
        mInputEnabled = true;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();

        mSquareWidth = width / mPatternCol;
        mSquareHeight = height / mPatternRow;
    }

    private int resolveMeasured(int measureSpec, int desired)
    {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        result = Math.min(specSize, desired);
        return result;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return mPatternCol * mBitmapWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return mPatternRow * mBitmapHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();

        
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        if (mPatternRow==2 || mPatternCol==2 ){
        	viewWidth=(int)(minimumWidth*1.4);
        	viewHeight=(int)(minimumHeight*1.4);
        } else if (mPatternRow==3 || mPatternCol==3){
        	viewWidth=(int)(minimumWidth*1.1);
        	viewHeight=(int)(minimumHeight*1.1);
        } 


        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_WIDTH:
                viewHeight = Math.min(viewWidth, viewHeight);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(viewWidth, viewHeight);
                break;
        }
//        Log.v(TAG, "LockPatternView dimensions: " + viewWidth + "x" + viewHeight);
        setMeasuredDimension(viewWidth, viewHeight);
    }

 
    
	//produce xy coordinates for any point touched (left top is 11, left bottom is n1, right top is 1n, right bottom is nn)
	public int [] stoneCoord(float x, float y) {
		
		float x_r; // x deduct left padding
		float y_r; //y deduct top padding
		int coord_x = 0;
		int coord_y = 0 ;
		int coord_x_tmp;
		int coord_y_tmp;
		float distance_x; // distance on X-axis to the nearest intersection
		float distance_y; // distance on Y-axis to the nearest intersection
		float distance; // the square of the distance to the nearest intersection
		float radius; // the square of the radius of the sensitive area
		
		//if xy is not in the padding area
		if ( !mHold && x<(getPaddingLeft()+mSquareWidth*mPatternCol) && x>getPaddingLeft() && y<(getPaddingTop()+mSquareHeight*mPatternRow) && y>getPaddingTop()) {
			x_r = x - getPaddingLeft();
			y_r = y - getPaddingTop();
			coord_x_tmp = (int)(x_r / mSquareWidth);
			coord_y_tmp = (int)(y_r / mSquareHeight);
			distance_x = Math.abs(x_r % mSquareWidth - mSquareWidth/2) ;
			distance_y = Math.abs(y_r % mSquareHeight - mSquareHeight/2);
			distance = (float)(Math.pow(distance_x,2) + Math.pow( distance_y,2));
			radius = (float)(Math.pow(Math.min(mSquareWidth*0.4, mSquareHeight*0.4),2));
			if ( distance < radius) {
				coord_x = coord_x_tmp+1;
				coord_y = coord_y_tmp+1;
			} 
			
		};
		int [] r = { coord_x, coord_y,};
	//	Log.i(TAG,Integer.toString(coord_x)+Integer.toString(coord_y));
		return r;
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                return true;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                return true;
            case MotionEvent.ACTION_CANCEL:
                resetPattern();
//                mPatternInProgress = false;
                notifyPatternCleared();
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }

    private void handleActionMove(MotionEvent event) {
    	  	
        final float x = event.getX();
        final float y = event.getY();
        
        coord = stoneCoord(x,y);
        
        //if the xy coordinates of the point is not zero, and is not as same as the last xy,
        //namely a new intersection is touched, then add it to the password and change the new "last xy"
        if (  coord[0] != 0 && (coord[0]!=last_x || coord[1]!=last_y)) {
        	if (mEnableHapticFeedback) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
        	String pad_x="";
        	String pad_y="";
        	if(coord[0]<10){
        		pad_x="0";
        	}
        	if(coord[1]<10){
        		pad_y="0";
        	}
        	mPassword=mPassword+pad_x+coord[0]+pad_y+coord[1];
        	last_x=coord[0];
        	last_y=coord[1];
			
        	invalidate();
           }    
    }

    private void sendAccessEvent(int resId) {
        setContentDescription(getContext().getString(resId));
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        setContentDescription(null);
    }

    private void handleActionUp(MotionEvent event) {
    	
    	// if last xy is not 0, then should append a "00" to the password
    	if (last_x != 0 ){
    		mPassword=mPassword+"0000";
    		last_x=0;
    		last_y=0;
//    		mPatternInProgress = true;
    		notifyPatternDetected();  
    		setWillNotDraw(false);
    		postInvalidate();

        }

        // if trying to setup a password,  at the 1st enter, 
        // do not save the password until the user click "continue" explicitly
        if (PROFILE_DRAWING) {
        	System.out.println("this is profile_drawing");
            if (mDrawingProfilingStarted) {
                Debug.stopMethodTracing();
                mDrawingProfilingStarted = false;
            }
        }
    }

    private void handleActionDown(MotionEvent event) {

        final float x = event.getX();
        final float y = event.getY();
        coord = stoneCoord(x,y);
        // if the down hit an intersection, add it to the password
        if (  coord[0] != 0 ) {
        	if (mEnableHapticFeedback) {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                        | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
        	String pad_x="";
        	String pad_y="";
        	if(coord[0]<10){
        		pad_x="0";
        	}
        	if(coord[1]<10){
        		pad_y="0";
        	}
        	mPassword=mPassword+pad_x+coord[0]+pad_y+coord[1];
        	last_x=coord[0];
        	last_y=coord[1];
        }      
    }

    private float getCenterXForColumn(int column) {
        return getPaddingLeft() + column * mSquareWidth + mSquareWidth / 2f;
    }

    private float getCenterYForRow(int row) {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	

        	final int paddingTop = getPaddingTop();
        	final int paddingLeft = getPaddingLeft();
	        float l_start_y = paddingTop;
	        float l_stop_y = paddingTop+mPatternRow* mSquareHeight;
	        
	        float l_start_x = paddingLeft+mSquareWidth/2;
	        float l_stop_x = paddingLeft+mPatternCol* mSquareWidth-mSquareWidth/2;
	        
	        //define the size of circles
	        int min_grid_size = Math.min(mPatternRow, mPatternCol);
	        int max_grid_size = Math.max(mPatternRow, mPatternCol);
	        
	        float r_circle;
	        if ( max_grid_size <=2 ) {
	        	r_circle = (float)0.15;
	        } else if	( max_grid_size <=5 ) {
	           	r_circle = (float)0.2;
	        } else if ( max_grid_size <=7 ){
		        r_circle = (float)0.25;
	        } else {
	        	r_circle=(float)0.3;
	        }

	       //Pattern Line Thickness line factor from the setting
	        mPassGoLineThickness_factor = PassgoGlobalData.getDataInt(getContext(), PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_LINE_THICKNESS, 3);
	        float mPassGoLineThickness_factor_float;
	        
	        if (mPassGoLineThickness_factor<3){
	        	mPassGoLineThickness_factor_float=(float)(mPassGoLineThickness_factor+1)/4;
	        } else if (mPassGoLineThickness_factor==3){
	        	mPassGoLineThickness_factor_float=1;
	        } else {
	        	mPassGoLineThickness_factor_float=(float)(mPassGoLineThickness_factor-3)/3+1;
	        }
	        
	        
	        //define the thickness of the Pass-Go line
	        int line_thickness;
	        if ( max_grid_size <=2 ) {
	        	line_thickness = Math.min((int)(mSquareWidth/8), (int)(mSquareHeight/8));	        
	        } else if ( max_grid_size <=5 ) {
	        	line_thickness = Math.min((int)(mSquareWidth/6), (int)(mSquareHeight/6));
	        } else if ( max_grid_size <=7 ){
	        	line_thickness = Math.min((int)(mSquareWidth/5), (int)(mSquareHeight/5));
	        } else if ( max_grid_size <=9 ){
	        	line_thickness = Math.min((int)(mSquareWidth/4), (int)(mSquareHeight/4));	
	        } else {
	        	line_thickness = Math.min((int)(mSquareWidth/3), (int)(mSquareHeight/3));
	        }
	        
	        
	        mPassGoLineThickness=(int)((float)line_thickness*mPassGoLineThickness_factor_float);
	        
	        Go_line_paint.setStrokeWidth(mPassGoLineThickness); 
	        
	        
			double min_square = Math.min(mSquareWidth,mSquareHeight);
			float min = (float)min_square;
			float star_width = (float)(min*0.08);
			if (max_grid_size>5){
				star_width = (float)(min*0.12);
			}
			if (max_grid_size>=11){
				star_width = (float)(min*0.15);
			}
			if (max_grid_size>=15){
				star_width = (float)(min*0.18);
			}
	        
		//draw stars
			
			//center stars - if grid size is odd
			if (mPatternRow%2==1 && mPatternCol%2==1) {	
				// as long as grid is not 7x7 nor 3x3
				if ( ! ((mPatternRow==7 && mPatternCol==7) || (mPatternRow==3 && mPatternCol==3) ) ){
					canvas.drawRect((float)(l_start_x+(mPatternCol/2)*mSquareWidth-star_width), 
					(float)(l_start_y+(mPatternRow/2+0.5)*mSquareHeight-star_width), 
					(float)(l_start_x+(mPatternCol/2)*mSquareWidth+star_width), 
					(float)(l_start_y+(mPatternRow/2+0.5)*mSquareHeight+star_width), 
					Grid_line_paint);
				}
			}
	        
			// corner stars
			if ((min_grid_size>=7) && (min_grid_size <13) ){
				canvas.drawRect((float)(l_start_x+2*mSquareWidth-star_width), 
								(float)(l_start_y+2.5*mSquareHeight-star_width), 
								(float)(l_start_x+2*mSquareWidth+star_width), 
								(float)(l_start_y+2.5*mSquareHeight+star_width), 
								Grid_line_paint);
				canvas.drawRect((float)(l_start_x+2*mSquareWidth-star_width), 
								(float)(l_stop_y-2.5*mSquareHeight-star_width), 
								(float)(l_start_x+2*mSquareWidth+star_width), 
								(float)(l_stop_y-2.5*mSquareHeight+star_width), 
								Grid_line_paint);

				canvas.drawRect((float)(l_stop_x-2*mSquareWidth-star_width), 
								(float)(l_start_y+2.5*mSquareHeight-star_width), 
								(float)(l_stop_x-2*mSquareWidth+star_width), 
								(float)(l_start_y+2.5*mSquareHeight+star_width), 
								Grid_line_paint);
				canvas.drawRect((float)(l_stop_x-2*mSquareWidth-star_width), 
								(float)(l_stop_y-2.5*mSquareHeight-star_width), 
								(float)(l_stop_x-2*mSquareWidth+star_width), 
								(float)(l_stop_y-2.5*mSquareHeight+star_width), 
								Grid_line_paint);
			}
			
			if (  min_grid_size >=13 ){
				canvas.drawRect((float)(l_start_x+3*mSquareWidth-star_width), 
								(float)(l_start_y+3.5*mSquareHeight-star_width), 
								(float)(l_start_x+3*mSquareWidth+star_width), 
								(float)(l_start_y+3.5*mSquareHeight+star_width), 
								Grid_line_paint);
				canvas.drawRect((float)(l_start_x+3*mSquareWidth-star_width), 
								(float)(l_stop_y-3.5*mSquareHeight-star_width), 
								(float)(l_start_x+3*mSquareWidth+star_width), 
								(float)(l_stop_y-3.5*mSquareHeight+star_width), 
								Grid_line_paint);

				canvas.drawRect((float)(l_stop_x-3*mSquareWidth-star_width), 
								(float)(l_start_y+3.5*mSquareHeight-star_width), 
								(float)(l_stop_x-3*mSquareWidth+star_width), 
								(float)(l_start_y+3.5*mSquareHeight+star_width), 
								Grid_line_paint);
				canvas.drawRect((float)(l_stop_x-3*mSquareWidth-star_width), 
								(float)(l_stop_y-3.5*mSquareHeight-star_width), 
								(float)(l_stop_x-3*mSquareWidth+star_width), 
								(float)(l_stop_y-3.5*mSquareHeight+star_width), 
								Grid_line_paint);
			}
			
			// side stars
			if (  (mPatternRow%2==1 && mPatternCol%2==1) && min_grid_size>=15 ){
				canvas.drawRect((float)(l_start_x+(mPatternCol/2)*mSquareWidth-star_width), 
								(float)(l_start_y+3.5*mSquareHeight-star_width), 
								(float)(l_start_x+(mPatternCol/2)*mSquareWidth+star_width), 
								(float)(l_start_y+3.5*mSquareHeight+star_width), 
								Grid_line_paint);
				canvas.drawRect((float)(l_start_x+3*mSquareWidth-star_width), 
								(float)(l_stop_y-(mPatternRow/2+0.5)*mSquareHeight-star_width), 
								(float)(l_start_x+3*mSquareWidth+star_width), 
								(float)(l_stop_y-(mPatternRow/2+0.5)*mSquareHeight+star_width), 
								Grid_line_paint);

				canvas.drawRect((float)(l_stop_x-3*mSquareWidth-star_width), 
								(float)(l_start_y+(mPatternRow/2+0.5)*mSquareHeight-star_width), 
								(float)(l_stop_x-3*mSquareWidth+star_width), 
								(float)(l_start_y+(mPatternRow/2+0.5)*mSquareHeight+star_width), 
								Grid_line_paint);
				canvas.drawRect((float)(l_stop_x-(mPatternCol/2)*mSquareWidth-star_width), 
								(float)(l_stop_y-3.5*mSquareHeight-star_width), 
								(float)(l_stop_x-(mPatternCol/2)*mSquareWidth+star_width), 
								(float)(l_stop_y-3.5*mSquareHeight+star_width), 
								Grid_line_paint);
			}
			

			// draw grid line
			for (int i = 0; i < mPatternRow; i++)
			{
	            l_start_y = l_stop_y = (paddingTop+mSquareHeight/2) + i * mSquareHeight;
				canvas.drawLine(l_start_x, l_start_y, l_stop_x, l_stop_y, Grid_line_paint);
			}
			
	        l_start_y = paddingTop+mSquareHeight/2;
	        l_stop_y = paddingTop+mPatternRow* mSquareHeight-mSquareHeight/2;
			
			for (int j = 0; j < mPatternCol; j++) {
		        l_start_x = l_stop_x = (paddingLeft+mSquareWidth/2)+j*mSquareWidth;
	            canvas.drawLine(l_start_x, l_start_y, l_stop_x, l_stop_y, Grid_line_paint);
	        }
			

		
		// draw pattern now	
		System.out.print(mPassword);
        //Go_line_paint.setColor(Color.parseColor("red"));
		if (mPatternDisplayMode == DisplayMode.Wrong) {
			if (!hidePassGoLineDot || PassgoGlobalData.Changing_PassGo) {
				Go_line_paint.setColor(Color.GRAY);   
				Go_dot_paint.setColor(Color.GRAY); 
			}


        } else {
			Go_line_paint.setColor(mPatternLineColor);   
			Go_dot_paint.setColor(mPatternDotColor); 
        }

		int pass_array[] = new int [mPassword.length()]; 
		for(int z = 0; z < mPassword.length(); z+=4) {
			String zx1 = Integer.toString(Integer.valueOf(mPassword.charAt(z))-48);
			String zx2 = Integer.toString(Integer.valueOf(mPassword.charAt(z+1))-48);
			String zxs=zx1+zx2;
			int zx = Integer.valueOf(zxs);
			String zy1 = Integer.toString(Integer.valueOf(mPassword.charAt(z+2))-48);
			String zy2 = Integer.toString(Integer.valueOf(mPassword.charAt(z+3))-48);
			String zys=zy1+zy2;
			int zy = Integer.valueOf(zys);
			
			
			pass_array[z/2]=zx;
			pass_array[(z/2)+1]=zy;

			if (zx==0){
				if (z==4 || pass_array[z/2-3]==0) {
				      
					  float x_real = pass_array[z/2-2]*mSquareWidth+getPaddingLeft() - mSquareWidth/2;
					  float y_real = pass_array[z/2-1]*mSquareHeight+getPaddingTop() - mSquareHeight/2;

				      canvas.drawCircle(x_real, y_real, r_circle*min, Go_dot_paint);
				}
			} else {
				if (z/2>1 && pass_array[z/2-1]>0){

					float startX=pass_array[z/2-2]*mSquareWidth+getPaddingLeft() - mSquareWidth/2;
					float startY=pass_array[z/2-1]*mSquareHeight+getPaddingTop() - mSquareHeight/2;
					float stopX=pass_array[z/2]*mSquareWidth+getPaddingLeft() - mSquareWidth/2;
					float stopY=pass_array[z/2+1]*mSquareHeight+getPaddingTop() - mSquareHeight/2;
					
					canvas.drawLine(startX, startY, stopX, stopY, Go_line_paint);
				}
			}
		}
		



    }

    public void switchHold() {
    	mHold=!mHold;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,
        		mLockPatternUtils.patternToString(mPattern),
                mPatternDisplayMode.ordinal(),
                mInputEnabled, mInStealthMode, mEnableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(
                DisplayMode.Correct,
                mLockPatternUtils.stringToPattern(ss.getSerializedPattern()));
        mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        mInputEnabled = ss.isInputEnabled();
        mInStealthMode = ss.isInStealthMode();
        mEnableHapticFeedback = ss.isTactileFeedbackEnabled();
    }

    /**
     * The parecelable for saving and restoring a lock pattern view.
     */
    private static class SavedState extends BaseSavedState {

        private final String mSerializedPattern;
        private final int mDisplayMode;
        private final boolean mInputEnabled;
        private final boolean mInStealthMode;
        private final boolean mTactileFeedbackEnabled;

        /**
         * Constructor called from {@link LockPatternView#onSaveInstanceState()}
         */
        private SavedState(Parcelable superState, String serializedPattern, int displayMode,
                boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled) {
            super(superState);
            mSerializedPattern = serializedPattern;
            mDisplayMode = displayMode;
            mInputEnabled = inputEnabled;
            mInStealthMode = inStealthMode;
            mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mSerializedPattern = in.readString();
            mDisplayMode = in.readInt();
            mInputEnabled = (Boolean) in.readValue(null);
            mInStealthMode = (Boolean) in.readValue(null);
            mTactileFeedbackEnabled = (Boolean) in.readValue(null);
        }

        public String getSerializedPattern() {
            return mSerializedPattern;
        }

        public int getDisplayMode() {
            return mDisplayMode;
        }

        public boolean isInputEnabled() {
            return mInputEnabled;
        }

        public boolean isInStealthMode() {
            return mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled(){
            return mTactileFeedbackEnabled;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mSerializedPattern);
            dest.writeInt(mDisplayMode);
            dest.writeValue(mInputEnabled);
            dest.writeValue(mInStealthMode);
            dest.writeValue(mTactileFeedbackEnabled);
        }


    }
}
