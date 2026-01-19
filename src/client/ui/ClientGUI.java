package client.ui;

import common.model.Patient;
import common.dto.Request;
import common.dto.Response;
import client.config.ClientConfig;
import client.net.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.Normalizer;
import java.time.*;
import java.util.Date;
import java.util.regex.Pattern;

public class ClientGUI {

    private JFrame frame;
    private JTextField nomeField, idadeField, biField, telefoneField, enderecoField, emailField;
    private JComboBox<String> generoBox, planoBox;
    private JSpinner dataSpinner; // calendário
    private JTextArea historicoArea;
    private final ClientConnection conn;

    // ===== Regras de validação =====
    // Nome: aceita acentos (Unicode), espaços/apóstrofos/pontos/hífens, sem dígitos, min 2
    private static final Pattern NOME_RE = Pattern.compile("^[\\p{L}][\\p{L}\\s'’.-]{1,}$");

    // BI: 12 dígitos + 1 letra MAIÚSCULA (ex.: 123456789012A)
    private static final Pattern BI_RE = Pattern.compile("^[0-9]{12}[A-Z]$");

    // Telefone MZ: +258? 8(2|3|4|5|6|7) + 7 dígitos
    // 82/83 (mcel), 84/85 (Vodacom), 86/87 (Movitel)
    private static final Pattern MZ_PHONE_RE = Pattern.compile("^(?:\\+258)?8([2-7])\\d{7}$");

