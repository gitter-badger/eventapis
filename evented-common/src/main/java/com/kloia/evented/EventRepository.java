package com.kloia.evented;

import com.kloia.eventapis.pojos.EventKey;
import com.kloia.eventapis.pojos.PublishedEvent;

/**
 * Created by zeldalozdemir on 21/04/2017.
 */
public interface EventRepository<E extends Entity>{
//    <P extends PublishedOnly> void publishEvent(P publishedEvent) throws EventPulisherException;

//    <D> EventKey recordEntityEvent(E previousEntityState, D eventData) throws EventStoreException;
//    <D> EventKey recordEntityEvent(D eventData) throws EventStoreException;

    void markFail(String opId);

    <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException;
    <P extends PublishedEvent> EventKey recordAndPublish(E entity, P publishedEvent) throws EventStoreException;
    <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent) throws EventStoreException;

}
