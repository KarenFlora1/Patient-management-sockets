package server;

import server.service.PatientService;
import server.handler.PatientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 12345; 
    private static final String DB_FILE = "clinic.db"; 

    public static void main(String[] args) {
        try {
            PatientService patientService = new PatientService(DB_FILE);
            
            PatientHandler patientHandler = new PatientHandler(patientService);

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Servidor rodando na porta " + PORT);

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientHandler(socket, patientHandler)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                patientService.close(); 
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
