package com.github.akpraha.metricmonitor.metricstore.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

/**
 * Simple metric value with a timestamp and a double value.
 * @author Andy Key
 * @created 10/20/2024, Sun
 */
public record MetricValue(Instant timestamp, double value) {
}
