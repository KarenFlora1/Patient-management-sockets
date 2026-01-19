package common.dto;

import java.io.Serializable;
import common.model.Patient;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    
    public String action;       

    
    public Patient patient;

    
    public String username;     
    public String password;     
    public String token;        

    
    public static Request ping() {
        Request r = new Request();
        r.action = "ping";
        return r;
    }

    public static Request create(Patient p) {
        Request r = new Request();
        r.action = "createPatient";
        r.patient = p;
        return r;
    }

    public static Request listPatients() {
        Request r = new Request();
        r.action = "listPatients";
        return r;
    }

    public static Request update(Patient p) {
        Request r = new Request();
        r.action = "UPDATE_PATIENT";
        r.patient = p;
        return r;
    }

    
    public static Request login(String user, String pass) {
        Request r = new Request();
        r.action = "LOGIN";
        r.username = user;
        r.password = pass;
        return r;
    }
}
