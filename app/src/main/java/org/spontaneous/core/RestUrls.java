package org.spontaneous.core;

public enum RestUrls {

	SERVER_NAME("http://178.238.226.22"), //"http://192.168.178.38"),
	PORT("80"),
	REST_SERVICE_LOGIN("/spontaneous/secure/auth/token"),
	REST_SERVICE_USERINFO("/spontaneous/secure/userinfo"),
	REST_SERVICE_UPDATE_USER("/spontaneous/secure/user/update"),
	REST_SERVICE_LOGOUT("/spontaneous/secure/auth/revoke"),
	REST_SERVICE_REGISTER("/spontaneous/register"),
	REST_SERVICE_TRACKS("/spontaneous/secure/tracks"),
	REST_SERVICE_TRACK_DETAILS("/spontaneous/secure/tracks/"),
	REST_SERVICE_TRACK_CREATE("/spontaneous/secure/track"),
	REST_SERVICE_TRACK_DELETE("/spontaneous/secure/tracks/delete/"),
	REST_SERVICE_TRACK_UPDATE_ALL("/spontaneous/secure/updateTrack");
	/*
	REST_SERVICE_TRACKS("/oasp4j-sample-server/services/rest/trackmanagement/v1/tracks"),
	REST_SERVICE_CSRFTOKEN("/oasp4j-sample-server/services/rest/security/v1/csrftoken"),
	REST_SERVICE_LOGIN("/oasp4j-sample-server/services/rest/login"),
	REST_SERVICE_LOGOUT("/oasp4j-sample-server/services/rest/logout"),
	REST_SERVICE_CURRENTUSER("/oasp4j-sample-server/services/rest/security/v1/currentuser/");
	*/

	private final String url;
	
	RestUrls(String url) {
		this.url = url;
	}
	
	private String getUrl() {
		return this.url;
	}
	
	public String toString () {
		return getUrl();
	}
}
