#include <iostream>
#include <vector>
#include <string>
#include <ctime>
#include <cstring>
#include <sstream>
#include <iomanip>
#include <thread>
#include <mutex>
#include <atomic>
#include <chrono>
#include <openssl/sha.h>
#include <mpi.h>
#include <pthread.h>
#include <algorithm>

using namespace std;

const int TARGET_BLOCK_TIME = 10;
const int DIFFICULTY_ADJUSTMENT_INTERVAL = 10;
const int MAX_TIMESTAMP_FUTURE = 60;
const int MAX_TIMESTAMP_PAST = 60;

bool FIXED_DIFFICULTY = false;

int NUM_THREADS = 0;

atomic<bool> mining_complete(false);
atomic<unsigned long long> found_nonce(0);
mutex blockchain_mutex;
string mining_result_hash;

unsigned long long mpi_start_nonce = 0;
unsigned long long mpi_end_nonce = ULLONG_MAX;

string sha256(const string &input)
{
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, input.c_str(), input.length());
    SHA256_Final(hash, &sha256);

    stringstream hashStream;
    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++)
    {
        hashStream << hex << setw(2) << setfill('0') << (int)hash[i];
    }
    return hashStream.str();
}

int count_leading_zeros(const string &hash)
{
    int count = 0;
    for (char c : hash)
    {
        if (c == '0')
            count++;
        else
            break;
    }
    return count;
}

struct Block
{
    int index;
    string data;
    long timestamp;
    string prev_hash;
    int difficulty;
    unsigned long long nonce;
    string hash;

    Block() : index(0), timestamp(0), difficulty(0), nonce(0) {}

    Block(int idx, const string &d, long ts, const string &ph, int diff)
        : index(idx), data(d), timestamp(ts), prev_hash(ph), difficulty(diff), nonce(0) {}

    string calculate_hash(unsigned long long n) const
    {
        stringstream ss;
        ss << index << data << timestamp << prev_hash << difficulty << n;
        return sha256(ss.str());
    }

    string to_string_data() const
    {
        stringstream ss;
        ss << index << "|" << data << "|" << timestamp << "|" << prev_hash << "|"
           << difficulty << "|" << nonce << "|" << hash;
        return ss.str();
    }
};

class Blockchain
{
private:
    vector<Block> chain;

public:
    Blockchain()
    {
        Block genesis(0, "Genesis Block", time(nullptr), "0", 4);
        genesis.nonce = 0;
        genesis.hash = genesis.calculate_hash(0);
        chain.push_back(genesis);
    }

    vector<Block> &get_chain()
    {
        return chain;
    }

    Block &get_last_block()
    {
        return chain.back();
    }

    int get_current_difficulty()
    {
        if (FIXED_DIFFICULTY)
        {
            return chain.back().difficulty;
        }

        if (chain.size() < DIFFICULTY_ADJUSTMENT_INTERVAL)
        {
            return chain.back().difficulty;
        }

        int adjustment_block_idx = chain.size() - DIFFICULTY_ADJUSTMENT_INTERVAL;
        Block &adjustment_block = chain[adjustment_block_idx];
        Block &last_block = chain.back();

        long expected_time = TARGET_BLOCK_TIME * DIFFICULTY_ADJUSTMENT_INTERVAL;
        long actual_time = last_block.timestamp - adjustment_block.timestamp;
        int current_diff = adjustment_block.difficulty;

        if (actual_time < (expected_time / 2))
        {
            return current_diff + 1;
        }
        else if (actual_time > (expected_time * 2))
        {
            return max(1, current_diff - 1);
        }
        return current_diff;
    }

    double get_cumulative_difficulty()
    {
        double cum_diff = 0.0;
        for (const Block &block : chain)
        {
            cum_diff += pow(2.0, block.difficulty);
        }
        return cum_diff;
    }

    bool validate_block_hash(const Block &block)
    {
        string calculated = block.calculate_hash(block.nonce);
        int leading_zeros = count_leading_zeros(calculated);

        return (calculated == block.hash) && (leading_zeros >= block.difficulty);
    }

