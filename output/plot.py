import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("output/results_all_versions.csv")
df_grouped = df.groupby(["Matrix Size", "Version"]).mean(numeric_only=True).reset_index()

# === 1. Gráfico de tiempo de ejecución ===
plt.figure(figsize=(10, 6))
for version in df_grouped["Version"].unique():
    subset = df_grouped[df_grouped["Version"] == version]
    plt.plot(subset["Matrix Size"], subset["Execution Time (ms)"], label=version, marker='o')
plt.title("Average Execution Time")
plt.xlabel("Matrix Size")
plt.ylabel("Time (ms)")
plt.legend()
plt.tight_layout()
plt.savefig("output/execution_time.png")
plt.close()

# === 2. Gráfico de memoria y uso de CPU ===
plt.figure(figsize=(10, 6))
plt.plot(df_grouped["Matrix Size"], df_grouped["Memory Used (MB)"], label="Memory Used (MB)", marker='o')
plt.plot(df_grouped["Matrix Size"], df_grouped["CPU Usage (%)"], label="CPU Usage (%)", marker='x')
plt.title("Average Memory and CPU Usage")
plt.xlabel("Matrix Size")
plt.ylabel("Resource Usage")
plt.legend()
plt.tight_layout()
plt.savefig("output/memory_cpu_usage.png")
plt.close()

# === 3. Gráfico de tiempo de transferencia (solo distributed) ===
distributed = df_grouped[df_grouped["Version"] == "distributed"]
plt.figure(figsize=(10, 6))
plt.plot(distributed["Matrix Size"], distributed["Transfer Time (ms)"], label="Transfer Time", marker='s')
plt.title("Average Transfer Time (Distributed Only)")
plt.xlabel("Matrix Size")
plt.ylabel("Transfer Time (ms)")
plt.legend()
plt.tight_layout()
plt.savefig("output/transfer_time.png")
plt.close()
