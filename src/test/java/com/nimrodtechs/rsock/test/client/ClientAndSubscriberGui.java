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

import jakarta.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@Slf4j
@Component
public class ClientAndSubscriberGui extends JDialog {

    @Value("${spring.application.name:#{null}}")
    String subscriberProcessName;

    @Autowired
    SubscriberService subscriberService;

    @Autowired
    RemoteServerService remoteServerService;

    ClientAndSubscriberGui instance;

    public ClientAndSubscriberGui() {
        initComponents();
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

        btnSubmit2.addActionListener(new ActionListener() {
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

    private void btnSubmit(ActionEvent e) {
        // TODO add your code here
    }

    private void btnSubmit2(ActionEvent e) {
        // TODO add your code here
    }

//    @Override
//    public void messageReceived(String publisherName, String subject, Object message) {
//        log.info("RECEIVED " + publisherName + ":" + subject + ":" + message.toString());
//        SwingUtilities.invokeLater(() -> {
//            txtMessages1.append(publisherName + ":" + subject + ":" + message.toString() + "\n");
//
//        });
//    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        contentPane = new JPanel();
        var panel1 = new JPanel();
        var panel2 = new JPanel();
        buttonOK = new JButton();
        buttonCancel = new JButton();
        txtMonitor = new JTextField();
        var panel3 = new JPanel();
        tabbedPane1 = new JTabbedPane();
        var panel4 = new JPanel();
        var panel5 = new JPanel();
        var label1 = new JLabel();
        var hSpacer1 = new Spacer();
        var vSpacer1 = new Spacer();
        txtPublisherName1 = new JTextField();
        var label2 = new JLabel();
        txtSubject1 = new JTextField();
        subscribeButton1 = new JButton();
        unsubscribeButton1 = new JButton();
        chkConflate1 = new JCheckBox();
        var panel6 = new JPanel();
        var scrollPane1 = new JScrollPane();
        txtMessages1 = new JTextArea();
        var panel7 = new JPanel();
        var panel8 = new JPanel();
        var label3 = new JLabel();
        var hSpacer2 = new Spacer();
        var vSpacer2 = new Spacer();
        txtPublisherName2 = new JTextField();
        var label4 = new JLabel();
        txtSubject2 = new JTextField();
        subscribeButton2 = new JButton();
        unsubscribeButton2 = new JButton();
        chkConflate2 = new JCheckBox();
        var panel9 = new JPanel();
        var scrollPane2 = new JScrollPane();
        txtMessages2 = new JTextArea();
        var panel10 = new JPanel();
        var panel11 = new JPanel();
        var label5 = new JLabel();
        var label6 = new JLabel();
        var label7 = new JLabel();
        txtServerName = new JTextField();
        txtMethod = new JTextField();
        txtParams = new JTextField();
        btnSubmit = new JButton();
        txtAreaResponse = new JTextArea();
        var panel12 = new JPanel();
        var panel13 = new JPanel();
        var label8 = new JLabel();
        var label9 = new JLabel();
        var label10 = new JLabel();
        txtServerName2 = new JTextField();
        txtMethod2 = new JTextField();
        txtParams2 = new JTextField();
        btnSubmit2 = new JButton();
        txtAreaResponse2 = new JTextArea();

        //======== contentPane ========
        {
            contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));

            //======== panel1 ========
            {
                panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));

                //======== panel2 ========
                {
                    panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));

