package com.github.akpraha.metricmonitor.metricstore.api.controllers;

import com.github.akpraha.metricmonitor.metricstore.api.dto.MetricSeriesDTO;
import com.github.akpraha.metricmonitor.metricstore.api.dto.MetricSeriesDataTuple;
import com.github.akpraha.metricmonitor.metricstore.api.dto.MetricSeriesSearchRequest;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricDefinition;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricSeries;
import com.github.akpraha.metricmonitor.metricstore.domain.MetricValue;
import com.github.akpraha.metricmonitor.metricstore.service.EntityExistsException;
import com.github.akpraha.metricmonitor.metricstore.service.MetricIngestor;
import com.github.akpraha.metricmonitor.metricstore.service.MetricStoreService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Andy Key
 * @created 10/20/2024, Sun
 */
@RestController
@RequestMapping("/api/v1")
public class MetricSeriesController {
    private final MetricStoreService metricStoreService;
    private final MetricIngestor metricIngestor;

    public MetricSeriesController(MetricStoreService metricStoreService, MetricIngestor metricIngestor) {
        this.metricStoreService = metricStoreService;
        this.metricIngestor = metricIngestor;
    }

    @GetMapping(path = "/series")
    public ResponseEntity<List<UUID>> findMetricSeries(@RequestParam Map<String, String> filters) {
        String name = filters.get("metric");
        filters.remove("metric");
        Collection<MetricSeries> collection = metricStoreService.search(name, filters);
        List<UUID> list = collection.stream().map(ms -> ms.getId()).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping(path = "/series")
    public ResponseEntity<MetricSeriesDTO> createMetricSeries(@RequestBody MetricSeriesSearchRequest request) {
        UUID metricSeriesId = metricStoreService.createMetricSeries(request.name(), request.dimensions());
        MetricSeriesDTO resp = new MetricSeriesDTO();
        resp.setMetricName(request.name());
        resp.setDimensions(request.dimensions());
        resp.setId(metricSeriesId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping(path = "/definition")
    public ResponseEntity<Collection<MetricDefinition>> listMetricDefinitions() {
        Collection<MetricDefinition> list = metricStoreService.getAllMetricDefinitions();
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/definition/count")
    public ResponseEntity<Integer> definitionCount() {
        Collection<MetricDefinition> list = metricStoreService.getAllMetricDefinitions();
        return ResponseEntity.ok(list.size());
    }

    @PostMapping(path = "/definition")
    public ResponseEntity<MetricDefinition> createMetricDefinition(@RequestBody MetricDefinition metricDefinition) {
        try {
            MetricDefinition result = metricStoreService.createMetricDefinition(metricDefinition.getName(),
                    metricDefinition.getDimensions());
            return ResponseEntity.ok(result);
        } catch (EntityExistsException ex) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Already exists");
        }
    }

    @PostMapping(path = "/series/data")
    public ResponseEntity<Void> storeMetricSeriesData(@RequestBody List<MetricSeriesDataTuple> request) {
        metricIngestor.ingest(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/series/{uuid}/data")
    public ResponseEntity<List<MetricValue>> getMetricSeriesData(@PathVariable UUID uuid, @RequestParam(required = false) Long startMs, @RequestParam(required = false) Long endMs) {
        long duration = 600000L;
        Instant startInstant = startMs == null ? Instant.now().minus(600, ChronoUnit.SECONDS) : Instant.ofEpochMilli(startMs);
        if (startMs != null) {
            startInstant = Instant.ofEpochMilli(startMs);
            if (endMs != null) {
                duration = endMs - startMs;
            }
        }
        List<MetricValue> data = metricStoreService.readMetricData(uuid, startInstant, duration);
        return ResponseEntity.ok(data);
    }
}
