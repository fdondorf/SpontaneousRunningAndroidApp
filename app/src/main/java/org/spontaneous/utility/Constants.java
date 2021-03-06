package org.spontaneous.utility;

/**
 * Various application wide constants
 * 
 * @version $Id$
 * @author rene (c) Mar 22, 2009, Sogeti B.V.
 */
public class Constants
{
   public static final String PREFERENCES = "org.spontaneous.Prefs" ;
   public static final String PREF_USERID = "org.spontaneous.UserId";
   public static final String PREF_FIRSTNAME = "org.spontaneous.Firstname";
   public static final String PREF_LASTNAME = "org.spontaneous.Lastname";
   public static final String PREF_EMAIL = "org.spontaneous.Email";
   public static final String PREF_STAY_LOGGED = "org.spontaneous.StayLogged";
   
   public static final String DISABLEBLANKING = "disableblanking";
   public static final String DISABLEDIMMING = "disabledimming";
   public static final String SATELLITE = "SATELLITE";
   public static final String TRAFFIC = "TRAFFIC";
   public static final String SPEED = "showspeed";
   public static final String ALTITUDE = "showaltitude";
   public static final String DISTANCE = "showdistance";
   public static final String COMPASS = "COMPASS";
   public static final String LOCATION = "LOCATION";
   public static final String MAPPROVIDER = "mapprovider";
   public static final String TRACKCOLORING = "trackcoloring";
   public static final String SPEEDSANITYCHECK = "speedsanitycheck";
   public static final String PRECISION = "precision";
   public static final String LOGATSTARTUP = "logatstartup";
   public static final String STARTUPATBOOT = "startupatboot";
   public static final String LOGATDOCK = "logatdock";
   public static final String STOPATUNDOCK = "stopatundock";
   public static final String LOGATPOWERCONNECTED = "logatpowerconnected";
   public static final String STOPATPOWERDISCONNECTED = "stopatpowerdisconnected";
   public static final String SERVICENAME = "nl.sogeti.android.gpstracker.intent.action.GPSLoggerService";
   public static final String STREAMBROADCAST = "nl.sogeti.android.gpstracker.intent.action.STREAMBROADCAST";
   public static final String UNITS = "units";
   public static final int UNITS_DEFAULT = 0;
   public static final int UNITS_IMPERIAL = 1;
   public static final int UNITS_METRIC = 2;
   public static final int UNITS_NAUTIC = 3;
   public static final int UNITS_METRICPACE = 4;
   public static final int UNITS_IMPERIALPACE = 5;
   public static final int UNITS_IMPERIALSURFACE = 6;
   public static final int UNITS_METRICSURFACE = 7;
   public static final String SDDIR_DIR = "SDDIR_DIR";
   public static final String DEFAULT_EXTERNAL_DIR = "/OpenGPSTracker/";
   public static final String TMPICTUREFILE_SUBPATH = "media_tmp.tmp";
   //public static final Uri NAME_URI = Uri.parse("content://" + GPStracking.AUTHORITY + ".string");
   public static final int GOOGLE = 0;
   public static final int OSM = 1;
   public static final String JOGRUNNER_AUTH = "JOGRUNNER_AUTH";
   public static final String EXPORT_TYPE = "SHARE_TYPE";
   public static final String EXPORT_GPXTARGET = "EXPORT_GPXTARGET";
   public static final String EXPORT_KMZTARGET = "EXPORT_KMZTARGET";
   public static final String EXPORT_TXTTARGET = "EXPORT_TXTTARGET";

   public static final double MIN_STATISTICS_SPEED = 1.0d;
   public static final int OSM_CLOUDMADE = 0;
   public static final int OSM_MAKNIK = 1;
   public static final int OSM_CYCLE = 2;
   public static final String OSMBASEOVERLAY = "OSM_BASE_OVERLAY";

   public static final String LOGGING_INTERVAL = "customprecisiontime";
   public static final String LOGGING_DISTANCE = "customprecisiondistance";
   public static final String STATUS_MONITOR = "gpsstatusmonitor";
   public static final String OSM_USERNAME = "OSM_USERNAME";
   public static final String OSM_PASSWORD = "OSM_PASSWORD";
   public static final String OSM_VISIBILITY = "OSM_VISIBILITY";
   public static final String DATASOURCES_KEY = "DATASOURCES";
   public static final String BROADCAST_STREAM = "broadcast_stream";

   public static final Float KILOMETER = 1000f;
   
   /**
    * Broadcast intent action indicating that the logger service state has changed. Includes the logging state and its precision.
    * 
    * @see #EXTRA_LOGGING_PRECISION
    * @see #EXTRA_LOGGING_STATE
    */

   public static final String LOGGING_STATE_CHANGED_ACTION = "nl.sogeti.android.gpstracker.LOGGING_STATE_CHANGED";

   /**
    * The precision the service is logging on.
    * 
    * @see #LOGGING_FINE
    * @see #LOGGING_NORMAL
    * @see #LOGGING_COARSE
    * @see #LOGGING_GLOBAL
    * @see #LOGGING_CUSTOM
    */
   public static final String EXTRA_LOGGING_PRECISION = "nl.sogeti.android.gpstracker.EXTRA_LOGGING_PRECISION";

   /**
    * The state the service is.
    * 
    * @see #UNKNOWN
    * @see #LOGGING
    * @see #PAUSED
    * @see #STOPPED
    */
   public static final String EXTRA_LOGGING_STATE = "nl.sogeti.android.gpstracker.EXTRA_LOGGING_STATE";

   /**
    * The state of the service is unknown
    */
   public static final int UNKNOWN = -1;

   /**
    * The service is actively logging, it has requested location update from the location provider.
    */
   public static final int LOGGING = 1;

   /**
    * The service is not active, but can be resumed to become active and store location changes as part of a new segment of the current track.
    */
   public static final int PAUSED = 2;

   /**
    * The service is not active and can not resume a current track but must start a new one when becoming active.
    */
   public static final int STOPPED = 3;

   /**
    * The precision of the GPS provider is based on the custom time interval and distance.
    */
   public static final int LOGGING_CUSTOM = 0;

   /**
    * The GPS location provider is asked to update every 10 seconds or every 5 meters.
    */
   public static final int LOGGING_FINE = 1;

   /**
    * The GPS location provider is asked to update every 15 seconds or every 10 meters.
    */
   public static final int LOGGING_NORMAL = 2;

   /**
    * The GPS location provider is asked to update every 30 seconds or every 25 meters.
    */
   public static final int LOGGING_COARSE = 3;

   /**
    * The radio location provider is asked to update every 5 minutes or every 500 meters.
    */
   public static final int LOGGING_GLOBAL = 4;

   /**
    * A distance in meters
    */
   public static final String EXTRA_DISTANCE = "nl.sogeti.android.gpstracker.EXTRA_DISTANCE";
   /**
    * A time period in minutes
    */
   public static final String EXTRA_TIME = "nl.sogeti.android.gpstracker.EXTRA_TIME";
   /**
    * The location that pushed beyond the set minimum time or distance
    */
   public static final String EXTRA_LOCATION = "nl.sogeti.android.gpstracker.EXTRA_LOCATION";
   /**
    * The track that is being logged
    */
   public static final String EXTRA_TRACK = "nl.sogeti.android.gpstracker.EXTRA_TRACK";

   /**
    * Timeout for Requests
    */
   public static final int COMMON_REQUEST_TIMEOUT = 15 * 1000;
}
