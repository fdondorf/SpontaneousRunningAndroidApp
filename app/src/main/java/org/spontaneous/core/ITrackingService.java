package org.spontaneous.core;

import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.TrackModel;
import org.spontaneous.activities.model.UserModel;

import java.util.List;

public interface ITrackingService {

	/******************************
	  * Tracking-Management
	 ******************************/
	
	public TrackModel readTrackById(Long trackId);

	public List<TrackModel> getAllTracks();
	 
	public List<GeoPointModel> getGeoPointsByTrack(Long trackId);

	public boolean deleteTrack(Long trackId);

	/******************************
	 * User-Management
	 ******************************/
	 
	public UserModel login(String email, String password, boolean stayLoggedIn);

	public boolean checkUserAlreadyLoggedin();
	 
	public boolean register(UserModel user);
	 
	public UserModel findUserByMail(String email);

}
