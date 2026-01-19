package server.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
    private int port;
    private String dbFile;

    public ServerConfig(String configFilePath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Arquivo de configuração não encontrado, usando valores padrões.");
        }

        // Porta e ficheiro de BD com defaults ajustados
        this.port = Integer.parseInt(props.getProperty("server.port", "9090"));
        this.dbFile = props.getProperty("db.file", "clinic.db");
    }

    public int getPort() {
        return port;
    }

    public String getDbFile() {
        return dbFile;
    }

    @Override
    public String toString() {
        return "ServerConfig{port=" + port + ", dbFile='" + dbFile + "'}";
    }
}
