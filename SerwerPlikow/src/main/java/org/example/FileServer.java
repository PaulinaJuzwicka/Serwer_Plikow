package org.example;

import java.io.*;
import java.net.*;

public class FileServer {
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // Maksymalny rozmiar pliku (100 MB)
    private static final String FILE_DIRECTORY = "server_files/";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Serwer plików działa...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket).start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Weryfikacja klienta
                if (!authenticateClient()) {
                    // Odmowa dostępu
                    System.err.println("Klient nieautoryzowany.");
                    return;
                }

                // Obsługa przesyłania lub pobierania pliku
                String request = receiveRequest();
                if (request.equals("UPLOAD")) {
                    receiveFile();
                } else if (request.equals("DOWNLOAD")) {
                    sendFile();
                }
            } catch (IOException e) {
                System.err.println("Błąd obsługi klienta: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Błąd przy zamykaniu socketu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private boolean authenticateClient() {
            // Tutaj można dodać logikę uwierzytelniania klienta
            // Na potrzeby przykładu zawsze zwracamy true
            return true;
        }

        private String receiveRequest() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return reader.readLine();
        }

        private void receiveFile() throws IOException {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            if (fileSize > MAX_FILE_SIZE) {
                System.err.println("Przekroczono maksymalny rozmiar pliku.");
                return;
            }

            File file = new File(FILE_DIRECTORY + fileName);
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
            System.out.println("Plik \"" + fileName + "\" został przesłany.");
        }

        private void sendFile() throws IOException {
            // Kod do obsługi wysyłania pliku z serwera do klienta
            // Możesz dodać go w odpowiednim miejscu
        }
    }
}

