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
#include <omp.h>
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
string mining_result_hash;
mutex blockchain_mutex;

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

Block mine_block_omp(Block &new_block, int num_threads)
{
    cout << "Mining with " << num_threads << " OpenMP threads..." << endl;

    mining_complete = false;
    found_nonce = 0;
    mining_result_hash = "";

    auto start_time = chrono::high_resolution_clock::now();

    omp_set_num_threads(num_threads);

    const int batch_size = 500; // 500 nonces za eno iteracijo thread-a

    #pragma omp parallel
    {
        unsigned long long local_nonce = omp_get_thread_num();
        string local_hash;

        while (!mining_complete.load(memory_order_relaxed))
        {
            for (int i = 0; i < batch_size; i++)
            {
                local_hash = new_block.calculate_hash(local_nonce);

                if (count_leading_zeros(local_hash) >= new_block.difficulty)
                {
                    bool expected = false;
                    if (mining_complete.compare_exchange_strong(expected, true))
                    {
                        found_nonce = local_nonce;
                        mining_result_hash = local_hash;
                    }
                    break;
                }

                local_nonce += num_threads;
            }
        }
    }

    auto end_time = chrono::high_resolution_clock::now();
    auto duration = chrono::duration_cast<chrono::milliseconds>(end_time - start_time);

    new_block.nonce = found_nonce;
    new_block.hash = mining_result_hash;

    cout << "Block mined in " << duration.count() << " ms with nonce: " << found_nonce << endl;

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

    int num_blocks_to_mine = 20;

    for (int i = 1; i < argc; i++)
    {
        if (strcmp(argv[i], "--fixed-difficulty") == 0) // nastavljanje konstantne zahtevnosti (za benchmarking)
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

    int hardware_threads = thread::hardware_concurrency();
    int threads_per_node = 0;

    if (size == 1) // uporabimo samo en node
    {
        threads_per_node = (NUM_THREADS > 0) ? NUM_THREADS : hardware_threads;
        
        if (rank == 0)
        {
            cout << "=== BLOCKCHAIN WITH SINGLE NODE ===" << endl;
            cout << "Using " << threads_per_node << " threads" << endl;
        }
    }
    else // več nodov in delamo po princupu client-server
    {
        if (NUM_THREADS > 0)
        {
            if (rank == 0)
            {
                cout << "=== BLOCKCHAIN WITH MPI CLIENT-SERVER ===" << endl;
                cout << "Running on " << size << " nodes" << endl;
                cout << "Manual thread configuration: " << NUM_THREADS << " threads per client node" << endl;
                cout << "Server will coordinate only (no mining)" << endl;
                threads_per_node = 0;
            }
            else
            {
                threads_per_node = NUM_THREADS;
                cout << "Rank " << rank << " (client) ready with " << threads_per_node << " threads" << endl;
            }
        }
        else
        {
            // Avtomatska razporeditev
            int client_nodes = size - 1;
            threads_per_node = (hardware_threads - 1) / client_nodes;
            int remainder = (hardware_threads - 1) % client_nodes;

            if (rank == 0)
            {
                cout << "=== BLOCKCHAIN WITH MPI CLIENT-SERVER ===" << endl;
                cout << "Running on " << size << " nodes" << endl;
                cout << "Hardware threads available: " << hardware_threads << endl;
                cout << "Client threads per node: " << threads_per_node << endl;
                
                int server_threads = remainder + 1;
                if (server_threads > 1)
                {
                    cout << "Server threads: " << server_threads << endl;
                    threads_per_node = server_threads;
                }
                else
                {
                    cout << "Server will coordinate only (no mining)" << endl;
                    threads_per_node = 0;
                }
            }
            else
            {
                cout << "Rank " << rank << " (client) ready with " << threads_per_node << " threads" << endl;
            }
        }
    }

    Blockchain blockchain;

    if (size == 1) // uporabimo samo en node (klasicni OMP)
    {
        if (rank == 0)
        {
            cout << "\n--- Mining Blocks (Single Node) ---" << endl;

            for (int i = 1; i <= num_blocks_to_mine; i++)
            {
                int difficulty = blockchain.get_current_difficulty();
                cout << "\nMining block " << i << " with difficulty " << difficulty << "..." << endl;

                Block new_block(i, "Block data " + to_string(i), time(nullptr),
                                blockchain.get_last_block().hash, difficulty);

                new_block = mine_block_omp(new_block, threads_per_node);

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
    }
    else // client-server MPI pristop
    {
        if (rank == 0) // server
        {
            cout << "\n--- Mining Blocks (Server Mode) ---" << endl;

            for (int i = 1; i <= num_blocks_to_mine; i++)
            {
                int difficulty = blockchain.get_current_difficulty();
                cout << "\nMining block " << i << " with difficulty " << difficulty << "..." << endl;

                Block new_block(i, "Block data " + to_string(i), time(nullptr),
                                blockchain.get_last_block().hash, difficulty);

                char data_buffer[256];
                strncpy(data_buffer, new_block.data.c_str(), 255);
                data_buffer[255] = '\0';

                for (int r = 1; r < size; r++) // posiljanje blokov clientom
                {
                    MPI_Send(&new_block.index, 1, MPI_INT, r, 0, MPI_COMM_WORLD);
                    MPI_Send(data_buffer, 256, MPI_CHAR, r, 0, MPI_COMM_WORLD);
                    MPI_Send(&new_block.timestamp, 1, MPI_LONG, r, 0, MPI_COMM_WORLD);
                    MPI_Send(new_block.prev_hash.c_str(), 65, MPI_CHAR, r, 0, MPI_COMM_WORLD);
                    MPI_Send(&new_block.difficulty, 1, MPI_INT, r, 0, MPI_COMM_WORLD);
                }

                mining_complete = false;
                found_nonce = 0;
                mining_result_hash = "";

                if (threads_per_node > 0) // kopanje ce ima niti na serverju
                {
                    cout << "Server also mining with " << threads_per_node << " threads..." << endl;
                    auto server_start = chrono::high_resolution_clock::now();
                    
                    new_block = mine_block_omp(new_block, threads_per_node);
                    
                    auto server_end = chrono::high_resolution_clock::now();
                    auto server_duration = chrono::duration_cast<chrono::milliseconds>(server_end - server_start);
                    cout << "Server finished mining in " << server_duration.count() << " ms" << endl;
                }
                else
                {
                    unsigned long long winning_nonce = 0;
                    int winning_rank = -1;
                    
                    for (int r = 1; r < size; r++)
                    {
                        unsigned long long client_nonce;
                        MPI_Recv(&client_nonce, 1, MPI_UNSIGNED_LONG_LONG, r, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                        
                        if (winning_rank == -1 || client_nonce > 0)
                        {
                            // prvi ki najde ali prvi odgovor
                            string hash = new_block.calculate_hash(client_nonce);
                            int leading_zeros = count_leading_zeros(hash);
                            
                            if (leading_zeros >= new_block.difficulty)
                            {
                                winning_nonce = client_nonce;
                                winning_rank = r;
                                cout << "Client rank " << r << " found nonce: " << client_nonce << endl;
                                break;
                            }
                        }
                    }
                    
                    new_block.nonce = winning_nonce;
                    new_block.hash = new_block.calculate_hash(winning_nonce);
                }

                if (blockchain.add_block(new_block))
                {
                    cout << "Block added successfully!" << endl;
                }
                else
                {
                    cout << "Block validation failed!" << endl;
                }
            }

            // pošiljanje signala za konec
            int end_signal = -1;
            for (int r = 1; r < size; r++)
            {
                MPI_Send(&end_signal, 1, MPI_INT, r, 0, MPI_COMM_WORLD);
            }

            print_blockchain(blockchain);
        }
        else // client -> prjema delo in ga paralelno izvaja, glede na stevilo niti, ki jih imamo
        {
            cout << "Client rank " << rank << " waiting for work..." << endl;

            while (true)
            {
                int index;
                MPI_Recv(&index, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

                if (index == -1)
                {
                    cout << "Rank " << rank << " received end signal" << endl;
                    break;
                }

                char data_buffer[256];
                long timestamp;
                char prev_hash_buffer[65];
                int difficulty;

                MPI_Recv(data_buffer, 256, MPI_CHAR, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                MPI_Recv(&timestamp, 1, MPI_LONG, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                MPI_Recv(prev_hash_buffer, 65, MPI_CHAR, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                MPI_Recv(&difficulty, 1, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

                Block new_block(index, string(data_buffer), timestamp, string(prev_hash_buffer), difficulty);

                cout << "Rank " << rank << " mining block " << index << " with difficulty " << difficulty << endl;

                new_block = mine_block_omp(new_block, threads_per_node);

                // Pošiljanje rezultata nazaj serverju
                MPI_Send(&new_block.nonce, 1, MPI_UNSIGNED_LONG_LONG, 0, 0, MPI_COMM_WORLD);
            }
        }
    }

    MPI_Finalize();

    return 0;
}
