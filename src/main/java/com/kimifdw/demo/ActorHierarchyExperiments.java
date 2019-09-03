package com.kimifdw.demo;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo
 * @date 2019/8/30 12:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class ActorHierarchyExperiments {

    public static void main(String[] args) throws IOException {
        ActorSystem system = ActorSystem.create("testSystem");

        ActorRef firstRef = system.actorOf(PrintMyActorRefActor.props(), "first-actor");
        System.out.println("First: " + firstRef);
        firstRef.tell("print it", ActorRef.noSender());


        ActorRef first = system.actorOf(StartStopActor1.props(), "first");
        first.tell("stop", ActorRef.noSender());

        ActorRef supervisingActor = system.actorOf(SupervisingActor.props(), "supervising-actor");
        supervisingActor.tell("failChild", ActorRef.noSender());


        System.out.println(">>> Press Enter to exit <<<");
        try {
            System.in.read();
        } finally {
            system.terminate();
        }
    }

}

class PrintMyActorRefActor extends AbstractActor {

    static Props props() {
        return Props.create(PrintMyActorRefActor.class, PrintMyActorRefActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("print it", p -> {
                    ActorRef secondRef = getContext().actorOf(Props.empty(), "second-actor");
                    System.out.println("Second: " + secondRef);
                }).build();
    }
}

/**
 * actor lifecycle
 */
class StartStopActor1 extends AbstractActor {

    static Props props() {
        return Props.create(StartStopActor1.class, StartStopActor1::new);
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("first started");

        getContext().actorOf(StartStopActor2.props(), "second");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("first stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("stop", s -> {
            getContext().stop(self());
        }).build();
    }
}

class StartStopActor2 extends AbstractActor {

    static Props props() {
        return Props.create(StartStopActor2.class, StartStopActor2::new);
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("second started");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("second stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }
}

/**
 * fail handling
 */
class SupervisingActor extends AbstractActor {
    static Props props() {
        return Props.create(SupervisingActor.class, SupervisingActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("failChild", f -> {
                    getContext().actorOf(SupervisedActor.props(), "superised-actor")
                            .tell("fail", getSelf());
                }).build();
    }
}

class SupervisedActor extends AbstractActor {
    static Props props() {
        return Props.create(SupervisedActor.class, SupervisedActor::new);
    }

    @Override
    public void preStart() throws Exception {
        System.out.println("supervised actor started");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("supervised actor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("fail", f -> {
                    System.out.println("supervised actor fails now");
                    throw new Exception(" I failed!");
                }).build();
    }
}
