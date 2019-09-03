package com.kimifdw.demo.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.actor
 * @date 2019/9/2 13:29
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class ActorApp {

    public static void main(String[] args) {
        // create actor
        Props props1 = Props.create(MyActor.class);
        // 获取demo actor的引用
        ActorSystem actorSystem = ActorSystem.create();
        ActorRef demoActor = actorSystem.actorOf(DemoActor.props(42), "demo");

    }
}
