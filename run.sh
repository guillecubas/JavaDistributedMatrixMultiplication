#!/bin/bash

# Config
JAR_NAME="hazelcast-matrix-multiplication-1.0-SNAPSHOT.jar"
OUTPUT_DIR="./output"
JAR_PATH="./target/$JAR_NAME"

# 1. Compile the project
echo "🔧 Building project with Maven..."
mvn clean install -DskipTests

# Check build success
if [[ ! -f "$JAR_PATH" ]]; then
  echo "❌ JAR not found at $JAR_PATH. Build failed."
  exit 1
fi

# 2. Create output directory if missing
mkdir -p "$OUTPUT_DIR"

# 3. Run the benchmark
echo "🚀 Running benchmark locally..."
java -jar "$JAR_PATH"

# 4. Show results
if [[ -f "$OUTPUT_DIR/results_distributed.csv" ]]; then
  echo "📄 Results:"
  cat "$OUTPUT_DIR/results_distributed.csv"
else
  echo "❌ No results found."
fi
