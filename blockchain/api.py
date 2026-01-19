from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
import subprocess
import re
from datetime import datetime
from typing import List, Dict
import json
import asyncio

app = FastAPI(title="Blockchain Mining API", version="1.0")

class BlockchainRunner:
    def __init__(self):
        self.executable = "./build/blockchain_mpi"

    def run_blockchain(self, num_processes: int = 1, num_blocks: int = 10) -> Dict:
        """Run blockchain and parse output"""
        try:
            if num_processes == 1:
                cmd = [self.executable, str(num_blocks)]
            else:
                cmd = ["mpirun", "-n", str(num_processes), self.executable, str(num_blocks)]

            timeout = max(120, num_blocks * 2)
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)

            if result.returncode != 0:
                raise Exception(f"Blockchain execution failed: {result.stderr}")

            return self.parse_output(result.stdout)

        except subprocess.TimeoutExpired:
            raise Exception("Blockchain execution timeout")
        except Exception as e:
            raise Exception(f"Error running blockchain: {str(e)}")

    def parse_output(self, output: str) -> Dict:
        """Parse blockchain output into structured data"""
        blocks = []
        current_block = None

        lines = output.split('\n')

        print(f"DEBUG: Parsing {len(lines)} lines of output")

        for line in lines:
            if line.startswith('=== Block'):
                if current_block:
                    blocks.append(current_block)
                    print(f"DEBUG: Added block {current_block.get('index')}")
                match = re.search(r'Block (\d+)', line)
                if match:
                    current_block = {'index': int(match.group(1))}
                    print(f"DEBUG: Started parsing block {current_block['index']}")

            elif current_block is not None:
                if line.startswith('Data:'):
                    current_block['data'] = line.split('Data:', 1)[1].strip()
                elif line.startswith('Timestamp:'):
                    current_block['timestamp'] = int(line.split('Timestamp:', 1)[1].strip())
                elif line.startswith('Previous Hash:'):
                    current_block['prev_hash'] = line.split('Previous Hash:', 1)[1].strip()
                elif line.startswith('Difficulty:'):
                    current_block['difficulty'] = int(line.split('Difficulty:', 1)[1].strip())
                elif line.startswith('Nonce:'):
                    current_block['nonce'] = int(line.split('Nonce:', 1)[1].strip())
                elif line.startswith('Hash:'):
                    current_block['hash'] = line.split('Hash:', 1)[1].strip()

        if current_block:
            blocks.append(current_block)
            print(f"DEBUG: Added final block {current_block.get('index')}")

        print(f"DEBUG: Total blocks parsed: {len(blocks)}")

        chain_length = 0
        cumulative_difficulty = 0.0
        valid = False

        for line in lines:
            if 'Chain length:' in line:
                chain_length = int(re.search(r'Chain length: (\d+)', line).group(1))
            elif 'Cumulative Difficulty:' in line:
                match = re.search(r'Cumulative Difficulty: ([\d.]+)', line)
                if match:
                    cumulative_difficulty = float(match.group(1))
            elif 'Valid:' in line:
                valid = 'YES' in line

        return {
            'blocks': blocks,
            'chain_length': chain_length,
            'cumulative_difficulty': cumulative_difficulty,
            'valid': valid,
            'timestamp': datetime.now().isoformat()
        }

runner = BlockchainRunner()

