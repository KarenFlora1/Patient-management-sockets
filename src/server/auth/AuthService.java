package server.auth;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Auth em memória: utilizadores fixos, sessão por token com expiração,
 * contagem de falhas e bloqueios temporários por utilizador/IP.
 */
public class AuthService {

    // ===== Utilizadores de teste =====
    private static final Map<String, String> USERS = Map.of(
        "admin",  "1234",
        "medico", "sd2025"
    );

    // ===== Sessões =====
    private static final long TOKEN_TTL_MS = 30 * 60_000L; // 30 minutos
    private static final ConcurrentHashMap<String, Session> TOKENS = new ConcurrentHashMap<>();
    private record Session(String user, long expiresAtMs) {}

    // ===== Rate limiting / lockouts =====
    private static final int  MAX_FAILS         = 2;                 // 5 falhas
    private static final long FAIL_WINDOW_MS    = 30_000L;      // em 10 min
    private static final long LOCK_DURATION_MS  = 60_000L;      // bloqueio 15 min

    // Trackers por user e por IP
    private static final ConcurrentHashMap<String, FailTracker> USER_FAILS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, FailTracker> IP_FAILS   = new ConcurrentHashMap<>();

    private static class FailTracker {
        int  attempts;           // nº falhas no intervalo
        long windowStartMs;      // início da janela
        long lockedUntilMs;      // 0 = não bloqueado
    }

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    // ===== API =====

    /** Tenta login. Se OK, cria token e devolve-o. Se bloqueado ou credenciais erradas, devolve null. */
    public static String login(String user, String pass, String ip) {
        long now = System.currentTimeMillis();

        // bloqueios
        if (isIpLocked(ip, now) || isUserLocked(user, now)) {
            return null;
        }

        // valida credenciais
        String expected = USERS.get(user);
        if (expected != null && expected.equals(pass)) {
            // sucesso → limpa contadores e cria token
            recordSuccess(user, ip);
            String token = UUID.randomUUID().toString();
            TOKENS.put(token, new Session(user, now + TOKEN_TTL_MS));
            return token;
        }

        // falha → conta e pode bloquear
        recordFailed(user, ip, now);
        return null;
    }

    /** Verifica token (e renova expiração de forma deslizante). */
    public static boolean isValid(String token) {
        if (token == null || token.isBlank()) return false;
        long now = System.currentTimeMillis();
        Session s = TOKENS.get(token);
        if (s == null) return false;
        if (s.expiresAtMs < now) {
            TOKENS.remove(token);
            return false;
        }
        // renovação deslizante: se passou de metade do TTL, estende
        long ageLeft = s.expiresAtMs - now;
        if (ageLeft < (TOKEN_TTL_MS / 2)) {
            TOKENS.replace(token, new Session(s.user, now + TOKEN_TTL_MS));
        }
        return true;
    }

    public static String userFromToken(String token) {
        Session s = TOKENS.get(token);
        return (s == null) ? null : s.user;
    }

    /** True se o utilizador está bloqueado neste instante. */
    public static boolean isUserLocked(String user) {
        return isUserLocked(user, System.currentTimeMillis());
    }

    /** True se o IP está bloqueado neste instante. */
    public static boolean isIpLocked(String ip) {
        return isIpLocked(ip, System.currentTimeMillis());
    }

    public static boolean isUserLocked(String user, long nowMs) {
        if (user == null) return false;
        FailTracker ft = USER_FAILS.get(user);
        return ft != null && ft.lockedUntilMs > nowMs;
    }

    public static boolean isIpLocked(String ip, long nowMs) {
        if (ip == null) return false;
        FailTracker ft = IP_FAILS.get(ip);
        return ft != null && ft.lockedUntilMs > nowMs;
    }

    /** Milissegundos restantes de bloqueio (user) — 0 se não bloqueado. */
    public static long userLockedRemainingMs(String user) {
        FailTracker ft = USER_FAILS.get(user);
        long now = System.currentTimeMillis();
        if (ft == null || ft.lockedUntilMs <= now) return 0;
        return ft.lockedUntilMs - now;
    }

    /** Milissegundos restantes de bloqueio (IP) — 0 se não bloqueado. */
    public static long ipLockedRemainingMs(String ip) {
        FailTracker ft = IP_FAILS.get(ip);
        long now = System.currentTimeMillis();
        if (ft == null || ft.lockedUntilMs <= now) return 0;
        return ft.lockedUntilMs - now;
    }

    // ===== Internos =====

    private static void recordFailed(String user, String ip, long now) {
        bump(USER_FAILS, user, now);
        bump(IP_FAILS, ip, now);
    }

    private static void recordSuccess(String user, String ip) {
        reset(USER_FAILS, user);
        reset(IP_FAILS, ip);
    }

    private static void bump(ConcurrentHashMap<String, FailTracker> map, String key, long now) {
        if (key == null) return;
        map.compute(key, (k, ft) -> {
            if (ft == null) ft = new FailTracker();
            // se bloqueado e ainda dentro do bloqueio, mantém
            if (ft.lockedUntilMs > now) return ft;

            // janela expirou? reinicia
            if (now - ft.windowStartMs > FAIL_WINDOW_MS) {
                ft.windowStartMs = now;
                ft.attempts = 0;
            }
            ft.attempts++;

            if (ft.attempts >= MAX_FAILS) {
                ft.lockedUntilMs = now + LOCK_DURATION_MS;
                ft.attempts = 0;
                ft.windowStartMs = now;
            }
            return ft;
        });
    }

    private static void reset(ConcurrentHashMap<String, FailTracker> map, String key) {
        if (key == null) return;
        map.remove(key);
    }

    // Apenas utilitário para logs ISO-8601 (se precisares noutro sítio)
    public static String nowIso() {
        return ISO.format(Instant.now().atOffset(ZoneOffset.UTC));
    }
}
