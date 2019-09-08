package com.kimifdw.demo.supervisor;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.supervisor
 * @date 2019/9/8 23:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class MySupervisorStrategy extends AbstractActor {

    private static SupervisorStrategy strategy = new OneForOneStrategy(
            10,
            Duration.ofMinutes(1),
            DeciderBuilder.match(ArithmeticException.class, e -> SupervisorStrategy.restart())
                    .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                    .matchAny(o -> SupervisorStrategy.escalate())
                    .build()
    );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
