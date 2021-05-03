/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.newrelic.jfr.tosummary;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Summary;
import java.util.stream.Stream;
import jdk.jfr.consumer.RecordedEvent;

public class PerThreadNetworkReadSummarizer implements EventToSummary {
  private final String threadName;
  private final LongSummarizer bytesSummary;
  private final SimpleDurationSummarizer duration;
  public static final String JFR_SOCKET_READ_DURATION = "jfr.SocketRead.duration";
  public static final String JFR_SOCKET_READ_BYTES_READ = "jfr.SocketRead.bytesRead";
  public static final String BYTES_READ = "bytesRead";
  public static final String THREAD_NAME = "thread.name";

  public PerThreadNetworkReadSummarizer(String threadName, long startTimeMs) {
    this(threadName, new LongSummarizer(BYTES_READ), new SimpleDurationSummarizer(startTimeMs));
  }

  public PerThreadNetworkReadSummarizer(
      String threadName, LongSummarizer longSummarizer, SimpleDurationSummarizer duration) {
    this.threadName = threadName;
    this.bytesSummary = longSummarizer;
    this.duration = duration;
  }

  @Override
  public String getEventName() {
    return NetworkReadSummarizer.EVENT_NAME;
  }

  @Override
  public void accept(RecordedEvent ev) {
    bytesSummary.accept(ev);
    duration.accept(ev);
  }

  @Override
  public Stream<Summary> summarize() {
    Attributes attr = new Attributes().put(THREAD_NAME, threadName);
    Summary outRead =
        new Summary(
            JFR_SOCKET_READ_BYTES_READ,
            bytesSummary.getCount(),
            bytesSummary.getSum(),
            bytesSummary.getMin(),
            bytesSummary.getMax(),
            duration.getStartTimeMs(),
            duration.getEndTimeMs(),
            attr);
    Summary outDuration =
        new Summary(
            JFR_SOCKET_READ_DURATION,
            bytesSummary.getCount(),
            duration.getDurationMillis(),
            duration.getMinDurationMillis(),
            duration.getMaxDurationMillis(),
            duration.getStartTimeMs(),
            outRead.getEndTimeMs(),
            attr);
    return Stream.of(outRead, outDuration);
  }

  public void reset() {
    bytesSummary.reset();
    duration.reset();
  }
}
