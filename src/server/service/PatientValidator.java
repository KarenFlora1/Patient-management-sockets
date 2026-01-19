package server.service;

import common.model.Patient;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/** Valida regras de negócio no SERVIDOR (criar/actualizar). */
public final class PatientValidator {

    // Nome: só letras (com acentos), espaços/apóstrofos/pontos/hífens, ≥2, sem dígitos
    private static final Pattern NOME_RE = Pattern.compile("^[\\p{L}][\\p{L}\\s'’.-]{1,}$");

    // BI: 12 dígitos + 1 letra MAIÚSCULA
    private static final Pattern BI_RE = Pattern.compile("^\\d{12}[A-Z]$");

    // Telefone MZ: +258? 8(2-7) + 7 dígitos
    private static final Pattern MZ_PHONE_RE = Pattern.compile("^(?:\\+258)?8[2-7]\\d{7}$");

    // Endereço: começa por letra (com acento), depois letras/dígitos/espaços; ≥3
    private static final Pattern ENDERECO_RE = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ][A-Za-zÀ-ÖØ-öø-ÿ0-9\\s]{2,}$");

    // Email (após normalização para ASCII minúsculo)
    private static final Pattern EMAIL_LOCAL_ASCII = Pattern.compile("^[a-z0-9.]+$");
    private static final Pattern EMAIL_DOMAIN_GMAIL = Pattern.compile("^gmail\\.com$");
    private static final Pattern EMAIL_DOMAIN_CO_MZ = Pattern.compile("^[a-z0-9-]+(\\.[a-z0-9-]+)*\\.co\\.mz$");

    private PatientValidator() {}

    /** Valida dados obrigatórios/consistência para CRIAR (chave = nome+dataNascimento). */
    public static void validateForCreate(Patient p) {
        if (p == null) throw new IllegalArgumentException("Dados do paciente não enviados.");
        requireKeysNomeData(p);
        validateCommonFields(p);
    }

    /** Valida dados obrigatórios/consistência para ACTUALIZAR quando a chave é nome+dataNascimento. */
    public static void validateForUpdateByNomeData(Patient p) {
        if (p == null) throw new IllegalArgumentException("Dados do paciente não enviados.");
        requireKeysNomeData(p);
        validateCommonFields(p);
    }

    /** Valida para ACTUALIZAR quando a chave usada é o ID. */
    public static void validateForUpdateById(Patient p) {
        if (p == null) throw new IllegalArgumentException("Dados do paciente não enviados.");
        if (p.id == null) throw new IllegalArgumentException("ID do paciente é obrigatório para actualizar.");
        // Se a data de nascimento vier preenchida, garantimos coerência com a idade.
        if (p.dataNascimento != null) {
            if (p.dataNascimento.isAfter(LocalDate.now()))
                throw new IllegalArgumentException("Data de nascimento não pode ser no futuro.");
            int idadeCalc = Period.between(p.dataNascimento, LocalDate.now()).getYears();
            if (p.idade < 0 || p.idade > 120 || p.idade != idadeCalc)
                throw new IllegalArgumentException("Idade não corresponde à data de nascimento.");
        } else {
            // Sem data, ao menos limitar a idade
            if (p.idade < 0 || p.idade > 120)
                throw new IllegalArgumentException("Idade inválida. Informe um valor entre 0 e 120.");
        }
        validateCommonFields(p);
    }

    /* ===== Helpers ===== */

    private static void requireKeysNomeData(Patient p) {
        if (p.nome == null || p.nome.isBlank() || !NOME_RE.matcher(p.nome).matches() || p.nome.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Nome inválido. Use apenas letras (pode conter acentos) e mínimo de 2 caracteres.");
        }
        if (p.dataNascimento == null) throw new IllegalArgumentException("Data de nascimento é obrigatória.");
        if (p.dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro.");
        }
        int idadeCalc = Period.between(p.dataNascimento, LocalDate.now()).getYears();
        if (p.idade < 0 || p.idade > 120 || p.idade != idadeCalc) {
            throw new IllegalArgumentException("Idade não corresponde à data de nascimento.");
        }
    }

    /** Valida campos comuns (BI, telefone, endereço, email). Normaliza email para ASCII/minúsculo. */
    public static void validateCommonFields(Patient p) {
        if (notEmpty(p.bi) && !BI_RE.matcher(p.bi).matches())
            throw new IllegalArgumentException("BI inválido. Formato: 12 dígitos seguidos de 1 letra maiúscula (ex.: 123456789012A).");

        if (notEmpty(p.telefone) && !MZ_PHONE_RE.matcher(p.telefone).matches())
            throw new IllegalArgumentException("Telefone inválido. Exemplos: 84xxxxxxx (Vodacom), 86xxxxxxx (Movitel) ou +2588xxxxxxx.");

        if (notEmpty(p.endereco) && !ENDERECO_RE.matcher(p.endereco).matches())
            throw new IllegalArgumentException("Endereço inválido. Deve começar por letra e ter pelo menos 3 caracteres.");

        if (notEmpty(p.email)) {
            String email = toAsciiLower(p.email);
            String[] parts = email.split("@", -1);
            if (parts.length != 2) throw new IllegalArgumentException("E-mail inválido.");
            String local = parts[0], domain = parts[1];

            if (!EMAIL_LOCAL_ASCII.matcher(local).matches() || local.startsWith(".") || local.endsWith(".") || local.contains(".."))
                throw new IllegalArgumentException("E-mail inválido. Use letras, números e pontos no nome (sem '..' e sem começar/terminar com ponto).");

            boolean gmail = EMAIL_DOMAIN_GMAIL.matcher(domain).matches();
            boolean comz  = EMAIL_DOMAIN_CO_MZ.matcher(domain).matches();
            if (!gmail && !comz)
                throw new IllegalArgumentException("Domínio de e-mail não suportado. Use gmail.com ou um domínio empresarial .co.mz.");
            if (gmail && !Character.isLetter(local.codePointAt(0)))
                throw new IllegalArgumentException("Para Gmail, o nome antes do @ deve começar por letra.");

            p.email = email; // normalizado
        }
    }

    private static boolean notEmpty(String s) { return s != null && !s.isBlank(); }

    private static String toAsciiLower(String s) {
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String noMarks = nfd.replaceAll("\\p{M}+", "");
        return noMarks.replaceAll("\\s+", "").toLowerCase();
    }
}
