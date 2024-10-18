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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@Slf4j
@Component
public class ClientAndSubscriberGui extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane1;
    private JTextField txtPublisherName1;
    private JTextField txtSubject1;
    private JButton subscribeButton1;
    private JButton unsubscribeButton1;
    private JTextArea txtMessages1;
    private JCheckBox chkConflate1;
    private JTextField txtServerName;
    private JTextField txtMethod;
    private JTextField txtParams;
    private JButton btnSubmit;
    private JTextArea txtAreaResponse;
    private JTextArea txtMessages2;
    private JTextField txtSubject2;
    private JTextField txtPublisherName2;
    private JButton subscribeButton2;
    private JButton unsubscribeButton2;
    private JCheckBox chkConflate2;
    private JTextField txtMonitor;

    @Value("${spring.application.name:#{null}}")
    String subscriberProcessName;

    @Autowired
    SubscriberService subscriberService;

    @Autowired
    RemoteServerService remoteServerService;

    ClientAndSubscriberGui instance;

    public ClientAndSubscriberGui() {
        instance = this;
    }

    class SubscriberPanel1 implements MessageReceiverInterface {
        @Override
        public void messageReceived(String publisherName, String subject, Object message) {
            log.info("PANEL1 RECEIVED " + publisherName + ":" + subject + ":" + message.toString());
            SwingUtilities.invokeLater(() -> {
                txtMessages1.append(publisherName + ":" + subject + ":" + message.toString() + "\n");

            });
        }
    }

    class SubscriberPanel2 implements MessageReceiverInterface {
        @Override
        public void messageReceived(String publisherName, String subject, Object message) {
            log.info("PANEL2 RECEIVED " + publisherName + ":" + subject + ":" + message.toString());
            SwingUtilities.invokeLater(() -> {
                txtMessages2.append(publisherName + ":" + subject + ":" + message.toString() + "\n");

            });
        }
    }

    @PostConstruct
    void init() {
        setTitle(subscriberProcessName);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        SubscriberPanel1 subscriberPanel1 = new SubscriberPanel1();
        SubscriberPanel2 subscriberPanel2 = new SubscriberPanel2();

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

        subscribeButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    subscriberService.subscribe(txtPublisherName1.getText(), txtSubject1.getText(), subscriberPanel1, MarketData.class, chkConflate1.isSelected());
                } catch (NimrodPubSubException ex) {
                    ex.printStackTrace();
                }
            }
        });

        unsubscribeButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscriberService.unsubscribe(txtPublisherName1.getText(), txtSubject1.getText(), subscriberPanel1);
            }
        });

        subscribeButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    subscriberService.subscribe(txtPublisherName2.getText(), txtSubject2.getText(), subscriberPanel2, MarketData.class, chkConflate2.isSelected());
                } catch (NimrodPubSubException ex) {
                    ex.printStackTrace();
                }
            }
        });

        unsubscribeButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscriberService.unsubscribe(txtPublisherName2.getText(), txtSubject2.getText(), subscriberPanel2);
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

//    @Override
//    public void messageReceived(String publisherName, String subject, Object message) {
//        log.info("RECEIVED " + publisherName + ":" + subject + ":" + message.toString());
//        SwingUtilities.invokeLater(() -> {
//            txtMessages1.append(publisherName + ":" + subject + ":" + message.toString() + "\n");
//
//        });
//    }

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
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtMonitor = new JTextField();
        txtMonitor.setText("Waiting for events...");
        panel1.add(txtMonitor, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tabbedPane1 = new JTabbedPane();
        panel3.add(tabbedPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Subscriber1", panel4);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, BorderLayout.WEST);
        final JLabel label1 = new JLabel();
        label1.setText("Publisher");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel5.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtPublisherName1 = new JTextField();
        txtPublisherName1.setText("publisher1");
        panel5.add(txtPublisherName1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Subject");
        panel5.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtSubject1 = new JTextField();
        panel5.add(txtSubject1, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        subscribeButton1 = new JButton();
        subscribeButton1.setText("Subscribe");
        panel5.add(subscribeButton1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unsubscribeButton1 = new JButton();
        unsubscribeButton1.setText("Unsubscribe");
        panel5.add(unsubscribeButton1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chkConflate1 = new JCheckBox();
        chkConflate1.setText("Conflate");
        panel5.add(chkConflate1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new BorderLayout(0, 0));
        panel4.add(panel6, BorderLayout.CENTER);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel6.add(scrollPane1, BorderLayout.CENTER);
        txtMessages1 = new JTextArea();
        scrollPane1.setViewportView(txtMessages1);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Subscriber2", panel7);
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, BorderLayout.WEST);
        final JLabel label3 = new JLabel();
        label3.setText("Publisher");
        panel8.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel8.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel8.add(spacer4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        txtPublisherName2 = new JTextField();
        txtPublisherName2.setText("publisher1");
        panel8.add(txtPublisherName2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Subject");
        panel8.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtSubject2 = new JTextField();
        panel8.add(txtSubject2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        subscribeButton2 = new JButton();
        subscribeButton2.setText("Subscribe");
        panel8.add(subscribeButton2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        unsubscribeButton2 = new JButton();
        unsubscribeButton2.setText("Unsubscribe");
        panel8.add(unsubscribeButton2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chkConflate2 = new JCheckBox();
        chkConflate2.setText("Conflate");
        panel8.add(chkConflate2, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new BorderLayout(0, 0));
        panel7.add(panel9, BorderLayout.CENTER);
        final JScrollPane scrollPane2 = new JScrollPane();
        panel9.add(scrollPane2, BorderLayout.CENTER);
        txtMessages2 = new JTextArea();
        scrollPane2.setViewportView(txtMessages2);
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("RMI Client", panel10);
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, BorderLayout.NORTH);
        final JLabel label5 = new JLabel();
        label5.setText("ServerName");
        panel11.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Method");
        panel11.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Parameters");
        panel11.add(label7, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtServerName = new JTextField();
        txtServerName.setText("server1");
        panel11.add(txtServerName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtMethod = new JTextField();
        txtMethod.setText("getMarketData1");
        panel11.add(txtMethod, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtParams = new JTextField();
        panel11.add(txtParams, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnSubmit = new JButton();
        btnSubmit.setText("Submit");
        panel11.add(btnSubmit, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtAreaResponse = new JTextArea();
        panel11.add(txtAreaResponse, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}