    // Endereço: letras (com acento)/números/espaços, mas NÃO começa com número, min 3
    private static final Pattern ENDERECO_RE = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ][A-Za-zÀ-ÖØ-öø-ÿ0-9\\s]{2,}$");

    // Email (após normalização ASCII + minúsculas):
    // - local (antes do @): apenas a-z, 0-9 e ponto; sem começar/terminar com ponto; sem ".."
    private static final Pattern EMAIL_LOCAL_ASCII = Pattern.compile("^[a-z0-9.]+$");
    // - domínios aceites: gmail.com OU *.co.mz
    private static final Pattern EMAIL_DOMAIN_GMAIL = Pattern.compile("^gmail\\.com$");
    private static final Pattern EMAIL_DOMAIN_CO_MZ = Pattern.compile("^[a-z0-9-]+(\\.[a-z0-9-]+)*\\.co\\.mz$");

    public ClientGUI(ClientConnection conn) {
        this.conn = conn;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Cadastro de Pacientes - Clínica");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 700);
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

        // Spinner de data (calendário) limitado até hoje
        SpinnerDateModel dateModel = new SpinnerDateModel(new Date(), null, new Date(), java.util.Calendar.DAY_OF_MONTH);
        dataSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dataSpinner, "yyyy-MM-dd");
        dataSpinner.setEditor(editor);

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
        addLabelAndComponent(frame, "Data de nascimento:", dataSpinner, gbc, y++);
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

        frame.setLocationRelativeTo(null);
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
            Patient p = validarForm(); // valida tudo e monta o Patient
            Request req = Request.create(p);
            Response resp = conn.send(req);

            if ("ok".equals(resp.status)) {
                JOptionPane.showMessageDialog(frame, "Paciente cadastrado com sucesso!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(frame, "Não foi possível cadastrar. Detalhes: " + resp.message);
            }
            this.frame.repaint();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Verifique os dados", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ocorreu um erro ao cadastrar. Tente novamente.");
        }
    }

    private void listarPacientes(ActionEvent e) {
        try {
            Request req = Request.listPatients();
            Response resp = conn.send(req);

            if ("ok".equals(resp.status) && resp.data != null) {
                java.util.List<Patient> pacientes = resp.data;
                new PatientListGUI(pacientes, conn);
            } else {
                JOptionPane.showMessageDialog(frame, "Não foi possível carregar a lista agora.");
            }
            this.frame.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Ocorreu um erro ao listar. Tente novamente.");
            this.frame.repaint();
        }
    }

    // ===== Validação centralizada =====
    private Patient validarForm() {
        String nome = trim(nomeField.getText());
        String idadeStr = trim(idadeField.getText());
        String bi = trim(biField.getText());
        String telefone = trim(telefoneField.getText());
        String endereco = trim(enderecoField.getText());
        String emailInput = trim(emailField.getText());

        String genero = (String) generoBox.getSelectedItem();
        String historico = historicoArea.getText();
        String plano = (String) planoBox.getSelectedItem();

        // Nome (aceita acentos)
        if (nome.length() < 2 || !NOME_RE.matcher(nome).matches() || nome.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Nome inválido. Use apenas letras (ex.: Joana Silva).");
        }

        // Idade
        int idade;
        try {
            idade = Integer.parseInt(idadeStr);
            if (idade < 0 || idade > 120) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Idade inválida. Informe um número entre 0 e 120.");
        }

        // BI
        if (!BI_RE.matcher(bi).matches()) {
            throw new IllegalArgumentException("BI inválido. Formato: 12 dígitos seguidos de 1 letra maiúscula (ex.: 123456789012A).");
        }

        // Telefone (com operadora)
        var m = MZ_PHONE_RE.matcher(telefone);
        if (!m.matches()) {
            throw new IllegalArgumentException("Telefone inválido. Exemplos: 84xxxxxxx (Vodacom), 86xxxxxxx (Movitel) ou +2588xxxxxxx.");
        }

        // Endereço
        if (!ENDERECO_RE.matcher(endereco).matches()) {
            throw new IllegalArgumentException("Endereço inválido. Comece com letras (ex.: Rua 25 de Setembro 123).");
        }

        // Email — normalizar para ASCII minúsculo, sem acentos
        String email = toAsciiLower(emailInput);
        emailField.setText(email); // mostra ao utilizador a versão normalizada

        String[] parts = email.split("@", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("E-mail inválido. Exemplos: joao.silva@gmail.com ou nome@empresa.co.mz.");
        }
        String local = parts[0];
        String domain = parts[1];

        // local: a-z, 0-9, pontos; sem começar/terminar com ponto; sem ".."
        if (!EMAIL_LOCAL_ASCII.matcher(local).matches() || local.startsWith(".") || local.endsWith(".") || local.contains("..")) {
            throw new IllegalArgumentException("E-mail inválido. Use letras, números e pontos no nome (sem '..' e sem começar/terminar com ponto).");
        }

        boolean gmail = EMAIL_DOMAIN_GMAIL.matcher(domain).matches();
        boolean comz  = EMAIL_DOMAIN_CO_MZ.matcher(domain).matches();
        if (!gmail && !comz) {
            throw new IllegalArgumentException("Domínio não suportado. Use gmail.com ou um domínio empresarial .co.mz.");
        }
        if (gmail && !Character.isLetter(local.codePointAt(0))) {
            throw new IllegalArgumentException("Para Gmail, o nome antes do @ deve começar por letra (ex.: joao.silva@gmail.com).");
        }

        // Data de nascimento (do spinner) – já limitada a não-futuro
        Date chosen = (Date) dataSpinner.getValue();
        LocalDate dataNascimento = chosen.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro.");
        }

        // Coerência idade x data (preciso: considera mês/dia)
        int idadeCalculada = Period.between(dataNascimento, LocalDate.now()).getYears();
        if (idade != idadeCalculada) {
            throw new IllegalArgumentException(
                "A idade informada não bate com a data de nascimento. Idade correta hoje: " + idadeCalculada + " ano(s).");
        }

        return new Patient(nome, idade, bi, telefone, endereco, email, genero, dataNascimento, historico, plano);
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }

    /** Converte para minúsculas ASCII removendo acentos (NFD) e espaços estranhos. */
    private static String toAsciiLower(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
        String noMarks = nfd.replaceAll("\\p{M}+", "");
        String cleaned = noMarks.replaceAll("\\s+", "");
        return cleaned.toLowerCase();
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
        dataSpinner.setValue(new Date());
        historicoArea.setText("");
    }

    public static void main(String[] args) {
        try {
            ClientConfig config = new ClientConfig("client.properties");
            ClientConnection conn = new ClientConnection(config);
            // Login antes de abrir a GUI principal
            new LoginDialog(null, conn).setVisible(true); // bloqueia até autenticar
            SwingUtilities.invokeLater(() -> new ClientGUI(conn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
