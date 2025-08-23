package server;

import server.config.ServerConfig;
import server.service.PatientService;
import server.handler.PatientHandler;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        // Lê configuração
        ServerConfig config = new ServerConfig("config.properties");

        try {
            PatientService patientService = new PatientService(config.getDbFile());
            PatientHandler patientHandler = new PatientHandler(patientService);

            try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
                System.out.println("Servidor rodando na porta " + config.getPort());

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Conexão aceita: " + socket);
                    new Thread(new ClientHandler(socket, patientHandler)).start();
                }
            } finally {
                patientService.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
