package com.github.akpraha.metricmonitor.metricstore.api.dto;

import java.util.UUID;

/**
 * @author Andy Key
 * @created 12/20/2024, Fri
 */
public record MetricSeriesDataTuple(UUID uuid, long[] values) {

}
