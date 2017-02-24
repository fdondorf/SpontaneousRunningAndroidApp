package org.spontaneous.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.spontaneous.R;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.core.impl.LogoutWebService;
import org.spontaneous.utility.Constants;

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

		logout();

		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    ((MainActivity) activity).onSectionAttached(1);
	}

	private void logout() {

		// 1. Delete SharedPrefs
		SharedPreferences sharedPrefs = getActivity().getSharedPreferences(Constants.PREFERENCES, getActivity().MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.clear();
        editor.commit();

		// 2. Call Logout-Service
		((MainActivity)getActivity()).onAttemptLogout();

	}
}
