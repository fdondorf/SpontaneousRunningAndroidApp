package org.spontaneous.activities.util;

public class NavItem {

	int mId;
    String mTitle;
    String mSubtitle;
    int mIcon;
	boolean mVisible;
 
    public NavItem(int id, String title, String subtitle, int icon, boolean visible) {
        mId = id;
		mTitle = title;
        mSubtitle = subtitle;
        mIcon = icon;
		mVisible = visible;
    }

	public Integer getmId() { return mId; }

	public String getmTitle() {
		return mTitle;
	}

	public String getmSubtitle() {
		return mSubtitle;
	}

	public int getmIcon() {
		return mIcon;
	}

	public boolean isVisible() { return mVisible; }
    
}
