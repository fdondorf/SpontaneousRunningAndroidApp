package org.spontaneous.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.spontaneous.R;
import org.spontaneous.activities.adapter.SplitTimeArrayAdapter;
import org.spontaneous.activities.model.GeoPoint;
import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SplitTimeModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.CustomExceptionHandler;
import org.spontaneous.activities.util.DateUtil;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.StringUtil;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.TrackingUtil;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.trackservice.IRemoteService;
import org.spontaneous.trackservice.IRemoteServiceCallback;
import org.spontaneous.trackservice.RemoteService;
import org.spontaneous.trackservice.WayPointModel;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import java.util.ArrayList;
import java.util.List;

public class CurrentActivityActivity extends Activity implements OnMapReadyCallback {

	private static final String TAG = "CurrentActivityActivity";

	private static final Float KILOMETER = 1000f;

	private static final int RESULT_ACTIVITY_SAVED = 1;
	private static final int RESULT_ACTIVITY_RESUMED = 2;
	private static final int RESULT_DELETED = 3;

	private SharedPreferences sharedPrefs;
	private static final String PREFERENCES = "PREFS";
	private static final String STOPTIME = "TIMEWHENSTOPPED";
	
	private ITrackingService trackingService = TrackingServiceImpl.getInstance(this);

	private Context mContext;

    /** The primary interface we will be calling on the service. */
    IRemoteService mService = null;

    private int mLoggingState = TrackingServiceConstants.UNKNOWN;

    private List<GeoPointModel> geoPoints = null;

	// GUI
    private GoogleMap map = null;
    
	private Button mStopButton;
    private Button mPauseButton;
    private Button mResumeButton;
    private ViewFlipper viewFlipper;
	private TextView latituteField;
	private TextView longitudeField;
	private TextView distanceField;
	private TextView speedField;
	private TextView mCaloriesField;
	private Chronometer mChronometer;
    private TextView mCallbackText;
    private ListView listView;
    private TextView mCurrentTimePerUnit;
    
    private boolean mIsBound;

    private float lastX;
    
	// Data
	private WayPointModel mTrackData = new WayPointModel();
	private Location mStartLocation;

	// Helper
	private long timeWhenStopped = 0;


