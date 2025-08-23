package common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private int idade;
    private String bi; // substituindo CPF

    // Novos campos
    private String telefone;
    private String endereco;
    private String email;
    private String genero;
    private LocalDate dataNascimento;
    private String historicoMedico;
    private String planoSaude;

    // Construtor
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

    // Getters
    public String getNome() { return nome; }
    public int getIdade() { return idade; }
    public String getBi() { return bi; }
    public String getTelefone() { return telefone; }
    public String getEndereco() { return endereco; }
    public String getEmail() { return email; }
    public String getGenero() { return genero; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getHistoricoMedico() { return historicoMedico; }
    public String getPlanoSaude() { return planoSaude; }

    @Override
    public String toString() {
        return nome + " | Idade: " + idade + " | BI: " + bi + " | Telefone: " + telefone;
    }
}

