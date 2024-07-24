package br.edu.utfpr.alunos.rviana.java.ejb.pratica06.model;

import java.io.Serializable;

/**
 *
 * @author Renato Borges Viana
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;

    private int pontos;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

}
