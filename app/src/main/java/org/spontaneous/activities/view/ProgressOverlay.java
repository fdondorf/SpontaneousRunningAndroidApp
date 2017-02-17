package org.spontaneous.activities.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import org.spontaneous.R;

import org.spontaneous.activities.view.api.EventDisablable;

/**
 * Displays progress overlay, with spinner and custom message.
 *
 * @author Dominik Dzienia
 */
public class ProgressOverlay {

    private final EventDisablable formView;
    private final View statusView;
    private final TextView statusMessageView;

    public ProgressOverlay(final EventDisablable formView, final View statusView, final String labelText)
    {
        super();
        this.formView = formView;
        this.statusView = statusView;
        statusMessageView = (TextView) statusView.findViewById(R.id.status_message);
        statusMessageView.setText(labelText);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = statusView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);

            statusView.setVisibility(View.VISIBLE);
            if (formView != null) {
                formView.setChildrenEventsDisabled(true);
            }
            statusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation)
                        {
                            statusView.setVisibility(show ? View.VISIBLE : View.GONE);
                            if (formView != null) {
                                formView.setChildrenEventsDisabled(show);
                            }
                        }
                    });


        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            statusView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (formView != null) {
                formView.setChildrenEventsDisabled(show);
            }
        }
    }

    public void showProgress(String customMessage)
    {
        statusMessageView.setText(customMessage);
        showProgress(true);
    }

}
