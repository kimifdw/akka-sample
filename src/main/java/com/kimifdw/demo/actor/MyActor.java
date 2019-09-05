package com.kimifdw.demo.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

import java.time.Duration;

/**
 * <p>
 * 我的actor
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.actor
 * @date 2019/9/2 13:23
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class MyActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    @Override
    public void preStart() {

    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        for (ActorRef each : getContext().getChildren()) {
            getContext().unwatch(each);
            getContext().stop(each);
        }
        postStop();
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        preStart();
    }

    @Override
    public void postStop() {

    }

    /**
     * 处理actor收到的消息
     *
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(
                String.class, s -> {
                    log.info("Received String message: {}", s);
                }
        ).matchAny(o -> log.info("received unknown message")).build()
                ;
    }
}

/**
 * stash or unstash
 */
class ActorWithProtocol extends AbstractActorWithStash {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("open", s -> {
                    getContext()
                            .become(receiveBuilder().matchEquals("write", ws -> {

                                    })
                                            .matchEquals("close", cs -> {
                                                unstashAll();
                                                getContext().unbecome();
                                            })
                                            .matchAny(msg -> stash()).build(), false
                            );
                })
                .matchAny(msg -> stash())
                .build();
    }
}

/**
 * become：热更新actor;unbecome：非热更新
 */
class Swapper extends AbstractLoggingActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("hi", s -> {
                    log().info("hi");
                    getContext().become(receiveBuilder().matchEquals("hi", x -> {
                        log().info("Ho");
                        getContext().unbecome();
                    }).build(), false);
                }).build();
    }
}

class HotSwapActor extends AbstractActor {

    private AbstractActor.Receive angry;
    private AbstractActor.Receive happy;

    public HotSwapActor() {
        angry = receiveBuilder()
                .matchEquals("foo", s -> {
                    getSender().tell("I am already angry?", getSelf());
                })
                .matchEquals("bar", s -> {
                    getSender().tell("I am already happy:-)", getSelf());
                })
                .matchEquals("foo", s -> {
                    getContext().become(angry);
                })
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("foo", s -> getContext().become(angry))
                .matchEquals("bar", s -> getContext().become(happy))
                .build();
    }
}

/**
 * 定时执行
 */
class TimerActor extends AbstractActorWithTimers {

    private static Object TICK_KEY = "TickKey";

    private static final class FirstTick {
    }

    private static final class Tick {
    }

    public TimerActor() {
        getTimers().startSingleTimer(TICK_KEY, new FirstTick(), Duration.ofMillis(50));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FirstTick.class,
                        message -> {
                            getTimers().startPeriodicTimer(TICK_KEY, new Tick(), Duration.ofSeconds(1));
                        })
                .match(Tick.class,
                        message -> {

                        }).build();
    }
}

/**
 * 接收超时actor
 */
class ReceiveTimeoutActor extends AbstractActor {

    public ReceiveTimeoutActor() {
        getContext().setReceiveTimeout(Duration.ofSeconds(10));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("Hello", s -> {
                    getContext().setReceiveTimeout(Duration.ofSeconds(1));
                })
                .match(ReceiveTimeout.class, r -> {
                    getContext().cancelReceiveTimeout();
                }).build();
    }
}

class OptimizedActor extends UntypedAbstractActor {

    public static class Msg1 {
    }

    public static class Msg2 {
    }

    public static class Msg3 {
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof Msg1) {
            receiveMsg1((Msg1) message);
        } else if (message instanceof Msg2) {
            receiveMsg2((Msg2) message);
        } else if (message instanceof Msg3) {
            receiveMsg3((Msg3) message);
        } else {
            unhandled(message);
        }
    }

    private void receiveMsg1(Msg1 msg) {

    }

    private void receiveMsg2(Msg2 msg2) {

    }

    private void receiveMsg3(Msg3 msg3) {

    }
}

class WellStructuredActor extends AbstractActor {

    public static class Msg1 {
    }

    public static class Msg2 {
    }

    public static class Msg3 {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Msg1.class, this::receiveMsg1)
                .match(Msg2.class, this::receiveMsg2)
                .match(Msg3.class, this::receiveMsg3)
                .build();

    }

    private void receiveMsg1(Msg1 msg) {

    }

    private void receiveMsg2(Msg2 msg2) {

    }

    private void receiveMsg3(Msg3 msg3) {

    }
}

class Follower extends AbstractActor {

    final Integer identifyId = 1;

    public Follower() {
        ActorSelection selection = getContext().actorSelection("/user/another");
        selection.tell(new Identify(identifyId), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorIdentity.class, id -> id.getActorRef().isPresent(),
                        id -> {
                            ActorRef ref = id.getActorRef().get();
                            getContext().watch(ref);
                            getContext().become(active(ref));
                        })
                .match(ActorIdentity.class,
                        id -> !id.getActorRef().isPresent(),
                        id -> {
                            getContext().stop(getSelf());
                        }).build()
                ;
    }

    final AbstractActor.Receive active(final ActorRef another) {
        return receiveBuilder()
                .match(Terminated.class, t -> t.actor().equals(another), t -> getContext().stop(getSelf())).build();
    }
}

/**
 * 监控actor
 */
class WatchActor extends AbstractActor {

    private final ActorRef child = getContext().actorOf(Props.empty(), "target");
    private ActorRef lastSender = ActorSystem.create().deadLetters();

    public WatchActor() {
        getContext().watch(child);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("kill",
                        s -> {
                            getContext().stop(child);
                            lastSender = getSender();
                        })
                .match(Terminated.class,
                        t -> t.actor().equals(child),
                        t -> {
                            lastSender.tell("finished", getSelf());

                        }
                ).build();
    }
}

/**
 * 依赖注入
 */
class DependencyInjector implements IndirectActorProducer {

    final Object applicationContext;
    final String beanName;

    public DependencyInjector(Object applicationContext, String beanName) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
    }

    @Override
    public DemoStringActor produce() {

        DemoStringActor result = new DemoStringActor((String) applicationContext);


        return result;
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return DemoStringActor.class;
    }
}

class DemoStringActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    static Props props(String message) {
        return Props.create(DemoStringActor.class, () -> new DemoStringActor(message));
    }

    final String message;

    public DemoStringActor(String message) {
        this.message = message;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, s -> {
            log.info("I received message of {}", message);
        }).build();
    }
}

/**
 * other good props create
 */
class DemoMessagesActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static class Greeting {

        private final String form;

        public Greeting(String form) {
            this.form = form;
        }

        public String getGreeter() {
            return form;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Greeting.class, g -> {
            log.info("I was greeted by {}", g.getGreeter());
        }).build();
    }
}

/**
 * one good props create
 */
class DemoActor extends AbstractActor {

    static Props props(Integer magicNumber) {
        return Props.create(DemoActor.class, () -> new DemoActor(magicNumber));
    }

    private final Integer magicNumber;

    public DemoActor(Integer magicNumber) {
        this.magicNumber = magicNumber;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Integer.class, i -> {
            getSender().tell(i + magicNumber, getSelf());
        }).build();
    }
}