package com.github.akpraha.metricmonitor.metricstore.api.dto;

import java.util.Map;
import java.util.UUID;
import lombok.Data;

/**
 * @author Andy Key
 * @created 10/20/2024, Sun
 */
@Data
public class MetricSeriesDTO {
    private UUID id;
    private String metricName;
    private Map<String, String> dimensions;
}
