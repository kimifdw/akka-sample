package com.kimifdw.demo.persistent;

import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.persistent
 * @date 2019/9/18 22:51
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class MyPersistentActor5 extends AbstractPersistentActor {

    @Override
    public String persistenceId() {
        return "my-stable-persistence-id";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
            .match(RecoveryCompleted.class,
                r -> {

                })
            .match(String.class, this::handleEvent)
            .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(String.class, s -> s.equals("cmd"), s -> persist("evt", this::handleEvent))
            .build();
    }

    private void handleEvent(String event) {

    }
}
