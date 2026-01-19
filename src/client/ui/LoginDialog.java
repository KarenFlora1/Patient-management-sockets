package client.ui;

import client.net.ClientConnection;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private final ClientConnection conn;

    public LoginDialog(Window owner, ClientConnection conn) {
        super(owner, "Autenticação", ModalityType.APPLICATION_MODAL);
        this.conn = conn;

        var user = new JTextField(16);
        var pass = new JPasswordField(16);

        var form = new JPanel(new GridBagLayout());
        var gbc  = new GridBagConstraints();
        gbc.insets = new Insets(6,8,6,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; form.add(new JLabel("Utilizador:"), gbc);
        gbc.gridx=1; form.add(user, gbc);

        gbc.gridx=0; gbc.gridy=1; form.add(new JLabel("Palavra-passe:"), gbc);
        gbc.gridx=1; form.add(pass, gbc);

        var btnOk = new JButton("Entrar");
        btnOk.addActionListener(e -> {
            try {
                if (conn.login(user.getText().trim(), new String(pass.getPassword()))) {
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Não foi possível autenticar: " + ex.getMessage(),
                        "Login", JOptionPane.ERROR_MESSAGE);
            }
        });

        var btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> System.exit(0));

        var bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnOk); bottom.add(btnCancel);

        var root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }
}
