package com.github.akpraha.metricmonitor.metricstore.api.dto;

import java.util.Map;

/**
 * @author Andy Key
 * @created 12/8/2024, Sun
 */
public record MetricSeriesSearchRequest(String name, Map<String, String> dimensions) {
}
