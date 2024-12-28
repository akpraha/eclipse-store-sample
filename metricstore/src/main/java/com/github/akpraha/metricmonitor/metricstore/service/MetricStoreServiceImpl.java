package com.github.akpraha.metricmonitor.metricstore.service;

import com.github.akpraha.metricmonitor.metricstore.domain.MetricSeriesNode;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricDefinition;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricSeries;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricValue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author Andy Key
 * @created 12/8/2024, Sun
 */
@Component
public class MetricStoreServiceImpl implements MetricStoreService {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Storage storage;

    public MetricStoreServiceImpl() {
        storage = new Storage(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public MetricDefinition createMetricDefinition(String name, List<String> dimensions) {
        readWriteLock.readLock().lock();
        try {
            MetricDefinition md = storage.findMetricDefinition(name);
            if (md != null) {
                throw new EntityExistsException("MetricDefinition " + name + " already exists");
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
        readWriteLock.writeLock().lock();
        try {
            MetricDefinition metricDefinition = MetricDefinition.builder()
                    .name(name).dimensions(new ArrayList<>(dimensions))
                    .build();

            storage.getMetricDefinitionMap().put(name, metricDefinition);
            return metricDefinition;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public Collection<MetricSeries> search(String metricName, Map<String, String> dimensions) {
        readWriteLock.readLock().lock();
        try {
            MetricDefinition metricDefinition = storage.findMetricDefinition(metricName);
            MetricSeriesNode node = storage.getMetricSeriesMap().get(metricDefinition);

            return findMetricSeries(metricDefinition.getDimensions(), dimensions, node);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public UUID createMetricSeries(String name, Map<String, String> dimensions) {
        readWriteLock.writeLock().lock();
        try {
            MetricDefinition metricDefinition = storage.findMetricDefinition(name);
            MetricSeries metricSeries = new MetricSeries();
            metricSeries.setId(UUID.randomUUID());
            metricSeries.setMetricDefinition(metricDefinition);
            metricSeries.setDimensions(dimensions);
            metricSeries.setTimeSeriesData(new ArrayList<>());

            storage.getMetricSeriesUUIDMap().put(metricSeries.getId(), metricSeries);

            MetricSeriesNode node = storage.getMetricSeriesMap().get(metricDefinition);
            if (node == null) {
                node = MetricSeriesNode.builder().children(new HashMap<>()).build();
                storage.getMetricSeriesMap().put(metricDefinition, node);
            }

            for (String dimensionName: metricDefinition.getDimensions()) {
                MetricSeriesNode child = node.getChildren().get(dimensionName);
                if (child == null) {
                    child = MetricSeriesNode.builder().children(new HashMap<>()).build();
                    node.getChildren().put(dimensionName, child);
                }
                node = child;
            }
            node.setMetricSeriesID(metricSeries.getId());

            return metricSeries.getId();
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<MetricDefinition> getMetricDefinitions(List<String> names) {
        readWriteLock.readLock().lock();
        try {
            return names.stream()
                    .map(n -> storage.getMetricDefinitionMap().get(n))
                    .filter(Objects::nonNull)
                    .map(md -> md.toBuilder().build())
                    .collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Collection<MetricDefinition> getAllMetricDefinitions() {
        readWriteLock.readLock().lock();
        try {
            return storage.getMetricDefinitionMap().values().stream()
                    .map(metricDefinition -> metricDefinition.toBuilder().build())
                    .collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public void storeMetricData(UUID uuid, List<MetricValue> data) {
        readWriteLock.writeLock().lock();
        try {
            MetricSeries metricSeries = storage.getMetricSeriesUUIDMap().get(uuid);
            metricSeries.getTimeSeriesData().addAll(data);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public List<MetricValue> readMetricData(UUID uuid, Instant startTime, long durationMs) {
        readWriteLock.readLock().lock();
        try {
            Instant endTime = startTime.plusMillis(durationMs);
            MetricSeries metricSeries = storage.getMetricSeriesUUIDMap().get(uuid);
            return metricSeries.getTimeSeriesData().stream().filter(mv -> inRange(mv, startTime, endTime))
                    .collect(Collectors.toList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    private boolean inRange(MetricValue metricValue, Instant start, Instant end) {
        return start.equals(metricValue.timestamp()) || end.equals(metricValue.timestamp()) ||
                (start.isBefore(metricValue.timestamp()) && end.isAfter(metricValue.timestamp()));
    }

    private Collection<MetricSeries> findMetricSeries(final List<String> dimensionNames, final Map<String, String> dimensions, MetricSeriesNode node) {
        List<MetricSeries> list = new ArrayList<>();
        if (node == null) {
            return list;
        }
        String value = dimensions.get(dimensionNames.get(0));
        if (node.getMetricSeriesID() != null) {
            list.add(storage.getMetricSeriesUUIDMap().get(node.getMetricSeriesID()));
        } else {
            List<MetricSeriesNode> nodes = new ArrayList<>();
            if ("*".equals(value)) {
                nodes.addAll(node.getChildren().values());
            } else {
                nodes.add(node.getChildren().get(value));
            }
            List<String> sublist = dimensionNames.subList(1, dimensionNames.size());
            for (MetricSeriesNode n: nodes) {
                list.addAll(findMetricSeries(sublist, dimensions, n));
            }
        }

        return list;
    }
}
