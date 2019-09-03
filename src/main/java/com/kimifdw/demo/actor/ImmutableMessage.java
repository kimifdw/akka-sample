package com.kimifdw.demo.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 不可变的消息
 * </p>
 *
 * @author fudongwei
 * @package com.kimifdw.demo.actor
 * @date 2019/9/3 23:05
 * @copyright: Copyright (c) 2019
 * @version: V1.0.0
 * @modified: fudongwei
 */
public class ImmutableMessage {

    private final int sequenceNumber;

    private final List<String> values;

    public ImmutableMessage(int sequenceNumber, List<String> values) {
        this.sequenceNumber = sequenceNumber;
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public List<String> getValues() {
        return values;
    }
}
