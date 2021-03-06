package com.kloia.eventapis.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.kloia.eventapis.api.EventRepository;
import com.kloia.eventapis.api.RollbackSpec;
import com.kloia.eventapis.api.ViewQuery;
import com.kloia.eventapis.cassandra.EntityEvent;
import com.kloia.eventapis.common.PublishedEvent;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import com.kloia.eventapis.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.lang.reflect.ParameterizedType;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AggregateListener {
    ViewQuery viewQuery;
    EventRepository eventRepository;
    SnapshotRepository snapshotRepository;
    private ObjectMapper objectMapper;
    private final Map<String, Map.Entry<Class<PublishedEvent>,RollbackSpec>> rollbackSpecMap;

    public AggregateListener(ViewQuery viewQuery, EventRepository eventRepository, SnapshotRepository snapshotRepository, List<RollbackSpec> rollbackSpecs, ObjectMapper objectMapper) {
        this.viewQuery = viewQuery;
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = objectMapper;
        rollbackSpecMap = new HashMap<>();
        rollbackSpecs.forEach(rollbackSpec -> {
            ParameterizedType type = (ParameterizedType) TypeToken.of(rollbackSpec.getClass()).getSupertype(RollbackSpec.class).getType();
            try {
                Class<PublishedEvent> publishedEventClass = (Class<PublishedEvent>) Class.forName(type.getActualTypeArguments()[0].getTypeName());
                rollbackSpecMap.put(publishedEventClass.getSimpleName(), new AbstractMap.SimpleEntry<>(publishedEventClass, rollbackSpec));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void listenOperations(ConsumerRecord<String, Operation> data) {
        try {
            log.info("Incoming Message: " + data.key()+ " "+ data.value());
            if (data.value().getTransactionState() == TransactionState.TXN_FAILED) {
                List<EntityEvent> entityEvents = eventRepository.markFail(data.key());
                entityEvents.forEach(entityEvent -> {
                    try {
                        Map.Entry<Class<PublishedEvent>, RollbackSpec> specEntry = rollbackSpecMap.get(entityEvent.getEventType());
                        if(specEntry != null)
                            specEntry.getValue().rollback(new EntityEventWrapper<>(specEntry.getKey(),objectMapper,entityEvent).getEventData());
                    } catch (EventStoreException e) {
                        log.warn(e.getMessage(),e);
                    }
                });
                snapshotRepository.save(viewQuery.queryByOpId(data.key()));
            }else if (data.value().getTransactionState() == TransactionState.TXN_SUCCEDEED) {
                snapshotRepository.save(viewQuery.queryByOpId(data.key()));
            }
        } catch (EventStoreException e) {
            log.error("Error while applying operation:"+data.toString()+" Exception:"+e.getMessage(),e);
        }
    }
}
