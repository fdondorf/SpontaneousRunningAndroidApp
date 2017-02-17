package org.spontaneous.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.spontaneous.R;
import org.spontaneous.activities.ActivitySummaryActivity;
import org.spontaneous.activities.MainActivity;
import org.spontaneous.activities.adapter.CustomArrayAdapter;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.util.DialogHelper;
import org.spontaneous.activities.util.TimeoutHandler;
import org.spontaneous.core.ITrackingService;
import org.spontaneous.core.RestUrls;
import org.spontaneous.core.common.Common;
import org.spontaneous.core.common.GenericWebService;
import org.spontaneous.core.common.SystemException;
import org.spontaneous.core.common.WebServiceCallHandler;
import org.spontaneous.core.common.WebServiceHandler;
import org.spontaneous.core.common.WebServiceResponse;
import org.spontaneous.core.common.error.ErrorType;
import org.spontaneous.core.common.error.SystemError;
import org.spontaneous.core.impl.GetTracksWebService;
import org.spontaneous.core.impl.SaveTrackWebService;
import org.spontaneous.core.impl.TrackingServiceImpl;
import org.spontaneous.db.GPSTracking;
import org.spontaneous.trackservice.util.TrackingServiceConstants;

import java.util.ArrayList;
import java.util.List;

public class MyActivitiesRESTFragment extends ListFragment implements WebServiceHandler {

	private static final String TAG = "MyActivitiesFragment";

	private static final int REQUEST_CODE_VALUE = 1;

	// Service for loading track data from local db
	private ITrackingService trackingService = TrackingServiceImpl.getInstance(this.getActivity());

	// Progress Dialog Object
	private ProgressDialog prgDialog;

	private SaveTrackWebService trackWS;
	private GetTracksWebService getTracksWS;


	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	private List<TrackModel> mTracks = null;
	private List<TrackModel> mTracksLocal = null;

	public static MyActivitiesRESTFragment newInstance(int sectionNumber, Activity parent) {

		MyActivitiesRESTFragment fragment = new MyActivitiesRESTFragment();

		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public MyActivitiesRESTFragment() {
		;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.my_activities, container, false);

		setHasOptionsMenu(true);

		// Instantiate Progress Dialog object
		prgDialog = new ProgressDialog(this.getActivity());
		prgDialog.setMessage("Please wait...");
		prgDialog.setCancelable(false);

		getMyActivitiesList();

		attemptGetActivities();

		return rootView;
	}

	private void attemptGetActivities() {

		final String endpointUrl = RestUrls.SERVER_NAME + ":" + RestUrls.PORT +
				RestUrls.REST_SERVICE_TRACKS.toString();

		if (endpointUrl == null) {
			DialogHelper.createFatalErrorDialog(this.getActivity(), "enpointUrl is null, should be given").show();

		} else {

			try {

				getTracksWS = new GetTracksWebService();
				getTracksWS.doRequest(getTracksCallHandler);

			} catch (SystemException e) {
				prgDialog.cancel();
				DialogHelper.createFatalErrorDialog(this.getActivity(), e.getMessage()).show();
				Log.e(TAG, "Cannot build proper request", e);
			}
		}
	}

	private void getMyActivitiesList() {

		// Defines a list of columns to retrieve from the Cursor and load into an output row
		String[] mTrackListColumns = {
				BaseColumns._ID,
				GPSTracking.TracksColumns.NAME,
				GPSTracking.TracksColumns.TOTAL_DISTANCE,
				GPSTracking.TracksColumns.TOTAL_DURATION,
				GPSTracking.TracksColumns.CREATION_TIME,
				GPSTracking.TracksColumns.USER_ID
		};

		String [] selectionArgs = { String.valueOf(((MainActivity)getActivity()).getUserId()) };
		Cursor mTracksCursor = getActivity().getContentResolver().query(GPSTracking.Tracks.CONTENT_URI, mTrackListColumns, null, selectionArgs, GPSTracking.TracksColumns.CREATION_TIME + " DESC");

		//mTracks = trackingService.getAllTracks();
		mTracksLocal = getTrackData(mTracksCursor);
		CustomArrayAdapter adapter = new CustomArrayAdapter(getActivity(), mTracksLocal);
		setListAdapter(adapter);
	}

