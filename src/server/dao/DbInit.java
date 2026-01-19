package server.dao;

import java.sql.*;

public class DbInit {
  public static void ensure(String dbFileOrUrl) throws SQLException {
    // aceita "clinic.db" ou "jdbc:sqlite:clinic.db"
    String url = dbFileOrUrl.startsWith("jdbc:") ? dbFileOrUrl : "jdbc:sqlite:" + dbFileOrUrl;

    try (Connection c = DriverManager.getConnection(url);
         Statement st = c.createStatement()) {

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
}
