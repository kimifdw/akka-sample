package com.kimifdw.demo.device;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 设备分组查询
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo
 * @date 2019/8/31 14:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class DeviceGroupQuery extends AbstractActor {

    private static final class CollectionTimeout {
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final Map<ActorRef, String> actorToDeviceId;
    private final long requestId;
    final ActorRef requester;

    Cancellable queryTimeoutTimer;

    private DeviceGroupQuery(Map<ActorRef, String> actorToDeviceId, long requestId, ActorRef requester, FiniteDuration timeout) {
        this.actorToDeviceId = actorToDeviceId;
        this.requestId = requestId;
        this.requester = requester;

        queryTimeoutTimer = getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(timeout,
                        getSelf(),
                        new CollectionTimeout(),
                        getContext().getDispatcher(),
                        getSelf());
    }

    public static Props props(Map<ActorRef, String> actorToDeviceId,
                              long requestId,
                              ActorRef requester,
                              FiniteDuration timeout) {
        return Props.create(DeviceGroupQuery.class, () -> new DeviceGroupQuery(actorToDeviceId, requestId, requester, timeout));
    }

    @Override
    public void preStart() throws Exception {
        for (ActorRef deviceActor : actorToDeviceId.keySet()) {
            getContext().watch(deviceActor);
            deviceActor.tell(new Device.ReadTemperature(0L), getSelf());
        }
    }

    @Override
    public void postStop() throws Exception {
        queryTimeoutTimer.cancel();
    }

    @Override
    public Receive createReceive() {
        return waitingForReplies(new HashMap<>(), actorToDeviceId.keySet());
    }

    /**
     * 等待回复
     *
     * @param repliesSoFar
     * @param stillWaiting
     * @return
     */
    public Receive waitingForReplies(Map<String, DeviceGroup.TemperatureReading> repliesSoFar, Set<ActorRef> stillWaiting) {
        return receiveBuilder()
                .match(Device.RespondTemperatue.class, r -> {
                    ActorRef deviceActor = getSender();
                    DeviceGroup.TemperatureReading reading = r.value.map(v -> (DeviceGroup.TemperatureReading) new DeviceGroup.Temperature(v))
                            .orElse(DeviceGroup.TemperatureNotAvailable.INSTANCE);
                    receivedResponse(deviceActor, reading, stillWaiting, repliesSoFar);
                })
                .match(Terminated.class, t -> {
                    receivedResponse(t.getActor(), DeviceGroup.DeviceNotAvailable.INSTANCE,
                            stillWaiting, repliesSoFar);
                })
                .match(CollectionTimeout.class, t -> {
                    Map<String, DeviceGroup.TemperatureReading> replies = new HashMap<>(repliesSoFar);
                    for (ActorRef deviceActor : stillWaiting) {
                        String deviceId = actorToDeviceId.get(deviceActor);
                        replies.put(deviceId, DeviceGroup.DeviceTimedOut.INSTANCE);
                    }
                    requester.tell(new DeviceGroup.RespondAllTemperatures(requestId, replies), getSelf());
                    getContext().stop(getSelf());
                })
                .build();
    }

    /**
     * 接收设备乡音
     *
     * @param deviceActor
     * @param reading
     * @param stillWaiting
     * @param repliesSoFar
     */
    public void receivedResponse(ActorRef deviceActor,
                                 DeviceGroup.TemperatureReading reading,
                                 Set<ActorRef> stillWaiting,
                                 Map<String, DeviceGroup.TemperatureReading> repliesSoFar) {
        getContext().unwatch(deviceActor);
        String deviceId = actorToDeviceId.get(deviceActor);

        Set<ActorRef> newStillWaiting = new HashSet<>(stillWaiting);
        newStillWaiting.remove(deviceActor);

        Map<String, DeviceGroup.TemperatureReading> newRepliesSoFar = new HashMap<>(repliesSoFar);
        newRepliesSoFar.put(deviceId, reading);

        if (newStillWaiting.isEmpty()) {
            requester.tell(new DeviceGroup.RespondAllTemperatures(requestId, newRepliesSoFar), getSelf());
            getContext().stop(getSelf());
        } else {
            getContext().become(waitingForReplies(newRepliesSoFar, newStillWaiting));
        }
    }
}
