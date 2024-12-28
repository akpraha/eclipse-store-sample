package com.github.akpraha.metricmonitor.metricstore.domain;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * @author Andy Key
 * @created 12/8/2024, Sun
 */
@Data
@Builder
public class MetricSeriesNode {
    private Map<String, MetricSeriesNode> children;
    private UUID metricSeriesID;
}
