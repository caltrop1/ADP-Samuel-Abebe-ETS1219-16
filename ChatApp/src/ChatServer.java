import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5000;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    private static final CopyOnWriteArrayList<DataOutputStream> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chat server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            while (true) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket));
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        DataOutputStream output = null;

        try (Socket clientSocket = socket;
             DataInputStream input = new DataInputStream(clientSocket.getInputStream())) {

            output = new DataOutputStream(clientSocket.getOutputStream());
            clients.add(output);
            System.out.println("Client connected. Total clients: " + clients.size());

            while (true) {
                String type = input.readUTF();
                int size = input.readInt();

                if (size < 0 || size > MAX_IMAGE_SIZE) {
                    System.out.println("Rejected message with invalid size: " + size);
                    break;
                }

                byte[] data = new byte[size];
                input.readFully(data);

                if ("TEXT".equals(type) || "IMAGE".equals(type)) {
                    broadcast(output, type, data);
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            if (output != null) {
                clients.remove(output);
            }
            System.out.println("Connected clients: " + clients.size());
        }
    }

    private static void broadcast(DataOutputStream sender, String type, byte[] data) {
        for (DataOutputStream client : clients) {
            if (client == sender) {
                continue;
            }

            try {
                synchronized (client) {
                    client.writeUTF(type);
                    client.writeInt(data.length);
                    client.write(data);
                    client.flush();
                }
            } catch (IOException e) {
                clients.remove(client);
            }
        }
    }
}
