package org.spontaneous.core.impl.data;

import java.util.List;

public class TrackCTO {

	private TrackTO trackMetadata;
	private List<TrackSegment> trackDetails;
	
	public TrackTO getTrackMetadata() {
		return trackMetadata;
	}
	
	public void setTrackMetadata(TrackTO trackMetadata) {
		this.trackMetadata = trackMetadata;
	}
	
	public List<TrackSegment> getTrackDetails() {
		return trackDetails;
	}
	
	public void setTrackDetails(List<TrackSegment> trackDetails) {
		this.trackDetails = trackDetails;
	}
	
}
