/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.newrelic.jfr.tometric;

import static com.newrelic.jfr.RecordedObjectValidators.*;

import com.newrelic.jfr.Workarounds;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jdk.jfr.consumer.RecordedEvent;

public class AllocationRequiringGCMapper implements EventToMetric {
  public static final String SIMPLE_CLASS_NAME = AllocationRequiringGCMapper.class.getSimpleName();
  public static final String EVENT_NAME = "jdk.AllocationRequiringGC";
  public static final String SIZE = "size";

  @Override
  public List<? extends Metric> apply(RecordedEvent ev) {
    Attributes attr = new Attributes();
    long timestamp = ev.getStartTime().toEpochMilli();
    Optional<String> threadName = Workarounds.getThreadName(ev);
    threadName.ifPresent(thread -> attr.put("thread.name", thread));
    long gaugeValue = 0;
    if (hasField(ev, SIZE, SIMPLE_CLASS_NAME)) {
      gaugeValue = ev.getLong(SIZE);
    }
    return Collections.singletonList(
        new Gauge("jfr.AllocationRequiringGC.allocationSize", gaugeValue, timestamp, attr));
  }

  @Override
  public String getEventName() {
    return EVENT_NAME;
  }
}
