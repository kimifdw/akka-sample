package com.kimifdw.demo;

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
 * @date 2019/8/28 09:03
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class Printer extends AbstractActor {
    //#printer-messages
    static public Props props() {
        return Props.create(Printer.class, Printer::new);
    }

    //#printer-messages
    static class Greeting {
        final String message;

        Greeting(String message) {
            this.message = message;
        }
    }
    //#printer-messages

    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Printer() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Greeting.class, greeting -> {
                    log.info(greeting.message);
                })
                .build();
    }
}
