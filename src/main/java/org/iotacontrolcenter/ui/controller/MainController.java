package org.iotacontrolcenter.ui.controller;


import org.iotacontrolcenter.ui.app.Constants;
import org.iotacontrolcenter.ui.dialog.ConfigureServerDialog;
import org.iotacontrolcenter.ui.dialog.IccDownloadDialogue;
import org.iotacontrolcenter.ui.dialog.IccSettingsDialog;
import org.iotacontrolcenter.ui.dialog.OpenServerDialog;
import org.iotacontrolcenter.ui.panel.ServerPanel;
import org.iotacontrolcenter.ui.panel.ServerTabPanel;
import org.iotacontrolcenter.ui.properties.locale.Localizer;
import org.iotacontrolcenter.ui.properties.source.PropertySource;
import org.iotacontrolcenter.ui.proxy.ServerProxy;
import org.iotacontrolcenter.ui.util.UiUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import static org.iotacontrolcenter.ui.properties.source.PropertySource.IOTA_DLD_LINK_PROP;

public class MainController implements ActionListener {

    private IccSettingsDialog iccSettingsDialog;
    private Localizer localizer;
    private IccDownloadDialogue iccDownloadDialogue;
    private ServerTabPanel serverTabPanel;
    private ConfigureServerDialog cfgServerDialog;
    private OpenServerDialog openServerDialog;
    private PropertySource propertySource = PropertySource.getInstance();

    public void init() {
        localizer = Localizer.getInstance();
    }

    public void initialPrompts() {
        /*
        Properties props = propertySource.getLocalServerProperties();

        if(props == null) {
            UiUtil.showInfoDialog(localizer.getLocalText("initialAddServerTitle"),
                    localizer.getLocalText("initialAddServerMsg"));
        }
        else {
            boolean addLocal = UiUtil.promptUserYorN(localizer.getLocalText("openServerPromptTitle"),
                    localizer.getLocalText("openServerPromptMsg") + " " +
                    props.getProperty(PropertySource.SERVER_NAME_PROP));
            if (addLocal) {
                addServerTabPanel(props);
            }
        }
        */
        if (propertySource.getNumServers() == 0) {
            Integer iotaDownloadLink = propertySource.getString(IOTA_DLD_LINK_PROP).length();
            if(iotaDownloadLink == 0){
                showIccDownloadDialogue();
            }
            UiUtil.showInfoDialog(localizer.getLocalText("initialAddServerTitle"),
                    localizer.getLocalText("initialAddServerMsg"));
        } else {
            showOpenServerDialog();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        System.out.println(action);
        if (action.equals(Constants.MM_ADD_SERVER_ACTION)) {
            showAddOrEditServerDialog(localizer.getLocalText("dialogTitleAddServer"), null,
                    null, true);
        } else if (action.equals(Constants.MM_OPEN_SERVER_ACTION)) {
            showOpenServerDialog();
        } else if (action.equals(Constants.MM_ICC_SETTINGS_ACTION)) {
            showIccSettingsDialog();
        } else if (action.equals(Constants.DIALOG_CONFIG_ADD_SERVER_SAVE)) {
            cfgServerDialogSave(action);
        } else if (action.equals(Constants.DIALOG_CONFIG_EDIT_SERVER_SAVE)) {
            cfgServerDialogSave(action);
        } else if (action.equals(Constants.DIALOG_CONFIG_SERVER_CANCEL)) {
            cfgServerDialogClose();
        } else if (action.equals(Constants.DIALOG_ICC_DOWNLOAD_SAVE)) {
            iccDownloadDialogClose();
        } else if (action.equals(Constants.DIALOG_ICC_SETTINGS_CANCEL)) {
            iccSettingsDialogClose();
        } else if (action.equals(Constants.DIALOG_OPEN_SERVER_CLOSE)) {
            int sizeofSelectedList = openServerDialog.serverList.getSelectedValuesList().size();
            if (sizeofSelectedList == 0) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogCloseServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
            } else {
                boolean selection = UiUtil.promptUserYorN(localizer.getLocalText("dialogCloseTitle"),
                        localizer.getLocalText("closeSelectedServers"));
                if (selection) {
                    for (String serverName : openServerDialog.serverList.getSelectedValuesList()) {
                        try {
                            iccCloseOpenServer(serverName);
                        } catch (IllegalStateException ise) {
                            System.out.println("Ill State Exception  server: " + ise.getLocalizedMessage());
                            UiUtil.showErrorDialog(localizer.getLocalText("dialogCloseServerErrorTitle"),
                                    ise.getMessage());
                        }
                    }
                }
            }
        } else if (action.equals(Constants.DIALOG_ICC_SETTINGS_SAVE)) {
            iccSettingsDialogSave();
        } else if (action.equals(Constants.DIALOG_OPEN_SERVER_OPEN)) {
            int sizeofSelectedList = openServerDialog.serverList.getSelectedValuesList().size();
            if (sizeofSelectedList == 0) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogOpenServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
                return;
            }
            for (String serverName : openServerDialog.serverList.getSelectedValuesList()) {
                openSelectedServer(serverName);
            }

        } else if (action.equals(Constants.DIALOG_OPEN_SERVER_EDIT)) {
            String serverName = openServerDialog.serverList.getSelectedValue();
            if (serverName == null || serverName.isEmpty()) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogEditServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
                return;
            }

