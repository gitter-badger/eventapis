package com.kloia.eventapis.kafka;

import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
public class KafkaOperationRepository implements IOperationRepository {
    private KafkaProducer<String,Operation> operationsKafka;
    private KafkaProducer<String,PublishedEventWrapper> eventsKafka;

    public KafkaOperationRepository(KafkaProducer<String, Operation> operationsKafka, KafkaProducer<String, PublishedEventWrapper> eventsKafka) {
        this.operationsKafka = operationsKafka;
        this.eventsKafka = eventsKafka;
    }

    /*    private KafkaTemplate<UUID,Operation> operationsKafka;
    private KafkaTemplate<UUID,PublishedEventWrapper> eventsKafka;

    @Autowired
    public KafkaOperationRepository(@Qualifier("operationsKafka") KafkaTemplate<UUID,Operation> operationsKafka,
                                    @Qualifier("eventsKafka") KafkaTemplate<UUID,PublishedEventWrapper> eventsKafka) {
        this.eventsKafka = eventsKafka;
        this.operationsKafka = operationsKafka;
    }*/

    @Override
    public void createOperation(String eventName, UUID opId) {
//        operationsKafka.send(new ProducerRecord("operation-events",opId,new CreateOperationEvent(eventName)));
//        operationsKafka.send("operation-events",opId, new CreateOperationEvent(eventName));
    }


/*    @Override
    public void publishEvent(UUID opId, Event event) {
        kafkaTemplate.send("operations",opId, event);

    }*/

    @Override
    public void appendEvent(String opId, Event event) {
//        operationsKafka.send("operation-events",opId, new AppendEventToOperation(event));
    }

    @Override
    public void updateEvent(String opId, String eventId, SerializableConsumer<Event> action) {
//        operationsKafka.send("operation-events",opId, new UpdateOperationEvent(eventId,action));
    }

    @Override
    public void failOperation(String opId, String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setTransactionState(TransactionState.TXN_FAILED);
        operationsKafka.send(new ProducerRecord<>("operation-events", opId, operation));
    }
    @Override
    public void successOperation(String opId, String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setTransactionState(TransactionState.TXN_SUCCEDEED);
        operationsKafka.send(new ProducerRecord<>("operation-events", opId, operation));
    }

    public void publishEvent(String name, PublishedEventWrapper event) {
        eventsKafka.send(new ProducerRecord<>(name,event.getOpId(),event));
    }
}
