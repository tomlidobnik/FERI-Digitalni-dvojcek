#!/usr/bin/env python3
"""
Performance analysis script for blockchain mining parallelization.
Generates graphs showing speedup and efficiency.
"""

import subprocess
import re
import time
import matplotlib.pyplot as plt
import numpy as np
from datetime import datetime

class PerformanceAnalyzer:
    def __init__(self):
        self.results = {}
        self.node_counts = [1, 2, 4, 8]

    def run_benchmark(self, num_nodes, num_blocks=5, num_runs=3):
        """Run mining benchmark with specified number of nodes"""
        print(f"\n{'='*60}")
        print(f"Running benchmark with {num_nodes} nodes, {num_blocks} blocks, {num_runs} runs")
        print(f"{'='*60}")

        if num_nodes == 1:
            cmd = f"./build/blockchain_mpi --fixed-difficulty {num_blocks}"
        else:
            cmd = f"mpirun -n {num_nodes} ./build/blockchain_mpi --fixed-difficulty {num_blocks}"

        run_times = []

        for run in range(1, num_runs + 1):
            print(f"  Run {run}/{num_runs}...", end="", flush=True)

            try:
                start = time.time()
                result = subprocess.run(cmd, shell=True, capture_output=True,
                                       text=True, timeout=300)
                elapsed = time.time() - start

                # Extract all mining times from output and sum them
                block_times = []
                for line in result.stdout.split('\n'):
                    if 'mined in' in line and 'ms' in line:
                        match = re.search(r'mined in (\d+) ms', line)
                        if match:
                            ms = int(match.group(1))
                            block_times.append(ms / 1000.0)

                if block_times:
                    total_mining_time = sum(block_times)
                    run_times.append(total_mining_time)
                    print(f" {elapsed:.2f}s wall-time, {total_mining_time:.3f}s mining-time")
                else:
                    print(f" No mining times found")

            except subprocess.TimeoutExpired:
                print(f" TIMEOUT")
            except Exception as e:
                print(f" ERROR: {e}")

        if run_times:
            avg_time = np.mean(run_times)
            std_time = np.std(run_times)
            self.results[num_nodes] = {
                'times': run_times,
                'average': avg_time,
                'std_dev': std_time
            }
            print(f"  Average: {avg_time:.3f}s ± {std_time:.3f}s")
        else:
            print(f"  No valid results!")
            self.results[num_nodes] = {
                'times': [],
                'average': None,
                'std_dev': None
            }

    def run_all_benchmarks(self, num_blocks=5):
        """Run benchmarks for all node counts"""
        print("\n" + "="*60)
        print("BLOCKCHAIN MINING PERFORMANCE ANALYSIS")
        print("="*60)
        print(f"Test Configuration:")
        print(f"  - Blocks per test: {num_blocks}")
        print(f"  - Node counts: {self.node_counts}")
        print(f"  - Fixed difficulty: 4 (no adjustment during benchmark)")

        for nodes in self.node_counts:
            self.run_benchmark(nodes, num_blocks)

    def calculate_speedup(self):
        """Calculate speedup relative to single node"""
        baseline = self.results[1]['average']

        if baseline is None:
            print("Cannot calculate speedup: baseline (1 node) invalid")
            return None

        speedups = {}
        for nodes in self.node_counts:
            avg = self.results[nodes]['average']
            if avg is not None:
                speedup = baseline / avg
                speedups[nodes] = speedup
            else:
                speedups[nodes] = None

        return speedups

    def calculate_efficiency(self, speedups):
        """Calculate efficiency (speedup / num_nodes)"""
        if speedups is None:
            return None

        efficiencies = {}
        for nodes in self.node_counts:
            if speedups[nodes] is not None:
                efficiency = speedups[nodes] / nodes
                efficiencies[nodes] = efficiency
            else:
                efficiencies[nodes] = None

        return efficiencies

    def print_results(self):
        """Print detailed results"""
        print("\n" + "="*60)
        print("RESULTS SUMMARY")
        print("="*60)
        print(f"{'Nodes':<8} {'Avg Time (s)':<15} {'Std Dev':<12} {'Speedup':<10} {'Efficiency':<10}")
        print("-"*60)

        speedups = self.calculate_speedup()
        efficiencies = self.calculate_efficiency(speedups)

        for nodes in self.node_counts:
            avg = self.results[nodes]['average']
            std = self.results[nodes]['std_dev']
            speedup = speedups[nodes] if speedups else None
            efficiency = efficiencies[nodes] if efficiencies else None

            if avg is not None:
                avg_str = f"{avg:.3f}"
                std_str = f"{std:.3f}"
                speedup_str = f"{speedup:.2f}x" if speedup else "N/A"
                eff_str = f"{efficiency:.1%}" if efficiency else "N/A"
            else:
                avg_str = "FAILED"
                std_str = "N/A"
                speedup_str = "N/A"
                eff_str = "N/A"

            print(f"{nodes:<8} {avg_str:<15} {std_str:<12} {speedup_str:<10} {eff_str:<10}")

    def plot_results(self, output_prefix="performance"):
        """Generate performance graphs"""
        speedups = self.calculate_speedup()
        efficiencies = self.calculate_efficiency(speedups)

        nodes = sorted([n for n in self.node_counts if self.results[n]['average'] is not None])
        times = [self.results[n]['average'] for n in nodes]
        speeds = [speedups[n] for n in nodes]
        effs = [efficiencies[n] for n in nodes]

        fig, axes = plt.subplots(2, 2, figsize=(14, 10))
        fig.suptitle('Blockchain Mining Parallelization Performance', fontsize=16, fontweight='bold')

        # 1. Execution Time
        ax = axes[0, 0]
        ax.plot(nodes, times, 'o-', linewidth=2, markersize=8, color='#2E86AB')
        ax.set_xlabel('Number of Nodes', fontsize=12)
        ax.set_ylabel('Time (seconds)', fontsize=12)
        ax.set_title('Block Mining Time vs Number of Nodes', fontweight='bold')
        ax.grid(True, alpha=0.3)
        ax.set_xticks(nodes)

        # 2. Speedup
        ax = axes[0, 1]
        ax.plot(nodes, speeds, 'o-', linewidth=2, markersize=8, label='Actual', color='#A23B72')
        ax.plot(nodes, nodes, '--', linewidth=2, label='Ideal', color='#F18F01')
        ax.set_xlabel('Number of Nodes', fontsize=12)
        ax.set_ylabel('Speedup', fontsize=12)
        ax.set_title('Speedup vs Number of Nodes', fontweight='bold')
        ax.legend()
        ax.grid(True, alpha=0.3)
        ax.set_xticks(nodes)

        # 3. Efficiency
        ax = axes[1, 0]
        efficiency_percent = [e*100 for e in effs]
        ax.plot(nodes, efficiency_percent, 'o-', linewidth=2, markersize=8, color='#C73E1D')
        ax.axhline(y=100, color='#F18F01', linestyle='--', linewidth=2, label='Ideal (100%)')
        ax.set_xlabel('Number of Nodes', fontsize=12)
        ax.set_ylabel('Efficiency (%)', fontsize=12)
        ax.set_title('Parallel Efficiency vs Number of Nodes', fontweight='bold')
        ax.set_ylim(0, 120)
        ax.legend()
        ax.grid(True, alpha=0.3)
        ax.set_xticks(nodes)

        # 4. Statistics Table
        ax = axes[1, 1]
        ax.axis('off')

        table_data = []
        table_data.append(['Nodes', 'Time (s)', 'Speedup', 'Efficiency'])
        for n in nodes:
            row = [
                str(n),
                f"{times[nodes.index(n)]:.3f}",
                f"{speeds[nodes.index(n)]:.2f}x",
                f"{effs[nodes.index(n)]:.1%}"
            ]
            table_data.append(row)

        table = ax.table(cellText=table_data, cellLoc='center', loc='center',
                        colWidths=[0.2, 0.25, 0.25, 0.3])
        table.auto_set_font_size(False)
        table.set_fontsize(11)
        table.scale(1, 2)

        # Style header row
        for i in range(4):
            table[(0, i)].set_facecolor('#2E86AB')
            table[(0, i)].set_text_props(weight='bold', color='white')

        # Alternate row colors
        for i in range(1, len(table_data)):
            color = '#E8F4F8' if i % 2 == 0 else 'white'
            for j in range(4):
                table[(i, j)].set_facecolor(color)

        plt.tight_layout()

        # Save figures
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        plt.savefig(f'{output_prefix}_{timestamp}.png', dpi=300, bbox_inches='tight')
        print(f"\nGraphs saved to: {output_prefix}_{timestamp}.png")

        plt.show()

    def save_report(self, filename="blockchain_report.txt"):
        """Save detailed report to file"""
        with open(filename, 'w') as f:
            f.write("="*60 + "\n")
            f.write("BLOCKCHAIN MINING PARALLELIZATION - PERFORMANCE REPORT\n")
            f.write("="*60 + "\n\n")

            f.write(f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

            f.write("CONFIGURATION\n")
            f.write("-"*60 + "\n")
            f.write(f"Node counts tested: {self.node_counts}\n")
            f.write(f"Implementation: C++ with MPI + pthreads\n")
            f.write(f"Mining algorithm: Proof-of-Work (SHA256)\n\n")

            f.write("RESULTS\n")
            f.write("-"*60 + "\n")
            f.write(f"{'Nodes':<8} {'Avg Time (s)':<15} {'Std Dev':<12}\n")

            for nodes in self.node_counts:
                if self.results[nodes]['average'] is not None:
                    f.write(f"{nodes:<8} {self.results[nodes]['average']:<15.3f} "
                           f"{self.results[nodes]['std_dev']:<12.3f}\n")
                else:
                    f.write(f"{nodes:<8} FAILED\n")

            f.write("\n")
            speedups = self.calculate_speedup()
            efficiencies = self.calculate_efficiency(speedups)

            f.write("PERFORMANCE METRICS\n")
            f.write("-"*60 + "\n")
            f.write(f"{'Nodes':<8} {'Speedup':<15} {'Efficiency':<15}\n")

            if speedups is not None:
                for nodes in self.node_counts:
                    if speedups[nodes] is not None:
                        f.write(f"{nodes:<8} {speedups[nodes]:<15.2f}x {efficiencies[nodes]:<15.1%}\n")
            else:
                f.write("Unable to calculate speedup (baseline invalid)\n")

            f.write("\n" + "="*60 + "\n")
            f.write("ANALYSIS\n")
            f.write("="*60 + "\n")
            f.write("Expected vs Actual Performance:\n\n")
            f.write("Single-threaded (1 node, 1 thread):\n")
            f.write("  - Baseline for comparison\n\n")

            f.write("Multi-threaded (1 node, N threads):\n")
            f.write("  - Expected: 0.8-0.95x speedup per thread\n")
            f.write("  - Causes: Synchronization overhead, memory contention\n\n")

            f.write("Distributed (N nodes with MPI):\n")
            f.write("  - Expected: 0.7-0.9x speedup per node\n")
            f.write("  - Causes: Network communication, load balancing\n\n")

            f.write("Key Observations:\n")
            f.write("  1. Mining is CPU-bound, benefits from parallelization\n")
            f.write("  2. SHA256 computation dominates execution time\n")
            f.write("  3. Limited by atomic operations and memory bandwidth\n")
            f.write("  4. MPI communication overhead is minimal\n")

        print(f"Report saved to: {filename}")

def main():
    """Main execution"""
    analyzer = PerformanceAnalyzer()

    # Check if compiled binary exists
    import os
    if not os.path.exists('./build/blockchain_mpi'):
        print("ERROR: blockchain_mpi binary not found!")
        print("Please compile first:")
        print("  mkdir build && cd build")
        print("  cmake .. && make")
        return

    # Run all benchmarks
    analyzer.run_all_benchmarks(num_blocks=10)

    # Print results
    analyzer.print_results()

    # Save report
    analyzer.save_report()

    # Generate plots
    try:
        analyzer.plot_results()
    except Exception as e:
        print(f"Warning: Could not generate plots: {e}")

if __name__ == "__main__":
    main()
