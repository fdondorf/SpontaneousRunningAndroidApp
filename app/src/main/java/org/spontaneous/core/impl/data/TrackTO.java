package org.spontaneous.core.impl.data;

public class TrackTO {

	private Long id;
	private String name;
	private String creationTime;
	private Integer totalDuration;
	private Double totalDistance;
	private Long userId;
	private Long averageSpeed;
	
	public TrackTO() {
		;
	}

	public TrackTO(Long id, String name, String creationTime, Integer totalDuration, Double totalDistance,
			Long userId, Long averageSpeed) {
		super();
		this.id = id;
		this.name = name;
		this.creationTime = creationTime;
		this.totalDuration = totalDuration;
		this.totalDistance = totalDistance;
		this.userId = userId;
		this.averageSpeed = averageSpeed;
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

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public Integer getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(Integer totalDuration) {
		this.totalDuration = totalDuration;
	}

	public Double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(Double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(Long averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

}
