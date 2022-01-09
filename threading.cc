#include <thread>
#include <queue>
#include <condition_variable>
#include <mutex>
#include <functional>
#include <atomic>
#include "transcribe.h"

class join_threads {
    std::vector<std::thread>& threads;
public:
    explicit join_threads(std::vector<std::thread>& threads_) : threads(threads_)
    {}

    ~join_threads()
    {
	for (unsigned long i = 0; i < threads.size(); ++i) {
	    if (threads[i].joinable())
		threads[i].join();
	}
    }
		    
};

template<typename T>
class threadsafe_queue
{
private:
    mutable std::mutex mut;
    std::queue<T> data_queue;
    std::condition_variable data_cond;
public:
    threadsafe_queue()
    {}

    void push(T new_value)
    {
	std::lock_guard<std::mutex> lk(mut);
	data_queue.push(std::move(new_value));
	data_cond.notify_one();
    }
	
    bool try_pop(T& value)
    {
	std::lock_guard<std::mutex> lk(mut);
	if (data_queue.empty())
	    return false;
	value = std::move(data_queue.front());
	data_queue.pop();
	return true;
    }

    bool empty() const
    {
	std::lock_guard<std::mutex> lk(mut);
	return data_queue.empty();
    }
};

class thread_pool {

    std::atomic<bool> done;

    threadsafe_queue<std::function<void()> > work_queue;
    std::vector<std::thread> threads;
    join_threads joiner;

    void worker_thread() {
	
	while (!done) {
	    std::function<void()> task;
	    
	    if (work_queue.try_pop(task)) {
		task();
	    } else {
		std::this_thread::yield();
	    }
	}
    }
public:
    thread_pool() : done(false), joiner(threads)
    {
	unsigned const thread_count = std::thread::hardware_concurrency();
	try {
	    for (unsigned i = 0; i < thread_count; ++i) {
		threads.push_back(std::thread(&thread_pool::worker_thread, this));
	    }
	}
	catch (...) {
	    done = true;
	    throw;
	}
    }
    ~thread_pool() {
	done = true;
    }

    void submit(std::function<void()> f) {
	work_queue.push(f);
    }
};
			
