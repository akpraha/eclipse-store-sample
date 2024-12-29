# Eclipse Store Sample
## Description
A small sample application to demonstrate the usage of [EclipseStore](https://github.com/eclipse-store/store). The metricstore application provides a REST API to submit simple time-series metric data, 
and persist and query the data.  Basic terminology:
| Entity | Description |
|--------|-------------|
| MetricDefinition | A metric definition is used to describe what kind of data, plus dimensions that are used to differentiate it. Dimensions could be URLs, IP addresses, ports, server names, etc. |
| MetricSeries | A metric series references a metric definition and provides values for all of the dimensions defined. It is the root for the time-series data |
| MetricIngestor | A component that buffers incoming time-series data, and every 15 seconds flushes it to the EclipseStore persistent storage |

## Samples
### Create a new metric definition
```
curl --location 'http://localhost:8080/api/v1/definition' \
--header 'Content-Type: application/json' \
--data '{
    "name": "metric.one",
    "dimensions": [
        "dimension1",
        "dimension2"
    ]
}'
```
This creates a new metric defintion named "metric.one" with two dimensions.
### Create a new metric series
```
curl --location 'http://localhost:8080/api/v1/series' \
--header 'Content-Type: application/json' \
--data '{
    "name": "metric.one",
    "dimensions": {
        "dimension1": "192.168.56.1",
        "dimension2": "tomcat"
    }
}'
```
This creates a new metric series using the metric definition from the previous step.
### Store time-series data
```
curl --location 'http://localhost:8080/api/v1/series/data' \
--header 'Content-Type: application/json' \
--data '[
    {
        "uuid": "a19516f2-895c-4347-8635-44f2a276f863",
        "values": [100, 101, 102]
    }
]'
```
We reference the metric series with the UUID returned in the previous step.  The values array can hold 0..n values.  All of the values will be stored with a timestamp normalized to a 15-second boundary.
### Query time-series data
```
curl --location 'http://localhost:8080/api/v1/series/a19516f2-895c-4347-8635-44f2a276f863/data?startMs=1735470939000&endMs=1735474539000'
```
Note that the startMs and endMs parameters are optional. If omitted, a default value of the last 10 minutes is used.
