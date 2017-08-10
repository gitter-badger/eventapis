package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.pojos.Event;
import com.kloia.eventapis.pojos.PublishedEventWrapper;

import java.util.UUID;

/**
 * Created by zeldalozdemir on 20/04/2017.
 */
public interface IOperationRepository {
    void createOperation(String mainAggregateName, UUID opId);

//    Operation getOperation(UUID opid);

    void publishEvent(String name, PublishedEventWrapper event);

    void appendEvent(String opId, Event event);

    void updateEvent(String opId, String eventId, SerializableConsumer<Event> action);

    void failOperation(String opId, String eventId, SerializableConsumer<Event> action);
}
