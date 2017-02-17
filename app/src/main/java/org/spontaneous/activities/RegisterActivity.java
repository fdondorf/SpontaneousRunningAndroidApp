package org.spontaneous.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.spontaneous.R;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.TimeoutHandler;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.Common;
import org.spontaneous.core.common.GenericWebService;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceHandler;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;
import org.spontaneous.core.impl.RegisterWebService;
import org.spontaneous.utility.Constants;
import org.spontaneous.utility.Utility;
/**
 *
 * Register Activity Class
 */
public class RegisterActivity extends Activity implements WebServiceHandler {

    private static final String TAG = RegisterActivity.class.toString();

    private RegisterWebService registerWS;

    private ProgressDialog prgDialog;
    
    private TextView errorMsg;

    private EditText firstnameET;
    private EditText lastnameET;
    private EditText emailET;
    private EditText pwdET;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        errorMsg = (TextView) findViewById(R.id.register_error);
        
        firstnameET = (EditText) findViewById(R.id.registerFirstname);
        lastnameET = (EditText) findViewById(R.id.registerLastname);
        
        emailET = (EditText) findViewById(R.id.registerEmail);

        pwdET = (EditText) findViewById(R.id.registerPassword);
        
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);
    }

    private TimeoutHandler requestTimeout = new TimeoutHandler() {

        @Override
        public void onTimeoutRestored()
        {
            prgDialog.show();//.showProgress(true);
        }

        @Override
        public void onTimedOut()
        {
            prgDialog.cancel();//.showProgress(false);
            onError(new WebServiceResponse.Builder().fail(SystemError.TIMEOUT).build());
            cancelHandlers();
        }

        @Override
        public void onTimeoutCanceled()
        {
            prgDialog.cancel();
            //progressOverlay.showProgress(false);
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
    private WebServiceCallHandler registerCallHandler = new WebServiceCallHandler(this) {

        @Override
        public void showWebServiceProgress()
        {
            RegisterActivity.this.showWebServiceProgress(getString(R.string.progress_register));
        }

        @Override
        public void onResponseSuccessful(WebServiceResponse response)
        {
            clear();
            RegisterActivity.this.onRegisterSuccessful(response, this.getWebService());
        }

        @Override
        public Context getContext()
        {
            return RegisterActivity.this;
        }
    };


    public void attemptRegister(View view) {

        String firstname = firstnameET.getText().toString();
        String lastname = lastnameET.getText().toString();

        String email = emailET.getText().toString();
        String password = pwdET.getText().toString();

        if (!Utility.isNotNull(firstname) || !Utility.isNotNull(lastname) ||
                !Utility.isNotNull(email) || !Utility.isNotNull(password)) {
            errorMsg.setText(getString(R.string.error_register_empty_fields));
        }
        else if (!Utility.validate(email)) {
            errorMsg.setText(getString(R.string.error_register_invalid_email));
        }
        else {
            this.setResult(Common.CallResult.OK);
            requestTimeout.startCountingFrom(Constants.COMMON_REQUEST_TIMEOUT);

            if (registerCallHandler.isTaskInProgress()) {
                return;
            }

            final String enpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
                    RestUrls.REST_SERVICE_LOGIN.toString();

            if (enpointUrl == null) {

                //GuiHelper.showFatalConfigError(this, "Login");
                errorMsg.setText(getString(R.string.error_login_url));

            } else {

                try {

                    registerWS = new RegisterWebService();
                    registerWS.setParam("firstname", firstname);
                    registerWS.setParam("lastname", lastname);
                    registerWS.setParam("email", email);
                    registerWS.setParam("password", password);

                    registerWS.doRequest(registerCallHandler);

                } catch (SystemException e) {

                    prgDialog.cancel();
                    //progressOverlay.showProgress(false);
                    errorMsg.setText(e.getMessage());
                    //GuiHelper.showFatalError(this);
                    Log.e(TAG, "Cannot build proper request", e);
                }
            }
        };

    }

    private void cancelHandlers()
    {
        if (registerCallHandler.isTaskInProgress()) {
            registerCallHandler.cancel(false);
        }
    }

    private void onRequestCancel()
    {
        registerCallHandler.clear();
    }

    public void onRegisterSuccessful(WebServiceResponse response, GenericWebService webService)
    {
        //Authentication.INSTANCE.setRememberedUserName(emailET.getText().toString());
        webService.interpretResponse(response);
        DialogHelper.createWarnDialog(this, "Registrierung erfolgreich! Bitte zur Anmeldung wechseln").show();
    }

    @Override
    public void showWebServiceProgress()
    {
        prgDialog.show();
        //progressOverlay.showProgress(true);
    }

    public void showWebServiceProgress(String customLabel)
    {
        prgDialog.setMessage(customLabel);
        prgDialog.show();
        //progressOverlay.showProgress(customLabel);
    }

    @Override
    public void hideWebServiceProgress()
    {
        prgDialog.cancel();
        //progressOverlay.showProgress(false);
    }

    @Override
    public void onResponseUnauthorized(WebServiceResponse response)
    {
        String errorCode = response.getError().getCode();

        //TODO: Register has no access, so this should never happen
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
        //fillFormIfCan();
        //PlayServicesHelper.INSTANCE.reset();
        super.onResume();
    }

    /**
     * Method which navigates from Register Activity to Login Activity
     */
    public void navigatetoLoginActivity(View view){
        Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
        // Clears History of Activity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
 
    /**
     * Set degault values for Edit View controls
     */
    public void setDefaultValues(){
        firstnameET.setText("");
    	lastnameET.setText("");
        emailET.setText("");
        pwdET.setText("");
    }

}
