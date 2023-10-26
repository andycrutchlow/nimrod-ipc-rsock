package com.nimrodtechs.rsock.test.server;

import com.nimrodtechs.rsock.test.model.MarketData;
import com.nimrodtechs.rsock.publisher.PublisherSocketImpl;
import com.nimrodtechs.rsock.common.SubscriptionListener;
import com.nimrodtechs.rsock.common.SubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.Random;

@Component
public class ServerAndPublisherGui extends JDialog implements SubscriptionListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane1;
    private JList listSubscriptions;
    private JTextField txtSubject;
    private JTextField txtData;
    private JButton publishButton;

    @Autowired
    PublisherSocketImpl publisherSocket;

    private DefaultListModel subscriptionList = new DefaultListModel();
    private static final int BOUND = 100;
    private Random random = new Random();

    public ServerAndPublisherGui() {
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
        listSubscriptions.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(listSubscriptions.getSelectedValue() == null) {
                    return;
                }
                System.out.println("Selected "+listSubscriptions.getSelectedValue());
                txtSubject.setText(((SubscriptionRequest)listSubscriptions.getSelectedValue()).getSubject());
            }
        });
        publishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                publisherSocket.publishData(txtSubject.getText(),new MarketData(txtData.getText(),random.nextInt(BOUND)));
            }
        });
    }

    @PostConstruct
    void init() {
        System.out.println("HERE");
        publisherSocket.addSubscriptionListener(this);
        listSubscriptions.setModel(subscriptionList);
        listSubscriptions.setCellRenderer(new CustomListBoxRenderer());
    }

    private void onOK() {
        if(JOptionPane.showConfirmDialog(this,"Are you sure?") == 0) {
            // add your code here
            dispose();
            System.exit(0);
        }

    }

    private void onCancel() {
        if(JOptionPane.showConfirmDialog(this,"Are you sure?") == 0) {
            // add your code here
            dispose();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        ServerAndPublisherGui dialog = new ServerAndPublisherGui();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    @Override
    public void onSubscription(SubscriptionRequest subscriptionRequest) {
        SwingUtilities.invokeLater(() -> {
            subscriptionList.addElement(subscriptionRequest);
        });
    }

    @Override
    public void onSubscriptionRemove(SubscriptionRequest subscriptionRequest) {
        SwingUtilities.invokeLater(() -> {
            subscriptionList.removeElement(subscriptionRequest);
        });
    }

    class CustomListBoxRenderer extends DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                               boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SubscriptionRequest) {
                value = ((SubscriptionRequest) value).getRequestor()+":"+((SubscriptionRequest) value).getSubject();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }
}