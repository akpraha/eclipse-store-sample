package com.github.akpraha.metricmonitor.metricstore.service;

import com.github.akpraha.metricmonitor.metricstore.domain.MetricDefinition;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricSeries;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricValue;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Andy Key
 * @created 12/20/2024, Fri
 */
public interface MetricStoreService {
    MetricDefinition createMetricDefinition(String name, List<String> dimensions);

    Collection<MetricDefinition> getMetricDefinitions(List<String> names);

    Collection<MetricSeries> search(String metricName, Map<String, String> dimensions);

    Collection<MetricDefinition> getAllMetricDefinitions();

    void storeMetricData(UUID uuid, List<MetricValue> data);

    List<MetricValue> readMetricData(UUID uuid, Instant startTime, long durationMs);

    UUID createMetricSeries(String name, Map<String, String> dimensions);
}
