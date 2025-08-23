package client.ui;

import common.model.Patient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PatientListGUI {
    private JFrame frame;

    public PatientListGUI(List<Patient> pacientes) {
        frame = new JFrame("Lista de Pacientes");
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();
        for (Patient p : pacientes) {
            model.addElement(p.toString());
        }

        JList<String> list = new JList<>(model);
        JScrollPane scroll = new JScrollPane(list);
        frame.add(scroll, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
