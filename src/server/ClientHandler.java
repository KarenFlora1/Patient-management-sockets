package server;

import common.dto.Request;
import common.dto.Response;
import server.handler.PatientHandler;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PatientHandler handler;

    public ClientHandler(Socket socket, PatientHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            Request req = (Request) in.readObject();
            Response resp = handler.handle(req);
            out.writeObject(resp);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


