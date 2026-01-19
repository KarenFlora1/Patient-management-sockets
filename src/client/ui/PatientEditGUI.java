package client.ui;

import client.net.ClientConnection;
import common.dto.Request;
import common.dto.Response;
import common.model.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

/**
 * Janela para editar um paciente existente.
 * Usa id (se existir) ou (nome + dataNascimento) como chave para o UPDATE no servidor.
 */
public class PatientEditGUI extends JDialog {

    private final ClientConnection conn;
    private final Patient paciente; // referência ao objeto a editar

    // campos de UI
    private JTextField idField;       // NOVO: ID (só leitura)
    private JTextField nomeField;
    private JTextField dataNascField;
    private JTextField idadeField;
    private JTextField biField;
    private JTextField telefoneField;
    private JTextField enderecoField;
    private JTextField emailField;
    private JComboBox<String> generoBox;
    private JComboBox<String> planoBox;
    private JTextArea historicoArea;

    /**
     * @param owner    Janela mãe (pode ser o frame da lista)
     * @param conn     Ligação cliente-servidor
     * @param paciente Paciente a editar (será atualizado in-place)
     */
    public PatientEditGUI(Window owner, ClientConnection conn, Patient paciente) {
        super(owner, "Editar Paciente", ModalityType.APPLICATION_MODAL);
        this.conn = conn;
        this.paciente = paciente;

        buildUI();
        fillFromPatient();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        var main = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int y = 0;

        // NOVO: ID (só leitura)
        idField = new JTextField();
        idField.setEditable(false);
        addRow(main, gbc, y++, "ID:", idField);

        nomeField = new JTextField();
        nomeField.setEditable(false); // chave
        addRow(main, gbc, y++, "Nome:", nomeField);

        dataNascField = new JTextField();
        dataNascField.setEditable(false); // chave (yyyy-MM-dd)
        addRow(main, gbc, y++, "Data de nascimento (yyyy-MM-dd):", dataNascField);

        idadeField = new JTextField();
        addRow(main, gbc, y++, "Idade:", idadeField);

        biField = new JTextField();
        addRow(main, gbc, y++, "BI:", biField);

        telefoneField = new JTextField();
        addRow(main, gbc, y++, "Telefone:", telefoneField);

        enderecoField = new JTextField();
        addRow(main, gbc, y++, "Endereço:", enderecoField);

        emailField = new JTextField();
        addRow(main, gbc, y++, "Email:", emailField);

        generoBox = new JComboBox<>(new String[]{"Masculino", "Feminino"});
        addRow(main, gbc, y++, "Género:", generoBox);

        historicoArea = new JTextArea(4, 20);
        var scrollHist = new JScrollPane(historicoArea);
        addRow(main, gbc, y++, "Histórico Médico:", scrollHist);

        planoBox = new JComboBox<>(new String[]{"Nenhum", "Plano A", "Plano B", "Plano C"});
        addRow(main, gbc, y++, "Plano de Saúde:", planoBox);

        // botões
        var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(this::guardar);

        var btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        buttons.add(btnGuardar);
        buttons.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        main.add(buttons, gbc);

        setContentPane(main);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(comp, gbc);
    }

    private void fillFromPatient() {
        idField.setText(paciente.id != null ? paciente.id.toString() : "—"); // NOVO
        nomeField.setText(nz(paciente.nome));
        dataNascField.setText(paciente.dataNascimento != null ? paciente.dataNascimento.toString() : "");

        idadeField.setText(Integer.toString(paciente.idade));
        biField.setText(nz(paciente.bi));
        telefoneField.setText(nz(paciente.telefone));
        enderecoField.setText(nz(paciente.endereco));
        emailField.setText(nz(paciente.email));

        generoBox.setSelectedItem(nz(paciente.genero).isEmpty() ? "Masculino" : paciente.genero);
        historicoArea.setText(nz(paciente.historicoMedico));
        planoBox.setSelectedItem(nz(paciente.planoSaude).isEmpty() ? "Nenhum" : paciente.planoSaude);
    }

    private void guardar(ActionEvent e) {
        try {
            // validações muito leves (mantemos a validação forte no formulário principal)
            int idade = parseIdade(idadeField.getText().trim());

            // Nome e Data não alteram (chave)
            String dataIso = dataNascField.getText().trim();
            LocalDate dn = dataIso.isEmpty() ? null : LocalDate.parse(dataIso);

            // atualiza o objeto
            paciente.idade = idade;
            paciente.bi = biField.getText().trim();
            paciente.telefone = telefoneField.getText().trim();
            paciente.endereco = enderecoField.getText().trim();
            paciente.email = emailField.getText().trim();
            paciente.genero = (String) generoBox.getSelectedItem();
            paciente.historicoMedico = historicoArea.getText();
            paciente.planoSaude = (String) planoBox.getSelectedItem();
            paciente.dataNascimento = dn; // reatribui o mesmo valor

            // envia UPDATE ao servidor (server dá prioridade a id, se existir)
            Request req = Request.update(paciente);
            Response resp = conn.send(req);

            if ("ok".equals(resp.status)) {
                JOptionPane.showMessageDialog(this, "Paciente actualizado com sucesso.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível actualizar: " + resp.message,
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao guardar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int parseIdade(String s) {
        try {
            int v = Integer.parseInt(s);
            if (v < 0 || v > 120) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Idade inválida. Informe um número entre 0 e 120.");
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
