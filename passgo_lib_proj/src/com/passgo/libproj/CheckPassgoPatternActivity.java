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

import java.io.File;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.passgo.libproj.PassgoGlobalData;
//import com.hipassgo.screenlocker.utils.TimeManager;
import com.passgo.libproj.Cell;
import com.passgo.libproj.PassgoPatternUtils;
import com.passgo.libproj.PassgoPatternView;
import com.passgo.libproj.PassgoPatternView.DisplayMode;
import com.passgo.libproj.PassgoPatternView.OnPatternListener;

import com.passgo.libproj.R;

public class CheckPassgoPatternActivity extends Activity implements
		OnClickListener {
	private final static String TAG = "CheckPassgoPatternActivity";
	
	public static final int PassgoPasswordOk = 11;
    public static final int PassgoTooFails = 12;

	private Context mContext;
	private int mSleep_time = 500;

	private String mBgPath;

	private WindowManager mWindowManager;
	private View mLockView;
	private LayoutParams mLockViewLayoutParams;

	private PassgoPatternView mPassgoPatternView;
	private PassgoPatternUtils mPassgoPatternUtils;

	private RelativeLayout mLockPINView;
	private ImageButton mDeleteButton;
	private EditText mPasswordEditText;
	private ImageButton mNum1Button;
	private ImageButton mNum2Button;
	private ImageButton mNum3Button;
	private ImageButton mNum4Button;
	private ImageButton mNum5Button;
	private ImageButton mNum6Button;
	private ImageButton mNum7Button;
	private ImageButton mNum8Button;
	private ImageButton mNum9Button;
	private ImageButton mNum0Button;
	private ImageButton mNumOKButton;
	private String mPINPassword;


	// public TimeManager timeManager;

	private String mCurLockScreenType;

	private RelativeLayout mPatterninfoLayoutView;
	private TextView mDrawPatternTitle;

	private Runnable mRunnable;
	private Handler mHandler;

	private SensorManager mSensorManager;
	private PowerManager mPowerManager;

	private int mBGColor = PassgoGlobalData.KEY_COLOR_ORANGE;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.check_passgo_pattern_activity);

		PassgoGlobalData.setDataBool(mContext, PassgoGlobalData.KEY_IS_LOCKED,
				true);
		PassgoGlobalData.setDataBool(mContext,
				PassgoGlobalData.KEY_IS_LOCKSCREEN_DESTROYED, false);

		mPatterninfoLayoutView = (RelativeLayout) findViewById(R.id.passgo_patterninfo_layout_RELATIVELAYOUT);
		mDrawPatternTitle = (TextView) findViewById(R.id.passgo_pattern_draw_activity_title_TEXTVIEW);
		mPatterninfoLayoutView.setVisibility(View.INVISIBLE);

		mLockPINView = (RelativeLayout) findViewById(R.id.passgo_pin_layout_RELATIVELAYOUT);

		initPatternView();


		mPassgoPatternView.setVisibility(View.VISIBLE);
		mLockPINView.setVisibility(View.INVISIBLE);

		PassgoGlobalData.current_rotate = Settings.System.getInt(
				this.getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, 0);
		if (PassgoGlobalData.current_rotate == 1) {
			setAutoOrientationEnabled(this, false);
		}

		mPatterninfoLayoutView = (RelativeLayout) findViewById(R.id.passgo_patterninfo_layout_RELATIVELAYOUT);
		mDrawPatternTitle = (TextView) findViewById(R.id.passgo_pattern_draw_activity_title_TEXTVIEW);
		mPatterninfoLayoutView.setVisibility(View.INVISIBLE);

		mHandler = new Handler();

		mRunnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// mPatterninfoLayoutView.setVisibility(View.INVISIBLE); //If
				// you want just hide the View. But it will retain space
				// occupied by the View.
				mPatterninfoLayoutView.setVisibility(View.GONE); // This will
																	// remove
																	// the View.
																	// and frees
																	// the space
																	// occupied
																	// by the
																	// View
			}
		};

	}

	/** Called when leaving the activity */
	@Override
	public void onPause() {

		super.onPause();
	}

	/** Called before the activity is destroyed */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy");
		PassgoGlobalData.setDataBool(mContext,
				PassgoGlobalData.KEY_IS_LOCKSCREEN_DESTROYED, true);

		if (PassgoGlobalData.current_rotate == 1) {
			setAutoOrientationEnabled(this, true);
		}
		super.onDestroy();

	}

	@Override
	public void finish() {
		super.finish();

		overridePendingTransition(0, 0);
	}

	private void initPatternView() {

		mPassgoPatternView = (PassgoPatternView) findViewById(R.id.passgo_pattern_main_service_activity_LOCKPATTERNVIEW);
		mPassgoPatternUtils = new PassgoPatternUtils(this, mPassgoPatternView);

		mPassgoPatternView.setOnPatternListener(new OnPatternListener() {

			@Override
			public void onPatternStart() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPatternDetected(String password) {
				// TODO Auto-generated method stub
				// check pattern whether is correct
				System.out.println("detected password is " + password);
				int result_2 = mPassgoPatternUtils.checkIfWrongPattern(
						password, true);
				if (result_2 == 0) {
					int result = mPassgoPatternUtils.checkPattern(password,
							true);
					if (result == 1) { // right pattern
						Log.i(TAG, "right pattern");
						Log.i(TAG, password);
						mPassgoPatternView.setDisplayMode(DisplayMode.Correct);

						unlockscreen(PassgoPasswordOk);
					}
				} else { // wrong pattern
					Log.i(TAG, "wrong pattern");
					PassgoGlobalData.Wrong_PassGo_Count++;

					mPassgoPatternView.setDisplayMode(DisplayMode.Wrong);
					mPatterninfoLayoutView.setVisibility(View.VISIBLE);

					if (PassgoGlobalData
							.getDataBool(
									mContext,
									PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_CLEAR_LAST,
									false)) {
						mDrawPatternTitle
								.setText(R.string.LOGIN_PATTERN_HELP_CONTINUE);
					} else {
						mDrawPatternTitle
								.setText(R.string.LOGIN_PATTERN_HELP_ERROR);
					}

					mHandler.removeCallbacks(mRunnable);


					if (PassgoGlobalData.Wrong_PassGo_Count >= 6) {

						unlockscreen(PassgoTooFails);
					} else {

					}

					Thread thread = new Thread() {
						@Override
						public void run() {
							try {
								mPassgoPatternView.switchHold();
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								return;
							}

							runOnUiThread(new Runnable() {
								@Override
								public void run() {

									if (PassgoGlobalData
											.getDataBool(
													mContext,
													PassgoGlobalData.KEY_LOCK_SCREEN_PATTERN_CLEAR_LAST,
													false)) {
										mDrawPatternTitle
												.setText(R.string.LOGIN_PATTERN_HELP_CONTINUE);
										mPassgoPatternView.clearLastStroke();
									} else {
										mPassgoPatternView.clearPattern();
									}
									mPassgoPatternView
											.setDisplayMode(DisplayMode.Correct);
									mPatterninfoLayoutView
											.setVisibility(View.INVISIBLE);
									mPassgoPatternView.switchHold();
								}
							});
						}
					};
					thread.start();
					//

				}
			}

			@Override
			public void onPatternCleared() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPatternCellAdded(List<Cell> pattern) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPatternDetected(List<Cell> pattern) {
				// TODO Auto-generated method stub

			}

		});
	}

	private void returnSuccess(int result_code) {
		CheckPassgoPatternActivity.this.setResult(result_code);
		CheckPassgoPatternActivity.this.finish();

	}

	private void initPINView() {

		mLockPINView.setVisibility(View.INVISIBLE);

		mDeleteButton = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_pin_delete_password_IMAGEBUTTON);
		mPasswordEditText = (EditText) mLockView
				.findViewById(R.id.passgo_pin_layout_password_char_EDITTEXT);
		mNum1Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_1_IMAGEBUTTON);
		mNum2Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_2_IMAGEBUTTON);
		mNum3Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_3_IMAGEBUTTON);
		mNum4Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_4_IMAGEBUTTON);
		mNum5Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_5_IMAGEBUTTON);
		mNum6Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_6_IMAGEBUTTON);
		mNum7Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_7_IMAGEBUTTON);
		mNum8Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_8_IMAGEBUTTON);
		mNum9Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_9_IMAGEBUTTON);
		mNum0Button = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_0_IMAGEBUTTON);
		mNumOKButton = (ImageButton) mLockView
				.findViewById(R.id.passgo_pin_layout_i_activity_ok_IMAGEBUTTON);

		mDeleteButton.setOnClickListener(this);
		mNum1Button.setOnClickListener(this);
		mNum2Button.setOnClickListener(this);
		mNum3Button.setOnClickListener(this);
		mNum4Button.setOnClickListener(this);
		mNum5Button.setOnClickListener(this);
		mNum6Button.setOnClickListener(this);
		mNum7Button.setOnClickListener(this);
		mNum8Button.setOnClickListener(this);
		mNum9Button.setOnClickListener(this);
		mNum0Button.setOnClickListener(this);
		mNumOKButton.setOnClickListener(this);
	}


	private void unlockscreen(int resultdata) {
		Log.i(TAG, "unlockscreen");
		PassgoGlobalData.setDataBool(mContext, PassgoGlobalData.KEY_IS_LOCKED,
				false);
		PassgoGlobalData.Wrong_PassGo_Count = 0;
		PassgoGlobalData.Just_Unlock_Screen = 3;
		if (PassgoGlobalData.current_rotate == 1) {
			setAutoOrientationEnabled(this, true);
		}

		returnSuccess(resultdata);

		this.finish();
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();

		if (id == R.id.passgo_pin_layout_pin_delete_password_IMAGEBUTTON)
			deleteNum();

		else if (id == R.id.passgo_pin_layout_i_activity_1_IMAGEBUTTON)
			enterNum("1");
		else if (id == R.id.passgo_pin_layout_i_activity_2_IMAGEBUTTON)
			enterNum("2");
		else if (id == R.id.passgo_pin_layout_i_activity_3_IMAGEBUTTON)
			enterNum("3");
		else if (id == R.id.passgo_pin_layout_i_activity_4_IMAGEBUTTON)
			enterNum("4");
		else if (id == R.id.passgo_pin_layout_i_activity_5_IMAGEBUTTON)
			enterNum("5");
		else if (id == R.id.passgo_pin_layout_i_activity_6_IMAGEBUTTON)
			enterNum("6");
		else if (id == R.id.passgo_pin_layout_i_activity_7_IMAGEBUTTON)
			enterNum("7");
		else if (id == R.id.passgo_pin_layout_i_activity_8_IMAGEBUTTON)
			enterNum("8");
		else if (id == R.id.passgo_pin_layout_i_activity_9_IMAGEBUTTON)
			enterNum("9");
		else if (id == R.id.passgo_pin_layout_i_activity_0_IMAGEBUTTON)
			enterNum("0");
		else if (id == R.id.passgo_pin_layout_i_activity_ok_IMAGEBUTTON)
			checkPIN();

	}

	private void enterNum(String numStr) {
		String curPwd = mPasswordEditText.getText().toString();
		curPwd += numStr;
		mPasswordEditText.setText(curPwd);
	}

	private void deleteNum() {
		String curPwd = mPasswordEditText.getText().toString();
		try {
			mPasswordEditText.setText(curPwd.substring(0, curPwd.length() - 1));
		} catch (Exception e) {
			mPasswordEditText.setText("");
		}
	}

	private void checkPIN() {
		String curPwd = mPasswordEditText.getText().toString();
		if (curPwd.isEmpty()) {
			Toast.makeText(this, R.string.DRAW_PATTERN_PIN_HELP_ERROR_TOAST,
					Toast.LENGTH_SHORT).show();
		} else {
			if (mPINPassword != null) {
				if (mPINPassword.equals(curPwd)) {
					// enter the right PIN, unlock screen
					unlockscreen(PassgoPasswordOk);
				} else {
					mPasswordEditText.setText(null);
					Toast.makeText(this, R.string.LOGIN_PATTERN_PIN_HELP_ERROR,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		PassgoGlobalData.Changing_PassGo = false;
		mContext = this;
		if (PassgoGlobalData.getDataBool(mContext,
				PassgoGlobalData.KEY_IS_APP_LOCKED, false)) {
			sendBroadcast(new Intent("killApplicationLockActivity"));
		}

		if ((PassgoGlobalData.getDataBool(mContext,
				PassgoGlobalData.TESTING_FLAG, false) == true)
				|| (getIntent().getBooleanExtra(PassgoGlobalData.TESTING_FLAG,
						false) == true)) {

			PassgoGlobalData.setDataBool(mContext,
					PassgoGlobalData.TESTING_FLAG, false);

		}

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	public static void setAutoOrientationEnabled(Context context,
			boolean enabled) {
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);
	}

}