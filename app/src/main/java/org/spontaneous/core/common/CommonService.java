package org.spontaneous.core.common;

/**
 * Stores different constant values like keys and provides it for use in other classes. It has
 * constants in different categories:
 * <ul>
 * <li>MessageSync - source of Sync service Sync event</li>
 * <li>BroadcastEndpoint - identifies broadcast sources and destinations</li>
 * <li>BroadcastParam - parameters sent via broadcasts</li>
 * <li>GuiAction - possible action identifiers, for GUI_ACTION broadcast parameter.</li>
 * </ul>
 *
 * For clarity and performance, constants are categorized in static classes.
 *
 * @author Dominik Dzienia
 */
public final class CommonService {

    public static final class SyncSource {
        public static final int UNKNOWN = 0;
        public static final int FROM_TIMER = 100;
        public static final int DIRECT = 110;
        public static final int FROM_EVENT = 120;
        public static final int FROM_APP_RESTORED = 130;
    }

    public static final class BroadcastEndpoint {
        public static final String SERVICE = Common.BASE + ".service";
        public static final String GUI = Common.BASE + ".gui";
    }

    // broadcast intent param type

    public static final class BroadcastParam {
        public static final String QUEUE_LENGHT = Common.BASE + ".queue_length";
        public static final String QUEUE_PARCELS_COUNT = Common.BASE + ".queue_parcels_count";
        public static final String QUEUE_CONTENTS = Common.BASE + ".queue_contents";
        public static final String ACTION_SYNC = Common.BASE + "action.sync";
        public static final String ACTION_CASHSYNC = Common.BASE + "action.cashsync";
        public static final String ACTION_CLEAR = Common.BASE + "action.clear";
        public static final String ACTION_STATUS = Common.BASE + ".status";
        public static final String ACTION_TRYSYNC = Common.BASE + ".trysync";
        public static final String GUI_ACTION = Common.BASE + ".qui_action";
        public static final String BUSINESS_ACTION = Common.BASE + ".action";
        public static final String SYNC_SOURCE = Common.BASE + ".action.source";
        public static final String ERROR_PAYLOAD = Common.BASE + ".error.payload";
        public static final String USER_CHANGED = Common.BASE + ".event.user.changed";
    }

    // broadcast intent param values - GUI

    public static final class GuiAction {
        public static final String SHOW_PROGRESS = "progress_show";
        public static final String HIDE_PROGRESS = "progress_hide";
        public static final String ASYNC_WS_STARTED = "request_start";
        public static final String ASYNC_WS_ERROR = "request_error";
        public static final String ASYNC_WS_SUCCESS = "request_success";
    }


}
