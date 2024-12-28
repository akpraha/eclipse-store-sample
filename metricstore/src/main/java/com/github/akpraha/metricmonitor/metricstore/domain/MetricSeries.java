package com.github.akpraha.metricmonitor.metricstore.domain;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/**
 * @author Andy Key
 * @created 10/20/2024, Sun
 */
@Data
public class MetricSeries {
    private UUID id;
    private MetricDefinition metricDefinition;
    private Map<String, String> dimensions;
    private List<MetricValue> timeSeriesData;
}
