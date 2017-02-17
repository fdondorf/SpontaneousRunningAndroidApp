
package org.spontaneous.activities.view.api;

/**
 * Interface implemented by extensions of standard layout, enabling them to
 * disable and enable event capture on-demand, making all of implementing layout
 * children to ignore input.
 *
 * Used when progress overlay is shown above form, to make sure none of form
 * component is active and editable.
 *
 * @author Dominik Dzienia
 */
public interface EventDisablable {
    public abstract boolean isChildrenEventsDisabled();

    public abstract void setChildrenEventsDisabled(boolean disableChildrenEvents);

    public abstract void setDisabledEditsList(int[] disbledIdList);
}