    bool validate_block(const Block &new_block)
    {
        Block &last_block = chain.back();

        if (new_block.index != last_block.index + 1)
        {
            cerr << "Invalid index" << endl;
            return false;
        }

        if (new_block.prev_hash != last_block.hash)
        {
            cerr << "Invalid previous hash" << endl;
            return false;
        }

        long current_time = time(nullptr);
        if (new_block.timestamp > current_time + MAX_TIMESTAMP_FUTURE)
        {
            cerr << "Timestamp too far in future" << endl;
            return false;
        }

        if (new_block.timestamp < last_block.timestamp - MAX_TIMESTAMP_PAST)
        {
            cerr << "Timestamp too far in past" << endl;
            return false;
        }

        if (!validate_block_hash(new_block))
        {
            cerr << "Invalid block hash" << endl;
            return false;
        }

        return true;
    }

    bool validate_chain()
    {
        for (int i = 1; i < chain.size(); i++)
        {
            if (!validate_block_hash(chain[i]))
            {
                cerr << "Invalid hash at block " << i << endl;
                return false;
            }

            if (chain[i].index != chain[i - 1].index + 1)
            {
                cerr << "Invalid index at block " << i << endl;
                return false;
            }

            if (chain[i].prev_hash != chain[i - 1].hash)
            {
                cerr << "Invalid previous hash at block " << i << endl;
                return false;
            }

            if (chain[i].timestamp < chain[i - 1].timestamp - MAX_TIMESTAMP_PAST)
            {
                cerr << "Invalid timestamp at block " << i << endl;
                return false;
            }
        }
        return true;
    }

    bool add_block(Block &block)
    {
        lock_guard<mutex> lock(blockchain_mutex);

        if (!validate_block(block))
        {
            return false;
        }

        chain.push_back(block);
        return true;
    }
};

void *mine_thread(void *arg)
{
    Block *block = (Block *)arg;
    unsigned long long thread_id = (unsigned long long)pthread_self();

    unsigned long long range_size = mpi_end_nonce - mpi_start_nonce;
    unsigned long long start_nonce = mpi_start_nonce + (thread_id % range_size);
    unsigned long long nonce = start_nonce;

    while (!mining_complete && nonce < mpi_end_nonce)
    {
        string hash = block->calculate_hash(nonce);
        int leading_zeros = count_leading_zeros(hash);

        if (leading_zeros >= block->difficulty)
        {
            mining_complete = true;
            found_nonce = nonce;
            mining_result_hash = hash;
            break;
        }

        nonce++;

        if ((nonce - start_nonce) % 100000 == 0)
        {
            if (mining_complete || nonce >= mpi_end_nonce)
                break;
        }
    }

    return nullptr;
}

Block mine_block_threaded(Block &new_block)
{
    int num_threads = (NUM_THREADS > 0) ? NUM_THREADS : thread::hardware_concurrency();
    cout << "Mining with " << num_threads << " threads..." << endl;

    mining_complete = false;
    found_nonce = 0;
    mining_result_hash = "";

    auto start_time = chrono::high_resolution_clock::now();

    vector<pthread_t> threads(num_threads);
    for (int i = 0; i < num_threads; i++)
    {
        pthread_create(&threads[i], nullptr, mine_thread, (void *)&new_block);
    }

    for (int i = 0; i < num_threads; i++)
    {
        pthread_join(threads[i], nullptr);
    }

    auto end_time = chrono::high_resolution_clock::now();
    auto duration = chrono::duration_cast<chrono::milliseconds>(end_time - start_time);

    new_block.nonce = found_nonce;
    new_block.hash = mining_result_hash;

    cout << "Block mined in " << duration.count() << " ms with nonce: " << found_nonce << endl;

    return new_block;
}

