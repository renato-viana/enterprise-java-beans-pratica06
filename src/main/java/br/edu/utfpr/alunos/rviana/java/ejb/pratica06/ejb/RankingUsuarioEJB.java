package br.edu.utfpr.alunos.rviana.java.ejb.pratica06.ejb;

import br.edu.utfpr.alunos.rviana.java.ejb.pratica06.model.Usuario;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.Stateful;

/**
 *
 * @author Renato Borges Viana
 */
@Stateful
public class RankingUsuarioEJB {

    private final Map<String, Integer> ranking = new LinkedHashMap<>();

    public void addUsuario(Usuario usuario) {
        ranking.merge(usuario.getNome(), 1, Integer::sum);
    }

    public Map<String, Integer> getRanking() {
        return ranking.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