                    //---- buttonOK ----
                    buttonOK.setText("OK");
                    panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));

                    //---- buttonCancel ----
                    buttonCancel.setText("Cancel");
                    panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
                }
                panel1.add(panel2, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- txtMonitor ----
                txtMonitor.setText("Waiting for events...");
                panel1.add(txtMonitor, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            }
            contentPane.add(panel1, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null, null, null));

            //======== panel3 ========
            {
                panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));

                //======== tabbedPane1 ========
                {

                    //======== panel4 ========
                    {
                        panel4.setLayout(new BorderLayout());

                        //======== panel5 ========
                        {
                            panel5.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));

                            //---- label1 ----
                            label1.setText("Publisher");
                            panel5.add(label1, new GridConstraints(0, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel5.add(hSpacer1, new GridConstraints(0, 1, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                                null, null, null));
                            panel5.add(vSpacer1, new GridConstraints(4, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                null, null, null));

                            //---- txtPublisherName1 ----
                            txtPublisherName1.setText("publisher1");
                            panel5.add(txtPublisherName1, new GridConstraints(0, 2, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- label2 ----
                            label2.setText("Subject");
                            panel5.add(label2, new GridConstraints(1, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel5.add(txtSubject1, new GridConstraints(1, 2, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- subscribeButton1 ----
                            subscribeButton1.setText("Subscribe");
                            panel5.add(subscribeButton1, new GridConstraints(2, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- unsubscribeButton1 ----
                            unsubscribeButton1.setText("Unsubscribe");
                            panel5.add(unsubscribeButton1, new GridConstraints(3, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- chkConflate1 ----
                            chkConflate1.setText("Conflate");
                            panel5.add(chkConflate1, new GridConstraints(2, 2, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                        }
                        panel4.add(panel5, BorderLayout.WEST);

                        //======== panel6 ========
                        {
                            panel6.setLayout(new BorderLayout());

                            //======== scrollPane1 ========
                            {
                                scrollPane1.setViewportView(txtMessages1);
                            }
                            panel6.add(scrollPane1, BorderLayout.CENTER);
                        }
                        panel4.add(panel6, BorderLayout.CENTER);
                    }
                    tabbedPane1.addTab("Subscriber1", panel4);

                    //======== panel7 ========
                    {
                        panel7.setLayout(new BorderLayout());

                        //======== panel8 ========
                        {
                            panel8.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));

                            //---- label3 ----
                            label3.setText("Publisher");
                            panel8.add(label3, new GridConstraints(0, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel8.add(hSpacer2, new GridConstraints(0, 1, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                                null, null, null));
                            panel8.add(vSpacer2, new GridConstraints(4, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                null, null, null));

                            //---- txtPublisherName2 ----
                            txtPublisherName2.setText("publisher1");
                            panel8.add(txtPublisherName2, new GridConstraints(0, 2, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- label4 ----
                            label4.setText("Subject");
                            panel8.add(label4, new GridConstraints(1, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel8.add(txtSubject2, new GridConstraints(1, 2, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- subscribeButton2 ----
                            subscribeButton2.setText("Subscribe");
                            panel8.add(subscribeButton2, new GridConstraints(2, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- unsubscribeButton2 ----
                            unsubscribeButton2.setText("Unsubscribe");
                            panel8.add(unsubscribeButton2, new GridConstraints(3, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- chkConflate2 ----
                            chkConflate2.setText("Conflate");
                            panel8.add(chkConflate2, new GridConstraints(2, 2, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                        }
                        panel7.add(panel8, BorderLayout.WEST);

                        //======== panel9 ========
                        {
                            panel9.setLayout(new BorderLayout());

                            //======== scrollPane2 ========
                            {
                                scrollPane2.setViewportView(txtMessages2);
                            }
                            panel9.add(scrollPane2, BorderLayout.CENTER);
                        }
                        panel7.add(panel9, BorderLayout.CENTER);
                    }
                    tabbedPane1.addTab("Subscriber2", panel7);

                    //======== panel10 ========
                    {
                        panel10.setLayout(new BorderLayout());

                        //======== panel11 ========
                        {
                            panel11.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));

                            //---- label5 ----
                            label5.setText("ServerName");
                            panel11.add(label5, new GridConstraints(0, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- label6 ----
                            label6.setText("Method");
                            panel11.add(label6, new GridConstraints(1, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- label7 ----
                            label7.setText("Parameters");
                            panel11.add(label7, new GridConstraints(2, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- txtServerName ----
                            txtServerName.setText("server1");
                            panel11.add(txtServerName, new GridConstraints(0, 1, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- txtMethod ----
                            txtMethod.setText("getMarketData1");
                            panel11.add(txtMethod, new GridConstraints(1, 1, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel11.add(txtParams, new GridConstraints(2, 1, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- btnSubmit ----
                            btnSubmit.setText("Submit");
                            btnSubmit.addActionListener(e -> btnSubmit(e));
                            panel11.add(btnSubmit, new GridConstraints(3, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel11.add(txtAreaResponse, new GridConstraints(3, 1, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                null, new Dimension(150, 50), null));
                        }
                        panel10.add(panel11, BorderLayout.NORTH);
                    }
                    tabbedPane1.addTab("RMI Client", panel10);

                    //======== panel12 ========
                    {
                        panel12.setLayout(new BorderLayout());

                        //======== panel13 ========
                        {
                            panel13.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));

                            //---- label8 ----
                            label8.setText("ServerName");
                            panel13.add(label8, new GridConstraints(0, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- label9 ----
                            label9.setText("Method");
                            panel13.add(label9, new GridConstraints(1, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- label10 ----
                            label10.setText("Parameters");
                            panel13.add(label10, new GridConstraints(2, 0, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                GridConstraints.SIZEPOLICY_FIXED,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- txtServerName2 ----
                            txtServerName2.setText("server1");
                            panel13.add(txtServerName2, new GridConstraints(0, 1, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- txtMethod2 ----
                            txtMethod2.setText("getMarketData1");
                            panel13.add(txtMethod2, new GridConstraints(1, 1, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel13.add(txtParams2, new GridConstraints(2, 1, 1, 1,
                                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));

                            //---- btnSubmit2 ----
                            btnSubmit2.setText("Submit");
                            btnSubmit2.addActionListener(e -> btnSubmit2(e));
                            panel13.add(btnSubmit2, new GridConstraints(3, 0, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                GridConstraints.SIZEPOLICY_FIXED,
                                null, null, null));
                            panel13.add(txtAreaResponse2, new GridConstraints(3, 1, 1, 1,
                                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                                null, new Dimension(150, 50), null));
                        }
                        panel12.add(panel13, BorderLayout.NORTH);
                    }
                    tabbedPane1.addTab("RMI Client2", panel12);
                }
                panel3.add(tabbedPane1, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, new Dimension(200, 200), null));
            }
            contentPane.add(panel3, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtMonitor;
    private JTabbedPane tabbedPane1;
    private JTextField txtPublisherName1;
    private JTextField txtSubject1;
    private JButton subscribeButton1;
    private JButton unsubscribeButton1;
    private JCheckBox chkConflate1;
    private JTextArea txtMessages1;
    private JTextField txtPublisherName2;
    private JTextField txtSubject2;
    private JButton subscribeButton2;
    private JButton unsubscribeButton2;
    private JCheckBox chkConflate2;
    private JTextArea txtMessages2;
    private JTextField txtServerName;
    private JTextField txtMethod;
    private JTextField txtParams;
    private JButton btnSubmit;
    private JTextArea txtAreaResponse;
    private JTextField txtServerName2;
    private JTextField txtMethod2;
    private JTextField txtParams2;
    private JButton btnSubmit2;
    private JTextArea txtAreaResponse2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}