package com.kimifdw.demo.device;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo
 * @date 2019/8/30 14:13
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class IotSupervisor extends AbstractActor {

    /**
     * akka log
     */
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    public static Props props() {
        return Props.create(IotSupervisor.class, IotSupervisor::new);
    }

    @Override
    public void preStart() throws Exception {
        log.info("IOT Application started");
    }

    @Override
    public void postStop() throws Exception {
        log.info("IOT Application stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}
