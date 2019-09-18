package com.kimifdw.demo.persistent;

import akka.persistence.AbstractPersistentActor;
import akka.persistence.Recovery;
import akka.persistence.SnapshotSelectionCriteria;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.persistent
 * @date 2019/9/18 22:29
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
class Cmd implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String data;

    public Cmd(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

class Evt implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String data;

    public Evt(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}

class ExampleState implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<String> events;

    public ExampleState(ArrayList<String> events) {
        this.events = events;
    }

    public ExampleState() {
        this(new ArrayList<>());
    }

    public ExampleState copy() {
        return new ExampleState(new ArrayList<>(events));
    }

    public void update(Evt evt) {
        events.add(evt.getData());
    }

    public int size() {
        return events.size();
    }

    @Override
    public String toString() {
        return events.toString();
    }
}

public class ExamplePersistentActor extends AbstractPersistentActor {

    private ExampleState state = new ExampleState();
    private int snapShotInterval = 1000;

    public int getNumEvents() {
        return state.size();
    }

    @Override
    public String persistenceId() {
        return "sample-id-1";
    }

    /**
     * 自定义恢复
     *
     * @return
     */
    @Override
    public Recovery recovery() {
        return Recovery.create(SnapshotSelectionCriteria.none());
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
            .match(Evt.class, state::update)
            //.matchAny(SnapshotOffer.class, ss -> state = (ExampleState) ss.)
            .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Cmd.class, c -> {
                final String data = c.getData();
                final Evt evt = new Evt(data + "-" + getNumEvents());
                persist(
                    evt,
                    (Evt e) -> {
                        state.update(e);
                        getContext().getSystem().getEventStream().publish(e);
                        if (lastSequenceNr() % snapShotInterval == 0 && lastSequenceNr() != 0) {
                            saveSnapshot(state.copy());
                        }
                    }
                );
            })
            .matchEquals("print", s -> System.out.println(state))
            .build();
    }
}
