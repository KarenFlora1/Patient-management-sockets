package server.service;

import server.dao.PatientDAO;
import common.model.Patient;

import java.sql.SQLException;
import java.util.List;

public class PatientService {

    private PatientDAO dao;

    public PatientService(String dbFile) throws SQLException {
        this.dao = new PatientDAO(dbFile);
    }

    public void addPatient(Patient p) throws SQLException {
        dao.addPatient(p);
    }

    public List<Patient> listPatients() throws SQLException {
        return dao.getAllPatients();
    }

    public void close() throws SQLException {
        dao.close();
    }
}
