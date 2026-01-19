package server.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Logger JSON simples:
 * - Escreve em UTF-8 para logs/server-YYYYMMDD.log
 * - Cria a pasta logs/ se não existir
 * - Fallback para consola (UTF-8) se falhar o ficheiro
 * - Campos: ts, event, ip, user, action, message
 */
public final class JsonLogger {

    private JsonLogger() {}

    private static final Object LOCK = new Object();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO  = DateTimeFormatter.ISO_INSTANT;

    /** Diretório configurável via -Dserver.log.dir=... (default "logs") */
    private static Path logPathForToday() {
        String dir = System.getProperty("server.log.dir", "logs");
        String file = "server-" + LocalDate.now().format(DATE) + ".log";
        Path folder = Path.of(dir);
        try {
            Files.createDirectories(folder);
        } catch (IOException ignored) {}
        return folder.resolve(file);
    }

    public static void log(String event, String ip, String user, String action, String message) {
        String ts = ISO.format(Instant.now().atOffset(ZoneOffset.UTC));
        String json = buildJson(ts, event, ip, user, action, message) + System.lineSeparator();
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        synchronized (LOCK) {
            try {
                Files.write(logPathForToday(), bytes,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                // Fallback para consola em UTF-8 (caso Windows mude a página de códigos)
                try {
                    PrintStream ps = new PrintStream(System.out, true, "UTF-8");
                    ps.print(json);
                } catch (UnsupportedEncodingException ex) {
                    System.out.print(json);
                }
            }
        }
    }

    private static String buildJson(String ts, String event, String ip, String user, String action, String message) {
        return "{\"ts\":\"" + esc(ts) + "\"," +
               "\"event\":\"" + esc(event) + "\"," +
               "\"ip\":\"" + esc(ip) + "\"," +
               "\"user\":\"" + esc(user) + "\"," +
               "\"action\":\"" + esc(action) + "\"," +
               "\"message\":\"" + esc(message) + "\"}";
    }

    /** Escapes JSON simples (aspas, barra invertida e quebras). */
    private static String esc(String s) {
        if (s == null) return "-";
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}
