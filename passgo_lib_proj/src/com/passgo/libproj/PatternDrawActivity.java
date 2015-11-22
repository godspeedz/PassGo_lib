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

import java.util.List;

import android.R.integer;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PorterDuff;
import android.content.Intent;

//import com.hipassgo.screenlocker.help.PassGoFAQActivity;
//import com.hipassgo.screenlocker.help.PassGoHelpPageActivity;
//import com.hipassgo.screenlocker.help.PassGoHelpPageFeedbackEmailActivity;
//import com.hipassgo.screenlocker.lockscreensettings.LockscreenTypePatternActivity;
import com.passgo.libproj.PassgoGlobalData;
import com.passgo.libproj.Cell;
import com.passgo.libproj.PassgoPatternUtils;
import com.passgo.libproj.PassgoPatternView;
import com.passgo.libproj.PassgoPatternView.DisplayMode;
import com.passgo.libproj.PassgoPatternView.OnPatternListener;
import com.passgo.libproj.R;

public class PatternDrawActivity extends Activity {
	private final static String TAG = "passgoDrawActivity";

	private Context mContext;
	private String tmp_password = "";

	private PassgoPatternView mPassgoPatternView;
	private PassgoPatternUtils mPassgoPatternUtils;
	private TextView mDrawPatternTitle;

	private boolean isFirstDraw = true;
	private boolean isRightPattern = false;

	private RelativeLayout mNextLayout;
	private TextView mSetTextView;
	private TextView mConfirmTextView;

	private Button cancelbutton = null;
	private Button retrybutton = null;

	private Button continuebutton = null;
	private Button confirmbutton = null;
	
    private Runnable mRunnable;
    private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// comment out, for new UI
		setActionBarLayout(R.layout.passgo_pattern_draw_actionbar);
		setContentView(R.layout.passgo_pattern_draw_activity);



		mContext = this;
		
		