	/** Called when the activity is first created. */
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.layout_current_activity);
	    
	    mContext = this;

	    sharedPrefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
	    
	    registerExceptionHandler();
	    
	    // Read start location
	    Bundle data = getIntent().getExtras();
	    if (data != null) {
	    	//Long tId = data.getLong(TrackingServiceConstants.TRACK_ID);
	    	mStartLocation = (Location) data.getParcelable(TrackingServiceConstants.START_LOCATION);
	    }

	    // Map
	    MapFragment mapFragment = (MapFragment) getFragmentManager()
				  .findFragmentById(R.id.map);
	    mapFragment.getMapAsync(this);

	    viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
	    
	    latituteField = (TextView) findViewById(R.id.latitudeField);
	    longitudeField = (TextView) findViewById(R.id.longitudeField);
	    distanceField = (TextView) findViewById(R.id.distanceText);
	    distanceField.setText("0.0");
	    speedField = (TextView) findViewById(R.id.speedText);
	    speedField.setText("0");
	    mCaloriesField = (TextView) findViewById(R.id.caloriesValue);

	    mChronometer = (Chronometer) findViewById(R.id.chronometer);

	    mTrackData.setStartTime(System.currentTimeMillis());

	    mCallbackText = (TextView)findViewById(R.id.callback);
	    mCallbackText.setText("Not attached.");

	    mStopButton = (Button)findViewById(R.id.btn_stop);
	    mStopButton.setOnClickListener(mStopListener);

	    mResumeButton = (Button) findViewById(R.id.btn_resume);
	    mPauseButton = (Button) findViewById(R.id.btn_pause);

	    mPauseButton.setVisibility(View.VISIBLE);
	    mPauseButton.setOnClickListener(mPauseListener);

	    mResumeButton.setVisibility(View.GONE);
	    mResumeButton.setOnClickListener(mResumeListener);

	    // Aktueller Schnitt
	    mCurrentTimePerUnit = (TextView) findViewById(R.id.currentAveragePerUnit);
	    mCurrentTimePerUnit.setText("00:00");
	    
	    // Splittimes
	    List<SplitTimeModel> splitTimes = new ArrayList<SplitTimeModel>();
		listView = (ListView) findViewById(R.id.splitTimes);
		listView.setAdapter(new SplitTimeArrayAdapter(this, splitTimes));
	    
	    // Starte und Binde den Background-Logging-Service
	    startAndBindService();

	    // Starte Chronometer
	    mChronometer.start();
	    
	    computeAverageSpeed();
	    computeCaloriesValue();
	  }

	public void onMapReady(GoogleMap m) {

		map = m;

		//DO WHATEVER YOU WANT WITH GOOGLEMAP
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		//map.setMyLocationEnabled(true);
		map.setTrafficEnabled(true);
		map.setIndoorEnabled(true);
		map.setBuildingsEnabled(true);
		map.getUiSettings().setZoomControlsEnabled(true);

		if (mStartLocation != null) {
			LatLng start = new LatLng(mStartLocation.getLatitude(), mStartLocation.getLongitude());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
		}
	}

	  public boolean onTouchEvent(MotionEvent touchevent) {
	 
		  switch (touchevent.getAction()) {

	  	  	case MotionEvent.ACTION_DOWN: 
	  	            lastX = touchevent.getX();
	  	            break;
	  
	  	        case MotionEvent.ACTION_UP: 
	  	            float currentX = touchevent.getX();

	  	            // Handling left to right screen swap.
	  	            if (lastX < currentX) {
	  	            	
	  	                // If there aren't any other children, just break.
	  	                if (viewFlipper.getDisplayedChild() == 0)
	  	                    break;

	  	                // Next screen comes in from left.
	  	                viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);

	  	                // Current screen goes out from right. 
	  	                viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);
	  
	  	                // Display next screen.
	  	                viewFlipper.showNext();
	  	            }
	  	            // Handling right to left screen swap.
	  	             if (lastX > currentX) {
	  	            	 
	  	                 // If there is a child (to the left), kust break.
	  	                 if (viewFlipper.getDisplayedChild() == 1)
	  	                     break;

	  	                 // Next screen comes in from right.
	  	                 viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);

	  	                // Current screen goes out from left. 
	  	                 viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);

	  	                // Display previous screen.
	  	                 viewFlipper.showPrevious();
	  	             }
	  	             break;
	  	        }
	  	         return false;
	  }
	  
	  private void startAndBindService() {
		  // Make sure the service is started. It will continue running
		  // until someone calls stopService().
		  // We use an action code here, instead of explictly supplying
		  // the component name, so that other packages can replace
		  // the service.
		  //startService(new Intent("com.example.remoteserviceexample.REMOTE_SERVICE"));
		  Intent service = null;
		  try {
			  Log.i(TAG, Class.forName(RemoteService.class.getName()).toString());
			  service = new Intent(mContext,  Class.forName(RemoteService.class.getName()));
			  //Log.i(TAG, service.getAction());
		  } catch (ClassNotFoundException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  startService(service);

		  // Bind Service
		  bindService(service);
	  }

	  private void pauseLogging () {
		  try {
				mService.pauseLogging();
			} catch (RemoteException e) {
				e.printStackTrace();
				DialogHelper.createStandardErrorDialog(this);
			}
	  }

	  private void resumeLogging (long trackId) {
		  try {
				mService.resumeLogging(trackId);
			} catch (RemoteException e) {
				e.printStackTrace();
				DialogHelper.createStandardErrorDialog(this);
			}
	  }

	  private void stopLogging () {
		  try {
				mService.stopLogging();
			} catch (RemoteException e) {
				e.printStackTrace();
				DialogHelper.createStandardErrorDialog(this);
			}
	  }
	  
	  private void stopAndUnbindService() {
		  
		  stopLogging();

		  // Unbind service
		  unbindService();

		  // Cancel a previous call to startService(). Note that the
		  // service will not actually stop at this point if there are
		  // still bound clients.
		  Intent service = null;
		  try {
				service = new Intent(mContext,  Class.forName(RemoteService.class.getName()));
		  } catch (ClassNotFoundException e) {
			  	// TODO Auto-generated catch block
				e.printStackTrace();
		  }
		  stopService(service);

		  this.mService = null;
		  this.mIsBound = false;
	  }

	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	      // Inflate the menu items for use in the action bar
	      MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.current_activity, menu);
	      return super.onCreateOptionsMenu(menu);
	  }

	  @Override
	  protected void onStart() {
		  super.onStart();
	  }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the current tracking state
		if (mTrackData != null) {
			if (mTrackData.getTrackId() != null)
				savedInstanceState.putLong(TrackingServiceConstants.TRACK_ID, mTrackData.getTrackId());
			if (mTrackData.getSegmentId() != null)
				savedInstanceState.putLong(TrackingServiceConstants.SEGMENT_ID, mTrackData.getSegmentId());
		}
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}



	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long trackId = savedInstanceState.getLong(TrackingServiceConstants.TRACK_ID, -1);
		if (trackId > 0) {
			mTrackData.setTrackId(trackId);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	/**
	   *
	   * Die onResume-Methode wird aufgerufen wowohl wenn aus der ActivitySummaryActivity via
	   * Back-Button zurückgekehrt wird als auch wenn die App lange inaktiv war un reaktiviert wird.
	   * Im ersten Fall ist der Remote-Tracking-Service gestoppt und muss neu gestartet werden, im
	   * zweiten Fall ist der Service bereits gestartet und darf nicht neu gestartet werden.
	   */
	  @Override
	  protected void onResume() {
	    super.onResume();

	    // Starte und Binde den Background-Logging-Service
	    if (mService == null && !mIsBound) {
	    	startAndBindService();
	    	setButtonState(View.VISIBLE, View.GONE);
			setChronometerState(true);
			computeAverageSpeed();
	    	computeCaloriesValue();
	    }
	    else if (!mIsBound) {

	    	Intent service = null;
			  try {
				  service = new Intent(mContext,  Class.forName(RemoteService.class.getName()));
			  } catch (ClassNotFoundException e) {
				  e.printStackTrace();
				  DialogHelper.createStandardErrorDialog(this, e.getMessage());
			  }
	    	bindService(service);
	    }

	    try {
	    	
		    int logState = 0;
		    if (mService != null)
		    	logState = mService.loggingState();		    
	    	
			if (mService != null && mService.loggingState() == TrackingServiceConstants.STOPPED) {
				mTrackData.setSegmentId(mService.resumeLogging(mTrackData.getTrackId().longValue()));
				setButtonState(View.VISIBLE, View.GONE);
			}
			else if (mService != null && mService.loggingState() == TrackingServiceConstants.PAUSED) {
				setButtonState(View.GONE, View.VISIBLE);
			}
			else if (mService != null && mService.loggingState() == TrackingServiceConstants.LOGGING) {
				setButtonState(View.VISIBLE, View.GONE);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			DialogHelper.createStandardErrorDialog(this, e.getMessage());
		}
	}

	private void setChronometerState(boolean start) {

		TrackModel trackModel = trackingService.readTrackById(mTrackData.getTrackId());
		mTrackData.setTotalTime(trackModel.getTotalDuration());

		// Update Chronometer
		if (!start) {
	    	timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
	    	SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putLong(STOPTIME, timeWhenStopped);
	        editor.commit();
	    	mChronometer.stop();
		}
		
		if (start) {
			SharedPreferences sharedPrefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    		long stopTime = sharedPrefs.getLong(STOPTIME, 0);
    		mChronometer.setBase(SystemClock.elapsedRealtime() + stopTime);
    		mChronometer.start();
		}
	}


	private void setButtonState(int pauseBtnState, int resumeBtnState) {
		mPauseButton.setVisibility(pauseBtnState);
		mResumeButton.setVisibility(resumeBtnState);
	}

	@Override
	  protected void onPause() {
	    super.onPause();
	    SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putLong(STOPTIME, timeWhenStopped);
        editor.commit();
	  }

	  @Override
	  public void onBackPressed() {
		// Do nothing to disable back-button
	  }

	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		  super.onActivityResult(requestCode, resultCode, intent);

		  if (resultCode == RESULT_DELETED) {
			  finish();
		  }
		  else if (resultCode == RESULT_ACTIVITY_SAVED){
			  finish();
		  }
		  else if (resultCode == RESULT_ACTIVITY_RESUMED){
			  mTrackData.setTrackId(intent.getLongExtra(TrackingServiceConstants.TRACK_ID, -1));
			  System.out.println(mTrackData.getTrackId());
		  }
	  }

	  @Override
	  protected void onStop() {
		  super.onStop();
	  }

	  /**
	   * Class for interacting with the main interface of the service.
	   */
	  private ServiceConnection mConnection = new ServiceConnection() {

		    public void onServiceConnected(ComponentName className,
		    		IBinder service) {

			    // This is called when the connection with the service has been
			    // established, giving us the service object we can use to
			    // interact with the service. We are communicating with our
			    // service through an IDL interface, so get a client-side
			    // representation of that from the raw service object.
			    mService = IRemoteService.Stub.asInterface(service);
			    mCallbackText.setText("Attached");

			    // We want to monitor the service for as long as we are
			    // connected to it.
			    try {

			    	if (mLoggingState == TrackingServiceConstants.STOPPED) {
			    		mService.registerCallback(mCallback);
				    	mTrackData.setSegmentId(
				    			mService.resumeLogging(mTrackData.getTrackId().longValue()));
			    	}
			    	else {
				    	mService.registerCallback(mCallback);
				    	mTrackData.setTrackId(
				    			mService.startLogging(mStartLocation));
			    	}
			    	mLoggingState = mService.loggingState();
			    } catch (RemoteException e) {
				    // In this case the service has crashed before we could even
				    // do anything with it; we can count on soon being
				    // disconnected (and then reconnected if it can be restarted)
				    // so there is no need to do anything here.
			    }

			    // As part of the sample, tell the user what happened.
			    Toast.makeText(CurrentActivityActivity.this, R.string.remote_service_connected,
			    Toast.LENGTH_SHORT).show();
		    }

		    public void onServiceDisconnected(ComponentName className) {

			    // This is called when the connection with the service has been
			    // unexpectedly disconnected -- that is, its process crashed.
			    mService = null;
			    mCallbackText.setText("Disconnected.");
			    latituteField.setText("...");
			    longitudeField.setText("...");
			    distanceField.setText("...");

			    // As part of the sample, tell the user what happened.
			    Toast.makeText(CurrentActivityActivity.this, R.string.remote_service_disconnected,
			    Toast.LENGTH_SHORT).show();
		    }
	    };

	/****************************************
	 * Listener
	 * *************************************/

    private OnClickListener mPauseListener = new OnClickListener() {
	    public void onClick(View v) {

	    	// Pasue logging
	    	pauseLogging();

	    	// Button Dynamic
	    	setButtonState(View.GONE, View.VISIBLE);

	    	// Update Chronometer
	    	setChronometerState(false);

	    }
    };

    private OnClickListener mResumeListener = new OnClickListener() {
	    public void onClick(View v) {

	    	// Button Dynamic
	    	setButtonState(View.VISIBLE, View.GONE);

	    	resumeLogging(mTrackData.getTrackId().longValue());

	    	// Update Chronometer
	    	setChronometerState(true);
	    	mTrackData.setTotalTime(mChronometer.getBase() - SystemClock.elapsedRealtime());
	    	
	    	computeAverageSpeed();
	    	computeCaloriesValue();
	    }
    };

    private OnClickListener mStopListener = new OnClickListener() {
	    public void onClick(View v) {

	    	setChronometerState(false);
	    	mTrackData.setTotalTime(mChronometer.getBase() - SystemClock.elapsedRealtime());

        	stopAndUnbindService();

        	mLoggingState = TrackingServiceConstants.STOPPED;

	    	Intent intent = new Intent();
	    	intent.setClass(mContext, ActivitySummaryActivity.class);
	    	intent.putExtra(TrackingServiceConstants.TRACK_ID, mTrackData.getTrackId());
	    	intent.putExtra(TrackingServiceConstants.REQUEST_CODE, 2);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivityForResult(intent, RESULT_ACTIVITY_RESUMED);
	    }
    };


    /******************************************
     *  Private Helper
     * ****************************************/

    private void bindService(Intent service) {
	    bindService(service, mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	    mCallbackText.setText("Binding...");
    }

    private void unbindService() {

    	if (mIsBound) {
		    // If we have received the service, and hence registered with
		    // it, then now is the time to unregister.
		    if (mService != null) {
			    try {
			    	mService.unregisterCallback(mCallback);
			    } catch (RemoteException e) {
				    // There is nothing special we need to do if the service
				    // has crashed.
			    }
		    }

		    // Detach our existing connection.
		    unbindService(mConnection);

		    //mKillButton.setEnabled(false);
		    mIsBound = false;
		    mCallbackText.setText("Unbinding.");
	    }
    }

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------
    /**
    * This implementation is used to receive callbacks from the remote
    * service.
    */
    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {

    	/**
	    * This is called by the remote service regularly to tell us about
	    * new values. Note that IPC calls are dispatched through a thread
	    * pool running in each process, so the code executing here will
	    * NOT be running in our main thread like most other things -- so,
	    * to update the UI, we need to use a Handler to hop over there.
	    */
	    public void valueChanged(int value) {
		    mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
		}

		@Override
		public void locationChanged(WayPointModel wayPointModel) throws RemoteException {
			Message msg = mHandler.obtainMessage(LOCATION_MSG);

			Bundle data = new Bundle();
			data.putParcelable(TrackingServiceConstants.LOCATION, wayPointModel);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	};

    private static final int BUMP_MSG = 1;
    private static final int LOCATION_MSG = 2;

    private Handler mHandler = new Handler() {

    	@Override
	    public void handleMessage(Message msg) {
		    switch (msg.what) {
			    case BUMP_MSG:
				    mCallbackText.setText("Received from service: " + msg.arg1);
				    break;
			    case LOCATION_MSG:
			    	WayPointModel wayPointModel = (WayPointModel) msg.getData().getParcelable(TrackingServiceConstants.LOCATION);
			    	mTrackData.setTrackId(wayPointModel.getTrackId());
			    	mTrackData.setSegmentId(wayPointModel.getSegmentId());
			    	mTrackData.setWayPointId(wayPointModel.getWayPointId());
			    	mTrackData.setGeopoint(wayPointModel.getGeopoint());
			    	mTrackData.setTotalDistance(wayPointModel.getTotalDistance());
			    	mTrackData.setTotalTime(wayPointModel.getTotalTime());
			    	
			    	GeoPoint lastGeoPoint = wayPointModel.getGeopoint();
				    latituteField.setText(String.valueOf(lastGeoPoint.getLatitude()));
				    longitudeField.setText(String.valueOf(lastGeoPoint.getLongitude()));
				    distanceField.setText(StringUtil.getDistanceStringWithoutUnit(wayPointModel.getTotalDistance()));
				    speedField.setText(StringUtil.getSpeedStringWithoutUnit(lastGeoPoint.getSpeed()));
				    
				    // 1. Erstelle Linie
				    geoPoints = trackingService.getGeoPointsByTrack(wayPointModel.getTrackId());
				    TrackingUtil.drawCurrentTrackOnMap(mContext, geoPoints, map, lastGeoPoint);
				    
				    // 2. Berechne aktuelle Durchschnittszeiten
				    TrackModel trackModel = trackingService.readTrackById(wayPointModel.getTrackId());
					List<GeoPointModel> geoPoints = trackingService.getGeoPointsByTrack(wayPointModel.getTrackId());
					List<SplitTimeModel> splitTimes = TrackingUtil.computeAverageSpeedPerUnit(trackModel, geoPoints, KILOMETER);
					listView = (ListView) findViewById(R.id.splitTimes);
					listView.setAdapter(new SplitTimeArrayAdapter(mContext, splitTimes));
				    
				    break;
			    default:
			    	super.handleMessage(msg);
		    }
	    }
    };

    /*************************************
     *  Threads for UI
     *  **********************************
     */
    
    private void computeAverageSpeed() {

	    Thread timer = new Thread() { //new thread    
	    	
	    	Long averageTimePerUnit = 0L;	
	    	Long totalDuration = 0L;
	    	
	        public void run() {
	            try {
	                do {
	                    sleep(1000);

	                    runOnUiThread(new Runnable() {  
	                    	
	                    @Override
	                    public void run() {
	                    	
	                    	totalDuration = SystemClock.elapsedRealtime() - mChronometer.getBase();
	                    	averageTimePerUnit =
	                    			TrackingUtil.computeAverageTimePerKilometer(totalDuration, 
	                    					mTrackData.getTotalDistance());
	                    	mCurrentTimePerUnit.setText(DateUtil.millisToShortDHMS(averageTimePerUnit));
	                    }
	                });

	                }

	                while (mService != null && mService.loggingState() == TrackingServiceConstants.LOGGING);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            } catch (RemoteException re) {
	                re.printStackTrace();
	            }
	            finally {
	            }
	        };
	    };
	    timer.start();
	}
    
    private void computeCaloriesValue() {

	    Thread timer = new Thread() { //new thread    
	
	    Long caloriesValue = 0L;
	    Integer weight = 75;
	    	
	        public void run() {
	            try {
	                do {
	                    sleep(1000);

	                    runOnUiThread(new Runnable() {  
	                    	
	                    @Override
	                    public void run() {
	                    	
	                    	caloriesValue =
	                    			TrackingUtil.computeCalories(weight, 
	                    					mTrackData.getTotalDistance());
	                    	mCaloriesField.setText(String.valueOf(caloriesValue));
	                    }
	                });

	                }

	                while (mService != null && mService.loggingState() == TrackingServiceConstants.LOGGING);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            } catch (RemoteException re) {
	                re.printStackTrace();
	            }
	            finally {
	            }
	        };
	    };
	    timer.start();
	}
	private void registerExceptionHandler() {
		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
					getExternalCacheDir().toString(), null));
		}
	}
}