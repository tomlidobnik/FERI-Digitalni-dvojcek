#!/usr/bin/env python3
"""
Comprehensive performance analysis for blockchain mining.
Tests varying numbers of threads and MPI nodes.
"""

import subprocess
import re
import time
import matplotlib.pyplot as plt
import numpy as np
from datetime import datetime

class ComprehensiveAnalyzer:
    def __init__(self):
        self.results = {}  # results[nodes][threads] = {...}
        self.node_counts = [1, 2, 4, 8]
        self.thread_counts = [1, 2, 4, 8]

    def run_benchmark(self, num_nodes, num_threads, num_blocks=10, num_runs=3):
        """Run mining benchmark with specified nodes and threads"""
        key = (num_nodes, num_threads)

        if num_nodes == 1:
            cmd = f"./build/blockchain_mpi --fixed-difficulty --threads {num_threads} {num_blocks}"
        else:
            cmd = f"mpirun -n {num_nodes} ./build/blockchain_mpi --fixed-difficulty --threads {num_threads} {num_blocks}"

        run_times = []

        for run in range(1, num_runs + 1):
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

            except subprocess.TimeoutExpired:
                pass
            except Exception as e:
                pass

        if run_times:
            avg_time = np.mean(run_times)
            std_time = np.std(run_times)
            self.results[key] = {
                'times': run_times,
                'average': avg_time,
                'std_dev': std_time,
                'nodes': num_nodes,
                'threads': num_threads
            }
        else:
            self.results[key] = {
                'times': [],
                'average': None,
                'std_dev': None,
                'nodes': num_nodes,
                'threads': num_threads
            }

    def run_all_benchmarks(self, num_blocks=10):
        """Run benchmarks for all combinations"""
        print("\n" + "="*80)
        print("COMPREHENSIVE BLOCKCHAIN MINING PERFORMANCE ANALYSIS")
        print("="*80)
        print(f"Test Configuration:")
        print(f"  - Blocks per test: {num_blocks}")
        print(f"  - Node counts: {self.node_counts}")
        print(f"  - Thread counts per node: {self.thread_counts}")
        print(f"  - Fixed difficulty: 4")
        print(f"  - Total tests: {len(self.node_counts) * len(self.thread_counts)}")

        total_tests = len(self.node_counts) * len(self.thread_counts)
        current_test = 0

        for nodes in self.node_counts:
            for threads in self.thread_counts:
                current_test += 1
                print(f"\n[{current_test}/{total_tests}] Testing {nodes} nodes × {threads} threads/node...", end="", flush=True)
                self.run_benchmark(nodes, threads, num_blocks)

                key = (nodes, threads)
                if self.results[key]['average'] is not None:
                    print(f" {self.results[key]['average']:.3f}s ± {self.results[key]['std_dev']:.3f}s")
                else:
                    print(f" FAILED")

    def plot_results(self):
        """Generate comprehensive performance graphs"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")

        fig, axes = plt.subplots(2, 2, figsize=(16, 12))
        fig.suptitle('Blockchain Mining Performance Analysis', fontsize=16, fontweight='bold')

        # Graph 1: Performance vs Threads (for each node count)
        ax1 = axes[0, 0]
        for nodes in self.node_counts:
            times = []
            threads_list = []
            for threads in self.thread_counts:
                key = (nodes, threads)
                if key in self.results and self.results[key]['average'] is not None:
                    times.append(self.results[key]['average'])
                    threads_list.append(threads)

            if times:
                ax1.plot(threads_list, times, marker='o', linewidth=2, markersize=8,
                        label=f'{nodes} node{"s" if nodes > 1 else ""}')

        ax1.set_xlabel('Threads per Node', fontsize=12)
        ax1.set_ylabel('Total Mining Time (s)', fontsize=12)
        ax1.set_title('Mining Time vs Thread Count', fontsize=14, fontweight='bold')
        ax1.legend()
        ax1.grid(True, alpha=0.3)
        ax1.set_xscale('log', base=2)

        # Graph 2: Performance vs Nodes (for each thread count)
        ax2 = axes[0, 1]
        for threads in self.thread_counts:
            times = []
            nodes_list = []
            for nodes in self.node_counts:
                key = (nodes, threads)
                if key in self.results and self.results[key]['average'] is not None:
                    times.append(self.results[key]['average'])
                    nodes_list.append(nodes)

            if times:
                ax2.plot(nodes_list, times, marker='s', linewidth=2, markersize=8,
                        label=f'{threads} thread{"s" if threads > 1 else ""}/node')

        ax2.set_xlabel('Number of Nodes', fontsize=12)
        ax2.set_ylabel('Total Mining Time (s)', fontsize=12)
        ax2.set_title('Mining Time vs Node Count', fontsize=14, fontweight='bold')
        ax2.legend()
        ax2.grid(True, alpha=0.3)
        ax2.set_xscale('log', base=2)

        # Graph 3: Speedup vs Total Cores
        ax3 = axes[1, 0]
        baseline_key = (1, 1)
        if baseline_key in self.results and self.results[baseline_key]['average'] is not None:
            baseline = self.results[baseline_key]['average']

            cores_list = []
            speedups = []
            ideal_speedups = []

            for nodes in self.node_counts:
                for threads in self.thread_counts:
                    key = (nodes, threads)
                    total_cores = nodes * threads

                    if key in self.results and self.results[key]['average'] is not None:
                        speedup = baseline / self.results[key]['average']
                        cores_list.append(total_cores)
                        speedups.append(speedup)
                        ideal_speedups.append(total_cores)

            # Sort by cores for cleaner plotting
            sorted_data = sorted(zip(cores_list, speedups, ideal_speedups))
            cores_list, speedups, ideal_speedups = zip(*sorted_data)

            ax3.plot(cores_list, speedups, marker='o', linewidth=2, markersize=8,
                    label='Actual Speedup', color='blue')
            ax3.plot(cores_list, ideal_speedups, linestyle='--', linewidth=2,
                    label='Ideal Speedup', color='red', alpha=0.7)

            ax3.set_xlabel('Total Cores (Nodes × Threads)', fontsize=12)
            ax3.set_ylabel('Speedup', fontsize=12)
            ax3.set_title('Speedup vs Total Cores', fontsize=14, fontweight='bold')
            ax3.legend()
            ax3.grid(True, alpha=0.3)
            ax3.set_xscale('log', base=2)
            ax3.set_yscale('log', base=2)

        # Graph 4: Efficiency Heatmap
        ax4 = axes[1, 1]
        baseline_key = (1, 1)
        if baseline_key in self.results and self.results[baseline_key]['average'] is not None:
            baseline = self.results[baseline_key]['average']

            efficiency_matrix = np.zeros((len(self.node_counts), len(self.thread_counts)))

            for i, nodes in enumerate(self.node_counts):
                for j, threads in enumerate(self.thread_counts):
                    key = (nodes, threads)
                    total_cores = nodes * threads

                    if key in self.results and self.results[key]['average'] is not None:
                        speedup = baseline / self.results[key]['average']
                        efficiency = (speedup / total_cores) * 100  # as percentage
                        efficiency_matrix[i, j] = efficiency

            im = ax4.imshow(efficiency_matrix, cmap='RdYlGn', aspect='auto', vmin=0, vmax=100)

            ax4.set_xticks(range(len(self.thread_counts)))
            ax4.set_yticks(range(len(self.node_counts)))
            ax4.set_xticklabels(self.thread_counts)
            ax4.set_yticklabels(self.node_counts)
            ax4.set_xlabel('Threads per Node', fontsize=12)
            ax4.set_ylabel('Number of Nodes', fontsize=12)
            ax4.set_title('Parallel Efficiency (%)', fontsize=14, fontweight='bold')

            # Add text annotations
            for i in range(len(self.node_counts)):
                for j in range(len(self.thread_counts)):
                    text = ax4.text(j, i, f'{efficiency_matrix[i, j]:.1f}',
                                   ha="center", va="center", color="black", fontsize=10)

            plt.colorbar(im, ax=ax4, label='Efficiency (%)')

        plt.tight_layout()
        filename = f'performance_comprehensive_{timestamp}.png'
        plt.savefig(filename, dpi=300, bbox_inches='tight')
        print(f"\nGraphs saved to: {filename}")

        return filename

    def print_summary(self):
        """Print summary table"""
        print("\n" + "="*80)
        print("RESULTS SUMMARY")
        print("="*80)
        print(f"{'Nodes':<8}{'Threads':<10}{'Total':<10}{'Avg Time (s)':<15}{'Speedup':<12}{'Efficiency'}")
        print("-"*80)

        baseline_key = (1, 1)
        baseline = None
        if baseline_key in self.results and self.results[baseline_key]['average'] is not None:
            baseline = self.results[baseline_key]['average']

        for nodes in self.node_counts:
            for threads in self.thread_counts:
                key = (nodes, threads)
                if key in self.results:
                    result = self.results[key]
                    total_cores = nodes * threads

                    avg_str = f"{result['average']:.3f}" if result['average'] is not None else "FAIL"

                    if baseline and result['average'] is not None:
                        speedup = baseline / result['average']
                        efficiency = (speedup / total_cores) * 100
                        speedup_str = f"{speedup:.2f}x"
                        efficiency_str = f"{efficiency:.1f}%"
                    else:
                        speedup_str = "-"
                        efficiency_str = "-"

                    print(f"{nodes:<8}{threads:<10}{total_cores:<10}{avg_str:<15}{speedup_str:<12}{efficiency_str}")

def main():
    print("\nComprehensive Blockchain Mining Performance Benchmark")
    print("This will test various combinations of nodes and threads\n")

    analyzer = ComprehensiveAnalyzer()

    # Check if compiled binary exists
    import os
    if not os.path.exists('./build/blockchain_mpi'):
        print("ERROR: blockchain_mpi binary not found!")
        print("Please compile first: just build")
        return

    # Run all benchmarks
    analyzer.run_all_benchmarks(num_blocks=10)

    # Print results
    analyzer.print_summary()

    # Generate plots
    try:
        analyzer.plot_results()
    except Exception as e:
        print(f"Warning: Could not generate plots: {e}")

if __name__ == "__main__":
    main()
