package org.spontaneous.db;

import android.content.ContentUris;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;

/**
 * The GPStracking provider stores all static information about GPStracking.
 * 
 * @version $Id$
 * @author fdondorf
 */
public final class GPSTracking
{
   /** The authority of this provider: org.spontaneous.SpontaneousRunning */
   public static final String AUTHORITY = "org.spontaneous.SpontaneousRunning";
   
   /** The content:// style Uri for this provider, content://org.spontaneous.SpontaneousRunning */
   public static final Uri CONTENT_URI = Uri.parse( "content://" + GPSTracking.AUTHORITY );
   
   /** The name of the database file */
   public static final String DATABASE_NAME = "SPONTANEOUS_RUNNING.db";
   
   /** The version of the database schema */
   public static final int DATABASE_VERSION = 3;

   /**
    * This table contains tracks.
    * 
    * @author fdondorf
    */
   public static final class Tracks extends TracksColumns implements android.provider.BaseColumns
   {
      /** The MIME type of a CONTENT_URI subdirectory of a single track. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.spontaneous.SpontaneousRunning.track";
      
      /** The MIME type of CONTENT_URI providing a directory of tracks. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.spontaneous.SpontaneousRunning.track";
      
      /** The content:// style URL for this provider, content://org.spontaneous.android.SpontaneousRunning/tracks */
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPSTracking.AUTHORITY + "/" + Tracks.TABLE );

      /** The name of this table */
      public static final String TABLE = "tracks";
      static final String CREATE_STATEMENT = 
         "CREATE TABLE " + Tracks.TABLE + "(" 
        		 + " " + Tracks._ID + " " + Tracks._ID_TYPE + "," 
        		 + " " + Tracks.NAME + " " + Tracks.NAME_TYPE + "," 
        		 + " " + Tracks.TOTAL_DISTANCE + " " + Tracks.TOTAL_DISTANCE_TYPE + "," 
        		 + " " + Tracks.TOTAL_DURATION + " " + Tracks.TOTAL_DURATION_TYPE + "," 
        		 + " " + Tracks.CREATION_TIME + " " + Tracks.CREATION_TIME_TYPE + ","
        		 + " " + Tracks.USER_ID + " " + Tracks.USER_ID_TYPE
        		 + ");";
      
      static final String[] UPGRADE_STATEMENT_1_TO_2 = {
          "ALTER TABLE " + Tracks.TABLE + " ADD COLUMN " + TracksColumns.USER_ID + " " + TracksColumns.USER_ID_TYPE +" DEFAULT 1;"//,
          //"UPDATE TABLE " + Tracks.TABLE + " SET " + TracksColumns.USER_ID + "= 1;"
      };
      static final String[] UPGRADE_STATEMENT_2_TO_3 = {
              "UPDATE " + Tracks.TABLE + " SET " + TracksColumns.USER_ID + " = 1"
      };
   }
   
   /**
    * This table contains segments.
    * 
    * @author fdondorf
    */
   public static final class Segments extends SegmentsColumns implements android.provider.BaseColumns {

      /** The MIME type of a CONTENT_URI subdirectory of a single segment. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.spontaneous.android.segment";
      
      /** The MIME type of CONTENT_URI providing a directory of segments. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.spontaneous.android.segment";

      /** The content:// style URL for this provider, content://org.spontaneous.android.SpontaneousRunning/tracks */
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPSTracking.AUTHORITY + "/" + Segments.TABLE );

      /** The name of this table, segments */
      public static final String TABLE = "segments";
      static final String CREATE_STATMENT = 
         "CREATE TABLE " + Segments.TABLE + "(" 
        		 + " " + Segments._ID   + " " + Segments._ID_TYPE + "," 
        		 + " " + Segments.TRACK + " " + Segments.TRACK_TYPE  + "," 
        		 + " " + Segments.START_TIME + " " + Segments.START_TIME_TYPE  + "," 
        		 + " " + Segments.END_TIME + " " + Segments.END_TIME_TYPE 
        		 + ");";
