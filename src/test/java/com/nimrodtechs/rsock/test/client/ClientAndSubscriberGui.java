package com.nimrodtechs.rsock.test.client;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.nimrodtechs.ipcrsock.client.RemoteServerService;
import com.nimrodtechs.ipcrsock.common.MessageReceiverInterface;
import com.nimrodtechs.ipcrsock.common.NimrodPubSubException;
import com.nimrodtechs.rsock.test.model.MarketData;
import com.nimrodtechs.ipcrsock.subscriber.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@Slf4j
@Component
public class ClientAndSubscriberGui extends JDialog implements MessageReceiverInterface {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane1;
    private JTextField txtPublisherName;
    private JTextField txtSubject;
    private JButton subscribeButton;
    private JButton unsubscribeButton;
    private JTextArea txtMessages;
    private JCheckBox chkConflate;
    private JTextField txtServerName;
    private JTextField txtMethod;
    private JTextField txtParams;
    private JButton btnSubmit;
    private JTextArea txtAreaResponse;

    @Autowired
    SubscriberService subscriberService;

    @Autowired
    RemoteServerService remoteServerService;

    ClientAndSubscriberGui instance;

    public ClientAndSubscriberGui() {
        instance = this;
    }

    @PostConstruct
    void init() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        subscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    subscriberService.subscribe(txtPublisherName.getText(), txtSubject.getText(), instance, MarketData.class, chkConflate.isSelected());
                } catch (NimrodPubSubException ex) {
                    ex.printStackTrace();
                }
            }
        });
        unsubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscriberService.unsubscribe(txtPublisherName.getText(), txtSubject.getText(), instance);
            }
        });
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread(() -> {
                    try {
                        String response = "";
                        String[] params = txtParams.getText().split(",");
                        if (params.length == 0) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText());
                        } else if (params.length == 1) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(), params[0]);
                        } else if (params.length == 2) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(), params[0], params[1]);
                        } else if (params.length == 3) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(), params[0], params[1], params[2]);
                        } else if (params.length == 4) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(), params[0], params[1], params[2], params[3]);
                        }
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> {
                            txtAreaResponse.append(finalResponse + "\n");
                        });

                    } catch (Exception ex) {
                        log.error("Exception calling ", ex);
                    }
                });
                thread.start();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
        System.exit(0);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        ClientAndSubscriberGui dialog = new ClientAndSubscriberGui();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    @Override
    public void messageReceived(String publisherName, String subject, Object message) {
        log.info("RECEIVED " + publisherName + ":" + subject + ":" + message.toString());
        SwingUtilities.invokeLater(() -> {
            txtMessages.append(publisherName + ":" + subject + ":" + message.toString() + "\n");

        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tabbedPane1 = new JTabbedPane();
        panel3.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Subscriber", panel4);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, BorderLayout.WEST);
        final JLabel label1 = new JLabel();
        label1.setText("Publisher");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel5.add(spacer3, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtPublisherName = new JTextField();
        txtPublisherName.setText("publisher1");
        panel5.add(txtPublisherName, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Subject");
        panel5.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtSubject = new JTextField();
        panel5.add(txtSubject, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        subscribeButton = new JButton();
        subscribeButton.setText("Subscribe");
        panel5.add(subscribeButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unsubscribeButton = new JButton();
        unsubscribeButton.setText("Unsubscribe");
        panel5.add(unsubscribeButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chkConflate = new JCheckBox();
        chkConflate.setText("Conflate");
        panel5.add(chkConflate, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        panel4.add(panel6, BorderLayout.CENTER);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel6.add(scrollPane1, BorderLayout.CENTER);
        txtMessages = new JTextArea();
        scrollPane1.setViewportView(txtMessages);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("RMI Client", panel7);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, BorderLayout.NORTH);
        final JLabel label3 = new JLabel();
        label3.setText("ServerName");
        panel8.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Method");
        panel8.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Parameters");
        panel8.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtServerName = new JTextField();
        txtServerName.setText("server1");
        panel8.add(txtServerName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtMethod = new JTextField();
        txtMethod.setText("getMarketData1");
        panel8.add(txtMethod, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtParams = new JTextField();
        panel8.add(txtParams, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnSubmit = new JButton();
        btnSubmit.setText("Submit");
        panel8.add(btnSubmit, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAreaResponse = new JTextArea();
        panel8.add(txtAreaResponse, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}