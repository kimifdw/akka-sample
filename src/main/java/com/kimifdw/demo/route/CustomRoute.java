package com.kimifdw.demo.route;

import akka.actor.ActorRef;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.RoutingLogic;
import akka.routing.SeveralRoutees;
import scala.collection.immutable.IndexedSeq;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.route
 * @date 2019/9/16 22:26
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class CustomRoute implements RoutingLogic {

  private final int nbrCopies;

  public CustomRoute(int nbrCopies) {
    this.nbrCopies = nbrCopies;
  }

  RoundRobinRoutingLogic roundRobin = new RoundRobinRoutingLogic();

  @Override
  public Routee select(Object message, IndexedSeq<Routee> routees) {
    List<Routee> targets = new ArrayList<>();
    for (int i = 0; i < nbrCopies; i++) {
      targets.add(roundRobin.select(message, routees));
    }
    return new SeveralRoutees(targets);
  }
}

final class TeatRoutee implements Routee {

  public final int n;

  public TeatRoutee(int n) {
    this.n = n;
  }

  @Override
  public void send(Object message, ActorRef sender) {

  }


  @Override
  public int hashCode() {
    return n;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof TeatRoutee) && n == ((TeatRoutee) obj).n;
  }
}
