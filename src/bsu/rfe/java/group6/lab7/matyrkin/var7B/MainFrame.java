package bsu.rfe.java.group6.lab7.matyrkin.var7B;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class MainFrame extends JFrame implements MessageListener {
    private static final String FRAME_TITLE = "Клиент мгновенных сообщений";
    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int INCOMING_AREA_DEFAULT_ROWS = 10;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5;
    private static final int MEDIUM_GAP = 10;
    private static final int LARGE_GAP = 15;
    private static final int SERVER_PORT = 4567;

    private final JTextField textFieldTo;
    private final JTextArea textAreaIncoming;
    private final JTextArea textAreaOutgoing;

    private final InstantMessenger messenger;

    public MainFrame() {
        super(FRAME_TITLE);

        // Центрирование окна
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2,
                (kit.getScreenSize().height - HEIGHT)/2);

        // Создаем объект InstantMessenger для отправки и получения сообщений.
        messenger = new InstantMessenger("Vladislav Matyrkin", SERVER_PORT);
        messenger.addMessageListener(this);

        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textAreaIncoming = new JTextArea(10, 0);// Поле для отображения входящих сообщений.
        textAreaIncoming.setEditable(false);// Делаем его только для чтения.
        JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);// Прокрутка для входящих сообщений.

        JLabel labelTo = new JLabel("Получатель");// Метка для поля ввода адреса получателя.
        textFieldTo = new JTextField(20);

        textAreaOutgoing = new JTextArea(5, 0);// Поле для ввода исходящих сообщений.
        JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);

        JButton sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        // Панель для отправки сообщений.
        JPanel messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));

        GroupLayout layout = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(labelTo)// Метка и поле для получателя.
                                .addComponent(textFieldTo))
                        .addComponent(scrollPaneOutgoing)// Поле для ввода текстов сообщений.
                        .addComponent(sendButton)));// Кнопка "Отправить".
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelTo)
                        .addComponent(textFieldTo))
                .addComponent(scrollPaneOutgoing)
                .addComponent(sendButton));

        GroupLayout mainLayout = new GroupLayout(getContentPane());
        setLayout(mainLayout);
        mainLayout.setHorizontalGroup(mainLayout.createParallelGroup()
                .addComponent(scrollPaneIncoming)
                .addComponent(messagePanel));
        mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
                .addComponent(scrollPaneIncoming)
                .addComponent(messagePanel));

        pack();
    }

    private void sendMessage() {
        String destination = textFieldTo.getText();// Получаем адрес получателя.
        String message = textAreaOutgoing.getText();// Получаем текст сообщения.

        if (destination.isEmpty() || message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Не все поля заполнены", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Peer recipient = new Peer("Получатель", destination, SERVER_PORT);
            messenger.sendMessage(recipient, message);
            textAreaIncoming.append("Я -> " + recipient + ": " + message + "\n");
            textAreaOutgoing.setText("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Не удалось найти узел", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при отправке сообщения", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void messageReceived(Peer sender, String message) {
        textAreaIncoming.append(sender + ": " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

