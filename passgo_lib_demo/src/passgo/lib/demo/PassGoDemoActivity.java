package passgo.lib.demo;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.passgo.libproj.CheckPassgoPatternActivity;
import com.passgo.libproj.PassgoPatternSizeActivity;
import com.passgo.libproj.PassgoPatternUtils;
import com.passgo.libproj.PassgoPatternView;

public class PassGoDemoActivity extends Activity {

	private final static String TAG = "PassGoDemoActivity";

	public static final int PassgoSettingOk = 10;
	public static final int PassgoPasswordOk = 11;
	public static final int PassgoTooFails = 12;

	private Context mContext;

	private PassgoPatternView mPassgoPatternView;
	private PassgoPatternUtils mPassgoPatternUtils;

	private Button setpassgoButton = null;
	private Button crackpassgoButton = null;
	private Button okButton = null;
	private Button retryButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passgodemo_layout);

		setpassgoButton = (Button) findViewById(R.id.setpassgobutton);
		crackpassgoButton = (Button) findViewById(R.id.crackpassgobutton);
		okButton = (Button) findViewById(R.id.donebutton);
		retryButton = (Button) findViewById(R.id.retrybutton);

		crackpassgoButton.setVisibility(View.GONE);

		okButton.setVisibility(View.GONE);
		retryButton.setVisibility(View.GONE);

		mPassgoPatternView = (PassgoPatternView) findViewById(R.id.demo_PassgoPATTERNVIEW);
		mPassgoPatternUtils = new PassgoPatternUtils(this, mPassgoPatternView);
		mPassgoPatternView.setVisibility(View.GONE);

		// Button handle function
		setupDemoButtonViews();
	}

	protected void setupDemoButtonViews() {
		//
		setpassgoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(PassGoDemoActivity.this,
						PassgoPatternSizeActivity.class);
				startActivityForResult(intent, 1);

			}
		});

		//
		crackpassgoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(PassGoDemoActivity.this,
						CheckPassgoPatternActivity.class);
				startActivityForResult(intent, 2);

			}
		});

		//
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				okButton.setVisibility(View.GONE);
				mPassgoPatternView.setVisibility(View.GONE);
				setpassgoButton.setVisibility(View.VISIBLE);

			}
		});

		retryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				retryButton.setVisibility(View.GONE);
				mPassgoPatternView.setVisibility(View.GONE);
				setpassgoButton.setVisibility(View.VISIBLE);

			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (resultCode) {
		case PassgoSettingOk:
			setpassgoButton.setVisibility(View.GONE);
			crackpassgoButton.setVisibility(View.VISIBLE);
			break;

		case PassgoTooFails:
			crackpassgoButton.setVisibility(View.GONE);
			retryButton.setVisibility(View.VISIBLE);
			break;

		case PassgoPasswordOk:
			crackpassgoButton.setVisibility(View.GONE);
			okButton.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
	}

}
