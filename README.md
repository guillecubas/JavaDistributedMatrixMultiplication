# Hazelcast Distributed Matrix Multiplication

This project benchmarks three matrix multiplication methods:
- Basic (single-threaded)
- Parallel (multithreaded)
- Distributed (Hazelcast cluster)

## Requirements

- Java 11+
- Maven
- Docker (for multi-node testing)

## Build

```bash
mvn clean package
```

## Run locally

```bash
java -jar target/hazelcast-matrix-multiplication-1.0-SNAPSHOT.jar
```

## Run in Docker

```bash
docker-compose up --build
```

## Output

Results are saved to:
```
output/results_all_versions.csv
```

Make sure the `output/` folder exists or is mounted in Docker.