Block mine_block_mpi(Block &new_block, int rank, int size)
{
    int num_threads_per_node = (NUM_THREADS > 0) ? NUM_THREADS : thread::hardware_concurrency();

    unsigned long long nonce_per_process = ULLONG_MAX / size;
    mpi_start_nonce = rank * nonce_per_process;
    mpi_end_nonce = (rank == size - 1) ? ULLONG_MAX : (rank + 1) * nonce_per_process;

    cout << "Rank " << rank << " mining nonces from " << mpi_start_nonce << " to " << mpi_end_nonce << endl;

    mining_complete = false;
    found_nonce = 0;
    mining_result_hash = "";

    auto start_time = chrono::high_resolution_clock::now();

    vector<pthread_t> threads(num_threads_per_node);
    for (int i = 0; i < num_threads_per_node; i++)
    {
        pthread_create(&threads[i], nullptr, mine_thread, (void *)&new_block);
    }

    for (int i = 0; i < num_threads_per_node; i++)
    {
        pthread_join(threads[i], nullptr);
    }

    auto end_time = chrono::high_resolution_clock::now();
    auto duration = chrono::duration_cast<chrono::milliseconds>(end_time - start_time);

    struct MineResult
    {
        unsigned long long nonce;
        int rank;
        int found;
    } local_result, global_result;

    local_result.nonce = found_nonce;
    local_result.rank = rank;
    local_result.found = (found_nonce > 0) ? 1 : 0;

    MPI_Allreduce(&local_result, &global_result, 1, MPI_UNSIGNED_LONG_LONG, MPI_MIN, MPI_COMM_WORLD);

    unsigned long long winning_nonce = found_nonce;
    MPI_Bcast(&winning_nonce, 1, MPI_UNSIGNED_LONG_LONG, global_result.rank, MPI_COMM_WORLD);

    new_block.nonce = winning_nonce;
    new_block.hash = new_block.calculate_hash(winning_nonce);

    if (rank == 0 || found_nonce > 0)
    {
        cout << "Rank " << rank << " finished in " << duration.count() << " ms" << endl;
    }

    return new_block;
}

void print_block(const Block &block)
{
    cout << "\n=== Block " << block.index << " ===" << endl;
    cout << "Data: " << block.data << endl;
    cout << "Timestamp: " << block.timestamp << endl;
    cout << "Previous Hash: " << block.prev_hash << endl;
    cout << "Difficulty: " << block.difficulty << endl;
    cout << "Nonce: " << block.nonce << endl;
    cout << "Hash: " << block.hash << endl;
}

void print_blockchain(Blockchain &bc)
{
    cout << "\n=== BLOCKCHAIN ===" << endl;
    cout << "Chain length: " << bc.get_chain().size() << endl;
    cout << "Cumulative Difficulty: " << bc.get_cumulative_difficulty() << endl;
    cout << "Valid: " << (bc.validate_chain() ? "YES" : "NO") << endl;
    cout << "\nBlocks:" << endl;

    for (const Block &block : bc.get_chain())
    {
        print_block(block);
    }
}

int main(int argc, char *argv[])
{
    int rank, size;
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);

    int num_blocks_to_mine = 10;

    for (int i = 1; i < argc; i++)
    {
        if (strcmp(argv[i], "--fixed-difficulty") == 0)
        {
            FIXED_DIFFICULTY = true;
        }
        else if (strcmp(argv[i], "--threads") == 0)
        {
            if (i + 1 < argc)
            {
                NUM_THREADS = atoi(argv[++i]);
                if (NUM_THREADS < 1 || NUM_THREADS > 64)
                {
                    if (rank == 0)
                        cerr << "Thread count must be between 1 and 64" << endl;
                    MPI_Finalize();
                    return 1;
                }
            }
        }
        else
        {
            num_blocks_to_mine = atoi(argv[i]);
            if (num_blocks_to_mine < 1 || num_blocks_to_mine > 100)
            {
                if (rank == 0)
                    cerr << "Number of blocks must be between 1 and 100" << endl;
                MPI_Finalize();
                return 1;
            }
        }
    }

    if (rank == 0)
    {
        cout << "=== BLOCKCHAIN WITH MPI MINING ===" << endl;
        cout << "Running on " << size << " nodes" << endl;
        cout << "Threads per node: " << thread::hardware_concurrency() << endl;
    }

    Blockchain blockchain;

    if (rank == 0)
    {
        cout << "\n--- Mining Blocks ---" << endl;

        for (int i = 1; i <= num_blocks_to_mine; i++)
        {
            int difficulty = blockchain.get_current_difficulty();
            cout << "\nMining block " << i << " with difficulty " << difficulty << "..." << endl;

            Block new_block(i, "Block data " + to_string(i), time(nullptr),
                            blockchain.get_last_block().hash, difficulty);

            new_block = mine_block_threaded(new_block);

            if (blockchain.add_block(new_block))
            {
                cout << "Block added successfully!" << endl;
            }
            else
            {
                cout << "Block validation failed!" << endl;
            }
        }

        print_blockchain(blockchain);
    }

    int chain_size = blockchain.get_chain().size();
    MPI_Bcast(&chain_size, 1, MPI_INT, 0, MPI_COMM_WORLD);

    MPI_Finalize();

    return 0;
}
