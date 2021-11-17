package com.batal.actions.services;

import com.batal.actions.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;

@Service
public class JmsService {

    private final JmsTemplate jmsTemplate;

    @Autowired
    public JmsService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    // TODO @Transactional
    public Message getMessage(String queueName) throws JMSException {
        jmsTemplate.setReceiveTimeout(100);
        javax.jms.Message message = jmsTemplate.receive(queueName);
        if (message == null) {
            return null;
        }
        // TODO Destination replyTo = message.getJMSReplyTo();
        String correlationID = message.getJMSCorrelationID();
        return new com.batal.actions.model.Message(
                message.getJMSMessageID().trim(),
                message.getBody(Object.class),
                correlationID != null ? correlationID.trim() : null);
    }

    // TODO @Transactional
    public void put(String queueName, Message message) {
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setDeliveryPersistent(false); // TODO Persistent config or parameter
        jmsTemplate.convertAndSend(queueName, message.getPayload(), m -> {
            m.setJMSCorrelationID(message.getCorrelationId());
            return m;
        });
    }
}
