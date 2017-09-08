package org.spontaneous.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.spontaneous.R;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.TimeoutHandler;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.Common;
import org.spontaneous.core.common.Common.CallResult;
import org.spontaneous.core.common.GenericWebService;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceHandler;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.ConfigProvider;
import org.spontaneous.core.impl.LoginWebService;
import org.spontaneous.core.impl.UserInfoWebService;
import org.spontaneous.utility.Constants;

/**
 * Created by fdondorf on 12.11.2016.
 */

public class LoginActivity extends Activity implements WebServiceHandler, AdapterView.OnItemSelectedListener {

    private static final String TAG = LoginActivity.class.toString();

    private ITrackingService trackingService;
    private LoginWebService loginWS;
    private UserInfoWebService useInfoWS;

    // Error Msg TextView Object
    private TextView errorMsg;
    // Email Edit View Object
    private EditText emailET;
    // Passwprd Edit View Object
    private EditText pwdET;

    private Button loginBtn;

    private CheckBox stayLoggedIn;

    private Spinner spEnvironments;

    private ArrayAdapter adapterEnvItems;
    private SharedPreferences sharedPrefs;

    private ProgressDialog prgDialog;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        sharedPrefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);

        // Find Error Msg Text View control by ID
        errorMsg = (TextView)findViewById(R.id.login_error);

        // Find Email Edit View control by ID
        emailET = (EditText)findViewById(R.id.loginEmail);
        emailET.setText(Authentication.INSTANCE.getRememberedUserName());

        // Find Password Edit View control by ID
        pwdET = (EditText)findViewById(R.id.loginPassword);

        stayLoggedIn = (CheckBox) findViewById(R.id.cb_stayLoggedIn);

        // Initialize environment spinner
        spEnvironments = (Spinner) findViewById(R.id.spEnvironments);
        spEnvironments.setOnItemSelectedListener(this);

        adapterEnvItems = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.env_choices));
        adapterEnvItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEnvironments.setAdapter(adapterEnvItems);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        mContext = this;

        loginBtn = (Button)  findViewById(R.id.btnLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                attemptLogin();
            }
        });

        Common.ensureContextForStaticManagers(this.getApplicationContext());

        if (userAlreadyLoggedAndStaysLogged()) {
            navigateToHomeActivity();
        }

    }

    private boolean userAlreadyLoggedAndStaysLogged() {

        if (Authentication.INSTANCE.getToken() != null && Authentication.INSTANCE.isRememberLoggedIn()) {
            return true;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        Authentication.INSTANCE.setConfigKey(item);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        ;
    }

    private TimeoutHandler requestTimeout = new TimeoutHandler() {

        @Override
        public void onTimeoutRestored()
        {
            prgDialog.cancel();
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

    private WebServiceCallHandler loginCallHandler = new WebServiceCallHandler(this) {

        @Override
        public void showWebServiceProgress()
        {
            LoginActivity.this.showWebServiceProgress(getString(R.string.progress_signing_in));
        }

        @Override
        public void onResponseSuccessful(WebServiceResponse response)
        {
            clear();
            LoginActivity.this.onWsAuthSuccessful(response, this.getWebService());
        }

        @Override
        public Context getContext()
        {
            return LoginActivity.this;
        }
    };

    private WebServiceCallHandler userInfoCallHandler = new WebServiceCallHandler(this) {

        @Override
        public void showWebServiceProgress()
        {
            LoginActivity.this.showWebServiceProgress(getString(R.string.progress_user_info));
        }

        @Override
        public void onResponseSuccessful(WebServiceResponse response)
        {
            clear();
            LoginActivity.this.onWsUserInfoSuccessful(response, this.getWebService());
        }

        @Override
        public Context getContext()
        {
            return LoginActivity.this;
        }
    };

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin()
    {

        // Reset errors.
        emailET.setError(null);
        pwdET.setError(null);

        // Store values at the time of the login attempt.
        String mUserId = emailET.getText().toString();
        String mPassword = pwdET.getText().toString();

        // smoke test - if all credentials are filled
        if ((mUserId.length() == 0) || (mPassword.length() == 0)) {

            showCredentialsError();

        } else {

            this.setResult(CallResult.OK);
            requestTimeout.startCountingFrom(Constants.COMMON_REQUEST_TIMEOUT);

            if (loginCallHandler.isTaskInProgress()) {
                return;
            }

            String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
            final String enpointUrl = serverUrl + RestUrls.REST_SERVICE_LOGIN.toString();

            if (enpointUrl == null) {
                //GuiHelper.showFatalConfigError(this, "Login");
                errorMsg.setText(getString(R.string.error_login_url));

            } else {

                try {

                    loginWS = new LoginWebService(this);
                    loginWS.setParam("username", mUserId);
                    loginWS.setParam("password", mPassword);

                    loginWS.doRequest(loginCallHandler);

                } catch (SystemException e) {

                    prgDialog.cancel();
                    errorMsg.setText(e.getMessage());
                    //GuiHelper.showFatalError(this);
                    Log.e(TAG, "Cannot build proper request", e);
                }
            }

        }
    }

    public void attemptGetUserInfo()
    {

        this.setResult(CallResult.OK);
        requestTimeout.startCountingFrom(Constants.COMMON_REQUEST_TIMEOUT);

        if (loginCallHandler.isTaskInProgress()) {
            return;
        }

        if (userInfoCallHandler.isTaskInProgress()) {
            return;
        }

        String serverUrl = ConfigProvider.INSTANCE.getConfig(Authentication.INSTANCE.getConfigKey());
        final String enpointUrl = serverUrl + RestUrls.REST_SERVICE_USERINFO.toString();

        if (enpointUrl == null) {
            errorMsg.setText(getString(R.string.error_userinfo_url));

        } else {

            try {

                useInfoWS = new UserInfoWebService();
                useInfoWS.doRequest(userInfoCallHandler);

            } catch (SystemException e) {

                prgDialog.cancel();
                errorMsg.setText(e.getMessage());
                Log.e(TAG, "Cannot build proper request", e);

            }
        }
    }

    private void cancelHandlers()
    {
        if (loginCallHandler.isTaskInProgress()) {
            loginCallHandler.cancel(false);
        }
        if (userInfoCallHandler.isTaskInProgress()) {
            userInfoCallHandler.cancel(false);
        }
    }

    private void onRequestCancel()
    {
        loginCallHandler.clear();
        userInfoCallHandler.clear();
    }

    public void onWsAuthSuccessful(WebServiceResponse response, GenericWebService webService)
    {
        Authentication.INSTANCE.setRememberedUserName(emailET.getText().toString());
        Authentication.INSTANCE.setRememberLogin(stayLoggedIn.isChecked());

        webService.interpretResponse(response);
        attemptGetUserInfo();
    }

    public void onWsUserInfoSuccessful(WebServiceResponse response, GenericWebService webService)
    {
        webService.interpretResponse(response);
        //postLoginChecks.initOperations();
        //postLoginChecks.performNext();

        navigateToHomeActivity();
    }

    @Override
    public void onError(WebServiceResponse response)
    {
        if (this == null) {
            return;
        }

        this.setResult(CallResult.FAILED);

        if (response.getError().getType() == ErrorType.USER_CANCELED) {
            onRequestCancel();
        } else {
            showFatalError(response);
        }

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
    public void onResponseSuccessful(WebServiceResponse response)
    {
        finish();
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigateToHomeActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void showCredentialsError()
    {
        Authentication.INSTANCE.setRememberedUserName(null);
        emailET.setError(getString(R.string.error_incorrect_password));
        emailET.setText("");
        pwdET.setError(getString(R.string.error_incorrect_password));
        pwdET.setText("");
        errorMsg.setText(getString(R.string.error_auth_wrong_credentials_msg));
    }

    public void showFatalError(WebServiceResponse response) {
        DialogHelper.createFatalErrorDialog(this, response.getError().getDisplayMessage()).show();
    }

    @Override
    public void onResponseUnauthorized(WebServiceResponse response)
    {
        String errorCode = response.getError().getCode();

        if (errorCode.equals("invalid_grant") || errorCode.equals("unauthorized")) {
            //AnalyticsHelper.INSTANCE.logEvent("InvalidCredentials");
            showCredentialsError();
        } else {
            showFatalError(response);
        }
    }

    /**
     * Method gets triggered when Register button is clicked
     *
     * @param view
     */
    public void navigateToRegisterActivity(View view){
        Intent loginIntent = new Intent(getApplicationContext(),RegisterActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    /**
     *
     * @param view
     */
    public void onConfigSetClick(View view) {
        ;
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
    protected void onResume()
    {
        requestTimeout.resume();
        //fillFormIfCan();
        //PlayServicesHelper.INSTANCE.reset();

        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        requestTimeout.store(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause()
    {
        requestTimeout.pause();
        super.onPause();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

}
