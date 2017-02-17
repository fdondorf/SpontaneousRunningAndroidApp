package org.spontaneous.activities.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.spontaneous.activities.view.api.EventDisablable;

/**
 * Allows Linearlayout where we can disable it's children from getting events.
 *
 */
public class DisablableLinearLayout extends LinearLayout implements EventDisablable {

    private boolean disableChildrenEvents = false;
    private int[] disbledIdList = null;

    @Override
    public boolean isChildrenEventsDisabled()
    {
        return disableChildrenEvents;
    }

    @Override
    public void setChildrenEventsDisabled(boolean disableChildrenEvents)
    {
        this.disableChildrenEvents = disableChildrenEvents;
        if (disbledIdList != null) {
            for (int disabledId : disbledIdList) {
                final View candidate = this.findViewById(disabledId);
                if (candidate != null) {
                    candidate.setEnabled(!disableChildrenEvents);
                }
            }
        }
    }

    public DisablableLinearLayout(Context context)
    {
        super(context);
    }

    public DisablableLinearLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public DisablableLinearLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        return disableChildrenEvents;
    }

    @Override
    public void setDisabledEditsList(int[] disbledIdList)
    {
        this.disbledIdList = disbledIdList;
    }
}