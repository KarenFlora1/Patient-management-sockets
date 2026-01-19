package client.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientConfig {
    private String host;
    private int port;

    public ClientConfig(String configFilePath) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Arquivo de configuração do cliente não encontrado, usando defaults.");
        }

        this.host = props.getProperty("server.host", "localhost");
        this.port = Integer.parseInt(props.getProperty("server.port", "9090")); // default ajustado
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ClientConfig{host='" + host + "', port=" + port + "}";
    }
}
