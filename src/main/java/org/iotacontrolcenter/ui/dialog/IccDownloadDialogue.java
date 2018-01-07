package org.iotacontrolcenter.ui.dialog;

/**
 * Created by Kashif on 1/6/18.
 */

import org.iotacontrolcenter.ui.app.Constants;
import org.iotacontrolcenter.ui.app.Main;
import org.iotacontrolcenter.ui.properties.locale.Localizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class IccDownloadDialogue extends JDialog {
    private ActionListener ctlr;
    public JTextField iotaDownloadTextField;
    public JPanel panel, labelP, buttonPanel;
    public JButton save;
    private Localizer localizer;

    public IccDownloadDialogue(Localizer localizer, ActionListener ctlr) {
        super();
        this.localizer = localizer;
        this.ctlr = ctlr;
        init();
    }

    private void init() {
        setIconImages(Main.icons);
        setTitle(localizer.getLocalText("dialogTitleIotaVersion"));
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocation(375, 300);
        setSize(600, 300);
        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, -80, 0, 20));
        JLabel nbrRefreshTime = new JLabel(Constants.MESSAGE_ICC_DOWNLOAD);
        panel.add(nbrRefreshTime);
        add(panel, BorderLayout.PAGE_START);
        labelP = new JPanel();
        JLabel textLabel = new JLabel(Constants.TEXT_FOR_TEXTFIELD);
        labelP.add(textLabel);
        iotaDownloadTextField = new JTextField(25);
        labelP.add(iotaDownloadTextField);
        add(labelP, BorderLayout.WEST);

        buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 10, 0, 0));
        save = new JButton(localizer.getLocalText("buttonLabelSave"));
        save.setActionCommand(Constants.DIALOG_ICC_DOWNLOAD_SAVE);
        save.addActionListener(ctlr);
        getRootPane().setDefaultButton(save);
        buttonPanel.add(save);
        add(buttonPanel);
        pack();

    }
}
