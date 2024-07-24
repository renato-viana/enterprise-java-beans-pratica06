package br.edu.utfpr.alunos.rviana.java.ejb.pratica06.ejb;

import java.util.Random;
import javax.ejb.Stateless;

/**
 *
 * @author Renato Borges Viana
 */
@Stateless
public class SomaEjb {

    private Random random = new Random();

    public int gerarNumeroAleatorio() {
        return random.nextInt(100);
    }

    public boolean verificarResultado(int soma, int resultado) {
        return soma == resultado;
    }

}
