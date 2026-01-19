package server.handler;

import common.dto.Request;
import common.dto.Response;
import server.service.PatientService;

public class PatientHandler {

    private final PatientService service;

    public PatientHandler(PatientService service) {
        this.service = service;
    }

    public Response handle(Request req) {
        // delega para o service, que já sabe tratar cada ação
        return service.handle(req);
    }
}
