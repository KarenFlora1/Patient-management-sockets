package server.dao;

import common.model.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private Connection conn;

    public PatientDAO(String dbFile) throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        initDatabase();
    }

    private void initDatabase() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS patients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "idade INTEGER," +
                "bi TEXT UNIQUE NOT NULL," +
                "telefone TEXT," +
                "endereco TEXT," +
                "email TEXT," +
                "genero TEXT," +
                "data_nascimento TEXT," +
                "historicoMedico TEXT," +
                "planoSaude TEXT" +
                ");";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void addPatient(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (nome, idade, bi, telefone, endereco, email, genero, data_nascimento, historicoMedico, planoSaude) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setInt(2, p.getIdade());
            ps.setString(3, p.getBi());
            ps.setString(4, p.getTelefone());
            ps.setString(5, p.getEndereco());
            ps.setString(6, p.getEmail());
            ps.setString(7, p.getGenero());
            ps.setString(8, p.getDataNascimento().toString());
            ps.setString(9, p.getHistoricoMedico());
            ps.setString(10, p.getPlanoSaude());
            ps.executeUpdate();
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Patient p = new Patient(
                        rs.getString("nome"),
                        rs.getInt("idade"),
                        rs.getString("bi"),
                        rs.getString("telefone"),
                        rs.getString("endereco"),
                        rs.getString("email"),
                        rs.getString("genero"),
                        LocalDate.parse(rs.getString("data_nascimento")),
                        rs.getString("historicoMedico"),
                        rs.getString("planoSaude")
                );
                list.add(p);
            }
        }
        return list;
    }

    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
