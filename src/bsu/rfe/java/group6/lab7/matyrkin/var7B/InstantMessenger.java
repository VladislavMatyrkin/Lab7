package bsu.rfe.java.group6.lab7.matyrkin.var7B;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class InstantMessenger {
    private final int serverPort;// Порт сервера
    private final String sender;// Имя отправителя
    private final List<MessageListener> listeners = new ArrayList<>();// Список слушателей
    private final ExecutorService connectionPool; // Пул потоков для обработки соединений
    //Конструктор. Инициализирует поля и запускает сервер.
    public InstantMessenger(String sender, int serverPort) {
        this.sender = sender;
        this.serverPort = serverPort;
        this.connectionPool = Executors.newFixedThreadPool(10); // Создаем пул из 10 потоков
        // Запускаем сервер для приема сообщений
        startServer();
    }

    //Добавляет слушателя, который будет уведомлен при получении сообщения.
    public void addMessageListener(MessageListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    //Удаляет слушателя из списка.
    public void removeMessageListener(MessageListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void sendMessage(Peer recipient, String message) throws IOException {
        try (Socket socket = new Socket(recipient.getAddress(), recipient.getPort());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            out.writeUTF(sender);
            out.writeUTF(message);
        }
    }

    private void startServer() {
        // Запускаем сервер в отдельном потоке
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Сервер запущен на порту " + serverPort);
                while (!Thread.interrupted()) {
                    // Принимаем входящее соединение
                    Socket socket = serverSocket.accept();
                    System.out.println("Принято соединение от " + socket.getRemoteSocketAddress());

                    // Передаем обработку соединения в пул потоков
                    connectionPool.submit(() -> handleConnection(socket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    //Обрабатывает входящее соединение: читает данные и уведомляет слушателей.
    private void handleConnection(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
            // Читаем имя отправителя
            String senderName = in.readUTF();

            // Читаем сообщение
            String message = in.readUTF();

            // Определяем адрес отправителя
            Peer sender = new Peer(senderName,
                    ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress(),
                    serverPort);

            // Уведомляем всех слушателей о получении сообщения
            notifyListeners(sender, message);

            System.out.println("Сообщение от " + sender + ": " + message);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();// Закрываем соединение после обработки
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }//Уведомляет всех зарегистрированных слушателей о получении сообщения.
    private void notifyListeners(Peer sender, String message) {
        synchronized (listeners) {
            for (MessageListener listener : listeners) {
                listener.messageReceived(sender, message);
            }
        }
    }
    public void shutdown() {
        connectionPool.shutdown(); // Завершаем работу пула потоков
    }
}

