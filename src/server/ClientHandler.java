package server;

import common.dto.Request;
import common.dto.Response;
import common.protocol.LineJson;
import server.auth.AuthService;
import server.handler.PatientHandler;
import server.util.JsonLogger;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Handler por ligação. Lê vários requests (LineJson) da mesma ligação.
 * - Timeout de leitura
 * - Autenticação com token + expiração
 * - Contagem de falhas & lockouts
 * - Logs estruturados (JSON) para ficheiro UTF-8 (rotação diária)
 */
public class ClientHandler implements Runnable {

    private static final int READ_TIMEOUT_MS = 7000;

    private final Socket socket;
    private final PatientHandler handler;

    public ClientHandler(Socket socket, PatientHandler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        final String ip = socket.getInetAddress().getHostAddress();
        log("conn_open", ip, "-", "-", "Ligação iniciada");

        try (
            socket;
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)
        ) {
            socket.setSoTimeout(READ_TIMEOUT_MS);

            while (true) {
                Request req;
                try {
                    req = LineJson.recv(in, Request.class);
                } catch (EOFException eof) {
                    log("conn_close", ip, "-", "-", "Cliente fechou a ligação");
                    break;
                } catch (SocketTimeoutException to) {
                    log("conn_timeout", ip, "-", "-", "Timeout de leitura; a fechar ligação");
                    break;
                } catch (Exception ex) {
                    log("conn_error", ip, "-", "-", "Erro a ler request: " + ex.getMessage());
                    break;
                }

                String action = (req.action == null ? "-" : req.action);
                String userFromToken = safeUserFromToken(req.token);

                // LOGIN não requer token
                if ("LOGIN".equalsIgnoreCase(action)) {
                    if (AuthService.isIpLocked(ip)) {
                        long ms = AuthService.ipLockedRemainingMs(ip);
                        String msg = "IP bloqueado. Aguarde " + (ms / 1000) + " segundos.";
                        log("login_blocked_ip", ip, "-", "LOGIN", msg);
                        LineJson.send(Response.error(msg), out);
                        continue;
                    }
                    if (req.username != null && AuthService.isUserLocked(req.username)) {
                        long ms = AuthService.userLockedRemainingMs(req.username);
                        String msg = "Utilizador bloqueado. Aguarde " + (ms / 1000) + " segundos.";
                        log("login_blocked_user", ip, req.username, "LOGIN", msg);
                        LineJson.send(Response.error(msg), out);
                        continue;
                    }

                    String token = AuthService.login(req.username, req.password, ip);
                    if (token != null) {
                        var r = Response.okMsg("Login bem sucedido.");
                        r.token = token;
                        log("login_success", ip, req.username, "LOGIN", "OK");
                        LineJson.send(r, out);
                    } else {
                        log("login_failure", ip, req.username, "LOGIN", "Credenciais inválidas ou bloqueado");
                        LineJson.send(Response.error("Credenciais inválidas ou bloqueado temporariamente."), out);
                    }
                    continue;
                }

                // PING pode passar sem token (opcional)
                if (!"ping".equalsIgnoreCase(action)) {
                    if (!AuthService.isValid(req.token)) {
                        log("auth_denied", ip, userFromToken, action, "Token inválido/expirado");
                        LineJson.send(Response.error("Não autorizado ou sessão expirada. Faça login."), out);
                        continue;
                    }
                }

                // Negócio
                try {
                    Response resp = handler.handle(req);
                    log("request_ok", ip, userFromToken, action, "OK");
                    LineJson.send(resp, out);
                } catch (Exception ex) {
                    log("request_error", ip, userFromToken, action, "Erro: " + ex.getMessage());
                    LineJson.send(Response.error("Erro interno."), out);
                }
            }
        } catch (Exception e) {
            log("conn_error", ip, "-", "-", "Excepção no handler: " + e.getMessage());
        }
    }

    // ===== util =====

    private static String safeUserFromToken(String token) {
        try {
            String u = AuthService.userFromToken(token);
            return (u == null ? "-" : u);
        } catch (Exception ignore) {
            return "-";
        }
    }

    /** Encaminha para o logger UTF-8 com rotação diária. */
    private static void log(String event, String ip, String user, String action, String message) {
        server.util.JsonLogger.log(event, ip, user, action, message);
    }
}
