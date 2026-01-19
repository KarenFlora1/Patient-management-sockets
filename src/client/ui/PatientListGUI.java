package client.ui;

import client.net.ClientConnection;
import common.dto.Request;
import common.dto.Response;
import common.model.Patient;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class PatientListGUI {
    private final JFrame frame;
    private final JTable tabela;
    private final PacientesTableModel model;
    private final TableRowSorter<PacientesTableModel> sorter;
    private final ClientConnection conn;

    private final List<Patient> pacientes;

    public PatientListGUI(List<Patient> pacientesIniciais, ClientConnection conn) {
        this.conn = conn;
        this.pacientes = new ArrayList<>(pacientesIniciais);
        this.model = new PacientesTableModel(this.pacientes);

        frame = new JFrame("Lista de Pacientes");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1060, 540);
        frame.setLayout(new BorderLayout());

        // Topo: filtro
        var top = new JPanel(new BorderLayout(8, 8));
        var txtFiltro = new JTextField();
        top.add(new JLabel("Pesquisar:"), BorderLayout.WEST);
        top.add(txtFiltro, BorderLayout.CENTER);
        frame.add(top, BorderLayout.NORTH);

        tabela = new JTable(model);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setDefaultEditor(Object.class, null);

        sorter = new TableRowSorter<>(model);
        // Comparador numérico para a coluna ID (coluna 0)
        sorter.setComparator(0, Comparator.nullsLast(Integer::compareTo));
        // Ordenação por defeito: ID ASC
        sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        tabela.setRowSorter(sorter);

        // Pesquisa:
        // - se só dígitos -> localizar e seleccionar a linha por ID (sem filtrar)
        // - caso contrário -> filtra por todas as colunas (case-insensitive)
        txtFiltro.getDocument().addDocumentListener((SimpleDocListener) e -> {
            String q = txtFiltro.getText().trim();

            // Caso 1: só dígitos -> select por ID
            if (q.matches("\\d+")) {
                // limpar qualquer filtro activo
                sorter.setRowFilter(null);

                // normalizar zeros à esquerda e tentar localizar
                int id;
                try { id = Integer.parseInt(q.replaceFirst("^0+", "")); }
                catch (NumberFormatException ex) { return; }

                int modelRow = findRowById(id);
                if (modelRow >= 0) {
                    int viewRow = tabela.convertRowIndexToView(modelRow);
                    if (viewRow >= 0) {
                        tabela.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                        tabela.scrollRectToVisible(tabela.getCellRect(viewRow, 0, true));
                    }
                } else {
                    // se não encontrou, limpa selecção
                    tabela.clearSelection();
                }
                return;
            }

            // Caso 2: texto -> filtro por todas as colunas
            if (q.isEmpty()) {
                sorter.setRowFilter(null);
                tabela.clearSelection();
                return;
            }
            final String needle = q.toLowerCase();
            sorter.setRowFilter(new RowFilter<>() {
                @Override public boolean include(Entry<? extends PacientesTableModel, ? extends Integer> entry) {
                    for (int c = 0; c < entry.getValueCount(); c++) {
                        Object v = entry.getValue(c);
                        if (v != null && v.toString().toLowerCase().contains(needle)) return true;
                    }
                    return false;
                }
            });
        });

        // duplo clique = ver detalhes
        tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() != -1) {
                    abrirDetalhesSelecionado();
                }
            }
        });

        frame.add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Barra inferior
        var bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        var btnEditar = new JButton("Editar");
        btnEditar.addActionListener(e -> editarSelecionado());

        var btnDetalhes = new JButton("Ver detalhes");
        btnDetalhes.addActionListener(e -> abrirDetalhesSelecionado());

        var btnRemover = new JButton("Remover");
        btnRemover.addActionListener(e -> removerSelecionado());

        var btnReload = new JButton("Recarregar");
        btnReload.addActionListener(e -> recarregar());

        var btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> frame.dispose());

        bottom.add(btnEditar);
        bottom.add(btnDetalhes);
        bottom.add(btnRemover);
        bottom.add(btnReload);
        bottom.add(btnFechar);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private int findRowById(int id) {
        for (int i = 0; i < pacientes.size(); i++) {
            Integer pid = pacientes.get(i).id;
            if (pid != null && pid == id) return i;
        }
        return -1;
    }

    private void abrirDetalhesSelecionado() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(frame, "Seleccione um paciente."); return; }
        int modelRow = tabela.convertRowIndexToModel(viewRow);
        Patient p = pacientes.get(modelRow);
        new PatientDetailsDialog(frame, p).setVisible(true);
    }

    private void editarSelecionado() {
        if (conn == null) {
            JOptionPane.showMessageDialog(frame, "Ligação ao servidor indisponível nesta janela.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(frame, "Seleccione um paciente para editar."); return; }
        int modelRow = tabela.convertRowIndexToModel(viewRow);
        Patient p = pacientes.get(modelRow);

        new PatientEditGUI(frame, conn, p).setVisible(true);
        recarregar();
    }

    private void removerSelecionado() {
        int viewRow = tabela.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(frame, "Seleccione um paciente para remover."); return; }
        int modelRow = tabela.convertRowIndexToModel(viewRow);
        Patient p = pacientes.get(modelRow);

        String dataIso = (p.dataNascimento != null) ? p.dataNascimento.toString() : "";
        int opt = JOptionPane.showConfirmDialog(
                frame,
                "Remover o paciente:\n\n" + (p.id != null ? ("#" + p.id + " - ") : "") + p.nome + "  (" + dataIso + ")\n\nEsta operação é irreversível.",
                "Confirmar remoção",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (opt != JOptionPane.OK_OPTION) return;

        try {
            Request req = new Request();
            req.action = "DELETE_PATIENT";
            req.patient = p;
            Response resp = conn.send(req);

            if ("ok".equals(resp.status)) {
                JOptionPane.showMessageDialog(frame, "Paciente removido.");
                recarregar();
            } else {
                JOptionPane.showMessageDialog(frame, "Não foi possível remover: " + resp.message,
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Erro ao comunicar com o servidor: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recarregar() {
        if (conn == null) {
            JOptionPane.showMessageDialog(frame,
                    "Ligação ao servidor indisponível nesta janela.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Request req = Request.listPatients();
            Response resp = conn.send(req);

            if ("ok".equals(resp.status) && resp.data != null) {
                int antes = pacientes.size();
                pacientes.clear();
                pacientes.addAll(resp.data);
                model.fireTableDataChanged();

                // manter ordenação por ID asc
                sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));

                tabela.revalidate();
                tabela.repaint();
                JOptionPane.showMessageDialog(frame,
                        "Lista actualizada: " + pacientes.size() + " registos"
                                + (pacientes.size() == antes ? " (sem alterações)." : "."));
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Falha ao recarregar: " + resp.message,
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                    "Erro ao recarregar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== TableModel =====
    private static class PacientesTableModel extends AbstractTableModel {
        private final String[] colunas = { "ID", "Nome", "Idade", "BI", "Telefone", "Email", "Plano de Saúde", "Data Nascimento" };
        private final List<Patient> data;

        PacientesTableModel(List<Patient> data) { this.data = data; }
        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public int getColumnCount() { return colunas.length; }
        @Override public String getColumnName(int column) { return colunas[column]; }
        @Override public boolean isCellEditable(int r, int c) { return false; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Patient p = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.id; // Integer (facilita sort numérico)
                case 1 -> p.nome;
                case 2 -> p.idade;
                case 3 -> nz(p.bi);
                case 4 -> nz(p.telefone);
                case 5 -> nz(p.email);
                case 6 -> nz(p.planoSaude);
                case 7 -> p.dataNascimento != null ? p.dataNascimento.toString() : "";
                default -> "";
            };
        }
        private static String nz(String s) { return s == null ? "" : s; }
    }

    // ===== utilitários p/ filtro =====
    private interface SimpleDocListener extends javax.swing.event.DocumentListener {
        void onChange(javax.swing.event.DocumentEvent e);
        @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { onChange(e); }
        @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { onChange(e); }
        @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { onChange(e); }
    }
}
