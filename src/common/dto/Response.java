package common.dto;

import java.io.Serializable;
import java.util.List;
import common.model.Patient;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    public String status;        // "ok" | "error"
    public String message;       // mensagem opcional
    public List<Patient> data;   // lista (listPatients)
    public String token;         // devolvido no LOGIN

    public static Response okMsg(String msg) {
        Response r = new Response();
        r.status = "ok";
        r.message = msg;
        return r;
    }

    public static Response error(String msg) {
        Response r = new Response();
        r.status = "error";
        r.message = msg;
        return r;
    }
}
