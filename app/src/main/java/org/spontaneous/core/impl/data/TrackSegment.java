package org.spontaneous.core.impl.data;

import java.util.ArrayList;
import java.util.List;

public class TrackSegment {

	private Long id;
	private String startTime;
	private String endTime;
	private List<Waypoint> waypoints;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getStartTime() {
		return startTime;
	}
	
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	public String getEndTime() {
		return endTime;
	}
	
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	public List<Waypoint> getWaypoints() {
		return waypoints;
	}
	
	public void setWaypoints(List<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}
	
	public boolean addWaypoint(Waypoint waypoint) {
		if (this.waypoints == null) {
			this.waypoints = new ArrayList<>();
		}
		return this.waypoints.add(waypoint);
	}
	
}
