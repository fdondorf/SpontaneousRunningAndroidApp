package org.spontaneous.activities.model;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Model containing the data of a track
 * @author fdondorf
 *
 */
public class TrackModel {

	private Long id;
	private String name;
	private Float totalDistance;
	private Long totalDuration;
	private Long creationTime;
	private Integer userId;
	private List<SegmentModel> segments = new ArrayList<SegmentModel>();

	public TrackModel() {;}

	public TrackModel(Long id, String name, Float totalDistance, Long totalDuration, Long creationTime, Integer userId) {
		super();
		this.id = id;
		this.name = name;
		this.totalDistance = totalDistance;
		this.totalDuration = totalDuration;
		this.creationTime = creationTime;
		this.userId = userId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Float getTotalDistance() {
		return totalDistance;
	}
	
	public void setTotalDistance(Float totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	public Long getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(Long totalDuration) {
		this.totalDuration = totalDuration;
	}

	public Long getCreationDate() {
		return creationTime;
	}
	
	public void setCreationDate(Long creationTime) {
		this.creationTime = creationTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public List<SegmentModel> getSegments() {
		return segments;
	}

	public void setSegments(List<SegmentModel> segments) {
		this.segments = segments;
	}
	
	public boolean addSegment(SegmentModel segment) {
		if (this.segments == null) {
			this.segments = new ArrayList<SegmentModel>();
		}
		return this.segments.add(segment);
	}

	public JSONObject storeInJSON() throws JSONException {
		JSONObject to = new JSONObject();
		to.put("id", this.id);
		to.put("name", this.name);
		to.put("totalDistance", this.totalDistance);
		to.put("totalDuration", this.totalDuration);
		to.put("creationTime", this.creationTime);
		to.put("userId", this.userId);

		JSONArray segments = new JSONArray();
		for (SegmentModel segment : this.segments) {
			segments.put(segment.storeInJSON());
		}
		to.put("segments", segments);
		return to;
	}

	public TrackModel restoreFromJSON(JSONObject to) throws JSONException
	{
		Gson gson = new Gson();
		TrackModel trackModel = gson.fromJson(to.toString(), TrackModel.class);
		return trackModel;
	}
}
