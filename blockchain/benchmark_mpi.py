#!/usr/bin/env python3
"""
Benchmark script for testing blockchain mining performance with different MPI node counts.
Runs mining with 1 to 8 nodes and plots the results.
"""

import subprocess
import re
import matplotlib.pyplot as plt
import numpy as np
from datetime import datetime
import json

def run_blockchain_with_nodes(num_nodes, num_blocks=5):
    """
    Run blockchain mining with specified number of MPI nodes.
    
    Args:
        num_nodes: Number of MPI nodes to use
        num_blocks: Number of blocks to mine (default 5 for faster testing)
    
    Returns:
        dict: Results containing timing information
    """
    print(f"\n{'='*60}")
    print(f"Running with {num_nodes} node(s)...")
    print(f"{'='*60}")
    
    try:
        subprocess.run(['just', 'build'], check=True, capture_output=True)
        
        if num_nodes == 1:
            cmd = ['./build/blockchain_mpi', '--fixed-difficulty', str(num_blocks)] # nastavimo fiksno težavnost
        else:
            cmd = ['mpirun', '-n', str(num_nodes), './build/blockchain_mpi', '--fixed-difficulty', str(num_blocks)]
        
        import time
        wall_start = time.time()
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=300
        )
        
        wall_end = time.time()
        wall_clock_time = (wall_end - wall_start) * 1000
        
        output = result.stdout + result.stderr
        print(output)
        
        times = []
        pattern = r'Block mined in (\d+) ms'
        matches = re.findall(pattern, output)
        
        for match in matches:
            times.append(int(match))
        
        if not times:
            print(f"Warning: No timing data found for {num_nodes} nodes")
            return None
        
        nodes_info = {
            'num_nodes': num_nodes,
            'block_times': times,
            'avg_time': sum(times) / len(times),
            'min_time': min(times),
            'max_time': max(times),
            'blocks_mined': len(times),
        }
        
        thread_pattern = r'(\w+) threads(?: per node)?:\s*(\d+)'
        thread_matches = re.findall(thread_pattern, output)
        if thread_matches:
            nodes_info['thread_info'] = thread_matches
        
        return nodes_info
        
    except subprocess.TimeoutExpired:
        print(f"Timeout: Mining with {num_nodes} nodes took too long")
        return None
    except subprocess.CalledProcessError as e:
        print(f"Error running with {num_nodes} nodes: {e}")
        return None
    except Exception as e:
        print(f"Unexpected error with {num_nodes} nodes: {e}")
        return None

