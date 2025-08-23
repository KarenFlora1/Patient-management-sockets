package client.net;

import common.dto.Request;
import common.dto.Response;

import java.io.*;
import java.net.Socket;

public class ClientConnection {
    private String host;
    private int port;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Response send(Request req) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(req);
            return (Response) in.readObject();
        }
    }
}

