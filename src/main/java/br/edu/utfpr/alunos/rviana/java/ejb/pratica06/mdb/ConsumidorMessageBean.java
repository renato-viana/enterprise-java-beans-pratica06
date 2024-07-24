package br.edu.utfpr.alunos.rviana.java.ejb.pratica06.mdb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 *
 * @author Renato Borges Viana
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
            propertyValue = "java/Fila"),
    @ActivationConfigProperty(propertyName = "destinationType",
            propertyValue = "javax.jms.Queue")
})
public class ConsumidorMessageBean implements MessageListener {

    private static final Logger logger = Logger.getLogger(ConsumidorMessageBean.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String texto = textMessage.getText();
                logger.log(Level.INFO, "Mensagem recebida da fila JMS:");
                logger.log(Level.INFO, "{0}", texto);
            } else {
                logger.log(Level.WARNING, "Tipo de mensagem inesperado: {0}", message.getClass().getName());
            }
        } catch (JMSException e) {
            logger.log(Level.SEVERE, "Erro ao processar mensagem da fila JMS", e);
        }
    }

}
