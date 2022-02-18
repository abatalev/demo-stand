package com.batal.actions.model.fetchers;

import com.batal.actions.model.messages.Message;
import com.batal.actions.model.savers.MqSaver;
import com.batal.actions.services.JmsService;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

@SpringBootTest
public class MqTest {

    private static final Logger log = LoggerFactory.getLogger(MqTest.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private JmsService jmsService;

    // @Test
    public void check() {
        String payload = "111";
        String queueName = "DEV.QUEUE.1";

        // tuning
        jmsTemplate.setReceiveTimeout(100);
        jmsTemplate.setExplicitQosEnabled(true);
        jmsTemplate.setDeliveryPersistent(false);

        while (jmsTemplate.receive(queueName) != null) ;

        // send
        MqSaver mqSaver = getMqSaver(queueName);
        mqSaver.put(null, new Message(null, payload));

        // receive
        MqFetcher fetcher = getMqFetcher(queueName);
        Message fetchedMessage = fetcher.get(null);
        log.info("{}", fetchedMessage.getId());

        Assertions.assertEquals(payload, fetchedMessage.getPayload());
        // send with correlation id
        mqSaver.put(null, new Message(null, payload, fetchedMessage.getId()));
        // fetch with correlation id
        Message message2 = fetcher.get(null);
        log.info("{} {}", message2.getId(), message2.getCorrelationId());
    }

    private MqFetcher getMqFetcher(String queueName) {
        MqFetcher fetcher = new MqFetcher(queueName);
        fetcher.setJmsService(jmsService);
        return fetcher;
    }

    private MqSaver getMqSaver(String queueName) {
        MqSaver saver = new MqSaver(queueName);
        saver.setJmsService(jmsService);
        return saver;
    }
}
