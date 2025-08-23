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

        this.port = Integer.parseInt(props.getProperty("server.port", "12345"));
        this.dbFile = props.getProperty("db.file", "clinic.db");
    }

    public int getPort() {
        return port;
    }

    public String getDbFile() {
        return dbFile;
    }
}
