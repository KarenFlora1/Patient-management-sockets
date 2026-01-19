package client.ui;

import common.model.Patient;

import javax.swing.*;
import java.awt.*;

public class PatientDetailsDialog extends JDialog {

    public PatientDetailsDialog(Window owner, Patient p) {
        super(owner, "Detalhes do Paciente", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 440);
        setLocationRelativeTo(owner);

        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // NOVO: ID
        addRow(panel, gbc, row++, "ID:", p.id != null ? String.valueOf(p.id) : "—");

        addRow(panel, gbc, row++, "Nome:", nz(p.nome));
        addRow(panel, gbc, row++, "Idade:", String.valueOf(p.idade));
        addRow(panel, gbc, row++, "BI:", nz(p.bi));
        addRow(panel, gbc, row++, "Telefone:", nz(p.telefone));
        addRow(panel, gbc, row++, "Endereço:", nz(p.endereco));
        addRow(panel, gbc, row++, "Email:", nz(p.email));
        addRow(panel, gbc, row++, "Género:", nz(p.genero));
        addRow(panel, gbc, row++, "Data de Nascimento:", p.dataNascimento != null ? p.dataNascimento.toString() : "");
        addRow(panel, gbc, row++, "Plano de Saúde:", nz(p.planoSaude));

        // Histórico médico ocupa mais espaço
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel("Histórico Médico:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        var txt = new JTextArea(nz(p.historicoMedico));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setEditable(false);
        var scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(300, 110));
        panel.add(scroll, gbc);

        // Botões
        var buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        buttons.add(btnFechar);

        var container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.CENTER);
        container.add(buttons, BorderLayout.SOUTH);
        setContentPane(container);
    }

    private static void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        var field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        panel.add(field, gbc);
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
