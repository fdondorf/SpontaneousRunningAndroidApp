
package org.spontaneous.activities.util;

import android.os.Bundle;
import android.os.Handler;

import org.spontaneous.core.common.Common.ActivityState;

/**
 * Encapsulating timeout functionality, usually used together with web services
 * and progress dialogs.
 *
 * @author Dominik Dzienia
 */
public abstract class TimeoutHandler {
    private volatile int requestCountdown = 0;
    private Handler localUpdateHandler = new Handler();

    public abstract void onTimeoutRestored();

    public abstract void onTimeoutCanceled();

    public abstract void onTimedOut();

    private Runnable localUpdateTask = new Runnable() {
        @Override
        public void run()
        {
            if (requestCountdown > 0) {
                requestCountdown -= 100;
                if (requestCountdown <= 0) {
                    requestCountdown = 0;
                    onTimedOut();
                }
            }

            localUpdateHandler.postDelayed(this, 100);
        }
    };

    public void store(Bundle outState)
    {
        if (outState != null) {
            outState.putInt(ActivityState.CONFIRM_REQUEST_COUNTDOWN, requestCountdown);
        }
    }

    public void restore(Bundle savedInstanceState)
    {
        if (savedInstanceState != null) {
            requestCountdown = savedInstanceState.getInt(ActivityState.CONFIRM_REQUEST_COUNTDOWN, 0);
            if (requestCountdown > 0) {
                onTimeoutRestored();
            }
        }
    }

    public void pause()
    {
        localUpdateHandler.removeCallbacksAndMessages(null);
    }

    public void resume()
    {
        localUpdateHandler.postDelayed(localUpdateTask, 100);
    }

    public boolean cancelIfNeeded()
    {
        if (requestCountdown > 0) {
            requestCountdown = 0;
            onTimeoutCanceled();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets a ms counter to detect a timeout.
     *
     * @param countingMs The milliseconds before the timeout occurs.
     */
    public void startCountingFrom(int countingMs)
    {
        requestCountdown = countingMs;
    }

    public void stopCounting()
    {
        requestCountdown = 0;
    }
}