	private List<TrackModel> getTrackData(Cursor mTracksCursor) {

		List<TrackModel> tracks = new ArrayList<TrackModel>();
		if (mTracksCursor != null) {
			TrackModel trackModel = null;
			while (mTracksCursor.moveToNext()) {

				// TODO: Quickfix wieder entfernen
				Long totalDuration = 0L;
				String value = null;
				if (mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.TOTAL_DURATION)) != null) {
					try {
						totalDuration = Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.TOTAL_DURATION)));
						Log.i(TAG, value);
					} catch (Exception exc) {
						value = String.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.TOTAL_DURATION)));
					}
				}

				trackModel = new TrackModel(
						mTracksCursor.getLong(mTracksCursor.getColumnIndex(GPSTracking.Tracks._ID)),
						mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.NAME)),
						Float.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.TOTAL_DISTANCE))),
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.TOTAL_DURATION))),
						Long.valueOf(mTracksCursor.getString(mTracksCursor.getColumnIndex(GPSTracking.Tracks.CREATION_TIME))),
						Integer.valueOf(mTracksCursor.getInt(mTracksCursor.getColumnIndex(GPSTracking.Tracks.USER_ID))));
				tracks.add(trackModel);
			}
		}

		return tracks;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent();
		intent.setClass(getActivity(), ActivitySummaryActivity.class);
		intent.putExtra(TrackingServiceConstants.TRACK_ID, mTracks.get(position).getId());
		intent.putExtra(TrackingServiceConstants.REQUEST_CODE, REQUEST_CODE_VALUE);
		startActivityForResult(intent, REQUEST_CODE_VALUE);
	}

	private WebServiceCallHandler getTracksCallHandler = new WebServiceCallHandler(this) {

		@Override
		public void showWebServiceProgress()
		{
			MyActivitiesRESTFragment.this.showWebServiceProgress(getString(R.string.load_activities));
		}

		@Override
		public void onResponseSuccessful(WebServiceResponse response)
		{
			clear();
			MyActivitiesRESTFragment.this.onLoadTracksSuccessful(response, this.getWebService());
		}

		@Override
		public Context getContext()
		{
			return MyActivitiesRESTFragment.this.getActivity();
		}
	};


	public void onLoadTracksSuccessful(WebServiceResponse response, GenericWebService webService)
	{
		webService.interpretResponse(response);
		mTracks = ((GetTracksWebService)webService).getTracksFromResponse(response);
		CustomArrayAdapter adapter = new CustomArrayAdapter(getActivity(), mTracks);
		setListAdapter(adapter);
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
		;
	}

	@Override
	public void onBusinessError(WebServiceResponse response)
	{
		onError(response);
	}

	@Override
	public void onError(WebServiceResponse response)
	{
		if (this == null) {
			return;
		}

		this.getActivity().setResult(Common.CallResult.FAILED);

		if (response.getError().getType() == ErrorType.USER_CANCELED) {
			onRequestCancel();
		} else {
			showFatalError(response);
		}

	}

	public void showFatalError(WebServiceResponse response) {
		DialogHelper.createFatalErrorDialog(this.getActivity(), response.getError().getDisplayMessage()).show();
	}

	private void onRequestCancel()
	{
		getTracksCallHandler.clear();
	}

	@Override
	public void onResponseUnauthorized(WebServiceResponse response)
	{
		DialogHelper.createFatalErrorDialog(this.getActivity(), response.getError().getDisplayMessage()).show();
	}

	@Override
	public void onAnyResult(WebServiceResponse response)
	{
		requestTimeout.stopCounting();
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
			//progressOverlay.showProgress(false);
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

	private void cancelHandlers()
	{
		if (getTracksCallHandler.isTaskInProgress()) {
			getTracksCallHandler.cancel(false);
		}
	}
}