@app.get("/", response_class=HTMLResponse)
async def root():
    """Serve simple HTML interface"""
    return """
<!DOCTYPE html>
<html>
<head>
    <title>Blockchain Viewer</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; }
        h1 { color: #333; }
        .controls { margin: 20px 0; padding: 20px; background: white; border-radius: 8px; }
        .block { background: white; padding: 20px; margin: 10px 0; border-radius: 8px; border-left: 4px solid #4CAF50; }
        .block h3 { margin-top: 0; color: #4CAF50; }
        .field { margin: 8px 0; }
        .label { font-weight: bold; color: #666; }
        .value { color: #333; font-family: monospace; word-break: break-all; }
        .stats { background: #2196F3; color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
        .stats h2 { margin-top: 0; }
        button { background: #4CAF50; color: white; border: none; padding: 10px 20px;
                 border-radius: 4px; cursor: pointer; font-size: 16px; }
        button:hover { background: #45a049; }
        .loading { color: #666; font-style: italic; }
        .error { color: red; padding: 20px; background: #ffebee; border-radius: 8px; }
        .json-view { background: #1e1e1e; color: #d4d4d4; padding: 20px; border-radius: 8px;
                     margin: 10px 0; font-family: 'Courier New', monospace; overflow-x: auto;
                     max-height: 400px; overflow-y: auto; }
        .json-view pre { margin: 0; white-space: pre-wrap; word-wrap: break-word; }
        .toggle-view { margin: 10px 0; }
        .toggle-view label { margin-right: 15px; cursor: pointer; }
        .mining-status { background: #ff9800; color: white; padding: 10px 20px; border-radius: 8px;
                        margin: 10px 0; font-weight: bold; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Blockchain Viewer</h1>

        <div class="controls">
            <label>Number of MPI Nodes: </label>
            <input type="number" id="nodes" value="1" min="1" max="16" style="width: 60px;">
            <label style="margin-left: 20px;">Number of Blocks: </label>
            <input type="number" id="blocks" value="10" min="1" max="100" style="width: 60px;">
            <button onclick="loadBlockchain()">Mine & Load Blockchain</button>
        </div>

        <div id="loading" class="loading" style="display:none;">Starting mining...</div>
        <div id="mining-status" class="mining-status" style="display:none;"></div>
        <div id="error" class="error" style="display:none;"></div>
        <div id="stats"></div>
        <div id="json-blocks" class="json-view"><pre id="json-content">[]</pre></div>
    </div>

    <script>
        let blockMap = new Map();

        function updateJsonView() {
            const blocksArray = Array.from(blockMap.values()).sort((a, b) => a.index - b.index);
            document.getElementById('json-content').textContent = JSON.stringify(blocksArray, null, 2);
        }

        async function loadBlockchain() {
            const nodes = document.getElementById('nodes').value;
            const numBlocks = document.getElementById('blocks').value;
            const loading = document.getElementById('loading');
            const miningStatus = document.getElementById('mining-status');
            const error = document.getElementById('error');
            const statsDiv = document.getElementById('stats');

            loading.style.display = 'block';
            loading.textContent = 'Starting mining...';
            miningStatus.style.display = 'none';
            error.style.display = 'none';
            statsDiv.innerHTML = '';
            blockMap.clear();
            updateJsonView();

            try {
                const eventSource = new EventSource(`/blockchain/stream?nodes=${nodes}&blocks=${numBlocks}`);

                eventSource.onmessage = (event) => {
                    const data = JSON.parse(event.data);
                    console.log('Received:', data);

                    if (data.type === 'progress') {
                        loading.style.display = 'none';
                        miningStatus.style.display = 'block';
                        miningStatus.textContent = '⛏️ ' + data.message;
                    }
                    else if (data.type === 'block') {
                        const block = data.block;
                        blockMap.set(block.index, block);
                        updateJsonView();
                    }
                    else if (data.type === 'stats') {
                        statsDiv.innerHTML = `
                            <div class="stats">
                                <h2>Blockchain Statistics</h2>
                                <div><strong>Chain Length:</strong> ${data.chain_length} blocks</div>
                                <div><strong>Blocks Displayed:</strong> ${blockMap.size}</div>
                                <div><strong>Mining in progress...</strong></div>
                            </div>
                        `;
                    }
                    else if (data.type === 'complete') {
                        loading.style.display = 'none';
                        miningStatus.style.display = 'none';
                        statsDiv.innerHTML = `
                            <div class="stats">
                                <h2>Blockchain Statistics</h2>
                                <div><strong>Chain Length:</strong> ${blockMap.size} blocks</div>
                                <div><strong>Valid:</strong> ✅ YES</div>
                                <div><strong>Completed:</strong> ${new Date().toLocaleString()}</div>
                            </div>
                        `;
                        updateJsonView();
                        eventSource.close();
                    }
                };

                eventSource.onerror = (err) => {
                    console.error('EventSource error:', err);
                    error.textContent = 'Connection error. Mining may still be in progress.';
                    error.style.display = 'block';
                    loading.style.display = 'none';
                    eventSource.close();
                };

            } catch (err) {
                error.textContent = 'Error: ' + err.message;
                error.style.display = 'block';
                loading.style.display = 'none';
            }
        }

        // Load on page load
        window.onload = () => loadBlockchain();
    </script>
</body>
</html>
    """

