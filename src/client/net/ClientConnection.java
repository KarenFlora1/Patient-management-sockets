package client.net;

import client.config.ClientConfig;
import common.dto.Request;
import common.dto.Response;
import common.protocol.LineJson;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Ligação persistente com timeouts e 1 tentativa de reconexão.
 * Protocol: LineJson (1 request/resposta por linha).
 */
public class ClientConnection implements Closeable {

    private final String host;
    private final int port;

    // Timeouts (ms)
    private final int connectTimeoutMs = 3000;
    private final int soTimeoutMs      = 6000;

    // Reenvio em caso de I/O falha
    private final int maxRetries = 1;

    // Socket/streams actuais
    private Socket socket;
    private BufferedReader in;
    private Writer out;

    // Token de sessão (preenchido após LOGIN bem sucedido)
    private String token;

    public ClientConnection(ClientConfig config) throws IOException {
        this.host = config.getHost();
        this.port = config.getPort();
        openSocket();
    }

    private void openSocket() throws IOException {
        closeQuiet();
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
        socket.setSoTimeout(soTimeoutMs);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
    }

    /** LOGIN e guarda token para requests seguintes. */
    public boolean login(String username, String password) throws Exception {
        Response resp = send(Request.login(username, password));
        if ("ok".equals(resp.status) && resp.token != null && !resp.token.isBlank()) {
            this.token = resp.token;
            return true;
        }
        throw new IllegalStateException(resp.message != null ? resp.message : "Falha no login.");
    }

    /** Envia request. Injecta token (se existir). Reenvia 1x se falhar I/O. */
    public Response send(Request req) throws IOException {
        if (req == null) throw new IllegalArgumentException("Request nulo.");
        if (!"LOGIN".equalsIgnoreCase(req.action)) {
            if (this.token != null && (req.token == null || req.token.isBlank())) {
                req.token = this.token;
            }
        }

        IOException last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                LineJson.send(req, out);
                return LineJson.recv(in, Response.class);
            } catch (IOException e) {
                last = e;
                // tenta reabrir a ligação e reenviar
                try { openSocket(); } catch (IOException re) { last = re; }
            }
        }
        throw last;
    }

    @Override
    public void close() throws IOException { closeQuiet(); }

    private void closeQuiet() {
        try { if (in != null) in.close(); } catch (Exception ignore) {}
        try { if (out != null) out.close(); } catch (Exception ignore) {}
        try { if (socket != null) socket.close(); } catch (Exception ignore) {}
        in = null; out = null; socket = null;
    }
}
