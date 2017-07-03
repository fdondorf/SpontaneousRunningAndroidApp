package org.spontaneous.activities.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.core.api.SerializableJSON;
import org.spontaneous.utility.JSONHelper;

/**
 * Model containing the data of a user
 * @author fdondorf
 *
 */
public class UserModel implements SerializableJSON<UserModel> {

	private Long id;
	private String firstname;
	private String lastname;
	private String email;
	private String password;
	private String gender;
	private String profileImage;
	
	public UserModel(Long id, String firstname, String lastname, 
			String email, String password) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
	}

	public UserModel(Long id, String firstname, String lastname, 
			String email) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGender() { return gender; }

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getProfileImage()
	{
		return profileImage;
	}

	public void setProfileImage(String profileImage)
	{
		this.profileImage = profileImage;
	}

	
	@Override
	public JSONObject storeInJSON() throws JSONException
	{
		JSONObject to = new JSONObject();

		JSONHelper.setLongIfNotEmpty(to, "id", id);
		JSONHelper.setStringIfNotEmpty(to, "email", email);
		JSONHelper.setStringIfNotEmpty(to, "firstname", firstname);
		JSONHelper.setStringIfNotEmpty(to, "lastname", lastname);
		JSONHelper.setStringIfNotEmpty(to, "gender", gender);
		JSONHelper.setStringIfNotEmpty(to, "image", profileImage);

		return to;
	}

	@Override
	public UserModel restoreFromJSON(JSONObject to) throws JSONException
	{
		id = JSONHelper.getLongFailsafe(to, "userId", -1L);
		email = JSONHelper.getStringFailsafe(to, "email", "");
		firstname = JSONHelper.getStringFailsafe(to, "firstName", "");
		lastname = JSONHelper.getStringFailsafe(to, "lastName", "");
		gender = JSONHelper.getStringFailsafe(to, "gender", "");
		profileImage = JSONHelper.getStringFailsafe(to, "profileImage", "");
		return this;
	}
}
