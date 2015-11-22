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

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.NumberPicker.OnScrollListener;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;

import com.passgo.libproj.R;

import com.passgo.libproj.PatternDrawActivity;
import com.passgo.libproj.PassgoGlobalData;

public class PassgoPatternSizeActivity extends Activity implements
		OnValueChangeListener, OnScrollListener, Formatter {
	private static final String TAG = "PassgoPatternSizeActivity";
	
	public static final int PassgoSettingOk = 10;

	private Context mContext;

	private RelativeLayout mConfirmPattern;

	private NumberPicker mRowPicker;
	private NumberPicker mColPicker;

	private int mRowNum;
	private int mColNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.passgo_pattern_size_activity);
		setActionBarLayout();

		mContext = this;

		mRowPicker = (NumberPicker) findViewById(R.id.passgo_type_pattern_kind_activity_pattern_row_NUMBERPICKER);
		mRowPicker.setFormatter(this);
		mRowPicker.setOnValueChangedListener(this);
		mRowPicker.setOnScrollListener(this);
		mRowPicker.setMaxValue(19);
		mRowPicker.setMinValue(2);
		mRowNum = PassgoGlobalData.getDataInt(mContext,
				PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_ROW, 3);
		mRowPicker.setValue(mRowNum);

		mColNum = PassgoGlobalData.getDataInt(mContext,
				PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_COL, 3);

		mColPicker = (NumberPicker) findViewById(R.id.passgo_type_pattern_kind_activity_pattern_col_NUMBERPICKER);
		mColPicker.setFormatter(this);
		mColPicker.setOnValueChangedListener(this);
		mColPicker.setOnScrollListener(this);
		mColPicker.setMaxValue(19);
		mColPicker.setMinValue(2);
		mColPicker.setValue(mColNum);

		mConfirmPattern = (RelativeLayout) findViewById(R.id.passgo_patternbutton_layout_RELATIVELAYOUT);
		mConfirmPattern.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goToNextStep();
			}
		});

	}

	// Currently as a dialog/floating activity, such dynamically setting
	// actionbarlayout is not working.
	public void setActionBarLayout() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowCustomEnabled(true);
			LayoutInflater inflator = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(R.layout.passgo_pattern_size_actionbar,
					null);
			ActionBar.LayoutParams layout = new ActionBar.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			actionBar.setCustomView(v, layout);

		}
	}

	@Override
	public String format(int value) {
		// TODO Auto-generated method stub
		String tmpStr = String.valueOf(value);
		return tmpStr;
	}

	@Override
	public void onScrollStateChange(NumberPicker view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub
		int id = picker.getId();
		if (id == R.id.passgo_type_pattern_kind_activity_pattern_row_NUMBERPICKER) {
			mRowNum = newVal;
		} else if (id == R.id.passgo_type_pattern_kind_activity_pattern_col_NUMBERPICKER) {
			mColNum = newVal;
		}

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	private void goToPreStep() {
		this.finish();
	}

	private void goToNextStep() {
		//
		PassgoGlobalData.setDataBool(mContext,
				PassgoGlobalData.KEY_IS_OFFICIAL_PATTERN, false);
		PassgoGlobalData.setDataInt(mContext,
				PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_ROW, mRowNum);
		PassgoGlobalData.setDataInt(mContext,
				PassgoGlobalData.KEY_UNOFFICIAL_PATTERN_NUM_COL, mColNum);

		int callingActivity = getIntent().getIntExtra("calling-activity", 0);

		switch (callingActivity) {
		case PassgoGlobalData.PATTERN_CALL_REFRESH:
			// this.finish();
			break;
		default:
			Intent intent = new Intent(PassgoPatternSizeActivity.this,
					PatternDrawActivity.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("calling-activity",
					PassgoGlobalData.CHANGE_GRID_SIZE);
			startActivityForResult(intent, 11);

			break;

		}

		// this.finish();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (resultCode) {
		case RESULT_OK: //
			returnSuccess(PassgoSettingOk);
			break;

		default:
			break;
		}
	}

	private void returnSuccess(int result_code) {
		PassgoPatternSizeActivity.this.setResult(PassgoSettingOk);
		PassgoPatternSizeActivity.this.finish();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) { // shield Back Key
			super.onBackPressed();
			// Log.i(TAG, "shield Back Key");
			// return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) { // shield Menu Key
			Log.i(TAG, "shield Menu Key");
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) { // this does not work
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
