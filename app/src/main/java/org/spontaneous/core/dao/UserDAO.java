package org.spontaneous.core.dao;

import org.json.JSONException;
import org.json.JSONObject;
import org.spontaneous.core.api.SerializableJSON;
import org.spontaneous.utility.JSONHelper;

import java.util.List;

/**
 * Currently logged-in user (ParcelShop worker or owner) detail,
 * including ParcelShop data.
 *
 * @author Florian Dondorf
 */
public class UserDAO implements SerializableJSON<UserDAO> {

    private Long userId;
    private String email = "";
    private String firstName = "";
    private String lastName = "";
    private boolean stayLogged = false;
    private List<String> authorities = null;

    private static final String TAG = UserDAO.class.getSimpleName();

    @Override
    public JSONObject storeInJSON() throws JSONException
    {
        JSONObject to = new JSONObject();

        JSONHelper.setLongIfNotEmpty(to, "userId", userId);
        JSONHelper.setStringIfNotEmpty(to, "email", email);
        JSONHelper.setStringIfNotEmpty(to, "firstName", firstName);
        JSONHelper.setStringIfNotEmpty(to, "lastName", lastName);
        JSONHelper.setBooleanIfNotEmpty(to, "stayLogged", stayLogged);
        JSONHelper.setStringArrayIfNotEmpty(to, "roles", authorities);

        return to;
    }

    @Override
    public UserDAO restoreFromJSON(JSONObject to) throws JSONException
    {
        userId = JSONHelper.getLongFailsafe(to, "userId", -1L);
        email = JSONHelper.getStringFailsafe(to, "email", "");
        firstName = JSONHelper.getStringFailsafe(to, "firstName", "");
        lastName = JSONHelper.getStringFailsafe(to, "lastName", "");
        stayLogged = JSONHelper.getBooleanFailsafe(to, "stayLogged", false);
        authorities = JSONHelper.getStringArrayFailsafe(to, "roles", null);
        return this;
    }

    public Long getUserId() {return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public boolean isStayLogged() {return stayLogged;}

    public void setStayLogged(boolean stayLogged) {this.stayLogged = stayLogged;}

    public List<String> getAuthorities() { return authorities; };

    public void setAuthorities(List<String> authorities) { this.authorities = authorities; };

}