		int callingActivity = getIntent().getIntExtra("calling-activity", 0);
		switch (callingActivity) {
		case PassgoGlobalData.MAIN_SERVICE:
			//mDrawPatternTitle.setText("Choose Grid Size");
			Intent intent = new Intent(this,
					PassgoPatternSizeActivity.class);
			PassgoGlobalData.Changing_PassGo=true;
			intent.putExtra("calling-activity", PassgoGlobalData.PATTERN_CALL_REFRESH);
			startActivity(intent);
			break;
		}

		
		
		
	}

	private void initPassGo() {
		
		mDrawPatternTitle = (TextView) findViewById(R.id.passgo_pattern_draw_activity_title_TEXTVIEW);
		
		mPassgoPatternView = (PassgoPatternView) findViewById(R.id.passgo_pattern_draw_activity_LOCKPATTERNVIEW);
		mPassgoPatternUtils = new PassgoPatternUtils(this, mPassgoPatternView);

		initButton();

		mPassgoPatternView.setOnPatternListener(new OnPatternListener() {

			@Override
			public void onPatternStart() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPatternDetected(String password) {
				// TODO Auto-generated method stub
				tmp_password = password;
				Log.i(TAG, "so far tmp password is " + password);
				if (isFirstDraw) {

					cancelbutton.setVisibility(View.GONE);
					retrybutton.setVisibility(View.VISIBLE);
					retrybutton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {

							mPassgoPatternView.clearPattern();
							continuebutton.setEnabled(false);
							continuebutton.setTextColor(Color.parseColor("#FF999999"));

						}
					});

					continuebutton.setEnabled(true);
					continuebutton.setTextColor(Color.parseColor("#FFFFFFFF"));
					continuebutton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							nextStep();
							continuebutton.setVisibility(View.GONE);
							confirmbutton.setVisibility(View.VISIBLE);
						}
					});

				} else {

					retrybutton.setVisibility(View.GONE);

					cancelbutton.setVisibility(View.VISIBLE);

					// check pattern whether is correct
					int result = mPassgoPatternUtils.checkIfWrongPattern(
							password, false);
					if (result == 0) { // so far so good passgo
						Log.i(TAG, "so far so good passgo");
						int result_2 = mPassgoPatternUtils.checkPattern(password,
								false);
						if (result_2 == 1) { // 2nd draw is good and complete,
												// ready to save it to be
												// offical

							mPassgoPatternView
									.setDisplayMode(DisplayMode.Correct);
							mDrawPatternTitle
									.setText(R.string.DRAW_PATTERN_HELP_SUCCESS);

		
							isRightPattern = true;


							confirmbutton.setEnabled(true);
							confirmbutton.setTextColor(Color
									.parseColor("#FFFFFFFF"));
							confirmbutton
									.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											nextStep();
										}
									});

						}
					} else { // if so far not good already, clear it
						Log.i(TAG, "so far is not good, clear it");

						mDrawPatternTitle
								.setText(R.string.DRAW_PATTERN_HELP_ERROR);
						mPassgoPatternView.setDisplayMode(DisplayMode.Wrong);
						
						
						
						Thread thread = new Thread() {
						    @Override
						    public void run() {
						        try {
						        	mPassgoPatternView.switchHold();
						            Thread.sleep(1000);
						        } catch (InterruptedException e) {
						        	return;
						        }
						        
						        runOnUiThread(new Runnable() {
						            @Override
						            public void run() {
						                // Do some stuff
								        System.out.println("delayed for 1000");
								        mPassgoPatternView.clearPattern();
										mPassgoPatternView.setDisplayMode(DisplayMode.Correct);	
										mPassgoPatternView.switchHold();
						            }
						        });
						    }
						};
						thread.start();

					}
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



	@Override
	protected void onResume() {
		super.onResume();
		isFirstDraw = true;

		setContentView(R.layout.passgo_pattern_draw_activity);
		initPassGo();

	}
	

	public void setActionBarLayout(int layoutId) {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowCustomEnabled(true);
			LayoutInflater inflator = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(layoutId, null);
			ActionBar.LayoutParams layout = new ActionBar.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			actionBar.setCustomView(v, layout);
			mNextLayout = (RelativeLayout) v
					.findViewById(R.id.passgo_pattern_draw_actionbar_nextselector_RELATIVELAYOUT);

			mSetTextView = (TextView) v
					.findViewById(R.id.home_launcher_select_actionbar_set_TEXTVIEW);
			mSetTextView.setVisibility(View.VISIBLE);
			mConfirmTextView = (TextView) v
					.findViewById(R.id.home_launcher_select_actionbar_confirm_TEXTVIEW);
			mConfirmTextView.setVisibility(View.INVISIBLE);
		}
	}

	private void initButton() {
		retrybutton = (Button) findViewById(R.id.retrybutton);

		confirmbutton = (Button) findViewById(R.id.confirmbutton);

		cancelbutton = (Button) findViewById(R.id.cancelbutton);
		// Create clean button.
		cancelbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {


				jumptoMain();
				finish();

			}
		});

		continuebutton = (Button) findViewById(R.id.continuebutton);
		continuebutton.setEnabled(false);

		continuebutton.setTextColor(Color.parseColor("#FF999999"));

		retrybutton.setVisibility(View.GONE);

		confirmbutton.setVisibility(View.GONE);
		confirmbutton.setEnabled(false);
		confirmbutton.setTextColor(Color.parseColor("#FF999999"));
	}

	private void nextStep() {
		if (isFirstDraw) {
			// first time to draw pattern, save it
			mPassgoPatternUtils.saveLockPattern(tmp_password, false);
			Log.i(TAG, "save the 1st draw to be the unofficial pattern");
			isFirstDraw = false;
			mPassgoPatternView.clearPattern();
			mDrawPatternTitle.setText(R.string.DRAW_PATTERN_HELP_II);

			retrybutton.setVisibility(View.GONE);
			cancelbutton.setVisibility(View.VISIBLE);

		} else if (isRightPattern) {

			// 
			mPassgoPatternUtils.saveLockPattern(tmp_password, true);
    		Toast.makeText(this, R.string.PassGo_changed, Toast.LENGTH_SHORT).show();
    		PassgoGlobalData.setDataBool(mContext,
    				PassgoGlobalData.KEY_IS_OFFICIAL_PATTERN, true);
			int row = PassgoGlobalData.getDataInt(mContext,
					PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_ROW, 3);
			int col = PassgoGlobalData.getDataInt(mContext,
					PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_COL, 3);
			PassgoGlobalData.setDataInt(mContext,
					PassgoGlobalData.KEY_OFFICIAL_PATTERN_NUM_ROW, row);
			PassgoGlobalData.setDataInt(mContext,
					PassgoGlobalData.KEY_OFFICIAL_PATTERN_NUM_COL, col);
			// 
			PassgoGlobalData.setDataStr(mContext,
					PassgoGlobalData.KEY_CUR_LOCK_SCREEN_TYPE, "Pattern");

			// double check pattern succeed, go to backup PIN setting UI
			SharedPreferences preference = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			String stored_pin_pasword = preference.getString(
					PassgoGlobalData.KEY_OFFICIAL_PATTERN_BACKUP_PIN_PWD, "");

			if (stored_pin_pasword.isEmpty()) {

			}
			else{
				//Jump to main
				jumptoMainDialog();
				
			}
			 returnSuccess(RESULT_OK);
			this.finish();
			
			Log.i(TAG,"");
		}
	}
    //
	private void goToPreStep() {
		this.finish();
	}
	
	private void returnSuccess(int result_code){
		PatternDrawActivity.this.setResult(RESULT_OK);
		PatternDrawActivity.this.finish();

	}
	
	private void jumptoMain(){

	}
	
	private void jumptoMainDialog(){

	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy");
		super.onDestroy();
		PassgoGlobalData.Changing_PassGo=false;
					
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) { // shield Back Key
			Log.i(TAG, "shield Back Key");
			jumptoMain();
			finish();
			//return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) { // shield Menu Key
			Log.i(TAG, "shield Menu Key");
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) { // this does not work
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void launchMarket() {
	    Uri uri = Uri.parse("market://details?id=" + getPackageName());
	    Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
	    try {
	        startActivity(myAppLinkToMarket);
	    } catch (ActivityNotFoundException e) {
	        Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
	    }
	}
	
}
