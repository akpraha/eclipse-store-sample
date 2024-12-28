package com.github.akpraha.metricmonitor.metricstore.service;

import com.github.akpraha.metricmonitor.metricstore.api.dto.MetricSeriesDataTuple;
import java.util.List;

/**
 * @author Andy Key
 * @created 12/28/2024, Sat
 */
public interface MetricIngestor {

    void ingest(List<MetricSeriesDataTuple> data);
}
