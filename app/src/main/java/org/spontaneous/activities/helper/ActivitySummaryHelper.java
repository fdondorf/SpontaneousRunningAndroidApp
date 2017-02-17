package org.spontaneous.activities.helper;

import org.spontaneous.activities.model.GeoPointModel;
import org.spontaneous.activities.model.SegmentModel;
import org.spontaneous.activities.model.TrackModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fdondorf on 03.02.2017.
 */

public class ActivitySummaryHelper {

    /**
     * Return a list of geopoints for the given track model.
     *
     * @param trackModel
     * @return
     */
    public List<GeoPointModel> getGeopointsFromModel(TrackModel trackModel) {

        List<GeoPointModel> result = new ArrayList<GeoPointModel>();
        if (trackModel != null) {
            for (SegmentModel segment : trackModel.getSegments()) {
                result.addAll(segment.getWayPoints());
            }
        }
        return result;
    }
}