def plot_results(results):
    """
    Create comprehensive plots of the benchmark results.
    
    Args:
        results: List of result dictionaries
    """
    results = [r for r in results if r is not None]
    
    if not results:
        print("No valid results to plot")
        return
    
    node_counts = [r['num_nodes'] for r in results]
    avg_times = [r['avg_time'] for r in results]
    min_times = [r['min_time'] for r in results]
    max_times = [r['max_time'] for r in results]
    E
    baseline_time = avg_times[0] if node_counts[0] == 1 else avg_times[0]
    speedups = [baseline_time / t for t in avg_times]
    
    efficiencies = [s / n * 100 for s, n in zip(speedups, node_counts)]
    
    fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(15, 12))
    fig.suptitle('Blockchain Mining Performance Analysis (MPI + OpenMP)', fontsize=16, fontweight='bold')
    
    ax1.plot(node_counts, avg_times, 'o-', linewidth=2, markersize=8, color='#2E86AB')
    ax1.fill_between(node_counts, min_times, max_times, alpha=0.3, color='#2E86AB')
    ax1.set_xlabel('Number of MPI Nodes', fontsize=12)
    ax1.set_ylabel('Time (ms)', fontsize=12)
    ax1.set_title('Average Block Mining Time', fontsize=14, fontweight='bold')
    ax1.grid(True, alpha=0.3)
    ax1.set_xticks(node_counts)
    
    for x, y in zip(node_counts, avg_times):
        ax1.annotate(f'{y:.0f}ms', (x, y), textcoords="offset points", 
                     xytext=(0,10), ha='center', fontsize=9)
    
    ax2.bar(node_counts, avg_times, color='#A23B72', alpha=0.7, edgecolor='black')
    ax2.set_xlabel('Number of MPI Nodes', fontsize=12)
    ax2.set_ylabel('Average Time per Block (ms)', fontsize=12)
    ax2.set_title('Average Mining Time per Block (per Node)', fontsize=14, fontweight='bold')
    ax2.grid(True, alpha=0.3, axis='y')
    ax2.set_xticks(node_counts)
    
    for x, y in zip(node_counts, avg_times):
        ax2.text(x, y, f'{y:.0f}ms', ha='center', va='bottom', fontsize=9)
    
    ax3.plot(node_counts, speedups, 'o-', linewidth=2, markersize=8, 
             color='#F18F01', label='Actual Speedup')
    ax3.plot(node_counts, node_counts, '--', linewidth=2, 
             color='gray', alpha=0.5, label='Linear Speedup (Ideal)')
    ax3.set_xlabel('Number of MPI Nodes', fontsize=12)
    ax3.set_ylabel('Speedup', fontsize=12)
    ax3.set_title('Speedup Relative to 1 Node', fontsize=14, fontweight='bold')
    ax3.grid(True, alpha=0.3)
    ax3.legend()
    ax3.set_xticks(node_counts)
    
    for x, y in zip(node_counts, speedups):
        ax3.annotate(f'{y:.2f}x', (x, y), textcoords="offset points", 
                     xytext=(0,10), ha='center', fontsize=9)
    
    ax4.plot(node_counts, efficiencies, 's-', linewidth=2, markersize=8, color='#06A77D')
    ax4.axhline(y=100, color='gray', linestyle='--', alpha=0.5, label='100% Efficiency')
    ax4.set_xlabel('Number of MPI Nodes', fontsize=12)
    ax4.set_ylabel('Efficiency (%)', fontsize=12)
    ax4.set_title('Parallel Efficiency', fontsize=14, fontweight='bold')
    ax4.grid(True, alpha=0.3)
    ax4.legend()
    ax4.set_xticks(node_counts)
    ax4.set_ylim([0, max(110, max(efficiencies) * 1.1)])
    
    for x, y in zip(node_counts, efficiencies):
        ax4.annotate(f'{y:.1f}%', (x, y), textcoords="offset points", 
                     xytext=(0,10), ha='center', fontsize=9)
    
    plt.tight_layout()
    
    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    filename = f'mpi_benchmark_{timestamp}.png'
    plt.savefig(filename, dpi=300, bbox_inches='tight')
    print(f"\n✓ Plot saved as: {filename}")
    
    plt.show()

def save_results_json(results, filename=None):
    """Save results to JSON file."""
    if filename is None:
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        filename = f'mpi_benchmark_{timestamp}.json'
    
    with open(filename, 'w') as f:
        json.dump(results, f, indent=2)
    
    print(f"✓ Results saved to: {filename}")

def print_summary(results):
    """Print summary table of results."""
    results = [r for r in results if r is not None]
    
    if not results:
        print("No results to summarize")
        return
    
    print("\n" + "="*80)
    print("BENCHMARK SUMMARY (Average Mining Time per Block)")
    print("="*80)
    print(f"{'Nodes':<8} {'Avg Time (ms)':<15} {'Speedup':<10} {'Efficiency':<12}")
    print("-"*80)
    
    baseline = results[0]['avg_time']
    for r in results:
        speedup = baseline / r['avg_time']
        efficiency = speedup / r['num_nodes'] * 100
        print(f"{r['num_nodes']:<8} {r['avg_time']:<15.1f} "
              f"{speedup:<10.2f} {efficiency:<12.1f}%")
    
    print("="*80)

def main():
    """Main benchmark execution."""
    print("="*80)
    print("MPI BLOCKCHAIN MINING BENCHMARK")
    print("="*80)
    print(f"Start time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("Testing with 1 to 8 MPI nodes...")
    print("="*80)
    
    num_blocks = 35 # stevilo blokov za rudarjenje
    
    results = []
    for num_nodes in range(1, 9):
        result = run_blockchain_with_nodes(num_nodes, num_blocks)
        if result:
            results.append(result)
    
    print_summary(results)
    
    save_results_json(results)
    
    if results:
        plot_results(results)
    else:
        print("No results to plot")
    
    print("\n✓ Benchmark complete!")

if __name__ == '__main__':
    main()
