package br.edu.utfpr.alunos.rviana.java.ejb.pratica06.jsf;

import br.edu.utfpr.alunos.rviana.java.ejb.pratica06.ejb.RankingUsuarioEJB;
import br.edu.utfpr.alunos.rviana.java.ejb.pratica06.ejb.SomaEjb;
import br.edu.utfpr.alunos.rviana.java.ejb.pratica06.model.Usuario;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

/**
 *
 * @author Renato Borges Viana
 */
@Named(value = "usuarioManagedBean")
@SessionScoped
public class RankingUsuarioManagedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RankingUsuarioManagedBean.class.getName());

    private String nome;
    private int numero1;
    private int numero2;
    private int resultado;
    private Map<String, Integer> rankingAnterior;

    @EJB
    private RankingUsuarioEJB rankingUsuarioEJB;

    @EJB
    private SomaEjb somaEjb;

    @Resource(mappedName = "java/Fila")
    private Queue javaFila;

    @Inject
    @JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
    private JMSContext context;

    public RankingUsuarioManagedBean() {
    }

    @PostConstruct
    public void init() {
        gerarNumerosAleatorios();
        rankingAnterior = new HashMap<>();
    }

    public void verificarResultado() {
        if (somaEjb.verificarResultado(numero1 + numero2, resultado)) {
            addUsuario();
            limparCampos();

            FacesContext.getCurrentInstance().addMessage("form:botaoVerificar",
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Resposta correta - Você ganhou um ponto!", null));

            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("nome", nome);

            gerarNumerosAleatorios();

            Map<String, Integer> rankingAtual = rankingUsuarioEJB.getRanking();
            if (houveMudancaNaLideranca(rankingAnterior, rankingAtual)) {
                enviarRankingParaFila(rankingAtual);
            }

            rankingAnterior = rankingAtual;

        } else {
            FacesContext.getCurrentInstance().addMessage("form:botaoVerificar",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Resposta incorreta - Tente novamente!", null));
        }
    }

    public void addUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        rankingUsuarioEJB.addUsuario(usuario);

        FacesContext.getCurrentInstance().addMessage("form:inputNome",
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Usuário " + nome + " adicionado com sucesso!", null));
    }

    public Map<String, Integer> getRanking() {
        return rankingUsuarioEJB.getRanking();
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public int getNumero1() {
        return numero1;
    }

    public int getNumero2() {
        return numero2;
    }

    public int getResultado() {
        return resultado;
    }

    public void setResultado(int resultado) {
        this.resultado = resultado;
    }

    private void gerarNumerosAleatorios() {
        this.numero1 = somaEjb.gerarNumeroAleatorio();
        this.numero2 = somaEjb.gerarNumeroAleatorio();
    }

    private void limparCampos() {
        nome = "";
        resultado = 0;
    }

    /**
     * Verifica se houve mudança na liderança do ranking entre o ranking
     * anterior e o atual. Não há um novo líder se o ranking anterior ou atual
     * estiver vazio, ou se os líderes do ranking atual estiverem empatados com
     * os líderes do ranking anterior.
     *
     * @param rankingAnterior O ranking anterior como um Mapa de nome de usuário
     * para pontuação.
     * @param rankingAtual O ranking atual como um Mapa de nome de usuário para
     * pontuação.
     * @return true se houve mudança na liderança, false caso contrário.
     */
    private boolean houveMudancaNaLideranca(Map<String, Integer> rankingAnterior, Map<String, Integer> rankingAtual) {
        if (rankingAnterior.isEmpty()) {
            logger.log(Level.INFO, "Ranking anterior está vazio.");
            return false;
        }

        if (rankingAtual == null || rankingAtual.isEmpty()) {
            logger.log(Level.INFO, "Ranking atual está vazio ou nulo");
            return false;
        }

        List<Map.Entry<String, Integer>> listaAnterior = rankingAnterior.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        List<Map.Entry<String, Integer>> listaAtual = rankingAtual.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        String liderAnterior = listaAnterior.get(0).getKey();
        String liderAtual = listaAtual.get(0).getKey();

        // Verifica se houve mudança na liderança
        if (!liderAtual.equals(liderAnterior)) {
            logger.log(Level.INFO, "Mudança na liderança detectada. Novo campeão: {0}", liderAtual);
            return true;
        } else {
            // Se não houve mudança na liderança, verifica se houve empate nos líderes
            boolean empate = listaAtual.size() > 1 && listaAtual.get(0).getValue().equals(listaAtual.get(1).getValue());
            if (empate) {
                logger.log(Level.INFO, "Empate no ranking. Não há novo campeão.");
                return false;
            } else {
                logger.log(Level.INFO, "O líder do ranking permaneceu o mesmo.");
                return false;
            }
        }

    }

    private void enviarRankingParaFila(Map<String, Integer> ranking) {
        String rankingString = formatRankingAsString(ranking);
        try {
            context.createProducer().send(javaFila, rankingString);
            logger.log(Level.INFO, "Ranking enviado para a fila JMS.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao enviar ranking para a fila JMS", e);
        }
    }

    private String formatRankingAsString(Map<String, Integer> ranking) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ranking:\n");
        for (Map.Entry<String, Integer> entry : ranking.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

}
