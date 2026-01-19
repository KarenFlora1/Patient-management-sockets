package server;

import server.config.ServerConfig;
import server.service.PatientService;
import server.handler.PatientHandler;
import server.dao.DbInit;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        ServerConfig config = new ServerConfig("server.properties");

        try {
            // garante BD
            DbInit.ensure("jdbc:sqlite:" + config.getDbFile());

            PatientService patientService = new PatientService(config.getDbFile());
            PatientHandler patientHandler = new PatientHandler(patientService);

            // Thread pool (escala com CPU; mínimo 8)
            int poolSize = Math.max(8, Runtime.getRuntime().availableProcessors() * 2);
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);

            try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
                System.out.println("Servidor a escutar na porta " + config.getPort() + " (pool=" + poolSize + ")");
                while (true) {
                    Socket socket = serverSocket.accept();
                    executor.submit(new ClientHandler(socket, patientHandler));
                }
            } finally {
                patientService.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
