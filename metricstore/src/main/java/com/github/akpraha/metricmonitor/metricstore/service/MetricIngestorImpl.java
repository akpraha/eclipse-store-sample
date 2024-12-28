package com.github.akpraha.metricmonitor.metricstore.service;

import com.github.akpraha.metricmonitor.metricstore.api.dto.MetricSeriesDataTuple;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricValue;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Service;

/**
 * The MetricIngestor ingests a stream of metrics and is responsible for placing them in the metric store
 *
 * @author Andy Key
 * @created 12/28/2024, Sat
 */
@Service
public class MetricIngestorImpl implements MetricIngestor {
    private final MetricStoreService metricStoreSvc;
    private final Map<UUID, List<long[]>> ingestionBuffer = new HashMap<>();
    private final  ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final TaskExecutor taskExecutor;
    private final ScheduledFuture<?> handle;
    private TaskScheduler taskScheduler;
    private boolean running = false;

    public MetricIngestorImpl(MetricStoreService metricStoreSvc) {
        this.metricStoreSvc = metricStoreSvc;
        taskExecutor = new SimpleAsyncTaskExecutor();
        ((SimpleAsyncTaskExecutor) taskExecutor).setVirtualThreads(true);
        SimpleAsyncTaskScheduler asyncTaskScheduler = new SimpleAsyncTaskScheduler();
        asyncTaskScheduler.setTargetTaskExecutor(taskExecutor);
        taskScheduler = asyncTaskScheduler;

        handle = taskScheduler.scheduleAtFixedRate(this::flushIngestionBuffer,
                Instant.now().plusSeconds(15), Duration.ofSeconds(15));
        running = true;
    }

    @Override
    public void ingest(List<MetricSeriesDataTuple> data) {
        if (!running) {
            throw new RuntimeException("Service not running");
        }
        lock.writeLock().lock();
        try {
            data.forEach(msdt -> {
                ingestionBuffer.computeIfAbsent(msdt.uuid(), (uuid) -> new ArrayList<>()).add(msdt.values());
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        // attempt to pre-emptively flush anything in the buffer and cancel
        flushIngestionBuffer();
        handle.cancel(false);
    }

    private void flushIngestionBuffer() {
        // Adjust to a 15-second interval
        long epochSeconds = Instant.now().getEpochSecond();
        Instant timestamp = Instant.ofEpochSecond(epochSeconds - (epochSeconds % 15));

        lock.writeLock().lock();

        final Map<UUID, List<long[]>> ingested = new HashMap<>();
        try {
            // clear the ingestion buffer to be ready for more data
            // we just replace the list with a new empty list, since we assume that agents will continue sending data to the
            // same series
            ingestionBuffer.keySet().forEach(key -> {
                List<long[]> values = ingestionBuffer.get(key);
                ingested.put(key, new ArrayList(values));
                values.clear();
            });
        } finally {
            lock.writeLock().unlock();
        }

        ingested.forEach((uuid, valueLists) -> {
            List<MetricValue> list = new ArrayList<>();
            valueLists.forEach(arr -> {
                Arrays.stream(arr).forEach(v -> {
                    list.add(new MetricValue(timestamp, v));
                });
            });
            if (list.size() > 0) {
                metricStoreSvc.storeMetricData(uuid, list);
            }
        });
    }
}
