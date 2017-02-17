package org.spontaneous.activities.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SegmentModel {

	private Long id;
	private Long trackId;
	private Long startTimeInMillis;
	private Long endTimeInMillis;
	private List<GeoPointModel> wayPoints = new ArrayList<GeoPointModel>();
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getTrackId() {
		return trackId;
	}
	
	public void setTrackId(Long trackId) {
		this.trackId = trackId;
	}
	
	public Long getStartTimeInMillis() {
		return startTimeInMillis;
	}

	public void setStartTimeInMillis(Long startTimeInMillis) {
		this.startTimeInMillis = startTimeInMillis;
	}

	public Long getEndTimeInMillis() {
		return endTimeInMillis;
	}

	public void setEndTimeInMillis(Long endTimeInMillis) {
		this.endTimeInMillis = endTimeInMillis;
	}

	public List<GeoPointModel> getWayPoints() {
		return wayPoints;
	}
	
	public void setWayPoints(List<GeoPointModel> wayPoints) {
		this.wayPoints = wayPoints;
	}
	
	public boolean addWayPoint(GeoPointModel geoPoint) {
		if (this.wayPoints == null) {
			this.wayPoints = new ArrayList<GeoPointModel>();
		}
		return this.wayPoints.add(geoPoint);
	}

	public JSONObject storeInJSON() throws JSONException {
		JSONObject to = new JSONObject();
		to.put("id", this.id);
		to.put("trackId", this.trackId);
		to.put("startTimeInMillis", this.startTimeInMillis);
		to.put("endTimeInMillis", this.endTimeInMillis);

		JSONArray wayPoints = new JSONArray();
		for (GeoPointModel geoPoint : this.wayPoints) {
			wayPoints.put(geoPoint.storeInJSON());
		}
		to.put("wayPoints", wayPoints);

		return to;
	}
}
