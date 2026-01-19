package common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    // Chave técnica (preenchida pelo DAO após INSERT ou ao listar)
    public Integer id;  // null para registos novos

    // Campos públicos (mantidos)
    public String nome;
    public int idade;
    public String bi;
    public String telefone;
    public String endereco;
    public String email;
    public String genero;
    public LocalDate dataNascimento;
    public String historicoMedico;
    public String planoSaude;

    // Construtor vazio (requisito para Gson)
    public Patient() {}

    // Construtor completo (sem id)
    public Patient(String nome, int idade, String bi, String telefone, String endereco, String email,
                   String genero, LocalDate dataNascimento, String historicoMedico, String planoSaude) {
        this.nome = nome;
        this.idade = idade;
        this.bi = bi;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.historicoMedico = historicoMedico;
        this.planoSaude = planoSaude;
    }

    // Construtor com id (usado ao carregar da BD)
    public Patient(Integer id, String nome, int idade, String bi, String telefone, String endereco, String email,
                   String genero, LocalDate dataNascimento, String historicoMedico, String planoSaude) {
        this(nome, idade, bi, telefone, endereco, email, genero, dataNascimento, historicoMedico, planoSaude);
        this.id = id;
    }

    @Override
    public String toString() {
        String idPart = (id != null ? "#" + id + " " : "");
        return idPart + nome + " | Idade: " + idade + " | BI: " + bi + " | Telefone: " + telefone;
    }
}
