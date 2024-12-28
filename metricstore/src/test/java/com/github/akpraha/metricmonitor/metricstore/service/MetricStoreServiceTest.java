package com.github.akpraha.metricmonitor.metricstore.service;

import com.github.akpraha.metricmonitor.metricstore.domain.MetricDefinition;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricValue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Map.entry;

/**
 * @author Andy Key
 * @created 12/21/2024, Sat
 */
public class MetricStoreServiceTest {
    public static final String METRIC_ONE = "metric.one";
    private MetricStoreService metricStoreService;

    @BeforeEach
    public void setup() {
        metricStoreService = new MetricStoreServiceImpl();
    }

    @Test
    public void testMetricDefinitions() {
        MetricDefinition md = createMetricDef();
        Collection<MetricDefinition> metricDefinitions = metricStoreService.getMetricDefinitions(Arrays.asList(METRIC_ONE, "metric.two"));
        Assertions.assertNotNull(metricDefinitions);
        Assertions.assertEquals(1, metricDefinitions.size());
        Assertions.assertEquals(md, metricDefinitions.iterator().next());
    }

    @Test
    public void testMetricSeries() {
        MetricDefinition md = createMetricDef();

        Map<String, String> dimensions = Map.ofEntries(
                entry("dim1", "192.168.0.1"),
                entry("dim2", "8080"),
                entry("dim3", "tomcat")
        );
        UUID ms1 = metricStoreService.createMetricSeries(METRIC_ONE, dimensions);


        Map<String, String> dimensions2 = Map.ofEntries(
                entry("dim1", "192.168.0.20"),
                entry("dim2", "8080"),
                entry("dim3", "tomcat")
        );
        UUID ms2 = metricStoreService.createMetricSeries(METRIC_ONE, dimensions2);

        metricStoreService.storeMetricData(ms1, genMetricData());
        List<MetricValue> metricValues = metricStoreService.readMetricData(ms1, Instant.now().minusSeconds(180), 180000);
        Assertions.assertNotNull(metricValues);
        Assertions.assertEquals(10, metricValues.size());

        metricValues = metricStoreService.readMetricData(ms2, Instant.now().minusSeconds(180), 180000);
        Assertions.assertNotNull(metricValues);
        Assertions.assertEquals(0, metricValues.size());
    }

    private List<MetricValue> genMetricData() {
        List<MetricValue> list = new ArrayList<>();
        Instant ts = Instant.now().minusSeconds(120);
        long n = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            list.add(new MetricValue(ts, n + i));
            ts = ts.plusMillis(150);
        }
        return list;
    }

    private MetricDefinition createMetricDef() {
        return metricStoreService.createMetricDefinition(METRIC_ONE, Arrays.asList("dim1", "dim2", "dim3"));
    }
}
