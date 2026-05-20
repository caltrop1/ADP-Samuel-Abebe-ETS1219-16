import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ChatClient extends Application {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;

    private VBox chatBox;
    private ScrollPane scrollPane;
    private TextField messageField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        chatBox = new VBox(8);
        chatBox.setPadding(new Insets(10));

        scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.vvalueProperty().bind(chatBox.heightProperty());

        messageField = new TextField();
        messageField.setPromptText("Type a message...");
        messageField.setOnAction(e -> sendTextMessage());

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendTextMessage());

        Button imageButton = new Button("Image");
        imageButton.setOnAction(e -> chooseAndSendImage(stage));

        HBox bottomBar = new HBox(8, imageButton, messageField, sendButton);
        bottomBar.setPadding(new Insets(10));
        HBox.setHgrow(messageField, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 520, 420);
        stage.setTitle("Simple Chat");
        stage.setScene(scene);
        stage.show();

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(HOST, PORT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            addTextMessage("Connected to server.");

            Thread.startVirtualThread(this::listenForMessages);
        } catch (IOException e) {
            addTextMessage("Could not connect to server: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                String type = input.readUTF();
                int size = input.readInt();

                if (size < 0 || size > MAX_IMAGE_SIZE) {
                    throw new IOException("Invalid message size: " + size);
                }

                byte[] data = new byte[size];
                input.readFully(data);

                if ("TEXT".equals(type)) {
                    String text = new String(data, "UTF-8");
                    Platform.runLater(() -> addTextMessage("Friend: " + text));
                } else if ("IMAGE".equals(type)) {
                    Platform.runLater(() -> addImageMessage(data));
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> addTextMessage("Disconnected from server."));
        }
    }

    private void sendTextMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        try {
            byte[] data = text.getBytes("UTF-8");
            sendMessage("TEXT", data);
            addTextMessage("Me: " + text);
            messageField.clear();
        } catch (IOException e) {
            addTextMessage("Could not send message.");
        }
    }

    private void chooseAndSendImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        if (file.length() > MAX_IMAGE_SIZE) {
            addTextMessage("Image is too large. Choose an image under 10 MB.");
            return;
        }

        try (FileInputStream fileInput = new FileInputStream(file)) {
            byte[] data = fileInput.readAllBytes();
            sendMessage("IMAGE", data);
            addImageMessage(data);
        } catch (IOException e) {
            addTextMessage("Could not send image.");
        }
    }

    private void sendMessage(String type, byte[] data) throws IOException {
        if (output == null) {
            throw new IOException("Not connected");
        }

        synchronized (output) {
            output.writeUTF(type);
            output.writeInt(data.length);
            output.write(data);
            output.flush();
        }
    }

    private void addTextMessage(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        chatBox.getChildren().add(label);
    }

    private void addImageMessage(byte[] data) {
        Image image = new Image(new java.io.ByteArrayInputStream(data));
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(240);
        chatBox.getChildren().add(imageView);
    }

    @Override
    public void stop() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
}
