package com.github.akpraha.metricmonitor.metricstore.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple metric value with a timestamp and a double value.
 * @author Andy Key
 * @created 10/20/2024, Sun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricValue {
    private Instant timestamp;
    private double value;
}
