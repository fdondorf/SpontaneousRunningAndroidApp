package org.spontaneous.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import org.spontaneous.R;
import org.spontaneous.activities.adapter.DrawerListAdapter;
import org.spontaneous.activities.util.CustomExceptionHandler;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.NavItem;
import org.spontaneous.activities.util.TimeoutHandler;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.Common;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.crossdomain.Authentication;
import org.spontaneous.core.crossdomain.UserInfo;
import org.spontaneous.core.dao.UserDAO;
import org.spontaneous.core.impl.LogoutWebService;
import org.spontaneous.fragment.LogoutFragment;
import org.spontaneous.fragment.MyActivitiesFragment;
import org.spontaneous.fragment.MyActivitiesRESTFragment;
import org.spontaneous.fragment.NavigationDrawerFragment;
import org.spontaneous.fragment.StartFragmentMap;
import org.spontaneous.utility.Constants;

import java.util.ArrayList;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private static String TAG = MainActivity.class.toString();

	private static final int ITEM_ID_START = 1;
	private static final int ITEM_ID_ACTIVITIES_LOCAL = 2;
	private static final int ITEM_ID_ACTIVITIES = 3;
	private static final int ITEM_ID_LOGOUT = 4;

	private LogoutWebService logoutWS;

	private SharedPreferences sharedPrefs;
	private Context mContext;
	
	private int backPressedCounter = 0;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private ListView mDrawerList;
	private RelativeLayout mDrawerPane;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	
	private TextView mUsername;
	private TextView mLastname;
	
	private ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

	// Progress Dialog Object
	private ProgressDialog prgDialog;

	private Toolbar toolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this;

		registerExceptionHandler();

		Common.ensureContextForStaticManagers(this.getApplicationContext());

		mUsername = (TextView) findViewById(R.id.userName);
		sharedPrefs = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
		if (mUsername != null) {
			String firstname = "Maria";
			if (UserInfo.INSTANCE.getUserInfo() != null) {
				firstname = UserInfo.INSTANCE.getUserInfo().getFirstName();//sharedPrefs.getString(Constants.PREF_FIRSTNAME, "unknown");
			}
			mUsername.setText("Hallo " + firstname + "!");
		}

	    mNavItems.add(new NavItem(ITEM_ID_START, getResources().getString(R.string.title_startMap),
	    		"Starte eine Aktivität", R.drawable.ic_activity, true));
		if (Authentication.INSTANCE.isAdmin()) {
			mNavItems.add(new NavItem(ITEM_ID_ACTIVITIES_LOCAL, getResources().getString(R.string.title_myActivitiesList),
					"Lokale Aktivitäten", R.drawable.ic_list, Authentication.INSTANCE.isAdmin()));
		}
		mNavItems.add(new NavItem(ITEM_ID_ACTIVITIES, getResources().getString(R.string.title_myActivitiesList),
				"Das habe ich erreicht", R.drawable.ic_list, true));
	    mNavItems.add(new NavItem(ITEM_ID_LOGOUT, getResources().getString(R.string.title_Logout),
	    		"Ich will hier raus", R.drawable.ic_logout, true));

		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(this);
		// Set Progress Dialog Text
		prgDialog.setMessage("Please wait...");
		// Set Cancelable as False
		prgDialog.setCancelable(false);

	    // DrawerLayout
	    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
	 
	    // Populate the Navigtion Drawer with options
	    mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
	    mDrawerList = (ListView) findViewById(R.id.navList);
	    DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
	    mDrawerList.setAdapter(adapter);

	    // Drawer Item click listeners
	    mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            selectItemFromDrawer(position);
	        }
	    });
	    
	    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
	    		R.string.navigation_drawer_open, 
	    		R.string.navigation_drawer_close) {

			@Override
	        public void onDrawerOpened(View drawerView) {
	            super.onDrawerOpened(drawerView);
	     
	            invalidateOptionsMenu();
	        }
	     
			@Override
	        public void onDrawerClosed(View drawerView) {
	            super.onDrawerClosed(drawerView);
	            Log.d(TAG, "onDrawerClosed: " + getTitle());
	     
	            invalidateOptionsMenu();
	        }
	    };
	     
	    mDrawerLayout.setDrawerListener(mDrawerToggle);

	    // Toolbar		
		toolbar = (Toolbar) findViewById(R.id.tool_bar);
	    toolbar.setTitle(R.string.app_name);
	    toolbar.setSubtitle(R.string.app_name_subtitle);

	    toolbar.inflateMenu(R.menu.toolbar_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.action_music:
                        Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                        startActivity(intent);
                        return true;
                }

                return false;
            }
        });
        
        //Navigation Icon
        toolbar.setNavigationIcon(R.drawable.ic_burger_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Navigation",Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        selectItemFromDrawer(0);
	}

	private void registerExceptionHandler() {
		if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
			Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
					getExternalCacheDir().toString(), null));
		}
	}

	public void onAttemptLogout()
	{
		requestTimeout.startCountingFrom(Constants.COMMON_REQUEST_TIMEOUT);

		if (logoutCallHandler.isTaskInProgress()) {
			return;
		}

		final String enpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
				RestUrls.REST_SERVICE_LOGOUT;//ConfigProvider.INSTANCE.getConfig("logout_endpoint");

		if (enpointUrl == null) {
			DialogHelper.createStandardErrorDialog(this, "Log Out");

		} else {

			try {

				logoutWS = new LogoutWebService();
				logoutWS.doRequest(logoutCallHandler);

			} catch (SystemException e) {

				prgDialog.hide();
				DialogHelper.createFatalErrorDialog(this, "Log Out");//(this, "Log Out");
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
			return getApplicationContext();
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
			navigatetoLoginActivity();
		}
		else {
			navigatetoLoginActivity();
		}
	}
	/**
	 * Method gets triggered when Register button is clicked
	 */
	public void navigatetoLoginActivity() {
		Intent loginIntent = new Intent(this.getApplicationContext(), LoginActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(loginIntent);
		finish();
	}

	/*
	* Called when a particular item from the navigation drawer
	* is selected.
	*/
	private void selectItemFromDrawer(int position) {
	    FragmentManager fragmentManager = getFragmentManager();

		NavItem navItem = mNavItems.get(position);
		switch (navItem.getmId()) {
			case ITEM_ID_START:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								StartFragmentMap.newInstance(position, this)).commit();
				break;

			case ITEM_ID_ACTIVITIES_LOCAL:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								MyActivitiesFragment.newInstance(position, this)).commit();
				break;

			case ITEM_ID_ACTIVITIES:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								MyActivitiesRESTFragment.newInstance(position, this)).commit();
				break;

			case ITEM_ID_LOGOUT:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								LogoutFragment.newInstance(position, this)).commit();
				break;
		};

	    mDrawerList.setItemChecked(position, true);
	    setTitle(mNavItems.get(position).getmTitle());
	 
	    // Close the drawer
	    mDrawerLayout.closeDrawer(mDrawerPane);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();

		NavItem navItem = mNavItems.get(position);
		switch (navItem.getmId()) {
			case ITEM_ID_START:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								StartFragmentMap.newInstance(position, this)).commit();
				break;

			case ITEM_ID_ACTIVITIES_LOCAL:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								MyActivitiesFragment.newInstance(position, this)).commit();
				break;

			case ITEM_ID_ACTIVITIES:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								MyActivitiesRESTFragment.newInstance(position, this)).commit();
				break;

			case ITEM_ID_LOGOUT:
				fragmentManager
						.beginTransaction()
						.replace(R.id.mainContent,
								LogoutFragment.newInstance(position, this)).commit();
				break;
		};
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 0:
			mTitle = getString(R.string.title_start);
			break;
		case 1:
			mTitle = getString(R.string.title_startMap);
			break;
		case 2:
			mTitle = getString(R.string.title_myActivities);
			break;
		case 3:
			mTitle = getString(R.string.title_myActivitiesREST);
			break;
		case 4:
			mTitle = getString(R.string.title_Logout);
			break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		//actionBar.setTitle(mTitle);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			Integer i = getArguments().getInt(ARG_SECTION_NUMBER);
			if (i == 1) {
				View rootView = inflater.inflate(R.layout.fragment_main, container,
						false);
				return rootView;
			}
			else {
				View rootView = inflater.inflate(R.layout.my_activities_rest, container,
						false);
				return rootView;
			}
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}

	@Override
	public void onBackPressed() {
		if (backPressedCounter <= 0) {
			Toast.makeText(this, "Nochmaliges Tippen beendet die App.", Toast.LENGTH_SHORT).show();
			backPressedCounter++;
			Thread timer = new Thread() {  
	            public void run() {  
	                try {  
	                    sleep(1000); // wait 1 second  
	                } catch (InterruptedException e) {  
	                    e.printStackTrace();  
	                } finally {  
	                	backPressedCounter = 0;
	                }  
	            }  
	        };  
	        timer.start();
		}
		else {
			backPressedCounter = 0;
			this.finish();
		}
	}

	public Long getUserId() {

		UserDAO userDAO = UserInfo.INSTANCE.getUserInfo();
		Long userId = userDAO.getUserId();
		return userId;
	}
	
}
