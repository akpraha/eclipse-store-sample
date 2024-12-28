package com.github.akpraha.metricmonitor.metricstore.domain;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Andy Key
 * @created 10/20/2024, Sun
 */
@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class MetricDefinition {
    private final String name;
    private final List<String> dimensions;
}
