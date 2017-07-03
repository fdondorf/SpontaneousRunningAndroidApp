package org.spontaneous.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.spontaneous.R;
import org.spontaneous.activities.model.UserModel;
import org.spontaneous.activities.util.CustomExceptionHandler;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.TimeoutHandler;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.Common;
import org.spontaneous.core.common.GenericWebService;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceHandler;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;
import org.spontaneous.core.crossdomain.UserInfo;
import org.spontaneous.core.dao.UserDAO;
import org.spontaneous.core.impl.ChangeProfileWebService;
import org.spontaneous.utility.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class ChangeProfileActivity extends Activity implements WebServiceHandler {

  	private static final String TAG = ChangeProfileActivity.class.toString();

  	private ChangeProfileWebService changeProfileWS;
  	private ProgressDialog prgDialog;

  	private int mRequestCode = 0;

	private View mProfileImageBox;
	private ImageView mImageView;
	private View mFirstnameBox;
  	private TextView mFirstname;
	private TextView mLastname;
	private TextView mEmail;
	private TextView mGender;
	private Button mSavePrefsButton;

	private boolean profileChanged = false;
	private String changedFirstname = null;
	private String changedProfileImage = null;

	private List<String> profileImageSelectionList = new ArrayList<>();

  	private Toolbar toolbar;

  	@Override
  	protected void onCreate(Bundle savedInstanceState) {
	  	super.onCreate(savedInstanceState);

	  	requestWindowFeature(Window.FEATURE_ACTION_BAR);

	  	setContentView(R.layout.layout_activity_change_profile);

	  	registerExceptionHandler();

	  	// Toolbar
	  	toolbar = (Toolbar) findViewById(R.id.tool_bar);
	  	toolbar.setTitle(R.string.title_change_profile_activity);
	  		toolbar.inflateMenu(R.menu.test);
	  	toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

		toolbar.setNavigationOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
              onBackPressed();
          }
      });

	  	ActionBar ab = getActionBar();
	  	if (ab != null) {
			ab.setHomeButtonEnabled(true);
	  	}

		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(this);
		prgDialog.setMessage("Please wait...");
		prgDialog.setCancelable(false);

	  	Bundle data = getIntent().getExtras();

	  	// Initialize GUI-Components
		profileImageSelectionList.add("Take picture");
		profileImageSelectionList.add("From gallery");
		initGUIComponents(this);

  }

	private void initGUIComponents(final Activity activity) {

		UserDAO userInfo = UserInfo.INSTANCE.getUserInfo();

		mProfileImageBox = (View) findViewById(R.id.profileImageBox);
		mProfileImageBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showRadioButtonSelectionDialog(profileImageSelectionList);
			}
		});

		mImageView = (ImageView) findViewById(R.id.avatar);

		mFirstnameBox = (View) findViewById(R.id.profile_firstname_box);
		mFirstnameBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showEditTextDialog();
			}
		});

		mFirstname = (TextView) findViewById(R.id.firstLine_firstname_right);
		mFirstname.setText(UserInfo.INSTANCE.getUserInfo().getFirstName());

		mLastname = (TextView) findViewById(R.id.firstLine_right_lastname);
		mLastname.setText(UserInfo.INSTANCE.getUserInfo().getLastName());

		mEmail = (TextView) findViewById(R.id.firstLine_right_email);
		mEmail.setText(UserInfo.INSTANCE.getUserInfo().getEmail());

		mGender = (TextView) findViewById(R.id.firstLine_right_gender);
		mGender.setText(UserInfo.INSTANCE.getUserInfo().getGender());

		mSavePrefsButton = (Button) findViewById(R.id.btn_savePreferences);
		mSavePrefsButton.setEnabled(false);
		mSavePrefsButton.setOnClickListener(mSaveProfileBtnListener);

	}

	private void setFirstname(String firstname) {
		this.mFirstname.setText(firstname);
	}

	private TimeoutHandler requestTimeout = new TimeoutHandler() {

		@Override
		public void onTimeoutRestored()
		{
			prgDialog.show();
		}

		@Override
		public void onTimedOut()
		{
			prgDialog.cancel();
			onError(new WebServiceResponse.Builder().fail(SystemError.TIMEOUT).build());
			cancelHandlers();
		}

		@Override
		public void onTimeoutCanceled()
		{
			prgDialog.cancel();
			cancelHandlers();
		}

	};

	@Override
	public void onError(WebServiceResponse response)
	{
		if (this == null) {
			return;
		}

		this.setResult(Common.CallResult.FAILED);

		if (response.getError().getType() == ErrorType.USER_CANCELED) {
			onRequestCancel();
		} else {
			DialogHelper.createFatalErrorDialog(this, response.getError().getDisplayMessage());
		}

	}
	private WebServiceCallHandler changeProfileCallHandler = new WebServiceCallHandler(this) {

		@Override
		public void showWebServiceProgress()
		{
			ChangeProfileActivity.this.showWebServiceProgress(getString(R.string.save_profile));
		}

		@Override
		public void onResponseSuccessful(WebServiceResponse response)
		{
			clear();
			ChangeProfileActivity.this.onChangeProfileSuccessful(response, this.getWebService());
		}

		@Override
		public Context getContext()
		{
			return ChangeProfileActivity.this;
		}
	};

  @Override
  public void onBackPressed() {
	  super.onBackPressed();
	  // TODO: setResult(RESULT_ACTIVITY_RESUMED);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
	  super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
	  switch(requestCode) {
		  case 0:
			  if(resultCode == RESULT_OK){
				  try {
					  Uri selectedImage = imageReturnedIntent.getData();
					  mImageView.setImageURI(selectedImage);

					  InputStream iStream = getContentResolver().openInputStream(selectedImage);
					  byte[] inputData = getBytes(iStream);

					  this.changedProfileImage = Base64.encodeToString(inputData, Base64.DEFAULT);
					  changedProfile();
				  } catch (IOException io) {
					  Log.e(TAG, "Exception during streaming image...", io);
				  }
			  }

			  break;
		  case 1:
			  if(resultCode == RESULT_OK){
				  try {
					  Uri selectedImage = imageReturnedIntent.getData();
					  mImageView.setImageURI(selectedImage);

					  InputStream iStream = getContentResolver().openInputStream(selectedImage);
					  byte[] inputData = getBytes(iStream);

					  this.changedProfileImage = Base64.encodeToString(inputData, Base64.DEFAULT);
					  changedProfile();
				  } catch (IOException io) {
					  Log.e(TAG, "Exception during streaming image...", io);
				  }
			  }
			  break;
	  }
  }

  	/****************************************
  	 * Listener
  	 * *************************************/

  	private OnClickListener mSaveProfileBtnListener = new OnClickListener() {

	    public void onClick(View v) {

	    	// Save profile
			attemptSaveProfile(v);

	    	if (mRequestCode == 0) {
	    		startMainActivity();
	    	}
	    	else if (mRequestCode == 1) {
	    		setResult(1);
	    		finish();
	    	}
	    	else {
	    		startMainActivity();
	    	}
	    }
  	};

	public void attemptSaveProfile(View view) {

		this.setResult(Common.CallResult.OK);
		requestTimeout.startCountingFrom(Constants.COMMON_REQUEST_TIMEOUT);

		if (changeProfileCallHandler.isTaskInProgress()) {
			return;
		}

		String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
		final String endpointUrl = serverUrl + RestUrls.REST_SERVICE_UPDATE_USER.toString();

		if (endpointUrl == null) {
			Toast.makeText(this, getString(R.string.error_change_profile_url), Toast.LENGTH_SHORT).show();

		} else {

			try {

				UserModel userModel = new UserModel(
						UserInfo.INSTANCE.getUserInfo().getUserId(),
						this.mFirstname.getText().toString(),
						this.mLastname.getText().toString(),
						this.mEmail.getText().toString()
				);
				userModel.setGender(this.mGender.getText().toString());
				if (this.changedProfileImage != null)
				userModel.setProfileImage(this.changedProfileImage);

				changeProfileWS = new ChangeProfileWebService(Constants.CONNECTION_TIMEOUT, userModel);
				changeProfileWS.doSynchronousRequest(changeProfileCallHandler);

			} catch (TimeoutException te) {

				prgDialog.cancel();
				Log.e(TAG, "TimeoutExceptoin during request", te);

			}
			saveUserInfo();
		}
	};


	private void cancelHandlers()
	{
		if (changeProfileCallHandler.isTaskInProgress()) {
			changeProfileCallHandler.cancel(false);
		}
	}

	private void onRequestCancel()
	{
		changeProfileCallHandler.clear();
	}

	public void onChangeProfileSuccessful(WebServiceResponse response, GenericWebService webService)
	{
		webService.interpretResponse(response);
		DialogHelper.createWarnDialog(this, "Ändern des Profils erfolgreich").show();
	}

	@Override
	public void showWebServiceProgress()
	{
		prgDialog.show();
	}

	public void showWebServiceProgress(String customLabel)
	{
		prgDialog.setMessage(customLabel);
		prgDialog.show();
	}

	@Override
	public void hideWebServiceProgress()
	{
		prgDialog.cancel();
	}

	@Override
	public void onResponseUnauthorized(WebServiceResponse response)
	{
		String errorCode = response.getError().getCode();

		if (errorCode.equals("invalid_grant") || errorCode.equals("unauthorized")) {
			DialogHelper.createFatalErrorDialog(this, response.getError().getDisplayMessage());
		} else {
			DialogHelper.createFatalErrorDialog(this, response.getError().getDisplayMessage());
		}
	}

	@Override
	public void onBusinessError(WebServiceResponse response)
	{
		onError(response);
	}

	@Override
	public void onAnyResult(WebServiceResponse response)
	{
		requestTimeout.stopCounting();
	}

	@Override
	public void onResponseSuccessful(WebServiceResponse response)
	{
		finish();
	}

	@Override
	protected void onResume()
	{
		requestTimeout.resume();
		super.onResume();
	}

	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  		getMenuInflater().inflate(R.menu.activity_summary, menu);
  		return true;
  	}

	private void startMainActivity() {
		Intent intent = new Intent();
    	intent.setClass(this, MainActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
	}

	private void registerExceptionHandler() {
		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
					getExternalCacheDir().toString(), null));
		}
	}

	private void showEditTextDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_cust_edit_text, null);
		dialogBuilder.setView(dialogView);

		final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
		edt.setText(mFirstname.getText());

		dialogBuilder.setTitle("Vornamen ändern");
		dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				changeFirstname(edt.getText().toString());
				dialog.cancel();
			}
		});
		dialogBuilder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		AlertDialog b = dialogBuilder.create();
		b.show();
	}

	private void changeFirstname(String firstname) {
		changedFirstname = firstname;
		this.mFirstname.setText(firstname);
		changedProfile();
	}

	private void changedProfile() {
		profileChanged = true;
		mSavePrefsButton.setEnabled(profileChanged);
	}

	private void saveUserInfo() {
		UserDAO userDao = UserInfo.INSTANCE.getUserInfo();
		userDao.setFirstName(this.changedFirstname);
		userDao.setProfileImage(this.changedProfileImage);
		UserInfo.INSTANCE.setUserInfo(userDao);
	}

	private void showRadioButtonSelectionDialog(List<String> selectionList) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_cust_radio, null);
		dialogBuilder.setView(dialogView);

		final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radio_group);

		RadioButton radioBtn = null;
		for (String s : selectionList) {
			radioBtn = new RadioButton(this);
			radioBtn.setText(s);
			radioGroup.addView(radioBtn);
		}

		dialogBuilder.setTitle("Profilbild ändern");
		dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				pickFromGallery();
				dialog.cancel();
			}
		});
		dialogBuilder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
		});
		AlertDialog b = dialogBuilder.create();
		b.show();
	}

	private void pickFromGallery() {
		Intent pickPhoto = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
	}

	private void takePicture() {
		Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePicture, 0);//zero can be replaced with any action code
	}

	public byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
	}
}