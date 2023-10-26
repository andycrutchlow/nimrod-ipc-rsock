package com.nimrodtechs.rsock.test.client;

import com.nimrodtechs.rsock.client.RemoteServerService;
import com.nimrodtechs.rsock.common.MessageReceiverInterface;
import com.nimrodtechs.rsock.common.NimrodPubSubException;
import com.nimrodtechs.rsock.test.model.MarketData;
import com.nimrodtechs.rsock.subscriber.SubscriberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
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
                    subscriberService.subscribe(txtPublisherName.getText(),txtSubject.getText(),instance, MarketData.class,chkConflate.isSelected());
                } catch (NimrodPubSubException ex) {
                    ex.printStackTrace();
                }
            }
        });
        unsubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscriberService.unsubscribe(txtPublisherName.getText(),txtSubject.getText(),instance);
            }
        });
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread(() -> {
                    try {
                        String response="";
                        String[] params = txtParams.getText().split(",");
                        if (params.length == 0) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText());
                        } else if (params.length == 1) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(),params[0]);
                        } else if (params.length == 2) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(),params[0],params[1]);
                        } else if (params.length == 3) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(),params[0],params[1],params[2]);
                        } else if (params.length == 4) {
                            response = remoteServerService.executeRmiMethod(String.class, txtServerName.getText(), txtMethod.getText(),params[0],params[1],params[2],params[3]);
                        }
                        String finalResponse = response;
                        SwingUtilities.invokeLater(() -> {
                            txtAreaResponse.append(finalResponse +"\n");
                        });

                    } catch (Exception ex){
                        log.error("Exception calling ",ex);
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
        log.info("RECEIVED "+publisherName+":"+subject+":"+message.toString());
        SwingUtilities.invokeLater(() -> {
            txtMessages.append(publisherName+":"+subject+":"+message.toString()+"\n");

        });
    }
}