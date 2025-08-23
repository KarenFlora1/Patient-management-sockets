package client.ui;

import common.model.Patient;
import common.dto.Request;
import common.dto.Response;
import client.config.ClientConfig;
import client.net.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClientGUI {

    private JFrame frame;
    private JTextField nomeField, idadeField, biField, telefoneField, enderecoField, emailField;
    private JComboBox<String> generoBox, planoBox;
    private JFormattedTextField dataNascimentoField;
    private JTextArea historicoArea;
    private ClientConnection conn;

    public ClientGUI(ClientConnection conn) {
        this.conn = conn;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Cadastro de Pacientes - Clínica");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 650);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5);

        nomeField = new JTextField();
        idadeField = new JTextField();
        biField = new JTextField();
        telefoneField = new JTextField();
        enderecoField = new JTextField();
        emailField = new JTextField();

        generoBox = new JComboBox<>(new String[]{"Masculino", "Feminino"});
        planoBox = new JComboBox<>(new String[]{"Nenhum", "Plano A", "Plano B", "Plano C"});

        dataNascimentoField = new JFormattedTextField(DateTimeFormatter.ofPattern("yyyy-MM-dd").toFormat());
        dataNascimentoField.setValue(LocalDate.now());

        historicoArea = new JTextArea(4, 20);
        JScrollPane historicoScroll = new JScrollPane(historicoArea);

        int y = 0;
        addLabelAndComponent(frame, "Nome:", nomeField, gbc, y++);
        addLabelAndComponent(frame, "Idade:", idadeField, gbc, y++);
        addLabelAndComponent(frame, "BI:", biField, gbc, y++);
        addLabelAndComponent(frame, "Telefone:", telefoneField, gbc, y++);
        addLabelAndComponent(frame, "Endereço:", enderecoField, gbc, y++);
        addLabelAndComponent(frame, "Email:", emailField, gbc, y++);
        addLabelAndComponent(frame, "Gênero:", generoBox, gbc, y++);
        addLabelAndComponent(frame, "Data Nascimento (YYYY-MM-DD):", dataNascimentoField, gbc, y++);
        addLabelAndComponent(frame, "Histórico Médico:", historicoScroll, gbc, y++);
        addLabelAndComponent(frame, "Plano de Saúde:", planoBox, gbc, y++);

        JButton cadastrarButton = new JButton("Cadastrar");
        cadastrarButton.addActionListener(this::cadastrar);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        frame.add(cadastrarButton, gbc);

        JButton listarButton = new JButton("Listar Pacientes");
        listarButton.addActionListener(this::listarPacientes);
        gbc.gridy = ++y;
        frame.add(listarButton, gbc);

        frame.setVisible(true);
    }

    private void addLabelAndComponent(Container container, String label, Component comp, GridBagConstraints gbc, int y) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        container.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        container.add(comp, gbc);
    }

    private void cadastrar(ActionEvent e) {
        try {
            // Obtendo dados diretamente dos campos
            String nome = nomeField.getText();
            int idade = Integer.parseInt(idadeField.getText());
            String bi = biField.getText();
            String telefone = telefoneField.getText();
            String endereco = enderecoField.getText();
            String email = emailField.getText();
            String genero = (String) generoBox.getSelectedItem();
            LocalDate dataNascimento = LocalDate.parse(dataNascimentoField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
            String historico = historicoArea.getText();
            String plano = (String) planoBox.getSelectedItem();

            // Criando paciente
            Patient p = new Patient(nome, idade, bi, telefone, endereco, email, genero, dataNascimento, historico, plano);

            // Enviando request
            Request req = new Request("ADD_PATIENT");
            req.addParam("patient", p);
            conn.send(req);

            JOptionPane.showMessageDialog(frame, "Paciente cadastrado com sucesso!");
            clearFields();
            this.frame.repaint(); // repaint após alteração
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao cadastrar paciente: " + ex.getMessage());
            this.frame.repaint();
        }
    }

    private void listarPacientes(ActionEvent e) {
        try {
            Request req = new Request("LIST_PATIENTS");
            Response resp = conn.send(req);

            if (resp.isSuccess() && resp.getPayload() != null) {
                @SuppressWarnings("unchecked")
                java.util.List<Patient> pacientes = (java.util.List<Patient>) resp.getPayload();
                new PatientListGUI(pacientes);
            } else {
                JOptionPane.showMessageDialog(frame, "Não foi possível obter a lista de pacientes.");
            }
            this.frame.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao listar pacientes: " + ex.getMessage());
            this.frame.repaint();
        }
    }

    private void clearFields() {
        nomeField.setText("");
        idadeField.setText("");
        biField.setText("");
        telefoneField.setText("");
        enderecoField.setText("");
        emailField.setText("");
        generoBox.setSelectedIndex(0);
        planoBox.setSelectedIndex(0);
        dataNascimentoField.setValue(LocalDate.now());
        historicoArea.setText("");
    }

    public static void main(String[] args) {
        try {
            ClientConfig config = new ClientConfig("client.properties");
            ClientConnection conn = new ClientConnection(config);
            SwingUtilities.invokeLater(() -> new ClientGUI(conn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
