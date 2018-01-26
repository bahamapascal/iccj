package org.iotacontrolcenter.ui.app;

public class Constants {

    public static final String IMAGE_ICON_FILENAME_MAIN_1_APP = "ICC-Icon-20x20.png";
    public static final String IMAGE_ICON_FILENAME_MAIN_2_APP = "ICC-Icon-40x40.png";
    public static final String IMAGE_ICON_FILENAME_MAIN_3_APP = "ICC-Icon-512x512.png";

    public static final String IMAGE_ICON_FILENAME_SERVER_ONLINE = "online-icon";
    public static final String IMAGE_ICON_FILENAME_SERVER_OFFLINE = "offline-icon";
    public static final String IMAGE_ICON_FILENAME_PLAY_PRESSED = "playPressed.png";
    public static final String IMAGE_ICON_FILENAME_PLAY_UNPRESSED = "playUnPressed.png";
    public static final String IMAGE_ICON_FILENAME_PAUSE_PRESSED = "pausePressed.png";
    public static final String IMAGE_ICON_FILENAME_PAUSE_UNPRESSED = "pauseUnPressed.png";
    public static final String IMAGE_ICON_FILENAME_GREEN_ADD = "greenAdd.png";
    public static final String IMAGE_ICON_FILENAME_LEFT_ARROW = "leftArrow.png";
    public static final String IMAGE_ICON_FILENAME_RIGHT_ARROW = "rightArrow.png";
    public static final String IMAGE_ICON_FILENAME_BLUE_LOADING = "blueLoading.gif";
    public static final String IMAGE_ICON_FILENAME_HELP = "helpIcon.png";

    public static final String IS_CONNECTED_EVENT = "IS-CONNECTED-EVENT";
    public static final String IS_CONNECTED_UNKNOWN_EVENT = "IS-CONNECTED-UNKNOWN-EVENT";

    public static final String IOTA_ACTION_INSTALL = "install";
    public static final String IOTA_ACTION_START = "start";
    public static final String IOTA_ACTION_STOP = "stop";
    public static final String IOTA_ACTION_STATUS = "status";
    public static final String IOTA_ACTION_DELETEDB = "deletedb";
    public static final String IOTA_ACTION_DELETE = "delete";
    public static final String IOTA_ACTION_NEIGHBORS = "neighbors";
    public static final String IOTA_ACTION_NODEINFO = "nodeinfo";

    public static final String ICCR_ACTION_RESTART = "restart";

    public static final String ACTION_STATUS_TRUE = "true";
    //  These must be same as action strings in the action impl classes
    // in ICCR
    public static final String ACTION_RESPONSE_IOTA_STATUS = "statusIota";
    public static final String ACTION_RESPONSE_IOTA_INSTALL = "installIota";
    public static final String ACTION_RESPONSE_IOTA_START = "startIota";
    public static final String ACTION_RESPONSE_IOTA_STOP = "stopIota";
    public static final String ACTION_RESPONSE_IOTA_DELETE = "deleteIota";
    public static final String ACTION_RESPONSE_IOTA_DELETE_DB = "deleteIotaDb";
    public static final String ACTION_RESPONSE_IOTA_GET_NODEINFO = "getIotaNodeInfo";
    public static final String ACTION_RESPONSE_IOTA_GET_NBRS = "getIotaNeighbors";
    public static final String ACTION_RESPONSE_ICCR_RESTART = "restartIccr";

    public static final String MM_ICC_SETTINGS_ACTION = "mm-icc-settings";
    public static final String MM_OPEN_SERVER_ACTION = "mm-open-server";
    public static final String MM_ADD_SERVER_ACTION = "mm-add-server";

    public static final String DIALOG_SERVER_SETTINGS_CANCEL = "dialog-server-settings-cancel";
    public static final String DIALOG_SERVER_SETTINGS_SAVE = "dialog-server-settings-save";

