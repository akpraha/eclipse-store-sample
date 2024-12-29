package com.github.akpraha.metricmonitor.metricstore.api.dto;

import java.util.UUID;
import lombok.Data;

/**
 * @author Andy Key
 * @created 12/20/2024, Fri
 */
@Data
public class MetricSeriesDataTuple {
    private UUID uuid;
    private long[] values;
}
