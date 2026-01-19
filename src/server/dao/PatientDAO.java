package server.dao;

import common.model.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    private final String url;
    private final Connection conn;

    public PatientDAO(String dbFile) throws SQLException {
        this.url = "jdbc:sqlite:" + dbFile;
        this.conn = DriverManager.getConnection(url);

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patient (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome             TEXT    NOT NULL,
                    idade            INTEGER,
                    bi               TEXT,
                    telefone         TEXT,
                    endereco         TEXT,
                    email            TEXT,
                    genero           TEXT,
                    dataNascimento   TEXT,   -- ISO yyyy-MM-dd
                    historicoMedico  TEXT,
                    planoSaude       TEXT,
                    UNIQUE(nome, dataNascimento)
                )
            """);
        }
    }

    /** Adiciona paciente; tenta preencher p.id (novo ou existente se duplicado). */
    public void addPatient(Patient p) throws SQLException {
        String sql = "INSERT OR IGNORE INTO patient " +
                "(nome, idade, bi, telefone, endereco, email, genero, dataNascimento, historicoMedico, planoSaude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.nome);
            ps.setInt(2, p.idade);
            ps.setString(3, p.bi);
            ps.setString(4, p.telefone);
            ps.setString(5, p.endereco);
            ps.setString(6, p.email);
            ps.setString(7, p.genero);
            ps.setString(8, p.dataNascimento != null ? p.dataNascimento.toString() : null);
            ps.setString(9, p.historicoMedico);
            ps.setString(10, p.planoSaude);

            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.id = rs.getInt(1);
                }
            } else {
                // Foi ignorado (duplicado pela UNIQUE). Tentar descobrir o id existente.
                p.id = findIdByNomeDataNascimento(p.nome, p.dataNascimento != null ? p.dataNascimento.toString() : null);
            }
        }
    }

    /** Atualiza por ID (preferível). Devolve nº de linhas afetadas. */
    public int updatePatientById(Patient p) throws SQLException {
        if (p.id == null) return 0;
        String sql = """
            UPDATE patient
            SET nome = ?, idade = ?, bi = ?, telefone = ?, endereco = ?, email = ?,
                genero = ?, dataNascimento = ?, historicoMedico = ?, planoSaude = ?
            WHERE id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.nome);
            ps.setInt(2, p.idade);
            ps.setString(3, p.bi);
            ps.setString(4, p.telefone);
            ps.setString(5, p.endereco);
            ps.setString(6, p.email);
            ps.setString(7, p.genero);
            ps.setString(8, p.dataNascimento != null ? p.dataNascimento.toString() : null);
            ps.setString(9, p.historicoMedico);
            ps.setString(10, p.planoSaude);
            ps.setInt(11, p.id);
            return ps.executeUpdate();
        }
    }

    /** Mantido para compatibilidade: atualização por (nome + dataNascimento). */
    public int updatePatient(Patient p) throws SQLException {
        String sql = """
            UPDATE patient
            SET idade = ?, bi = ?, telefone = ?, endereco = ?, email = ?,
                genero = ?, historicoMedico = ?, planoSaude = ?
            WHERE nome = ? AND dataNascimento = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.idade);
            ps.setString(2, p.bi);
            ps.setString(3, p.telefone);
            ps.setString(4, p.endereco);
            ps.setString(5, p.email);
            ps.setString(6, p.genero);
            ps.setString(7, p.historicoMedico);
            ps.setString(8, p.planoSaude);
            ps.setString(9, p.nome);
            ps.setString(10, (p.dataNascimento != null) ? p.dataNascimento.toString() : null);
            return ps.executeUpdate();
        }
    }

    /** Lista todos (inclui id). */
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT id, nome, idade, bi, telefone, endereco, email, genero, dataNascimento, historicoMedico, planoSaude " +
                     "FROM patient ORDER BY nome";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = new Patient(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getInt("idade"),
                        rs.getString("bi"),
                        rs.getString("telefone"),
                        rs.getString("endereco"),
                        rs.getString("email"),
                        rs.getString("genero"),
                        rs.getString("dataNascimento") != null ? LocalDate.parse(rs.getString("dataNascimento")) : null,
                        rs.getString("historicoMedico"),
                        rs.getString("planoSaude")
                );
                list.add(p);
            }
        }
        return list;
    }

    /** Remover por ID (preferível). */
    public int deleteById(int id) throws SQLException {
        String sql = "DELETE FROM patient WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** Compatibilidade: remover por (nome + dataNascimento). */
    public int deleteByNomeDataNascimento(String nome, String dataIso) throws SQLException {
        String sql = "DELETE FROM patient WHERE nome = ? AND dataNascimento = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, dataIso);
            return ps.executeUpdate();
        }
    }

    /** Obtém id pela chave antiga (nome + dataNascimento). */
    private Integer findIdByNomeDataNascimento(String nome, String dataIso) throws SQLException {
        String sql = "SELECT id FROM patient WHERE nome = ? AND dataNascimento = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, dataIso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return null;
    }

    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }
}
