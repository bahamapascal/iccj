package org.iotacontrolcenter.ui.controller;


import org.iotacontrolcenter.dto.*;
import org.iotacontrolcenter.ui.app.Constants;
import org.iotacontrolcenter.ui.controller.worker.*;
import org.iotacontrolcenter.ui.dialog.IccrEventLogDialog;
import org.iotacontrolcenter.ui.dialog.IotaLogDialog;
import org.iotacontrolcenter.ui.dialog.ServerSettingsDialog;
import org.iotacontrolcenter.ui.panel.ServerPanel;
import org.iotacontrolcenter.ui.properties.locale.Localizer;
import org.iotacontrolcenter.ui.properties.source.PropertySource;
import org.iotacontrolcenter.ui.proxy.BadResponseException;
import org.iotacontrolcenter.ui.proxy.ServerProxy;
import org.iotacontrolcenter.ui.timer.RefreshIotaLogTailTimerTask;
import org.iotacontrolcenter.ui.timer.RefreshIotaNeighborTimerTask;
import org.iotacontrolcenter.ui.timer.RefreshIotaNodeinfoTimerTask;
import org.iotacontrolcenter.ui.util.UiUtil;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServerController implements ActionListener, TableModelListener {

    public Localizer localizer;
    private List<PropertyChangeListener> listeners = new ArrayList<>();
    private boolean iotaActive = false;
    public Properties iccrProps;
    public IotaLogDialog iotaLogDialog;
    public java.util.Timer iotaNeighborRefreshTimer;
    public java.util.Timer iotaNodeinfoRefreshTimer;
    public java.util.Timer iotaLogRefreshTimer;
    public boolean isConnected = false;
    public String name;
    public IccrIotaNeighborsPropertyDto nbrsDto;
    public PropertySource propertySource;
    public ServerProxy proxy;
    public ServerPanel serverPanel;
    public Properties serverProps;
    public ServerSettingsDialog serverSettingsDialog;


    public ServerController(Localizer localizer, ServerProxy proxy, Properties serverProps) {
        this.localizer = localizer;
        this.proxy = proxy;
        this.serverProps = serverProps;
        this.name = serverProps.getProperty(PropertySource.SERVER_NAME_PROP);
        propertySource = PropertySource.getInstance();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void firePropertyChange(String prop, Object prev, Object cur) {
        PropertyChangeEvent e = new PropertyChangeEvent(this, prop, prev, cur);
        for(PropertyChangeListener l : listeners) {
            try {
                l.propertyChange(e);
            }
            catch(Exception exc) {
            }
        }
    }

    public void serverSetup() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogConnectingToIccr"));

        SwingUtilities.invokeLater(() -> {
            initialConnect();
        });
    }

    public void serverTeardown() {
        stopRefreshTimers();
        stopIotaLogTimer();
    }

    private void initialConnect() {
        SwingWorker worker = getServerSettingProperties();

        worker.addPropertyChangeListener(e -> {
            if(e.getPropertyName().equals("state")) {
                SwingWorker.StateValue state = (SwingWorker.StateValue)e.getNewValue();
                if(state == SwingWorker.StateValue.DONE) {
                    if (isConnected) {
                        serverActionGetConfigNbrsList();
                        serverActionStatusIota();
                    }
                    if(propertySource.getRunIotaRefresh()) {
                        startRefreshTimers();
                    }
                }
            }
        });
    }

    public void setIotaActive(boolean active) {
        boolean wasActive = this.iotaActive;
        boolean changed = wasActive != active;
        this.iotaActive = active;

        firePropertyChange(Constants.IS_CONNECTED_EVENT, wasActive, active);
    }

    public void setConnected(boolean connected) {
        boolean wasConnected = this.isConnected;
        boolean changed = wasConnected != connected;
        this.isConnected = connected;

        if(changed) {
            System.out.println(this.name + " connect state changed, isConnected: " + connected);
            if (connected) {
                serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogIsConnectedToIccr"));
            }
            else {
                serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogNotConnectedToIccr"));
            }
        }
    }

    public void updateIotaRefresh() {
        stopRefreshTimers();

        if(propertySource.getRunIotaRefresh()) {
            startRefreshTimers();
        }
    }

    public void stopRefreshTimers() {
        if(iotaNeighborRefreshTimer != null) {
            iotaNeighborRefreshTimer.cancel();
            iotaNeighborRefreshTimer = null;
        }
        if(iotaNodeinfoRefreshTimer != null) {
            iotaNodeinfoRefreshTimer.cancel();
            iotaNodeinfoRefreshTimer = null;
        }
    }

    public void stopIotaLogTimer() {
        System.out.println(this.name + ", stopIotaLogTimer");
        if(iotaLogRefreshTimer != null) {
            iotaLogRefreshTimer.cancel();
            iotaLogRefreshTimer = null;
        }
    }

    public void startIotaLogTimer() {
        System.out.println(this.name + ", startIotaLogTimer");
        try {
            if(iotaLogRefreshTimer == null) {
                iotaLogRefreshTimer = new java.util.Timer();
                iotaLogRefreshTimer.schedule(new RefreshIotaLogTailTimerTask(this),
                        2000,
                        5000);
            }
        }
        catch(Exception e) {
            System.out.println(name + " startTimers iota nbrs refresh exception: " + e);
        }
    }

    public void startRefreshTimers() {
        System.out.println(name + " startRefreshTimers");
        try {
            if(iotaNodeinfoRefreshTimer == null) {
                iotaNodeinfoRefreshTimer = new java.util.Timer();
                iotaNodeinfoRefreshTimer.schedule(new RefreshIotaNodeinfoTimerTask(this),
                        1000,
                        propertySource.getInteger(PropertySource.REFRESH_NODEINFO_PROP) * 1000);
            }
        }
        catch(Exception e) {
            System.out.println(name + " startTimers iota nodeinfo refresh exception: " + e);
        }

        try {
            if(iotaNeighborRefreshTimer == null) {
                iotaNeighborRefreshTimer = new java.util.Timer();
                iotaNeighborRefreshTimer.schedule(new RefreshIotaNeighborTimerTask(this),
                        5000,
                        propertySource.getInteger(PropertySource.REFRESH_NBRS_PROP) * 1000);
            }
        }
        catch(Exception e) {
            System.out.println(name + " startTimers iota nbrs refresh exception: " + e);
        }
    }

    public void setServerPanel(ServerPanel serverPanel) {
        this.serverPanel = serverPanel;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        /*
        int changeType = e.getType();
        int row = e.getFirstRow();
        int col = e.getColumn();

        if(changeType == TableModelEvent.DELETE) {
            System.out.println(name + " server nbr table change event " + changeType +
                    ", deleted row: " + row);
        }
        else if(changeType == TableModelEvent.INSERT) {
            System.out.println(name + " Server nbr table change event " + changeType +
                    ", inserted row: " + row);

            NeighborDto nbr = serverPanel.neighborPanel.neighborModel.getRow(row);
            System.out.println(name + " new nbr: " + nbr);
        }
        else {
            System.out.println(name + " Server nbr table change event " + changeType +
                    ", row: " + row + ", col: " + col);

            NeighborDto nbr = serverPanel.neighborPanel.neighborModel.getRow(row);
            System.out.println(name + " updated nbr: " + nbr);
        }
        */

        serverPanel.neighborPanel.save.setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println(name + " Server controller action: " + action);

        if(action.equals(Constants.SERVER_ACTION_SETTINGS)) {
            showServerSettingsDialog();
        }
        else if(action.equals(Constants.DIALOG_SERVER_SETTINGS_CANCEL)) {
            serverSettingsDialogClose();
        }
        else if(action.equals(Constants.DIALOG_SERVER_SETTINGS_SAVE)) {
            serverSettingsDialogSave();
        }
        else if(action.equals(Constants.SERVER_ACTION_INSTALL_IOTA)) {
            serverActionInstallIota();
        }
        else if(action.equals(Constants.SERVER_ACTION_START_IOTA)) {
            serverActionStartIota();
        }
        else if(action.equals(Constants.SERVER_ACTION_STATUS_IOTA)) {
            serverActionStatusIota();
        }
        else if(action.equals(Constants.SERVER_ACTION_STOP_IOTA)) {
            serverActionStopIota();
        }
        else if(action.equals(Constants.SERVER_ACTION_START_WALLET)) {
            serverActionStartWallet();
        }
        else if(action.equals(Constants.SERVER_ACTION_DELETEDB_IOTA)) {
            serverActionDeleteDb();
        }
        else if(action.equals(Constants.SERVER_ACTION_UNINSTALL_IOTA)) {
            serverActionDeleteIota();
        }
        else if(action.equals(Constants.NEIGHBOR_PANEL_SAVE_CHANGES)) {
            nbrPanelSave();
        }
        else if(action.equals(Constants.NEIGHBOR_PANEL_REMOVE_SELECTED)) {
            nbrPanelRemoveSelected();
        }
        else if(action.equals(Constants.NEIGHBOR_PANEL_ADD_NEW)) {
            nbrPanelAddNew();
        }
        else if(action.equals(Constants.SERVER_ACTION_ICCR_EVENTLOG)) {
            getIccrEventLog();
        }
        else if(action.equals(Constants.SERVER_ACTION_CLEAR_ICCR_EVENTLOG)) {
            deleteIccrEventLog();
        }
        else if(action.equals(Constants.SERVER_ACTION_IOTA_LOG)) {
             openIotaLogDialog();
        }
        else if(action.equals(Constants.DIALOG_IOTA_LOG_HEAD)) {
            onIotaLogHead();
        }
        else if(action.equals(Constants.DIALOG_IOTA_LOG_HEAD_MORE)) {
            onIotaLogHeadMore();
        }
        else if(action.equals(Constants.DIALOG_IOTA_LOG_TAIL)) {
            onIotaLogTail();
        }
        else if(action.equals(Constants.DIALOG_IOTA_LOG_TAIL_PLAY)) {
            onIotaLogTailPlay();
        }
        else if(action.equals(Constants.DIALOG_IOTA_LOG_TAIL_PAUSE)) {
            onIotaLogTailPause();
        }
        else {
            // TODO: localization
            System.out.println(name + " server controller, unrecognized action: " + action);
            UiUtil.showErrorDialog("Action Error", "Unrecognized action: " + action);
        }
    }

    public void enableIotaRefresh(boolean enable) {
        if(!enable) {
            stopRefreshTimers();
        }
        else {
            startRefreshTimers();
        }

    }

    public void deleteIccrEventLog() {
        try {
            proxy.deleteIccrEventLog();
        }
        catch(BadResponseException bre) {
            System.out.println("deleteIccrEventLog: bad response: " + bre.errMsgkey +
                    ", " + bre.resp.getMsg());
            /*
            UiUtil.showErrorDialog(localizer.getLocalText(bre.errMsgkey),
                    bre.resp.getMsg());
            */

            //serverPanel.addConsoleLogLine(bre.resp.getMsg());
        }
        catch(Exception e) {
            System.out.println("deleteIccrEventLog exception from proxy: ");
            e.printStackTrace();

            /*
            UiUtil.showErrorDialog(localizer.getLocalText("installIotaError"),
                    localizer.getLocalText("iccrApiException") + ": " + e.getLocalizedMessage());

            serverPanel.addConsoleLogLine(e.getLocalizedMessage());
            */
        }
    }


    public void getIccrEventLog() {
        System.out.println(name + " getIccrEventLog");

        try {
            List<String> log = proxy.getIccrEventLog();

            IccrEventLogDialog dialog = new IccrEventLogDialog(localizer,
                    localizer.getLocalText("dialogTitleIccrEventLog"), this);

            for(String s : log) {
                s = s.replaceAll(",","   ");
                dialog.eventText.append(s + "\n");
            }

            dialog.setLocationRelativeTo(serverPanel);

            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    //ctlr.serverSettingsDialog = null;
                }
            });

            dialog.setVisible(true);
        }
        catch(BadResponseException bre) {
            System.out.println("getIccrEventLog: bad response: " + bre.errMsgkey +
                    ", " + bre.resp.getMsg());
            /*
            UiUtil.showErrorDialog(localizer.getLocalText(bre.errMsgkey),
                    bre.resp.getMsg());
            */

            serverPanel.addConsoleLogLine(bre.resp.getMsg());
        }
        catch(Exception e) {
            System.out.println("getIccrEventLog exception from proxy: ");
            e.printStackTrace();

            /*
            UiUtil.showErrorDialog(localizer.getLocalText("installIotaError"),
                    localizer.getLocalText("iccrApiException") + ": " + e.getLocalizedMessage());

            serverPanel.addConsoleLogLine(e.getLocalizedMessage());
            */
        }
    }

    public void getIotaLogUpdate(String fileDirection) {
        System.out.println(name + " getIotaLogUpdate");

        if(iotaLogDialog == null) {
            return;
        }

        LogLinesResponse resp = null;
        try {
            resp = proxy.getIotaLog(fileDirection,
                    Constants.IOTA_LOG_QP_NUMLINES_DEFAULT,
                    iotaLogDialog.refreshLastFileSize,
                    iotaLogDialog.refreshLastFilePosition);

            if (resp.isSuccess()) {
                for (String s : resp.getLines()) {
                    iotaLogDialog.logText.append(s + "\n");
                }
                iotaLogDialog.refreshLastFilePosition =  resp.getLastFilePosition();
                iotaLogDialog.refreshLastFileSize =  resp.getLastFileSize();
            }
            else {
                System.out.println("getIotaLogTailUpdate: bad response: " + resp.getMsg());
                serverPanel.addConsoleLogLine(resp.getMsg());
            }
        }
        catch(BadResponseException bre) {
            System.out.println("getIotaLogUpdate: bad response: " + bre.errMsgkey +
                    ", " + bre.resp.getMsg());
            serverPanel.addConsoleLogLine(bre.resp.getMsg());
        }
        catch(Exception e) {
            System.out.println("getIotaLogUpdate exception from proxy: ");
            e.printStackTrace();
        }
    }

    private void openIotaLogDialog() {
        System.out.println(name + " openIotaLogDialog");

        startIotaLogTimer();

        AbstractSwingWorker worker = new AbstractSwingWorker(this) {
            @Override
            public Void runIt() {
                iotaLogDialog = new IotaLogDialog(localizer,
                        localizer.getLocalText("dialogTitleIotaLog"), ctlr);
                iotaLogDialog.setLocationRelativeTo(serverPanel);

                iotaLogDialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        super.windowClosed(e);
                        stopIotaLogTimer();
                        iotaLogDialog = null;
                    }
                });
                iotaLogDialog.setVisible(true);
                return null;
            }
        };
        worker.execute();
    }

    private void onIotaLogHead() {
        System.out.println(name + " onIotaLogHead");
        if(iotaLogDialog == null) {
            System.out.println(name + " onIotaLogHead, dialog not found");
            return;
        }

        iotaLogDialog.logText.setText("");
        iotaLogDialog.headAdd.setEnabled(true);

        iotaLogDialog.tail.setSelected(false);
        iotaLogDialog.tailPlay.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PLAY_UNPRESSED));
        iotaLogDialog.tailPlay.setEnabled(false);

        iotaLogDialog.tailPause.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PAUSE_UNPRESSED));
        iotaLogDialog.tailPause.setEnabled(false);

        iotaLogDialog.refreshLastFileSize = null;
        iotaLogDialog.refreshLastFilePosition = null;

        doIotaLogHead();
    }

    // Tail clicked when in head
    private void onIotaLogTail() {
        System.out.println(name + " onIotaLogTail");
        if(iotaLogDialog == null) {
            System.out.println(name + " onIotaLogTail, dialog not found");
            return;
        }
        iotaLogDialog.logText.setText("");

        iotaLogDialog.head.setSelected(false);
        iotaLogDialog.headAdd.setEnabled(false);

        iotaLogDialog.tailPlay.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PLAY_PRESSED));
        iotaLogDialog.tailPlay.setEnabled(true);
        iotaLogDialog.tailPlay.setSelected(true);

        iotaLogDialog.tailPause.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PAUSE_UNPRESSED));
        iotaLogDialog.tailPause.setEnabled(true);
        iotaLogDialog.tailPause.setSelected(false);

        iotaLogDialog.refreshLastFileSize = null;
        iotaLogDialog.refreshLastFilePosition = null;

        startIotaLogTimer();
    }

    private void onIotaLogHeadMore() {
        System.out.println(name + " onIotaLogHeadMore");
        if(iotaLogDialog == null) {
            System.out.println(name + " onIotaLogHead, dialog not found");
            return;
        }
        doIotaLogHeadMore();
    }

    // Play clicked when already in play
    private void onIotaLogTailPlay() {
        System.out.println(name + " onIotaLogTailPlay");
        if(iotaLogDialog == null) {
            System.out.println(name + " onIotaLogHead, dialog not found");
            return;
        }
        if(iotaLogDialog.tailPlay.isSelected()) {
            return;
        }
        iotaLogDialog.tailPlay.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PLAY_PRESSED));
        iotaLogDialog.tailPlay.setSelected(true);

        iotaLogDialog.tailPause.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PAUSE_UNPRESSED));
        iotaLogDialog.tailPause.setSelected(false);

        startIotaLogTimer();
    }

    private void onIotaLogTailPause() {
        System.out.println(name + " onIotaLogTailPause");
        if(iotaLogDialog == null) {
            System.out.println(name + " onIotaLogHead, dialog not found");
            return;
        }
        if(iotaLogDialog.tailPause.isSelected()) {
            return;
        }
        iotaLogDialog.tailPause.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PAUSE_PRESSED));
        iotaLogDialog.tailPause.setSelected(true);

        iotaLogDialog.tailPlay.setIcon(UiUtil.loadIcon(Constants.IMAGE_ICON_FILENAME_PLAY_UNPRESSED));
        iotaLogDialog.tailPlay.setSelected(false);

        doIotaLogTailPause();
    }

    private boolean nbrPanelRemoveSelected() {

        int row = serverPanel.neighborPanel.neighborTable.getSelectedRow();
        System.out.println(name + " nbrPanelRemoveSelected, row: " + row);

        if(row < 0) {
            UiUtil.showErrorDialog(localizer.getLocalText("dialogNbrErrorTitle"),
                    localizer.getLocalText("dialogNbrErrorUnselectedMsg"));
            return false;
        }

        NeighborDto nbr = serverPanel.neighborPanel.neighborModel.getRow(row);
        String who = nbr.getDescr() != null &&  !nbr.getDescr().isEmpty()  ? nbr.getDescr() : "";

        if(!UiUtil.promptUserYorN(localizer.getLocalText("removeNbrPromptTitle"),
                localizer.getLocalText("removeNbrPromptMsg") + " " + who)) {
            return false;
        }

        serverPanel.neighborPanel.neighborModel.removeNeighbor(row);

        serverPanel.neighborPanel.save.setEnabled(true);

        return true;
    }

    private void doIotaLogHead() {
        System.out.println(this.name + ", doIotaLogHead");
        stopIotaLogTimer();

        AbstractSwingWorker worker = new AbstractSwingWorker(this) {
            @Override
            public Void runIt() {
                ctlr.getIotaLogUpdate(Constants.IOTA_LOG_QP_DIRECTION_HEAD);
                return null;
            }
        };
        worker.execute();
    }

    private void doIotaLogHeadMore() {
        System.out.println(this.name + ", doIotaLogHeadMore");

        AbstractSwingWorker worker = new AbstractSwingWorker(this) {
            @Override
            public Void runIt() {
                ctlr.getIotaLogUpdate(Constants.IOTA_LOG_QP_DIRECTION_HEAD);
                return null;
            }
        };
        worker.execute();
    }

    private void startIotaLogTailPlay() {
        System.out.println(this.name + ", doIotaLogTailPlay");

        startIotaLogTai
    }

    private void doIotaLogTailPause() {
        System.out.println(this.name + ", doIotaLogTailPause");
        stopIotaLogTimer();
    }

    private boolean nbrPanelAddNew() {
        boolean isSuccess = false;

        NeighborDto nbr = new NeighborDto();
        nbr.setActive(true);
        nbr.setKey(propertySource.getNowDateTimestamp());
        String uri = "udp://0.0.0.0";
        /*
        nbr.setScheme("udp");
        nbr.setIp("0.0.0.0");
        if(iccrProps !=  null && !iccrProps.getProperty("iotaPortNumber").isEmpty()) {
            nbr.setPort(Integer.valueOf(iccrProps.getProperty("iotaPortNumber")));
        }
        */

        if(iccrProps !=  null && !iccrProps.getProperty("iotaPortNumber").isEmpty()) {
            uri += ":" + iccrProps.getProperty("iotaPortNumber");
        }
        nbr.setUri(uri);

        serverPanel.neighborPanel.neighborModel.addNeighbor(nbr);

        serverPanel.neighborPanel.save.setEnabled(true);

        return isSuccess;
    }

    private void nbrPanelSave() {
        boolean isSuccess = false;

        /*
        if(nbrToAdd.isEmpty() && nbrToRemove.isEmpty()) {
            UiUtil.showErrorDialog(localizer.getLocalText("dialogNbrNoChangesTitle"),
                    localizer.getLocalText("dialogNbrErrorNothingToSaveMsg"));
            return false;
        }
        */
        IccrIotaNeighborsPropertyDto nbrs = new IccrIotaNeighborsPropertyDto();

        String errors = "";
        String sep = "";
        for(NeighborDto nbr : serverPanel.neighborPanel.neighborModel.nbrs) {
            System.out.println(name + " saving nbr: " + nbr);
            if(nbr.getUri() == null || nbr.getUri().isEmpty()) {
                // nbr.getIp().equals("0.0.0.0") ) {
                //!UiUtil.isValidIpV4(nbr.getIp())) {
                errors += sep + localizer.getLocalText("neighborTableIpError");
                if(sep.isEmpty()) {
                    sep = "\n";
                }
            }
            /*
            if(nbr.getScheme() == null || nbr.getScheme().isEmpty()) {
                errors += sep + localizer.getLocalText("neighborTableSchemeError");
                if(sep.isEmpty()) {
                    sep = "\n";
                }
            }
            if(nbr.getPort() <= 0) {
                errors += sep + localizer.getLocalText("neighborTablePortError");
                if(sep.isEmpty()) {
                    sep = "\n";
                }
            }
            */
            if(nbr.getDescr() == null) {
                nbr.setDescr("");
            }
            nbrs.addNeighbor(nbr);
        };

        if(!errors.isEmpty()) {
            UiUtil.showErrorDialog("neighborSaveErrorTitle", errors);
            return;
        }

        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallIccrSetNeighborsList"));

        SetNbrsConfigPropertyWorker worker = new SetNbrsConfigPropertyWorker(localizer, serverPanel,  proxy, this, nbrs);
        worker.execute();
    }

    private boolean serverActionInstallIota() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallInstallIota"));

        SwingUtilities.invokeLater(() -> {
            doInstallIota();
        });
        return true;
    }

    private void doInstallIota() {
        boolean isSuccess = false;

        IccrPropertyListDto actionProps = new IccrPropertyListDto();
        actionProps.addProperty(new IccrPropertyDto(PropertySource.IOTA_DLD_LINK_PROP,
                propertySource.getIotaDownloadLink()));

        InstallIotaWorker worker = new InstallIotaWorker(localizer, serverPanel,  proxy, this,
                                            Constants.IOTA_ACTION_INSTALL, actionProps);

        worker.addPropertyChangeListener(e -> {
            if(e.getPropertyName().equals("state")) {
                SwingWorker.StateValue state = (SwingWorker.StateValue)e.getNewValue();
                if(state == SwingWorker.StateValue.DONE) {
                    serverActionStatusIota();
                }
            }
        });

        worker.execute();
    }

    public void serverActionGetIotaNeighbors() {
        //serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallRefreshIotaNeighbors") + "...");
        IotaNeighborsWorker worker = new IotaNeighborsWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_NEIGHBORS, null);
        worker.execute();
    }

    public void serverActionGetIotaNodeinfo() {
        //serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallRefreshIotaNodeinfo") + "...");

        IotaNodeinfoWorker worker = new IotaNodeinfoWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_NODEINFO, null);
        worker.execute();
    }

    public void serverActionStatusIota() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallStatusIota") + "...");

        StatusIotaWorker worker = new StatusIotaWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_STATUS, null);
        worker.execute();
    }

    private void serverActionStartIota() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallStartIota"));

        SwingUtilities.invokeLater(() -> {
            doStartIota();
        });
    }


    private void doStartIota() {

        StartIotaWorker worker = new StartIotaWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_START, null);
        worker.execute();
    }

    private void serverActionStopIota() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallStopIota"));

        SwingUtilities.invokeLater(() -> {
            doStopIota();
        });
    }

    private void doStopIota() {
        StopIotaWorker worker = new StopIotaWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_STOP, null);
        worker.execute();
    }

    private boolean serverActionStartWallet() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallStartWallet"));
        boolean isSuccess = false;
        // TODO
        /*
        try {
            proxy.();
        }
        catch(BadResponseException bre) {
            System.out.println("installIota: bad response: " + bre.errMsgkey +
                    ", " + bre.resp.getMsg());
            UiUtil.showErrorDialog(localizer.getLocalText(bre.errMsgkey),
                    bre.resp.getMsg());

            serverPanel.addConsoleLogLine(bre.resp.getMsg());
        }
        catch(Exception e) {
            System.out.println("installIota exception from proxy: ");
            e.printStackTrace();

            UiUtil.showErrorDialog(localizer.getLocalText("installIotaError"),
                    localizer.getLocalText("iccrApiException") + ": " + e.getLocalizedMessage());

            serverPanel.addConsoleLogLine(e.getLocalizedMessage());
        }
        */
        return isSuccess;
    }

    private void serverActionDeleteDb() {
        if(!UiUtil.promptUserYorN(localizer.getLocalText("deleteIotaDbPromptTitle"),
                localizer.getLocalText("deleteIotaDbPromptMsg"))) {
            return;
        }

        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallDeleteDb"));

        DeleteIotaDbWorker worker = new DeleteIotaDbWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_DELETEDB, null);
        worker.execute();
    }

    private void serverActionDeleteIota() {
        if(!UiUtil.promptUserYorN(localizer.getLocalText("deleteIotaPromptTitle"),
                localizer.getLocalText("deleteIotaPromptMsg"))) {
            return;
        }

        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallDeleteIota"));

        DeleteIotaWorker worker = new DeleteIotaWorker(localizer, serverPanel,  proxy, this,
                Constants.IOTA_ACTION_DELETE, null);
        worker.execute();
    }

    private void serverSettingsDialogSave() {
        if(serverSettingsDialog == null) {
            // TODO: localization
            System.out.println(name + " serverSettingsDialogSave: dialog found");
            UiUtil.showErrorDialog("Settings Save Fail", "Dialog not found");
            return;
        }
        Properties newProps = new Properties();
        if(serverSettingsDialog.propList == null ||
                serverSettingsDialog.propList.isEmpty()) {
            // TODO: localization
            System.out.println(name + " serverSettingsDialogSave: prop list found");
            UiUtil.showErrorDialog("Settings Save Fail", "Prop list not found");
            return;
        }

        boolean isChanged = false;

        String iccrPortNumber = serverSettingsDialog.iccrPortTextField.getText();
        if (!serverSettingsDialog.iccrProps.getProperty("iccrPortNumber").equals((iccrPortNumber))) {
            newProps.setProperty("iccrPortNumber", iccrPortNumber);
            isChanged = true;
        }

        String iotaDir = serverSettingsDialog.iotaFolderTextField.getText();
        if (!serverSettingsDialog.iccrProps.getProperty("iotaDir").equals((iotaDir))) {
            newProps.setProperty("iotaDir", iotaDir);
            isChanged = true;
        }

        String iotaNeighborRefreshTime = serverSettingsDialog.nbrRefreshTextField.getText();
        if (!serverSettingsDialog.iccrProps.getProperty("iotaNeighborRefreshTime").equals((iotaNeighborRefreshTime))) {
            newProps.setProperty("iotaNeighborRefreshTime", iotaNeighborRefreshTime);
            isChanged = true;
        }

        String iotaPortNumber = serverSettingsDialog.iotaPortTextField.getText();
        if (!serverSettingsDialog.iccrProps.getProperty("iotaPortNumber").equals((iotaPortNumber))) {
            newProps.setProperty("iotaPortNumber", iotaPortNumber);
            isChanged = true;
        }

        String iotaStartCmd = serverSettingsDialog.iotaStartTextField.getText();
        if (!serverSettingsDialog.iccrProps.getProperty("iotaStartCmd").equals((iotaStartCmd))) {
            newProps.setProperty("iotaStartCmd", iotaStartCmd);
            isChanged = true;
        }

        String errors = "";
        String sep = "";
        boolean isError = false;
        if(iccrPortNumber == null || iccrPortNumber.isEmpty() || !UiUtil.isValidPositiveNumber(iccrPortNumber)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    serverSettingsDialog.iccrPortTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(iotaPortNumber == null || iotaPortNumber.isEmpty() || !UiUtil.isValidPositiveNumber(iotaPortNumber)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    serverSettingsDialog.iotaPortTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(iotaDir == null || iotaDir.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    serverSettingsDialog.iotaFolderTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(iotaNeighborRefreshTime == null || iotaNeighborRefreshTime.isEmpty() || !UiUtil.isValidNumber(iotaNeighborRefreshTime)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    serverSettingsDialog.nbrRefreshTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }
        if(iotaStartCmd == null || iotaStartCmd.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    serverSettingsDialog.iotaStartTextField.getName();
            if(sep.isEmpty()) {
                sep = "\n";
            }
        }

        if(isError) {
            UiUtil.showErrorDialog("dialogSaveErrorTitle", errors);
            return;
        }

        if(!isChanged) {
            UiUtil.showErrorDialog("Server Settings", localizer.getLocalText("settingsUnchanged"));
            return;
        }

        serverSettingsDialogClose();

        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallIccrSetConfig"));

        try {
            proxy.iccrSetConfig(newProps);
        }
        catch(BadResponseException bre) {
            System.out.println(name + " serverSettingsDialogSave: bad response: " + bre.errMsgkey +
                    ", " + bre.resp.getMsg());

            serverPanel.addConsoleLogLine(localizer.getLocalText(bre.errMsgkey));
            serverPanel.addConsoleLogLine(bre.resp.getMsg());

            UiUtil.showErrorDialog(localizer.getLocalText(bre.errMsgkey),
                    bre.resp.getMsg());

        }
        catch(Exception e) {
            System.out.println(name + " serverSettingsDialogSave exception from proxy: ");
            e.printStackTrace();

            serverPanel.addConsoleLogLine(localizer.getLocalText("iccrSetConfigError"));
            serverPanel.addConsoleLogLine(e.getLocalizedMessage());

            UiUtil.showErrorDialog(localizer.getLocalText("iccrSetConfigError"),
                    localizer.getLocalText("iccrApiException") + ": " + e.getLocalizedMessage());

        }
    }

    private void showServerSettingsDialog() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallIccrGetConfig") + "...");


        ShowServerSettingsDialogWorker worker = new ShowServerSettingsDialogWorker(localizer, serverPanel, proxy, this);
        worker.execute();
    }

    public void serverActionGetConfigNbrsList() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallIccrGetNeighborsList"));

        GetIccrIotaNeighborsWorker worker = new GetIccrIotaNeighborsWorker(localizer, serverPanel, proxy, this);
        worker.execute();
    }

    private SwingWorker getServerSettingProperties() {
        serverPanel.addConsoleLogLine(localizer.getLocalText("consoleLogApiCallIccrGetConfig") + "...");

        GetIccrConfigWorker worker = new GetIccrConfigWorker(localizer, serverPanel, proxy, this);
        worker.execute();
        return worker;
    }

    private void serverSettingsDialogClose() {
        if(serverSettingsDialog != null) {
            serverSettingsDialog.setVisible(false);
            serverSettingsDialog.dispose();
            serverSettingsDialog = null;
        }
    }

    private String getActionStatusFromResponse(String key, ActionResponse ar) {
        String val = null;
        if(ar != null && ar.getProperties() !=  null) {
            for (IccrPropertyDto prop : ar.getProperties()) {
                System.out.println(name + ": " + prop.getKey() + " -> " + prop.getValue());
                if (prop.getKey().equals(key)) {
                    val = prop.getValue();
                    break;
                }
            }
        }
        return val;
    }
}
