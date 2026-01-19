package server.service;

import server.dao.PatientDAO;
import common.dto.Request;
import common.dto.Response;
import common.model.Patient;

import java.sql.SQLException;
import java.util.List;

public class PatientService {

    private final PatientDAO dao;

    public PatientService(String dbFile) throws SQLException {
        this.dao = new PatientDAO(dbFile);
    }

    public Response handle(Request req) {
        if (req == null || req.action == null) {
            return Response.error("Ação inválida");
        }

        String action = req.action;

        try {
            switch (action) {

                // ping
                case "ping":
                case "PING":
                    return Response.okMsg("pong");

                // criar paciente (mantém nomes antigos)
                case "createPatient":
                case "ADD_PATIENT": {
                    Patient p = req.patient;
                    try {
                        PatientValidator.validateForCreate(p);
                    } catch (IllegalArgumentException ve) {
                        return Response.error(ve.getMessage());
                    }
                    dao.addPatient(p); // preenche p.id se inserir ou ignora se duplicado (UNIQUE)
                    return Response.okMsg("Paciente criado com sucesso");
                }

                // listar (mantém nomes antigos)
                case "listPatients":
                case "LIST_PATIENTS": {
                    List<Patient> list = dao.getAllPatients();
                    Response r = new Response();
                    r.status = "ok";
                    r.message = "ok";
                    r.data = list;
                    return r;
                }

                // remover (prioriza id; fallback para (nome+dataNascimento))
                case "DELETE_PATIENT": {
                    Patient p = req.patient;
                    if (p == null) return Response.error("Payload ausente.");
                    int n;
                    if (p.id != null) {
                        n = dao.deleteById(p.id);
                    } else if (p.nome != null && p.dataNascimento != null) {
                        n = dao.deleteByNomeDataNascimento(p.nome, p.dataNascimento.toString());
                    } else {
                        return Response.error("Informe o id ou (nome + data de nascimento).");
                    }
                    if (n > 0) return Response.okMsg("Paciente removido.");
                    return Response.error("Não encontrado.");
                }

                // actualizar (prioriza id; fallback antigo)
                case "UPDATE_PATIENT":
                case "updatePatient": {
                    Patient p = req.patient;
                    if (p == null) return Response.error("Dados do paciente não enviados.");
                    int n;
                    if (p.id != null) {
                        try {
                            PatientValidator.validateForUpdateById(p);
                        } catch (IllegalArgumentException ve) {
                            return Response.error(ve.getMessage());
                        }
                        n = dao.updatePatientById(p);
                    } else if (p.nome != null && p.dataNascimento != null) {
                        try {
                            PatientValidator.validateForUpdateByNomeData(p);
                        } catch (IllegalArgumentException ve) {
                            return Response.error(ve.getMessage());
                        }
                        n = dao.updatePatient(p);
                    } else {
                        return Response.error("Para actualizar, informe id ou (nome + data de nascimento).");
                    }
                    if (n > 0) return Response.okMsg("Paciente actualizado com sucesso.");
                    return Response.error("Não encontrado para actualizar.");
                }

                default:
                    return Response.error("Ação desconhecida: " + action);
            }

        } catch (SQLException e) {
            return Response.error("Erro de BD: " + e.getMessage());
        } catch (Exception e) {
            return Response.error("Erro: " + e.getMessage());
        }
    }

    // utilitários
    public void addPatient(Patient p) throws SQLException { dao.addPatient(p); }
    public List<Patient> listPatients() throws SQLException { return dao.getAllPatients(); }
    public void close() throws SQLException { dao.close(); }
}
