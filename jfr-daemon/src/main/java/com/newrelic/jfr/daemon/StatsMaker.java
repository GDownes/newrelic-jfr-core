package com.newrelic.jfr.daemon;

import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import jdk.jfr.consumer.RecordedEvent;

public class StatsMaker implements TelemetrySender {

  private final MetricBatchMarshaller metricMarshaller =
      new MetricBatchMarshaller(
          new MetricBatchJsonCommonBlockWriter(new AttributesJson()),
          new MetricBatchJsonTelemetryBlockWriter(new MetricToJson()));

  private final EventBatchMarshaller eventMarshaller = new EventBatchMarshaller();
  private String metricJson;
  private String eventJson;

  public static void main(String[] args) {
    Path fileName = Paths.get(args[0]);

    StatsMaker maker = new StatsMaker();
    maker.run(fileName);
  }

  private void run(Path fileName) {
    BlockingQueue<RecordedEvent> queue = new LinkedBlockingQueue<>(250_000);
    RecordedEventBuffer recordedEventBuffer = new RecordedEventBuffer(queue);
    JFRUploader uploader = new JFRUploader(this, recordedEventBuffer);

    uploader.handleFile(fileName);
    outputStats();
  }

  private void outputStats() {
    double metricSize = (double) metricJson.length() / (1024 * 1024);
    System.out.println("Total metrics data (MB): " + metricSize);

    double eventSize = (double) metricJson.length() / (1024 * 1024);
    System.out.println("Total metrics data (MB): " + eventSize);
  }

  @Override
  public void sendBatch(MetricBatch batch) {
    System.out.println("Metrics to be sent: " + batch.size());
    metricJson = metricMarshaller.toJson(batch);
  }

  @Override
  public void sendBatch(EventBatch batch) {
    System.out.println("Events to be sent: " + batch.size());
    eventJson = eventMarshaller.toJson(batch);
  }
}