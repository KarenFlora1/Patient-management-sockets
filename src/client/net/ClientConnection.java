package client.net;

import common.dto.Request;
import common.dto.Response;
import java.io.*;
import java.net.Socket;
import client.config.ClientConfig;

public class ClientConnection {
    private String host;
    private int port;

    public ClientConnection(ClientConfig config) {
        this.host = config.getHost();
        this.port = config.getPort();
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
