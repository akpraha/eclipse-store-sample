package com.github.akpraha.metricmonitor.metricstore.service;

import com.github.akpraha.metricmonitor.metricstore.domain.MetricDefinition;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricSeries;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricSeriesNode;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Andy Key
 * @created 12/8/2024, Sun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Storage {
    private Map<String, MetricDefinition> metricDefinitionMap;
    private Map<MetricDefinition, MetricSeriesNode> metricSeriesMap;
    private Map<UUID, MetricSeries> metricSeriesUUIDMap;

    public MetricDefinition findMetricDefinition(String name) {
        return metricDefinitionMap.get(name);
    }

    public void storeMetricDefinition(String name, MetricDefinition metricDefinition) {
        metricDefinitionMap.put(name, metricDefinition);
    }

    public MetricSeriesNode findMetricSeriesNode(MetricDefinition metricDefinition) {
        return metricSeriesMap.get(metricDefinition);
    }

}
