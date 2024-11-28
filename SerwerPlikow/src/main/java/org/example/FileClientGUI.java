package org.example;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;


import java.io.*;
import java.net.Socket;

public class FileClientGUI extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Client");

        Button uploadButton = new Button("Wgraj plik");
        Button downloadButton = new Button("Pobierz plik");
        ListView<String> fileList = new ListView<>();
        Label statusLabel = new Label("");

        uploadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                uploadFile(primaryStage);
            }
        });

        downloadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String selectedFile = fileList.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    downloadFile(primaryStage, selectedFile);
                }
            }
        });

        VBox vbox = new VBox(uploadButton, fileList, downloadButton, statusLabel);
        Scene scene = new Scene(vbox, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void uploadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik do wgrania");
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());

                // Wysyłanie żądania UPLOAD na serwer
                dos.writeUTF("UPLOAD");
                dos.flush();

                // Wysyłanie nazwy pliku i rozmiaru
                dos.writeUTF(file.getName());
                dos.writeLong(file.length());
                dos.flush();

                // Wysyłanie zawartości pliku
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }

                dos.flush();
                fis.close();

                socket.close();
                System.out.println("Plik \"" + file.getName() + "\" został przesłany na serwer.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadFile(Stage stage, String selectedFile) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz plik");
        fileChooser.setInitialFileName(selectedFile);
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());

                // Wysyłanie żądania DOWNLOAD na serwer
                dos.writeUTF("DOWNLOAD");
                dos.flush();

                // Wysyłanie nazwy wybranego pliku
                dos.writeUTF(selectedFile);
                dos.flush();

                // Odbieranie zawartości pliku
                long fileSize = dis.readLong();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    if (totalBytesRead >= fileSize) {
                        break;
                    }
                }

                fos.close();
                System.out.println("Plik \"" + selectedFile + "\" został pobrany.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