@app.get("/blockchain/stream")
async def stream_blockchain(nodes: int = 1, blocks: int = 10):
    """
    Stream blockchain mining progress in real-time using Server-Sent Events
    """
    if nodes < 1 or nodes > 16:
        raise HTTPException(status_code=400, detail="Nodes must be between 1 and 16")

    if blocks < 1 or blocks > 100:
        raise HTTPException(status_code=400, detail="Blocks must be between 1 and 100")

    async def generate():
        if nodes == 1:
            cmd = [runner.executable, str(blocks)]
        else:
            cmd = ["mpirun", "-n", str(nodes), runner.executable, str(blocks)]

        process = await asyncio.create_subprocess_exec(
            *cmd,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE
        )

        current_block = {}

        while True:
            line = await process.stdout.readline()
            if not line:
                break

            line = line.decode().strip()

            if "Mining block" in line or "Block mined" in line or "Block added" in line:
                yield f"data: {json.dumps({'type': 'progress', 'message': line})}\n\n"

            if line.startswith('=== Block'):
                if current_block and 'hash' in current_block:
                    yield f"data: {json.dumps({'type': 'block', 'block': current_block})}\n\n"
                match = re.search(r'Block (\d+)', line)
                if match:
                    current_block = {'index': int(match.group(1))}

            elif current_block and 'index' in current_block:
                if line.startswith('Data:'):
                    current_block['data'] = line.split('Data:', 1)[1].strip()
                elif line.startswith('Timestamp:'):
                    current_block['timestamp'] = int(line.split('Timestamp:', 1)[1].strip())
                elif line.startswith('Previous Hash:'):
                    current_block['prev_hash'] = line.split('Previous Hash:', 1)[1].strip()
                elif line.startswith('Difficulty:'):
                    current_block['difficulty'] = int(line.split('Difficulty:', 1)[1].strip())
                elif line.startswith('Nonce:'):
                    current_block['nonce'] = int(line.split('Nonce:', 1)[1].strip())
                elif line.startswith('Hash:'):
                    current_block['hash'] = line.split('Hash:', 1)[1].strip()
                    yield f"data: {json.dumps({'type': 'block', 'block': current_block})}\n\n"
                    current_block = {}

            # Send stats
            if 'Chain length:' in line:
                match = re.search(r'Chain length: (\d+)', line)
                if match:
                    yield f"data: {json.dumps({'type': 'stats', 'chain_length': int(match.group(1))})}\n\n"

        yield f"data: {json.dumps({'type': 'complete'})}\n\n"

        await process.wait()

    return StreamingResponse(generate(), media_type="text/event-stream")

@app.get("/blockchain")
async def get_blockchain(nodes: int = 1, blocks: int = 10):
    """
    Run blockchain and return structured data

    Args:
        nodes: Number of MPI processes to use (1-16)
        blocks: Number of blocks to mine (1-100)
    """
    if nodes < 1 or nodes > 16:
        raise HTTPException(status_code=400, detail="Nodes must be between 1 and 16")

    if blocks < 1 or blocks > 100:
        raise HTTPException(status_code=400, detail="Blocks must be between 1 and 100")

    try:
        data = runner.run_blockchain(nodes, blocks)
        return data
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/stats")
async def get_stats():
    """Get blockchain statistics without full block data"""
    try:
        data = runner.run_blockchain(1)
        return {
            'chain_length': data['chain_length'],
            'cumulative_difficulty': data['cumulative_difficulty'],
            'valid': data['valid'],
            'block_count': len(data['blocks'])
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health():
    """Health check endpoint"""
    return {"status": "healthy", "service": "blockchain-api"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