    public static final String DIALOG_CONFIG_SERVER_CANCEL = "dialog-cfg-server-cancel";
    public static final String DIALOG_CONFIG_ADD_SERVER_SAVE = "dialog-cfg-add-server-save";
    public static final String DIALOG_CONFIG_EDIT_SERVER_SAVE = "dialog-cfg-edit-server-save";

    public static final String DIALOG_OPEN_SERVER_OPEN = "dialog-cfg-server-open";
    public static final String DIALOG_OPEN_SERVER_CLOSE = "dialog-cfg-server-close";
    public static final String DIALOG_OPEN_SERVER_EDIT = "dialog-cfg-server-edit";
    public static final String DIALOG_OPEN_SERVER_ADD_SERVER = "dialog-cfg-server-add-server";
    public static final String DIALOG_OPEN_SERVER_CANCEL = "dialog-open-server-cancel";
    public static final String DIALOG_OPEN_SERVER_REMOVE = "dialog-open-server-remove";

    public static final String DIALOG_ICC_SETTINGS_SAVE = "dialog-icc-settings-save";
    public static final String DIALOG_ICC_SETTINGS_CANCEL = "dialog-icc-settings-cancel";

    public static final String NEIGHBOR_PANEL_SAVE_CHANGES = "nbr-panel-save-changes";
    public static final String NEIGHBOR_PANEL_REMOVE_SELECTED = "nbr-panel-remove-selected";
    public static final String NEIGHBOR_PANEL_ADD_NEW = "nbr-panel-add-new";

    public static final String DIALOG_IOTA_LOG_DIRECTION_CHOOSER = "dialog-iota-log-dir-chooser";
    public static final String DIALOG_IOTA_LOG_HEAD_MORE = "dialog-iota-log-head-more";
    public static final String DIALOG_IOTA_LOG_TAIL_PLAY = "dialog-iota-log-tail-play";
    public static final String DIALOG_IOTA_LOG_TAIL_PAUSE = "dialog-iota-log-tail-pause";


    public static final String SERVER_ACTION_SETTINGS = "svr-action-settings";
    public static final String SERVER_ACTION_INSTALL_IOTA = "svr-action-install-iota";
    public static final String SERVER_ACTION_START_IOTA = "svr-action-start-iota";
    public static final String SERVER_ACTION_STATUS_IOTA = "svr-action-status-iota";
    public static final String SERVER_ACTION_STOP_IOTA = "svr-action-stop-iota";
    public static final String SERVER_ACTION_START_WALLET = "svr-action-start-wallet";
    public static final String SERVER_ACTION_DELETEDB_IOTA = "svr-action-deletedb-iota";
    public static final String SERVER_ACTION_UNINSTALL_IOTA = "svr-action-uninstall-iota";
    public static final String SERVER_ACTION_ICCR_EVENTLOG = "svr-action-iccr-eventlog";
    public static final String SERVER_ACTION_CLEAR_ICCR_EVENTLOG = "svr-action-clear-iccr-eventlog";
    public static final String SERVER_ACTION_ICCR_RUN_IOTA_REFRESH = "svr-action-run-iota-refresh";
    public static final String SERVER_ACTION_IOTA_LOG = "svr-action-iota-log";

    public static final String IOTA_LOG_QP_DIRECTION_TAIL = "tail";
    public static final String IOTA_LOG_QP_DIRECTION_HEAD = "head";
    public static final Long IOTA_LOG_QP_NUMLINES_DEFAULT = 250L;

    public static final String DIALOG_ICC_DOWNLOAD_SAVE = "Save";
    public static final String MESSAGE_ICC_DOWNLOAD = "<html>" +
            "<p>Welcome to the Iota Control Center<br>Before you start,please set the download for the latest " +
            "IOTA version.<br>You can change the download link at any time<br> in the \"ICC setting\"" +
            "<br><br><br></p></html>";
    public static final String TEXT_FOR_TEXTFIELD = "IOTA client download link:";
}
