package server.handler;

import common.dto.Request;
import common.dto.Response;
import common.model.Patient;
import server.service.PatientService;

import java.sql.SQLException;
import java.util.List;

public class PatientHandler {

    private PatientService service;

    public PatientHandler(PatientService service) {
        this.service = service;
    }

    public Response handle(Request req) {
        try {
            switch (req.getAction()) {
                case "ADD_PATIENT":
                    Patient p = (Patient) req.getParam("patient");
                    service.addPatient(p);
                    return new Response(true, "Paciente cadastrado com sucesso!", null);

                case "LIST_PATIENTS":
                    List<Patient> pacientes = service.listPatients();
                    return new Response(true, "Lista de pacientes", pacientes);

                default:
                    return new Response(false, "Ação não reconhecida", null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(false, "Erro ao acessar o banco: " + e.getMessage(), null);
        }
    }
}
