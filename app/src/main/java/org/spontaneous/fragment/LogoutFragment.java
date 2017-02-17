package org.spontaneous.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.spontaneous.R;
import org.spontaneous.activities.LoginActivity;
import org.spontaneous.activities.LoginActivityOld;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.TimeoutHandler;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.Common;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.UserInfo;
import org.spontaneous.core.impl.LogoutWebService;
import org.spontaneous.utility.Constants;

import java.io.UnsupportedEncodingException;

public class LogoutFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private static final String TAG = LogoutFragment.class.toString();

	private Context mContext;

	private LogoutWebService logoutWS;

	// Progress Dialog Object
    ProgressDialog prgDialog;

	public static LogoutFragment newInstance(int sectionNumber, Activity parent) {
	     LogoutFragment fragment = new LogoutFragment();

	     Bundle args = new Bundle();
		 args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		 fragment.setArguments(args);

	     return fragment;
	}

	public LogoutFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_logout, container, false);

		mContext = this.getActivity();

		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(getActivity());
		// Set Progress Dialog Text
		prgDialog.setMessage("Please wait...");
		// Set Cancelable as False
		prgDialog.setCancelable(false);

//		try {
//			logout(null);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		logout();
	    return rootView;
	}

	public void onLogOutClick(View sender)
	{
		if (UserInfo.INSTANCE.hasUserInfo()) {

		}
	}

	public void logout(RequestParams params) throws UnsupportedEncodingException, JSONException {

    	// Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this.getActivity());
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);

        // Show Progress Dialog
        prgDialog.show();

    	// Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        String adress = RestUrls.SERVER_NAME.toString() + ":" + RestUrls.PORT.toString()
				+ RestUrls.REST_SERVICE_LOGOUT.toString();

        PersistentCookieStore myCookieStore = new PersistentCookieStore(this.getActivity().getApplicationContext());
		client.setCookieStore(myCookieStore);
        client.get(mContext, adress, null, new AsyncHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
        	@Override
            public void onSuccess(int statusCode, String response) {
        		// Hide Progress Dialog
                prgDialog.hide();

                Toast.makeText(mContext, "Successfully logged out!", Toast.LENGTH_LONG).show();
                clearCookieStore();
                navigatetoLoginActivity();
             }
			// When the response returned by REST has Http response code other than '200'
             @Override
             public void onFailure(int statusCode, Throwable error,
                 String content) {
                 // Hide Progress Dialog
                 prgDialog.hide();
                 // When Http response code is '404'
                 if(statusCode == 404){
                     Toast.makeText(mContext, "Requested resource not found", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code is '500'
                 else if(statusCode == 500){
                     Toast.makeText(mContext, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                 }
                 // When Http response code other than 404, 500
                 else{
                     Toast.makeText(mContext, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                 }
             }
         });
    }

	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    ((MainActivity) activity).onSectionAttached(1);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void logout() {

		// 1. Delete SharedPrefs
		SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Constants.PREFERENCES, getActivity().MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.clear();
        editor.commit();

		// 2. Call Logout-Service
		((MainActivity)getActivity()).onAttemptLogout();

		// 2. navigateToLoginscreen
		//navigatetoLoginActivity();
	}

	private void onAttemptLogout()
	{
		requestTimeout.startCountingFrom(Constants.COMMON_REQUEST_TIMEOUT);

		if (logoutCallHandler.isTaskInProgress()) {
			return;
		}

		final String enpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
				RestUrls.REST_SERVICE_LOGOUT;//ConfigProvider.INSTANCE.getConfig("logout_endpoint");

		if (enpointUrl == null) {

			DialogHelper.createStandardErrorDialog(this.getActivity(), "Log Out");
			//GuiHelper.showFatalConfigError(this, "Log Out");

		} else {

			try {

				logoutWS = new LogoutWebService();
				logoutWS.doRequest(logoutCallHandler);

			} catch (SystemException e) {

				prgDialog.hide();
				DialogHelper.createFatalErrorDialog(getActivity(), "Log Out");//(this, "Log Out");
				//GuiHelper.showFatalError(this);
				Log.e(TAG, "Cannot build proper request", e);

			}
		}
	}

	private WebServiceCallHandler logoutCallHandler = new WebServiceCallHandler(null) {

		@Override
		public void showWebServiceProgress()
		{
			prgDialog.show();
			super.showWebServiceProgress();
		}

		@Override
		public void hideWebServiceProgress()
		{
			prgDialog.hide();
			super.hideWebServiceProgress();
		}

		@Override
		public void onAnyResult(WebServiceResponse response)
		{
			Log.i(TAG, "logoutCallHandler onAnyResult()");
			requestTimeout.stopCounting();
			Authentication.INSTANCE.clearAuth();
			checkAuth();
		}

		@Override
		public Context getContext()
		{
			return getActivity();
		}

	};

	private TimeoutHandler requestTimeout = new TimeoutHandler() {

		@Override
		public void onTimeoutRestored()
		{
			prgDialog.show();
		}

		@Override
		public void onTimedOut()
		{
			prgDialog.hide();
		}

		@Override
		public void onTimeoutCanceled()
		{
			Log.i(TAG, "onTimeoutCanceled()");
			prgDialog.hide();
			onCanceled();
		}

	};

	private void onCanceled()
	{
		Log.i(TAG, "onCanceled()");
		requestTimeout.stopCounting();
		if (logoutCallHandler.isTaskInProgress()) {
			logoutCallHandler.cancel(false);
		}
	}

	private void checkAuth()
	{
		if (!Authentication.INSTANCE.isAuthenticated()) {
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			this.startActivityForResult(intent, Common.CallSource.LOGIN_ACTION);
		}
		else {
			navigatetoLoginActivity();
		}
	}

    /**
     * Method gets triggered when Register button is clicked
     */
    public void navigatetoLoginActivity() {
        Intent loginIntent = new Intent(this.getActivity().getApplicationContext(), LoginActivityOld.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        this.getActivity().finish();
    }

    private void clearCookieStore() {
    	PersistentCookieStore myStore = new PersistentCookieStore(this.getActivity().getApplicationContext());
    	myStore.clear();
	}

  	private boolean createWarnDialog() {

  		boolean result = false;

  		new AlertDialog.Builder(getActivity())
  			.setTitle(R.string.standardWarningHdr)
  			.setMessage(R.string.warningNoGPSSignalTxt)
  			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
  				public void onClick(DialogInterface dialog, int whichButton) {
  					dialog.cancel();
		        }
		    })
		    .setPositiveButton(R.string.cont, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	dialog.cancel();
		        	logout();
		        }
		    })
		    .create().show();

  		return result;
  	}
}