            openServerDialogClose();

            Properties serverProps = propertySource.getServerPropertiesForServerName(serverName);

            showAddOrEditServerDialog(localizer.getLocalText("dialogTitleEditServer"), serverName, serverProps,
                    false);
        } else if (action.equals(Constants.DIALOG_OPEN_SERVER_ADD_SERVER)) {
            openServerDialogClose();
            showAddOrEditServerDialog(localizer.getLocalText("dialogTitleAddServer"), null,
                    null, true);
        } else if (action.equals(Constants.DIALOG_OPEN_SERVER_REMOVE)) {
            int serverName = openServerDialog.serverList.getSelectedValuesList().size();
            if (serverName == 0) {
                UiUtil.showErrorDialog(localizer.getLocalText("dialogRemoveServerErrorTitle"),
                        localizer.getLocalText("dialogServerErrorUnselected"));
                return;
            } else {
                boolean selection = UiUtil.promptUserYorN(localizer.getLocalText("dialogDeleteTitle"),
                        localizer.getLocalText("deleteSelectedServers"));
                if (selection) {
                    for (String selectedServer : openServerDialog.serverList.getSelectedValuesList()) {
                        openServerDialog.serverListModel.remove(propertySource.getServerIds().indexOf(selectedServer));
                        removeSelectedServer(selectedServer);
                    }
                }
            }


        } else if (action.equals(Constants.DIALOG_OPEN_SERVER_CANCEL)) {
            openServerDialogClose();
        }
        // No, change to run refresh will be activated on the icc dialog save
        /*
        else if(action.equals(Constants.SERVER_ACTION_ICCR_RUN_IOTA_REFRESH)) {
            toggleIotaRefresh();
        }
        */
    }

    public void enableIotaRefresh(boolean enable) {
        serverTabPanel.enableIotaRefresh(enable);
    }

    public void setServerTabPanel(ServerTabPanel serverTabPanel) {
        this.serverTabPanel = serverTabPanel;
    }

    private void iccCloseOpenServer(String serverName) {
        Properties serverProps = propertySource.getServerPropertiesForServerName(serverName);
        String name = serverProps.getProperty(PropertySource.SERVER_NAME_PROP);
        if (!serverTabPanel.serverIsOpen(name)) {
            throw new IllegalStateException(localizer.getLocalTextWithInsertValue(
                    "dialogServer", "dialogServerAlreadyClosed",
                    serverName));
        } else {
            boolean wasOpen = serverTabPanel.serverIsOpen(serverName);
            if (wasOpen) {
                serverTabPanel.removeServerTabByName(serverName);
            }
        }
    }

    private boolean iccSettingsDialogSave() {
        if (iccSettingsDialog == null) {
            System.out.println("iccSettingsDialog not found!");

            // TODO: localization
            UiUtil.showErrorDialog("ICC Settings Save Error", "Dialog not found!");
            return false;
        }

        String nbrRefresh = iccSettingsDialog.nbrRefreshTimeTextField.getText();
        String nodeInfoRefresh = iccSettingsDialog.nodeInfoRefreshTimeTextField.getText();
        String iotaDldLink = iccSettingsDialog.iotaDownloadLinkTextField.getText();
        boolean runRefresh = iccSettingsDialog.runRefresh.isSelected();

        String errors = "";
        String sep = "";
        boolean isError = false;
        if (nbrRefresh == null || nbrRefresh.isEmpty() || !UiUtil.isValidPositiveNumber(nbrRefresh)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.nbrRefreshTimeTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }
        if (nodeInfoRefresh == null || nodeInfoRefresh.isEmpty() ||
                !UiUtil.isValidPositiveNumber(nodeInfoRefresh)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.nodeInfoRefreshTimeTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }
        if (iotaDldLink == null || iotaDldLink.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    iccSettingsDialog.iotaDownloadLinkTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }

        if (isError) {
            UiUtil.showErrorDialog(localizer.getLocalText("dialogSaveIccErrorTitle"), errors);
            return false;
        }

        boolean refreshChanged = !nbrRefresh.equals(propertySource.getRefreshNbrsTime()) ||
                !nodeInfoRefresh.equals(propertySource.getRefreshNodeInfoTime()) ||
                runRefresh != propertySource.getRunIotaRefresh();
        propertySource.setProperty(PropertySource.REFRESH_NBRS_PROP, nbrRefresh);
        propertySource.setProperty(PropertySource.REFRESH_NODEINFO_PROP, nodeInfoRefresh);
        propertySource.setProperty(IOTA_DLD_LINK_PROP, iotaDldLink);
        propertySource.setProperty(PropertySource.RUN_IOTA_REFRESH_PROP, String.valueOf(runRefresh).toLowerCase());

        propertySource.storeProperties();

        iccSettingsDialogClose();

        if (refreshChanged) {
            System.out.println("Changed IOTA refresh settings");
            serverTabPanel.updateIotaRefresh();
        }

        return true;
    }

    private void iccDownloadDialogClose() {
        if (iccDownloadDialogue != null) {
            String downloadLink = iccDownloadDialogue.iotaDownloadTextField.getText();
            iccDownloadDialogue.dispose();
            iccDownloadDialogue = null;
            propertySource.setIotaDownloadLink(IOTA_DLD_LINK_PROP, downloadLink);
            propertySource.setProperty(IOTA_DLD_LINK_PROP, downloadLink);
            propertySource.storeProperties();
        }
    }

    private void iccSettingsDialogClose() {
        if (iccSettingsDialog != null) {
            iccSettingsDialog.dispose();
            iccSettingsDialog = null;
        }
    }

    private void openSelectedServer(String serverName) {
        try {
            addServerTabPanel(propertySource.getServerPropertiesForServerName(serverName), serverName);
        } catch (IllegalStateException ise) {
            System.out.println("Ill State Exception opening server: " + ise.getLocalizedMessage());
            UiUtil.showErrorDialog(localizer.getLocalText("dialogOpenServerErrorTitle"), ise.getMessage());
            return;
        }
        openServerDialogClose();
    }

    private void removeSelectedServer(String serverName) {
        boolean wasOpen = serverTabPanel.serverIsOpen(serverName);
        if (wasOpen) {
            serverTabPanel.removeServerTabByName(serverName);
        }

        boolean doDelete = false;
        if (wasOpen) {
            if (UiUtil.promptUserYorN(localizer.getLocalText("deleteServerConfigPromptTitle"),
                    localizer.getLocalText("deleteServerConfigPromptMsg"))) {
                doDelete = true;
            }
        } else {
            doDelete = true;
        }
        if (doDelete) {
            propertySource.removeServerConfigByName(serverName);
        }
    }

    private void showIccDownloadDialogue() {
        iccDownloadDialogue = new IccDownloadDialogue(localizer, this);
        iccDownloadDialogue.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                iccDownloadDialogue = null;
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                iccDownloadDialogue.isShowing();
            }
        });
        iccDownloadDialogue.setVisible(true);

    }

    private void showIccSettingsDialog() {
        iccSettingsDialog = new IccSettingsDialog(localizer, propertySource, this);
        iccSettingsDialog.setLocationRelativeTo(serverTabPanel);

        iccSettingsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                iccSettingsDialog = null;
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                iccSettingsDialog.nbrRefreshTimeTextField.requestFocusInWindow();
            }
        });

        iccSettingsDialog.setVisible(true);
    }

    private void showAddOrEditServerDialog(String title, String serverName, Properties serverProps, boolean isAdd) {
        // TODO: localization
        if (serverName != null && serverProps == null) {
            System.out.println("server properties is null for serverName " + serverName);
            UiUtil.showErrorDialog("Settings Error",
                    "Server properties not found!");
            return;
        }

        cfgServerDialog = new ConfigureServerDialog(localizer, title, this, serverProps, isAdd);
        cfgServerDialog.setLocationRelativeTo(serverTabPanel);

        cfgServerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                cfgServerDialog = null;
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                cfgServerDialog.serverIpTextField.requestFocusInWindow();
            }
        });

        if (isAdd) {
            cfgServerDialog.iccrPortTextField.setText(propertySource.getIccrDefaultPortNumber());
            cfgServerDialog.iccrPwdTextField.setText(propertySource.getIccrDefaultApiKey());
        }

        cfgServerDialog.setVisible(true);
    }

    private void cfgServerDialogSave(String action) {
        if (cfgServerDialog == null) {
            System.out.println("cfgServerDialog not found!");

            // TODO: localization
            UiUtil.showErrorDialog(
                    action.equals(Constants.DIALOG_CONFIG_ADD_SERVER_SAVE) ? "Add Server Save Error" :
                            "Edit Server Save Error",
                    "Dialog not found!");
            return;
        }

        boolean isAdd = cfgServerDialog.isAdd;
        boolean isIccrPortNumChange = false;
        boolean isIpChange = false;

        Properties newProps = new Properties();
        String ip = cfgServerDialog.serverIpTextField.getText();
        if (!isAdd && ip != null && !ip.isEmpty() &&
                !ip.equals(cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_IP_PROP))) {
            isIpChange = true;
        }
        newProps.setProperty(PropertySource.SERVER_IP_PROP, ip);

        String port = cfgServerDialog.iccrPortTextField.getText();
        if (!isAdd && port != null && !port.isEmpty()) {
            isIccrPortNumChange = !port.equals(cfgServerDialog.serverProps.getProperty(
                    PropertySource.SERVER_ICCR_PORT_NUM_PROP));
        }
        newProps.setProperty(PropertySource.SERVER_ICCR_PORT_NUM_PROP, port);

        String apiKey = String.valueOf(cfgServerDialog.iccrPwdTextField.getPassword());
        newProps.setProperty(PropertySource.SERVER_ICCR_API_KEY_PROP, apiKey);

        String name = cfgServerDialog.serverNameTextField.getText();
        newProps.setProperty(PropertySource.SERVER_NAME_PROP, name);

        /*
        String walletCmd = cfgServerDialog.walletCmdTextField.getText();
        newProps.setProperty(PropertySource.SERVER_WALLET_CMD_PROP, walletCmd);
        */

        String errors = "";
        String sep = "";
        boolean isError = false;
        if (ip == null || ip.isEmpty()) { // || !UiUtil.isValidIpV4(ip)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.serverIpTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }
        if (port == null || port.isEmpty() || !UiUtil.isValidPositiveNumber(port)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.iccrPortTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }
        if (apiKey == null || apiKey.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.iccrPwdTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }
        if (name == null || name.isEmpty()) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    cfgServerDialog.serverNameTextField.getName();
            if (sep.isEmpty()) {
                sep = "\n";
            }
        } else if (cfgServerDialog.isAdd && propertySource.isServerNameTaken(name)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    localizer.getLocalText("dialogSaveErrorServerNameTaken");
            if (sep.isEmpty()) {
                sep = "\n";
            }
        } else if (cfgServerDialog.isAdd && propertySource.isServerIpTaken(ip)) {
            isError = true;
            errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                    localizer.getLocalText("dialogSaveErrorServerIpTaken");
            if (sep.isEmpty()) {
                sep = "\n";
            }
        }
        // Not doing wallet command for now
        /*
        if(!isError) {
            // That last line just insured that we have a valid IP
            if (!isAdd && UiUtil.isLocalhostIp(ip) && (walletCmd == null || walletCmd.isEmpty())) {
                isError = true;
                errors += sep + localizer.getLocalText("dialogSaveErrorInvalidFieldValue") + " " +
                        cfgServerDialog.walletCmdTextField.getName();
                if (sep.isEmpty()) {
                    sep = "\n";
                }
            }
        }
        */

        // Need to check if the iccr port that is changing and it conflicts with the
        // iota port of an iccr server that is loaded:
        if (!isError && serverTabPanel.serverIsOpen(name)) {

            if (isIpChange) {
                isError = true;
                errors += sep + localizer.getLocalText("dialogSaveErrorIpServerOpen");
                if (sep.isEmpty()) {
                    sep = "\n";
                }
            }

            if (isIccrPortNumChange) {
                try {
                    if (port.equals(serverTabPanel.getIotaPortNumber(name))) {
                        isError = true;
                        errors += sep + localizer.getLocalText("dialogSaveErrorPortNumberConflict");
                        if (sep.isEmpty()) {
                            sep = "\n";
                        }
                    }
                } catch (IllegalStateException ise) {
                    System.out.println("Check for IOTA port conflict: illegal state exception: " + ise);
                }
            }
        }

        if (isError) {
            UiUtil.showErrorDialog(
                    action.equals(Constants.DIALOG_CONFIG_ADD_SERVER_SAVE) ? "Add Server Save Error" :
                            "Edit Server Save Error",
                    errors);
            return;
        }

        boolean apiKeyChange = false;
        boolean needToRestartIccr = false;

        if (isAdd) {
            newProps.setProperty(PropertySource.SERVER_ID_PROP, UiUtil.genServerId(name));
        } else {
            newProps.setProperty(PropertySource.SERVER_ID_PROP, UiUtil.genServerId(name));

            apiKeyChange = !newProps.getProperty(PropertySource.SERVER_ICCR_API_KEY_PROP).equals(
                    cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_ICCR_API_KEY_PROP));

            if (isIccrPortNumChange && serverTabPanel.serverIsOpen(name)) {

                try {
                    needToRestartIccr = !port.equals(serverTabPanel.getIccrPortNumber(name));
                } catch (IllegalStateException ise) {
                    System.out.println("Check for ICCR port change, illegal state exception: " + ise);
                }
            }
        }

        persistCfgServerSettings(newProps, cfgServerDialog.serverProps, cfgServerDialog.isAdd);

        if (!isAdd && propertySource.isServerNameChange(newProps, cfgServerDialog.serverProps)) {
            try {
                serverTabPanel.serverNameChange(
                        cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_NAME_PROP), name);
            } catch (IllegalStateException ise) {
                System.out.println("API key change, illegal state exception: " + ise);
            }
        }

        if (apiKeyChange) {
            try {
                serverTabPanel.serverApiKeyChange(name,
                        //cfgServerDialog.serverProps.getProperty(PropertySource.SERVER_NAME_PROP),
                        newProps.getProperty(PropertySource.SERVER_ICCR_API_KEY_PROP));
            } catch (IllegalStateException ise) {
                System.out.println("API key change, illegal state exception: " + ise);
            }
        }

        cfgServerDialogClose();

        if (isAdd) {
            showOpenServerDialog();
        } else {
            if (serverTabPanel.serverIsOpen(name) &&
                    (needToRestartIccr || isIccrPortNumChange)) {
                // This will prompt user for need to change and restart:
                serverTabPanel.serverIccrPortNumberChange(name, port);
            }
        }
    }

    private void persistCfgServerSettings(Properties newProps, Properties prevProps, boolean isAdd) {
        if (isAdd) {
            propertySource.addServerProperties(newProps);
        } else {
            propertySource.setServerProperties(newProps, prevProps);
        }
    }

    private void openServerDialogClose() {
        if (openServerDialog != null) {
            openServerDialog.dispose();
            openServerDialog = null;
        }
    }

    private void cfgServerDialogClose() {
        if (cfgServerDialog != null) {
            cfgServerDialog.dispose();
            cfgServerDialog = null;
        }
    }

    private void showOpenServerDialog() {
        openServerDialog = new OpenServerDialog(localizer,
                localizer.getLocalText("dialogTitleOpenServer"),
                this);
        openServerDialog.setLocationRelativeTo(serverTabPanel);

        openServerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                openServerDialog = null;
            }
        });

        openServerDialog.setVisible(true);
    }

    private void addServerTabPanel(Properties serverProps, String serverName) {
        String name = serverProps.getProperty(PropertySource.SERVER_NAME_PROP);
        if (serverTabPanel.serverIsOpen(name)) {
            throw new IllegalStateException(localizer.getLocalTextWithInsertValue(
                    "dialogServer", "dialogServeAlreadyOpend",
                    serverName));
        }
        ServerProxy proxy = new ServerProxy(serverProps);
        ServerController ctlr = new ServerController(localizer, proxy, serverProps);

        ServerPanel server = new ServerPanel(serverProps.getProperty(PropertySource.SERVER_ID_PROP), localizer, ctlr);
        ctlr.setServerPanel(server);
        serverTabPanel.add(name, server);

        serverTabPanel.setSelectedTabByName(name);

        SwingUtilities.invokeLater(() -> {
            ctlr.serverSetup();
        });
    }
}