//      static final String[] UPGRADE_STATEMENT_3_TO_4 = {
//          "ALTER TABLE " + Segments.TABLE + " ADD COLUMN " + SegmentsColumns.START_TIME + " " + SegmentsColumns.START_TIME_TYPE +";",
//          "ALTER TABLE " + Segments.TABLE + " ADD COLUMN " + SegmentsColumns.END_TIME + " " + SegmentsColumns.END_TIME_TYPE +";",
//          "UPDATE " + Segments.TABLE + " SET " + SegmentsColumns.START_TIME + " = 0 WHERE " +  SegmentsColumns.START_TIME + " IS NULL;",
//          "UPDATE " + Segments.TABLE + " SET " + SegmentsColumns.END_TIME + " = 0 WHERE " +  SegmentsColumns.END_TIME + " IS NULL;"
//       };
      
   }

   /**
    * This table contains waypoints.
    * 
    * @author fdondorf
    */
   public static final class Waypoints extends WaypointsColumns implements android.provider.BaseColumns {

      /** The MIME type of a CONTENT_URI subdirectory of a single waypoint. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.spontaneous.android.waypoint";
      
      /** The MIME type of CONTENT_URI providing a directory of waypoints. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.spontaneous.android.waypoint";
      
      /** The name of this table, waypoints */
      public static final String TABLE = "waypoints";
      static final String CREATE_STATEMENT = "CREATE TABLE " + Waypoints.TABLE + 
      "(" + " " + BaseColumns._ID + " " + WaypointsColumns._ID_TYPE + 
      "," + " " + WaypointsColumns.LATITUDE  + " " + WaypointsColumns.LATITUDE_TYPE + 
      "," + " " + WaypointsColumns.LONGITUDE + " " + WaypointsColumns.LONGITUDE_TYPE + 
      "," + " " + WaypointsColumns.TIME      + " " + WaypointsColumns.TIME_TYPE + 
      "," + " " + WaypointsColumns.SPEED     + " " + WaypointsColumns.SPEED_TYPE + 
      "," + " " + WaypointsColumns.SEGMENT   + " " + WaypointsColumns.SEGMENT_TYPE + 
      "," + " " + WaypointsColumns.ACCURACY  + " " + WaypointsColumns.ACCURACY_TYPE + 
      "," + " " + WaypointsColumns.ALTITUDE  + " " + WaypointsColumns.ALTITUDE_TYPE + 
      "," + " " + WaypointsColumns.BEARING   + " " + WaypointsColumns.BEARING_TYPE + 
      "," + " " + WaypointsColumns.DISTANCE  + " " + WaypointsColumns.DISTANCE_TYPE + 
      ");";
      
//      static final String[] UPGRADE_STATEMENT_2_TO_3 = {
//            "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.DISTANCE + " " + WaypointsColumns.DISTANCE_TYPE +";"
//         };

      /**
       * Build a waypoint Uri like:
       * content://org.spontaneous.android.gpstracker/tracks/2/segments/1/waypoints/52
       * using the provided identifiers
       * 
       * @param trackId
       * @param segmentId
       * @param waypointId
       * 
       * @return
       */
      public static Uri buildUri(long trackId, long segmentId, long waypointId) {
         Builder builder = Tracks.CONTENT_URI.buildUpon();
         ContentUris.appendId(builder, trackId);
         builder.appendPath(Segments.TABLE);
         ContentUris.appendId(builder, segmentId);
         builder.appendPath(Waypoints.TABLE);
         ContentUris.appendId(builder, waypointId);
         
         return builder.build();
      }
   }

   /**
    * This table contains user information
    * 
    * @author fdondorf
    */
   public static final class User extends UserColumns implements android.provider.BaseColumns {

      /** The MIME type of a CONTENT_URI subdirectory of a single user entry. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.spontaneous.android.user";
      
      /** The MIME type of CONTENT_URI providing a directory of user entry. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.spontaneous.android.user";
      
      /** The name of this table */
      public static final String TABLE = "user";
      static final String CREATE_STATEMENT = "CREATE TABLE " + User.TABLE + 
      "(" + " " + BaseColumns._ID       + " " + UserColumns._ID_TYPE + 
      "," + " " + UserColumns.CREATION_TIME   + " " + UserColumns.CREATION_TIME_TYPE + 
      "," + " " + UserColumns.FIRSTNAME   + " " + UserColumns.FIRSTNAME_TYPE + 
      "," + " " + UserColumns.LASTNAME  + " " + UserColumns.LASTNAME_TYPE + 
      "," + " " + UserColumns.EMAIL + " " + UserColumns.EMAIL_TYPE + 
      "," + " " + UserColumns.PASSWORD      + " " + UserColumns.PASSWORD_TYPE + 
      "," + " " + UserColumns.STAY_LOGGED_IN      + " " + UserColumns.STAY_LOGGED_IN_TYPE + 
      ");";
      
    static final String[] UPGRADE_STATEMENT_1_TO_2 = {
    		"CREATE TABLE " + User.TABLE + 
    			      "(" + " " + BaseColumns._ID       + " " + UserColumns._ID_TYPE + 
    			      "," + " " + UserColumns.CREATION_TIME   + " " + UserColumns.CREATION_TIME_TYPE + 
    			      "," + " " + UserColumns.FIRSTNAME   + " " + UserColumns.FIRSTNAME_TYPE + 
    			      "," + " " + UserColumns.LASTNAME  + " " + UserColumns.LASTNAME_TYPE + 
    			      "," + " " + UserColumns.EMAIL + " " + UserColumns.EMAIL_TYPE + 
    			      "," + " " + UserColumns.PASSWORD      + " " + UserColumns.PASSWORD_TYPE + 
    			      "," + " " + UserColumns.STAY_LOGGED_IN      + " " + UserColumns.STAY_LOGGED_IN_TYPE + 
    			      ");",
    		 "INSERT INTO " + User.TABLE + " VALUES (1, " + System.currentTimeMillis() + 
    		 	", 'Florian', 'Dondorf', 'f.dondorf@googlemail.com', 'admin', 0);"

    };
      
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPSTracking.AUTHORITY + "/" + User.TABLE );
   }
   
   /**
    * This table contains media URI's.
    * 
    * @author fdondorf
    */
   public static final class Media extends MediaColumns implements android.provider.BaseColumns
   {

      /** The MIME type of a CONTENT_URI subdirectory of a single media entry. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.spontaneous.android.media";
      
      /** The MIME type of CONTENT_URI providing a directory of media entry. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.spontaneous.android.media";
      
      /** The name of this table */
      public static final String TABLE = "media";
      static final String CREATE_STATEMENT = "CREATE TABLE " + Media.TABLE + 
      "(" + " " + BaseColumns._ID       + " " + MediaColumns._ID_TYPE + 
      "," + " " + MediaColumns.TRACK    + " " + MediaColumns.TRACK_TYPE + 
      "," + " " + MediaColumns.SEGMENT  + " " + MediaColumns.SEGMENT_TYPE + 
      "," + " " + MediaColumns.WAYPOINT + " " + MediaColumns.WAYPOINT_TYPE + 
      "," + " " + MediaColumns.URI      + " " + MediaColumns.URI_TYPE + 
      ");";
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPSTracking.AUTHORITY + "/" + Media.TABLE );
   }
   
   /**
    * This table contains media URI's.
    * 
    * @author fdondorf
    */
   public static final class MetaData extends MetaDataColumns implements android.provider.BaseColumns {

      /** The MIME type of a CONTENT_URI subdirectory of a single metadata entry. */
      public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.org.spontaneous.android.metadata";
      
      /** The MIME type of CONTENT_URI providing a directory of media entry. */
      public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.spontaneous.android.metadata";
      
      /** The name of this table */
      public static final String TABLE = "metadata";
      static final String CREATE_STATEMENT = "CREATE TABLE " + MetaData.TABLE + 
      "(" + " " + BaseColumns._ID          + " " + MetaDataColumns._ID_TYPE + 
      "," + " " + MetaDataColumns.TRACK    + " " + MetaDataColumns.TRACK_TYPE + 
      "," + " " + MetaDataColumns.SEGMENT  + " " + MetaDataColumns.SEGMENT_TYPE + 
      "," + " " + MetaDataColumns.WAYPOINT + " " + MetaDataColumns.WAYPOINT_TYPE + 
      "," + " " + MetaDataColumns.KEY      + " " + MetaDataColumns.KEY_TYPE + 
      "," + " " + MetaDataColumns.VALUE    + " " + MetaDataColumns.VALUE_TYPE + 
      ");";
      
      /**
       * content://org.spontaneous.android.gpstracker/metadata
       */
      public static final Uri CONTENT_URI = Uri.parse( "content://" + GPSTracking.AUTHORITY + "/" + MetaData.TABLE );
   }
   
   /**
    * Columns from the tracks table.
    * 
    * @author fdondorf
    */
   public static class TracksColumns {
      public static final String NAME          	= "name";
      public static final String TOTAL_DISTANCE	= "totalDistance";
      public static final String TOTAL_DURATION	= "totalDuration";
      public static final String CREATION_TIME 	= "creationtime";
      public static final String USER_ID		= "user_id";
      static final String CREATION_TIME_TYPE   	= "INTEGER NOT NULL";
      static final String NAME_TYPE            	= "TEXT";
      static final String TOTAL_DISTANCE_TYPE	= "REAL NOT NULL";
      static final String TOTAL_DURATION_TYPE	= "INTEGER NOT NULL";
      static final String USER_ID_TYPE			= "INTEGER NOT NULL";
      static final String _ID_TYPE             	= "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
   
   /**
    * Columns from the segments table.
    * 
    * @author fdondorf
    */
   public static class SegmentsColumns {
      /** The track _id to which this segment belongs */
      public static final String TRACK 		= "track";     
      public static final String START_TIME = "startTime";
      public static final String END_TIME  	= "endTime";
      static final String TRACK_TYPE   		= "INTEGER NOT NULL";
      static final String _ID_TYPE     		= "INTEGER PRIMARY KEY AUTOINCREMENT";
      static final String START_TIME_TYPE  	= "INTEGER";
      static final String END_TIME_TYPE   	= "INTEGER";
   }

   /**
    * Columns from the waypoints table.
    * 
    * @author fdondorf
    */
   public static class WaypointsColumns {

      /** The latitude */
      public static final String LATITUDE = "latitude";
      /** The longitude */
      public static final String LONGITUDE = "longitude";
      /** The recorded time */
      public static final String TIME = "time";
      /** The speed in meters per second */
      public static final String SPEED = "speed";
      /** The segment _id to which this segment belongs */
      public static final String SEGMENT = "tracksegment";
      /** The accuracy of the fix */
      public static final String ACCURACY = "accuracy";
      /** The altitude */
      public static final String ALTITUDE = "altitude";
      /** the bearing of the fix */
      public static final String BEARING = "bearing";
      /** the distance to the previous fix of this fix */
      public static final String DISTANCE = "distance";

      static final String LATITUDE_TYPE  = "REAL NOT NULL";
      static final String LONGITUDE_TYPE = "REAL NOT NULL";
      static final String TIME_TYPE      = "INTEGER NOT NULL";
      static final String SPEED_TYPE     = "REAL NOT NULL";
      static final String SEGMENT_TYPE   = "INTEGER NOT NULL";
      static final String ACCURACY_TYPE  = "REAL";
      static final String ALTITUDE_TYPE  = "REAL";
      static final String BEARING_TYPE   = "REAL";
      static final String DISTANCE_TYPE	 = "REAL";
      static final String _ID_TYPE       = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }

   /**
    * Columns from the user table.
    * 
    * @author fdondorf
    */
   public static class UserColumns {
	   
      public static final String FIRSTNAME   = "firstname";     
      static final String FIRSTNAME_TYPE     = "TEXT NOT NULL";
      public static final String LASTNAME    = "lastname";     
      static final String LASTNAME_TYPE      = "TEXT NOT NULL";
      public static final String EMAIL    	 = "email";     
      static final String EMAIL_TYPE      	 = "TEXT NOT NULL";
      public static final String PASSWORD    = "password";     
      static final String PASSWORD_TYPE      = "TEXT NOT NULL";
      public static final String CREATION_TIME 	= "creationtime";
      static final String CREATION_TIME_TYPE 	= "INTEGER NOT NULL";
      public static final String STAY_LOGGED_IN = "stayloggedin";
      static final String STAY_LOGGED_IN_TYPE 	= "BOOLEAN";
      static final String _ID_TYPE        	 = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
   
   /**
    * Columns from the media table.
    * 
    * @author fdondorf
    */
   public static class MediaColumns {
	   
      /** The track _id to which this segment belongs */
      public static final String TRACK    = "track";     
      static final String TRACK_TYPE      = "INTEGER NOT NULL";
      public static final String SEGMENT  = "segment";     
      static final String SEGMENT_TYPE    = "INTEGER NOT NULL";
      public static final String WAYPOINT = "waypoint";     
      static final String WAYPOINT_TYPE   = "INTEGER NOT NULL";
      public static final String URI      = "uri";     
      static final String URI_TYPE        = "TEXT";
      static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
   
   /**
    * Columns from the media table.
    * 
    * @author fdondorf
    */
   public static class MetaDataColumns {
      /** The track _id to which this segment belongs */
      public static final String TRACK    = "track";     
      static final String TRACK_TYPE      = "INTEGER NOT NULL";
      public static final String SEGMENT  = "segment";     
      static final String SEGMENT_TYPE    = "INTEGER";
      public static final String WAYPOINT = "waypoint";     
      static final String WAYPOINT_TYPE   = "INTEGER";
      public static final String KEY      = "key";          
      static final String KEY_TYPE        = "TEXT NOT NULL";
      public static final String VALUE    = "value";        
      static final String VALUE_TYPE      = "TEXT NOT NULL";
      static final String _ID_TYPE        = "INTEGER PRIMARY KEY AUTOINCREMENT";
   }
}
