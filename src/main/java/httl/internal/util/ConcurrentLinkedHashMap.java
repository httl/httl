/*
 * Copyright 2011-2013 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.internal.util;

import static httl.internal.util.ConcurrentLinkedHashMap.DrainStatus.IDLE;
import static httl.internal.util.ConcurrentLinkedHashMap.DrainStatus.PROCESSING;
import static httl.internal.util.ConcurrentLinkedHashMap.DrainStatus.REQUIRED;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A hash table supporting full concurrency of retrievals, adjustable expected
 * concurrency for updates, and a maximum capacity to bound the map by. This
 * implementation differs from {@link ConcurrentHashMap} in that it maintains a
 * page replacement algorithm that is used to evict an entry when the map has
 * exceeded its capacity. Unlike the <tt>Java Collections Framework</tt>, this
 * map does not have a publicly visible constructor and instances are created
 * through a Builder.
 * <p>
 * An entry is evicted from the map when the <tt>weighted capacity</tt> exceeds
 * its <tt>maximum weighted capacity</tt> threshold. A EntryWeigher
 * determines how many units of capacity that an entry consumes. The default
 * weigher assigns each value a weight of <tt>1</tt> to bound the map by the
 * total number of key-value pairs. A map that holds collections may choose to
 * weigh values by the number of elements in the collection and bound the map by
 * the total number of elements that it contains. A change to a value that
 * modifies its weight requires that an update operation is performed on the
 * map.
 * <p>
 * An EvictionListener may be supplied for notification when an entry is
 * evicted from the map. This listener is invoked on a caller's thread and will
 * not block other threads from operating on the map. An implementation should
 * be aware that the caller's thread will not expect long execution times or
 * failures as a side effect of the listener being notified. Execution safety
 * and a fast turn around time can be achieved by performing the operation
 * asynchronously, such as by submitting a task to an
 * {@link java.util.concurrent.ExecutorService}.
 * <p>
 * The <tt>concurrency level</tt> determines the number of threads that can
 * concurrently modify the table. Using a significantly higher or lower value
 * than needed can waste space or lead to thread contention, but an estimate
 * within an order of magnitude of the ideal value does not usually have a
 * noticeable impact. Because placement in hash tables is essentially random,
 * the actual concurrency will vary.
 * <p>
 * This class and its views and iterators implement all of the <em>optional</em>
 * methods of the {@link Map} and {@link Iterator} interfaces.
 * <p>
 * Like {@link java.util.Hashtable} but unlike {@link HashMap}, this class does
 * <em>not</em> allow <tt>null</tt> to be used as a key or value. Unlike
 * {@link java.util.LinkedHashMap}, this class does <em>not</em> provide
 * predictable iteration order. A snapshot of the keys and entries may be
 * obtained in ascending and descending order of retention.
 * 
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K>
 *			the type of keys maintained by this map
 * @param <V>
 *			the type of mapped values
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 *	  http://code.google.com/p/concurrentlinkedhashmap/</a>
 */
public class ConcurrentLinkedHashMap<K, V> extends AbstractMap<K, V>
		implements ConcurrentMap<K, V>, Serializable {

	/*
	 * This class performs a best-effort bounding of a ConcurrentHashMap using a
	 * page-replacement algorithm to determine which entries to evict when the
	 * capacity is exceeded.
	 * 
	 * The page replacement algorithm's data structures are kept eventually
	 * consistent with the map. An update to the map and recording of reads may
	 * not be immediately reflected on the algorithm's data structures. These
	 * structures are guarded by a lock and operations are applied in batches to
	 * avoid lock contention. The penalty of applying the batches is spread
	 * across threads so that the amortized cost is slightly higher than
	 * performing just the ConcurrentHashMap operation.
	 * 
	 * A memento of the reads and writes that were performed on the map are
	 * recorded in a buffer. These buffers are drained at the first opportunity
	 * after a write or when a buffer exceeds a threshold size. A mostly strict
	 * ordering is achieved by observing that each buffer is in a weakly sorted
	 * order relative to the last drain. This allows the buffers to be merged in
	 * O(n) time so that the operations are run in the expected order.
	 * 
	 * Due to a lack of a strict ordering guarantee, a task can be executed
	 * out-of-order, such as a removal followed by its addition. The state of
	 * the entry is encoded within the value's weight.
	 * 
	 * Alive: The entry is in both the hash-table and the page replacement
	 * policy. This is represented by a positive weight.
	 * 
	 * Retired: The entry is not in the hash-table and is pending removal from
	 * the page replacement policy. This is represented by a negative weight.
	 * 
	 * Dead: The entry is not in the hash-table and is not in the page
	 * replacement policy. This is represented by a weight of zero.
	 * 
	 * The Least Recently Used page replacement algorithm was chosen due to its
	 * simplicity, high hit rate, and ability to be implemented with O(1) time
	 * complexity.
	 */
	/** The maximum weighted capacity of the map. */
	static final long MAXIMUM_CAPACITY = Long.MAX_VALUE - Integer.MAX_VALUE;

	/** The maximum number of pending operations per buffer. */
	static final int MAXIMUM_BUFFER_SIZE = 1 << 20;

	/** The number of pending operations per buffer before attempting to drain. */
	static final int BUFFER_THRESHOLD = 16;

	/** The number of buffers to use. */
	static final int NUMBER_OF_BUFFERS;

	/** Mask value for indexing into the buffers. */
	static final int BUFFER_MASK;

	/** The maximum number of operations to perform per amortized drain. */
	static final int AMORTIZED_DRAIN_THRESHOLD;

	/** A queue that discards all entries. */
	static final Queue<?> DISCARDING_QUEUE = new DiscardingQueue();

	static {
		NUMBER_OF_BUFFERS = ceilingNextPowerOfTwo(Runtime.getRuntime()
				.availableProcessors());
		AMORTIZED_DRAIN_THRESHOLD = (1 + NUMBER_OF_BUFFERS) * BUFFER_THRESHOLD;
		BUFFER_MASK = NUMBER_OF_BUFFERS - 1;
	}

	static int ceilingNextPowerOfTwo(int x) {
		// From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
		return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
	}

	// The backing data store holding the key-value associations
	final ConcurrentMap<K, Node> data;
	final int concurrencyLevel;

	// These fields provide support to bound the map by a maximum capacity

	final LinkedDeque<Node> evictionDeque;

	// must write under lock
	final AtomicLong weightedSize;

	// must write under lock
	volatile long capacity;

	volatile int nextOrder;

	int drainedOrder;

	final Task[] tasks;

	final Lock evictionLock;
	final Queue<Task>[] buffers;
	final AtomicIntegerArray bufferLengths;
	final AtomicReference<DrainStatus> drainStatus;
	final EntryWeigher<? super K, ? super V> weigher;

	// These fields provide support for notifying a listener.
	final Queue<Node> pendingNotifications;
	final EvictionListener<K, V> listener;

	transient Set<K> keySet;
	transient Collection<V> values;
	transient Set<Entry<K, V>> entrySet;
	
	public ConcurrentLinkedHashMap() {
		this(MAXIMUM_CAPACITY);
	}

	public ConcurrentLinkedHashMap(long capacity) {
		this(new Builder<K, V>().maximumWeightedCapacity(capacity));
	}

	/**
	 * Creates an instance based on the builder's configuration.
	 */
	@SuppressWarnings({ "unchecked", "cast" })
	private ConcurrentLinkedHashMap(Builder<K, V> builder) {
		checkState(capacity >= 0);
		
		// The data store and its maximum capacity
		concurrencyLevel = builder.concurrencyLevel;
		capacity = Math.min(builder.capacity, MAXIMUM_CAPACITY);
		data = new ConcurrentHashMap<K, Node>(builder.initialCapacity, 0.75f,
				concurrencyLevel);

		// The eviction support
		weigher = builder.weigher;
		nextOrder = Integer.MIN_VALUE;
		weightedSize = new AtomicLong();
		drainedOrder = Integer.MIN_VALUE;
		evictionLock = new ReentrantLock();
		evictionDeque = new LinkedDeque<Node>();
		drainStatus = new AtomicReference<DrainStatus>(IDLE);

		bufferLengths = new AtomicIntegerArray(NUMBER_OF_BUFFERS);
		buffers = (Queue<Task>[]) new Queue[NUMBER_OF_BUFFERS];
		for (int i = 0; i < NUMBER_OF_BUFFERS; i++) {
			buffers[i] = new ConcurrentLinkedQueue<Task>();
		}

		// The drain is capped to the expected number of tasks plus additional
		// slack to optimistically handle the concurrent additions to the
		// buffers.
		tasks = new Task[AMORTIZED_DRAIN_THRESHOLD];

		// The notification queue and listener
		listener = builder.listener;
		pendingNotifications = (listener == DiscardingListener.INSTANCE) ? (Queue<Node>) DISCARDING_QUEUE
				: new ConcurrentLinkedQueue<Node>();
	}

	/** Ensures that the object is not null. */
	static void checkNotNull(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}

	/** Ensures that the argument expression is true. */
	static void checkArgument(boolean expression) {
		if (!expression) {
			throw new IllegalArgumentException();
		}
	}

	/** Ensures that the state expression is true. */
	static void checkState(boolean expression) {
		if (!expression) {
			throw new IllegalStateException();
		}
	}

	/* ---------------- Eviction Support -------------- */

	/**
	 * Retrieves the maximum weighted capacity of the map.
	 * 
	 * @return the maximum weighted capacity
	 */
	public long capacity() {
		return capacity;
	}

	/**
	 * Sets the maximum weighted capacity of the map and eagerly evicts entries
	 * until it shrinks to the appropriate size.
	 * 
	 * @param capacity
	 *			the maximum weighted capacity of the map
	 * @throws IllegalArgumentException
	 *			 if the capacity is negative
	 */
	public void setCapacity(long capacity) {
		checkArgument(capacity >= 0);
		evictionLock.lock();
		try {
			this.capacity = Math.min(capacity, MAXIMUM_CAPACITY);
			drainBuffers();
			evict();
		} finally {
			evictionLock.unlock();
		}
		notifyListener();
	}

	/** Determines whether the map has exceeded its capacity. */
	boolean hasOverflowed() {
		return weightedSize.get() > capacity;
	}

	/**
	 * Evicts entries from the map while it exceeds the capacity and appends
	 * evicted entries to the notification queue for processing.
	 */
	void evict() {
		// Attempts to evict entries from the map if it exceeds the maximum
		// capacity. If the eviction fails due to a concurrent removal of the
		// victim, that removal may cancel out the addition that triggered this
		// eviction. The victim is eagerly unlinked before the removal task so
		// that if an eviction is still required then a new victim will be
		// chosen
		// for removal.
		while (hasOverflowed()) {
			Node node = evictionDeque.poll();

			// If weighted values are used, then the pending operations will
			// adjust
			// the size to reflect the correct weight
			if (node == null) {
				return;
			}

			// Notify the listener only if the entry was evicted
			if (data.remove(node.key, node)) {
				pendingNotifications.add(node);
			}

			node.makeDead();
		}
	}

	/**
	 * Performs the post-processing work required after the map operation.
	 * 
	 * @param task
	 *			the pending operation to be applied
	 */
	void afterCompletion(Task task) {
		boolean delayable = schedule(task);
		DrainStatus status = drainStatus.get();
		if (status.shouldDrainBuffers(delayable)) {
			tryToDrainBuffers();
		}
		notifyListener();
	}

	/**
	 * Schedules the task to be applied to the page replacement policy.
	 * 
	 * @param task
	 *			the pending operation
	 * @return if the draining of the buffers can be delayed
	 */
	boolean schedule(Task task) {
		int index = bufferIndex();
		int buffered = bufferLengths.incrementAndGet(index);

		if (task.isWrite()) {
			buffers[index].add(task);
			drainStatus.set(REQUIRED);
			return false;
		}

		// A buffer may discard a read task if its length exceeds a tolerance
		// level
		if (buffered <= MAXIMUM_BUFFER_SIZE) {
			buffers[index].add(task);
			return (buffered <= BUFFER_THRESHOLD);
		} else { // not optimized for fail-safe scenario
			bufferLengths.decrementAndGet(index);
			return false;
		}
	}

	/** Returns the index to the buffer that the task should be scheduled on. */
	static int bufferIndex() {
		// A buffer is chosen by the thread's id so that tasks are distributed
		// in a
		// pseudo evenly manner. This helps avoid hot entries causing contention
		// due
		// to other threads trying to append to the same buffer.
		return (int) Thread.currentThread().getId() & BUFFER_MASK;
	}

	/** Returns the ordering value to assign to a task. */
	int nextOrdering() {
		// The next ordering is acquired in a racy fashion as the increment is
		// not
		// atomic with the insertion into a buffer. This means that concurrent
		// tasks
		// can have the same ordering and the buffers are in a weakly sorted
		// order.
		return nextOrder++;
	}

	/**
	 * Attempts to acquire the eviction lock and apply the pending operations,
	 * up to the amortized threshold, to the page replacement policy.
	 */
	void tryToDrainBuffers() {
		if (evictionLock.tryLock()) {
			try {
				drainStatus.set(PROCESSING);
				drainBuffers();
			} finally {
				drainStatus.compareAndSet(PROCESSING, IDLE);
				evictionLock.unlock();
			}
		}
	}

	/**
	 * Drains the buffers up to the amortized threshold and applies the pending
	 * operations.
	 */
	void drainBuffers() {
		// A mostly strict ordering is achieved by observing that each buffer
		// contains tasks in a weakly sorted order starting from the last drain.
		// The buffers can be merged into a sorted array in O(n) time by using
		// counting sort and chaining on a collision.

		// Moves the tasks into the output array, applies them, and updates the
		// marker for the starting order of the next drain.
		int maxTaskIndex = moveTasksFromBuffers(tasks);
		updateDrainedOrder(tasks, maxTaskIndex);
		runTasks(tasks, maxTaskIndex);
	}

	/**
	 * Moves the tasks from the buffers into the output array.
	 * 
	 * @param tasks
	 *			the ordered array of the pending operations
	 * @return the highest index location of a task that was added to the array
	 */
	int moveTasksFromBuffers(Task[] tasks) {
		int maxTaskIndex = -1;
		for (int i = 0; i < buffers.length; i++) {
			int maxIndex = moveTasksFromBuffer(tasks, i);
			maxTaskIndex = Math.max(maxIndex, maxTaskIndex);
		}
		return maxTaskIndex;
	}

	/**
	 * Moves the tasks from the specified buffer into the output array.
	 * 
	 * @param tasks
	 *			the ordered array of the pending operations
	 * @param bufferIndex
	 *			the buffer to drain into the tasks array
	 * @return the highest index location of a task that was added to the array
	 */
	int moveTasksFromBuffer(Task[] tasks, int bufferIndex) {
		// While a buffer is being drained it may be concurrently appended to.
		// The
		// number of tasks removed are tracked so that the length can be
		// decremented
		// by the delta rather than set to zero.
		Queue<Task> buffer = buffers[bufferIndex];
		int removedFromBuffer = 0;

		Task task;
		int maxIndex = -1;
		while ((task = buffer.poll()) != null) {
			removedFromBuffer++;

			// The index into the output array is determined by calculating the
			// offset
			// since the last drain
			int index = task.getOrder() - drainedOrder;
			if (index < 0) {
				// The task was missed by the last drain and can be run
				// immediately
				task.run();
			} else if (index >= tasks.length) {
				// Due to concurrent additions, the order exceeds the capacity
				// of the
				// output array. It is added to the end as overflow and the
				// remaining
				// tasks in the buffer will be handled by the next drain.
				maxIndex = tasks.length - 1;
				addTaskToChain(tasks, task, maxIndex);
				break;
			} else {
				// Add the task to the array so that it is run in sequence
				maxIndex = Math.max(index, maxIndex);
				addTaskToChain(tasks, task, index);
			}
		}
		bufferLengths.addAndGet(bufferIndex, -removedFromBuffer);
		return maxIndex;
	}

	/**
	 * Adds the task as the head of the chain at the index location.
	 * 
	 * @param tasks
	 *			the ordered array of the pending operations
	 * @param task
	 *			the pending operation to add
	 * @param index
	 *			the array location
	 */
	void addTaskToChain(Task[] tasks, Task task, int index) {
		task.setNext(tasks[index]);
		tasks[index] = task;
	}

	/**
	 * Runs the pending page replacement policy operations.
	 * 
	 * @param tasks
	 *			the ordered array of the pending operations
	 * @param maxTaskIndex
	 *			the maximum index of the array
	 */
	void runTasks(Task[] tasks, int maxTaskIndex) {
		for (int i = 0; i <= maxTaskIndex; i++) {
			runTasksInChain(tasks[i]);
			tasks[i] = null;
		}
	}

	/**
	 * Runs the pending operations on the linked chain.
	 * 
	 * @param task
	 *			the first task in the chain of operations
	 */
	void runTasksInChain(Task task) {
		while (task != null) {
			Task current = task;
			task = task.getNext();
			current.setNext(null);
			current.run();
		}
	}

	/**
	 * Updates the order to start the next drain from.
	 * 
	 * @param tasks
	 *			the ordered array of operations
	 * @param maxTaskIndex
	 *			the maximum index of the array
	 */
	void updateDrainedOrder(Task[] tasks, int maxTaskIndex) {
		if (maxTaskIndex >= 0) {
			Task task = tasks[maxTaskIndex];
			drainedOrder = task.getOrder() + 1;
		}
	}

	/** Notifies the listener of entries that were evicted. */
	void notifyListener() {
		Node node;
		while ((node = pendingNotifications.poll()) != null) {
			listener.onEviction(node.key, node.getValue());
		}
	}

	/** Updates the node's location in the page replacement policy. */
	class ReadTask extends AbstractTask {
		final Node node;

		ReadTask(Node node) {
			this.node = node;
		}

		public void run() {
			// An entry may scheduled for reordering despite having been
			// previously
			// removed. This can occur when the entry was concurrently read
			// while a
			// writer was removing it. If the entry is no longer linked then it
			// does
			// not need to be processed.
			if (evictionDeque.contains(node)) {
				evictionDeque.moveToBack(node);
			}
		}

		public boolean isWrite() {
			return false;
		}
	}

	/** Adds the node to the page replacement policy. */
	final class AddTask extends AbstractTask {
		final Node node;
		final int weight;

		AddTask(Node node, int weight) {
			this.weight = weight;
			this.node = node;
		}

		public void run() {
			weightedSize.set(weightedSize.get() + weight);

			// ignore out-of-order write operations
			if (node.get().isAlive()) {
				evictionDeque.add(node);
				evict();
			}
		}

		public boolean isWrite() {
			return true;
		}
	}

	/** Removes a node from the page replacement policy. */
	final class RemovalTask extends AbstractTask {
		final Node node;

		RemovalTask(Node node) {
			this.node = node;
		}

		public void run() {
			// add may not have been processed yet
			evictionDeque.remove(node);
			node.makeDead();
		}

		public boolean isWrite() {
			return true;
		}
	}

	/** Updates the weighted size and evicts an entry on overflow. */
	final class UpdateTask extends ReadTask {
		final int weightDifference;

		public UpdateTask(Node node, int weightDifference) {
			super(node);
			this.weightDifference = weightDifference;
		}

		public void run() {
			weightedSize.set(weightedSize.get() + weightDifference);
			super.run();
			evict();
		}

		public boolean isWrite() {
			return true;
		}
	}

	/* ---------------- Concurrent Map Support -------------- */

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public int size() {
		return data.size();
	}

	/**
	 * Returns the weighted size of this map.
	 * 
	 * @return the combined weight of the values in this map
	 */
	public long weightedSize() {
		return Math.max(0, weightedSize.get());
	}

	public void clear() {
		// The alternative is to iterate through the keys and call #remove(),
		// which
		// adds unnecessary contention on the eviction lock and buffers.
		evictionLock.lock();
		try {
			Node node;
			while ((node = evictionDeque.poll()) != null) {
				data.remove(node.key, node);
				node.makeDead();
			}

			// Drain the buffers and run only the write tasks
			for (int i = 0; i < buffers.length; i++) {
				Queue<Task> buffer = buffers[i];
				int removed = 0;
				Task task;
				while ((task = buffer.poll()) != null) {
					if (task.isWrite()) {
						task.run();
					}
					removed++;
				}
				bufferLengths.addAndGet(i, -removed);
			}
		} finally {
			evictionLock.unlock();
		}
	}

	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(Object value) {
		checkNotNull(value);

		for (Node node : data.values()) {
			if (node.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public V get(Object key) {
		final Node node = data.get(key);
		if (node == null) {
			return null;
		}
		afterCompletion(new ReadTask(node));
		return node.getValue();
	}

	/**
	 * Returns the value to which the specified key is mapped, or {@code null}
	 * if this map contains no mapping for the key. This method differs from
	 * {@link #get(Object)} in that it does not record the operation with the
	 * page replacement policy.
	 * 
	 * @param key
	 *			the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or {@code null}
	 *		 if this map contains no mapping for the key
	 * @throws NullPointerException
	 *			 if the specified key is null
	 */
	public V getQuietly(Object key) {
		final Node node = data.get(key);
		return (node == null) ? null : node.getValue();
	}

	public V put(K key, V value) {
		return put(key, value, false);
	}

	public V putIfAbsent(K key, V value) {
		return put(key, value, true);
	}

	/**
	 * Adds a node to the list and the data store. If an existing node is found,
	 * then its value is updated if allowed.
	 * 
	 * @param key
	 *			key with which the specified value is to be associated
	 * @param value
	 *			value to be associated with the specified key
	 * @param onlyIfAbsent
	 *			a write is performed only if the key is not already associated
	 *			with a value
	 * @return the prior value in the data store or null if no mapping was found
	 */
	V put(K key, V value, boolean onlyIfAbsent) {
		checkNotNull(key);
		checkNotNull(value);

		final int weight = weigher.weightOf(key, value);
		final WeightedValue<V> weightedValue = new WeightedValue<V>(value,
				weight);
		final Node node = new Node(key, weightedValue);

		for (;;) {
			final Node prior = data.putIfAbsent(node.key, node);
			if (prior == null) {
				afterCompletion(new AddTask(node, weight));
				return null;
			} else if (onlyIfAbsent) {
				afterCompletion(new ReadTask(prior));
				return prior.getValue();
			}
			for (;;) {
				final WeightedValue<V> oldWeightedValue = prior.get();
				if (!oldWeightedValue.isAlive()) {
					break;
				}

				if (prior.compareAndSet(oldWeightedValue, weightedValue)) {
					final int weightedDifference = weight
							- oldWeightedValue.weight;
					final Task task = (weightedDifference == 0) ? new ReadTask(
							prior) : new UpdateTask(prior, weightedDifference);
					afterCompletion(task);
					return oldWeightedValue.value;
				}
			}
		}
	}

	public V remove(Object key) {
		final Node node = data.remove(key);
		if (node == null) {
			return null;
		}

		node.makeRetired();
		afterCompletion(new RemovalTask(node));
		return node.getValue();
	}

	public boolean remove(Object key, Object value) {
		final Node node = data.get(key);
		if ((node == null) || (value == null)) {
			return false;
		}

		WeightedValue<V> weightedValue = node.get();
		for (;;) {
			if (weightedValue.contains(value)) {
				if (node.tryToRetire(weightedValue)) {
					if (data.remove(key, node)) {
						afterCompletion(new RemovalTask(node));
						return true;
					}
				} else {
					weightedValue = node.get();
					if (weightedValue.isAlive()) {
						// retry as an intermediate update may have replaced the
						// value with
						// an equal instance that has a different reference
						// identity
						continue;
					}
				}
			}
			return false;
		}
	}

	public V replace(K key, V value) {
		checkNotNull(key);
		checkNotNull(value);

		final int weight = weigher.weightOf(key, value);
		final WeightedValue<V> weightedValue = new WeightedValue<V>(value,
				weight);

		final Node node = data.get(key);
		if (node == null) {
			return null;
		}
		for (;;) {
			WeightedValue<V> oldWeightedValue = node.get();
			if (!oldWeightedValue.isAlive()) {
				return null;
			}
			if (node.compareAndSet(oldWeightedValue, weightedValue)) {
				int weightedDifference = weight - oldWeightedValue.weight;
				final Task task = (weightedDifference == 0) ? new ReadTask(node)
						: new UpdateTask(node, weightedDifference);
				afterCompletion(task);
				return oldWeightedValue.value;
			}
		}
	}

	public boolean replace(K key, V oldValue, V newValue) {
		checkNotNull(key);
		checkNotNull(oldValue);
		checkNotNull(newValue);

		final int weight = weigher.weightOf(key, newValue);
		final WeightedValue<V> newWeightedValue = new WeightedValue<V>(
				newValue, weight);

		final Node node = data.get(key);
		if (node == null) {
			return false;
		}
		for (;;) {
			final WeightedValue<V> weightedValue = node.get();
			if (!weightedValue.isAlive() || !weightedValue.contains(oldValue)) {
				return false;
			}
			if (node.compareAndSet(weightedValue, newWeightedValue)) {
				int weightedDifference = weight - weightedValue.weight;
				final Task task = (weightedDifference == 0) ? new ReadTask(node)
						: new UpdateTask(node, weightedDifference);
				afterCompletion(task);
				return true;
			}
		}
	}

	public Set<K> keySet() {
		Set<K> ks = keySet;
		return (ks == null) ? (keySet = new KeySet()) : ks;
	}

	/**
	 * Returns a unmodifiable snapshot {@link Set} view of the keys contained in
	 * this map. The set's iterator returns the keys whose order of iteration is
	 * the ascending order in which its entries are considered eligible for
	 * retention, from the least-likely to be retained to the most-likely.
	 * <p>
	 * Beware that, unlike in {@link #keySet()}, obtaining the set is
	 * <em>NOT</em> a constant-time operation. Because of the asynchronous
	 * nature of the page replacement policy, determining the retention ordering
	 * requires a traversal of the keys.
	 * 
	 * @return an ascending snapshot view of the keys in this map
	 */
	public Set<K> ascendingKeySet() {
		return ascendingKeySetWithLimit(Integer.MAX_VALUE);
	}

	/**
	 * Returns an unmodifiable snapshot {@link Set} view of the keys contained
	 * in this map. The set's iterator returns the keys whose order of iteration
	 * is the ascending order in which its entries are considered eligible for
	 * retention, from the least-likely to be retained to the most-likely.
	 * <p>
	 * Beware that, unlike in {@link #keySet()}, obtaining the set is
	 * <em>NOT</em> a constant-time operation. Because of the asynchronous
	 * nature of the page replacement policy, determining the retention ordering
	 * requires a traversal of the keys.
	 * 
	 * @param limit
	 *			the maximum size of the returned set
	 * @return a ascending snapshot view of the keys in this map
	 * @throws IllegalArgumentException
	 *			 if the limit is negative
	 */
	public Set<K> ascendingKeySetWithLimit(int limit) {
		return orderedKeySet(true, limit);
	}

	/**
	 * Returns an unmodifiable snapshot {@link Set} view of the keys contained
	 * in this map. The set's iterator returns the keys whose order of iteration
	 * is the descending order in which its entries are considered eligible for
	 * retention, from the most-likely to be retained to the least-likely.
	 * <p>
	 * Beware that, unlike in {@link #keySet()}, obtaining the set is
	 * <em>NOT</em> a constant-time operation. Because of the asynchronous
	 * nature of the page replacement policy, determining the retention ordering
	 * requires a traversal of the keys.
	 * 
	 * @return a descending snapshot view of the keys in this map
	 */
	public Set<K> descendingKeySet() {
		return descendingKeySetWithLimit(Integer.MAX_VALUE);
	}

	/**
	 * Returns an unmodifiable snapshot {@link Set} view of the keys contained
	 * in this map. The set's iterator returns the keys whose order of iteration
	 * is the descending order in which its entries are considered eligible for
	 * retention, from the most-likely to be retained to the least-likely.
	 * <p>
	 * Beware that, unlike in {@link #keySet()}, obtaining the set is
	 * <em>NOT</em> a constant-time operation. Because of the asynchronous
	 * nature of the page replacement policy, determining the retention ordering
	 * requires a traversal of the keys.
	 * 
	 * @param limit
	 *			the maximum size of the returned set
	 * @return a descending snapshot view of the keys in this map
	 * @throws IllegalArgumentException
	 *			 if the limit is negative
	 */
	public Set<K> descendingKeySetWithLimit(int limit) {
		return orderedKeySet(false, limit);
	}

	Set<K> orderedKeySet(boolean ascending, int limit) {
		checkArgument(limit >= 0);
		evictionLock.lock();
		try {
			drainBuffers();

			int initialCapacity = (weigher == Weighers.entrySingleton()) ? Math
					.min(limit, (int) weightedSize()) : 16;
			Set<K> keys = new LinkedHashSet<K>(initialCapacity);
			Iterator<Node> iterator = ascending ? evictionDeque.iterator()
					: evictionDeque.descendingIterator();
			while (iterator.hasNext() && (limit > keys.size())) {
				keys.add(iterator.next().key);
			}
			return unmodifiableSet(keys);
		} finally {
			evictionLock.unlock();
		}
	}

	public Collection<V> values() {
		Collection<V> vs = values;
		return (vs == null) ? (values = new Values()) : vs;
	}

	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> es = entrySet;
		return (es == null) ? (entrySet = new EntrySet()) : es;
	}

	/**
	 * Returns an unmodifiable snapshot {@link Map} view of the mappings
	 * contained in this map. The map's collections return the mappings whose
	 * order of iteration is the ascending order in which its entries are
	 * considered eligible for retention, from the least-likely to be retained
	 * to the most-likely.
	 * <p>
	 * Beware that obtaining the mappings is <em>NOT</em> a constant-time
	 * operation. Because of the asynchronous nature of the page replacement
	 * policy, determining the retention ordering requires a traversal of the
	 * entries.
	 * 
	 * @return a ascending snapshot view of this map
	 */
	public Map<K, V> ascendingMap() {
		return ascendingMapWithLimit(Integer.MAX_VALUE);
	}

	/**
	 * Returns an unmodifiable snapshot {@link Map} view of the mappings
	 * contained in this map. The map's collections return the mappings whose
	 * order of iteration is the ascending order in which its entries are
	 * considered eligible for retention, from the least-likely to be retained
	 * to the most-likely.
	 * <p>
	 * Beware that obtaining the mappings is <em>NOT</em> a constant-time
	 * operation. Because of the asynchronous nature of the page replacement
	 * policy, determining the retention ordering requires a traversal of the
	 * entries.
	 * 
	 * @param limit
	 *			the maximum size of the returned map
	 * @return a ascending snapshot view of this map
	 * @throws IllegalArgumentException
	 *			 if the limit is negative
	 */
	public Map<K, V> ascendingMapWithLimit(int limit) {
		return orderedMap(true, limit);
	}

	/**
	 * Returns an unmodifiable snapshot {@link Map} view of the mappings
	 * contained in this map. The map's collections return the mappings whose
	 * order of iteration is the descending order in which its entries are
	 * considered eligible for retention, from the most-likely to be retained to
	 * the least-likely.
	 * <p>
	 * Beware that obtaining the mappings is <em>NOT</em> a constant-time
	 * operation. Because of the asynchronous nature of the page replacement
	 * policy, determining the retention ordering requires a traversal of the
	 * entries.
	 * 
	 * @return a descending snapshot view of this map
	 */
	public Map<K, V> descendingMap() {
		return descendingMapWithLimit(Integer.MAX_VALUE);
	}

	/**
	 * Returns an unmodifiable snapshot {@link Map} view of the mappings
	 * contained in this map. The map's collections return the mappings whose
	 * order of iteration is the descending order in which its entries are
	 * considered eligible for retention, from the most-likely to be retained to
	 * the least-likely.
	 * <p>
	 * Beware that obtaining the mappings is <em>NOT</em> a constant-time
	 * operation. Because of the asynchronous nature of the page replacement
	 * policy, determining the retention ordering requires a traversal of the
	 * entries.
	 * 
	 * @param limit
	 *			the maximum size of the returned map
	 * @return a descending snapshot view of this map
	 * @throws IllegalArgumentException
	 *			 if the limit is negative
	 */
	public Map<K, V> descendingMapWithLimit(int limit) {
		return orderedMap(false, limit);
	}

	Map<K, V> orderedMap(boolean ascending, int limit) {
		checkArgument(limit >= 0);
		evictionLock.lock();
		try {
			drainBuffers();

			int initialCapacity = (weigher == Weighers.entrySingleton()) ? Math
					.min(limit, (int) weightedSize()) : 16;
			Map<K, V> map = new LinkedHashMap<K, V>(initialCapacity);
			Iterator<Node> iterator = ascending ? evictionDeque.iterator()
					: evictionDeque.descendingIterator();
			while (iterator.hasNext() && (limit > map.size())) {
				Node node = iterator.next();
				map.put(node.key, node.getValue());
			}
			return unmodifiableMap(map);
		} finally {
			evictionLock.unlock();
		}
	}

	/** The draining status of the buffers. */
	enum DrainStatus {

		/** A drain is not taking place. */
		IDLE {

			boolean shouldDrainBuffers(boolean delayable) {
				return !delayable;
			}
		},

		/** A drain is required due to a pending write modification. */
		REQUIRED {

			boolean shouldDrainBuffers(boolean delayable) {
				return true;
			}
		},

		/** A drain is in progress. */
		PROCESSING {

			boolean shouldDrainBuffers(boolean delayable) {
				return false;
			}
		};

		/**
		 * Determines whether the buffers should be drained.
		 * 
		 * @param delayable
		 *			if a drain should be delayed until required
		 * @return if a drain should be attempted
		 */
		abstract boolean shouldDrainBuffers(boolean delayable);
	}

	/** A value, its weight, and the entry's status. */
	static final class WeightedValue<V> {
		final int weight;
		final V value;

		WeightedValue(V value, int weight) {
			this.weight = weight;
			this.value = value;
		}

		boolean contains(Object o) {
			return (o == value) || value.equals(o);
		}

		/**
		 * If the entry is available in the hash-table and page replacement
		 * policy.
		 */
		boolean isAlive() {
			return weight > 0;
		}

		/**
		 * If the entry was removed from the hash-table and is awaiting removal
		 * from the page replacement policy.
		 */
		boolean isRetired() {
			return weight < 0;
		}

		/**
		 * If the entry was removed from the hash-table and the page replacement
		 * policy.
		 */
		boolean isDead() {
			return weight == 0;
		}
	}

	/**
	 * A node contains the key, the weighted value, and the linkage pointers on
	 * the page-replacement algorithm's data structures.
	 */
	@SuppressWarnings("serial")
	final class Node extends AtomicReference<WeightedValue<V>> implements
			Linked<Node> {
		final K key;

		Node prev;

		Node next;

		/** Creates a new, unlinked node. */
		Node(K key, WeightedValue<V> weightedValue) {
			super(weightedValue);
			this.key = key;
		}

		public Node getPrevious() {
			return prev;
		}

		public void setPrevious(Node prev) {
			this.prev = prev;
		}

		public Node getNext() {
			return next;
		}

		public void setNext(Node next) {
			this.next = next;
		}

		/** Retrieves the value held by the current <tt>WeightedValue</tt>. */
		V getValue() {
			return get().value;
		}

		/**
		 * Attempts to transition the node from the <tt>alive</tt> state to the
		 * <tt>retired</tt> state.
		 * 
		 * @param expect
		 *			the expected weighted value
		 * @return if successful
		 */
		boolean tryToRetire(WeightedValue<V> expect) {
			if (expect.isAlive()) {
				WeightedValue<V> retired = new WeightedValue<V>(expect.value,
						-expect.weight);
				return compareAndSet(expect, retired);
			}
			return false;
		}

		/**
		 * Atomically transitions the node from the <tt>alive</tt> state to the
		 * <tt>retired</tt> state, if a valid transition.
		 */
		void makeRetired() {
			for (;;) {
				WeightedValue<V> current = get();
				if (!current.isAlive()) {
					return;
				}
				WeightedValue<V> retired = new WeightedValue<V>(current.value,
						-current.weight);
				if (compareAndSet(current, retired)) {
					return;
				}
			}
		}

		/**
		 * Atomically transitions the node to the <tt>dead</tt> state and
		 * decrements the <tt>weightedSize</tt>.
		 */
		void makeDead() {
			for (;;) {
				WeightedValue<V> current = get();
				WeightedValue<V> dead = new WeightedValue<V>(current.value, 0);
				if (compareAndSet(current, dead)) {
					weightedSize.set(weightedSize.get()
							- Math.abs(current.weight));
					return;
				}
			}
		}
	}

	/** An adapter to safely externalize the keys. */
	final class KeySet extends AbstractSet<K> {
		final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

		public int size() {
			return map.size();
		}

		public void clear() {
			map.clear();
		}

		public Iterator<K> iterator() {
			return new KeyIterator();
		}

		public boolean contains(Object obj) {
			return containsKey(obj);
		}

		public boolean remove(Object obj) {
			return (map.remove(obj) != null);
		}

		public Object[] toArray() {
			return map.data.keySet().toArray();
		}

		public <T> T[] toArray(T[] array) {
			return map.data.keySet().toArray(array);
		}
	}

	/** An adapter to safely externalize the key iterator. */
	final class KeyIterator implements Iterator<K> {
		final Iterator<K> iterator = data.keySet().iterator();
		K current;

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public K next() {
			current = iterator.next();
			return current;
		}

		public void remove() {
			checkState(current != null);
			ConcurrentLinkedHashMap.this.remove(current);
			current = null;
		}
	}

	/** An adapter to safely externalize the values. */
	final class Values extends AbstractCollection<V> {

		public int size() {
			return ConcurrentLinkedHashMap.this.size();
		}

		public void clear() {
			ConcurrentLinkedHashMap.this.clear();
		}

		public Iterator<V> iterator() {
			return new ValueIterator();
		}

		public boolean contains(Object o) {
			return containsValue(o);
		}
	}

	/** An adapter to safely externalize the value iterator. */
	final class ValueIterator implements Iterator<V> {
		final Iterator<Node> iterator = data.values().iterator();
		Node current;

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public V next() {
			current = iterator.next();
			return current.getValue();
		}

		public void remove() {
			checkState(current != null);
			ConcurrentLinkedHashMap.this.remove(current.key);
			current = null;
		}
	}

	/** An adapter to safely externalize the entries. */
	final class EntrySet extends AbstractSet<Entry<K, V>> {
		final ConcurrentLinkedHashMap<K, V> map = ConcurrentLinkedHashMap.this;

		public int size() {
			return map.size();
		}

		public void clear() {
			map.clear();
		}

		public Iterator<Entry<K, V>> iterator() {
			return new EntryIterator();
		}

		public boolean contains(Object obj) {
			if (!(obj instanceof Entry<?, ?>)) {
				return false;
			}
			Entry<?, ?> entry = (Entry<?, ?>) obj;
			Node node = map.data.get(entry.getKey());
			return (node != null) && (node.getValue().equals(entry.getValue()));
		}

		public boolean add(Entry<K, V> entry) {
			return (map.putIfAbsent(entry.getKey(), entry.getValue()) == null);
		}

		public boolean remove(Object obj) {
			if (!(obj instanceof Entry<?, ?>)) {
				return false;
			}
			Entry<?, ?> entry = (Entry<?, ?>) obj;
			return map.remove(entry.getKey(), entry.getValue());
		}
	}

	/** An adapter to safely externalize the entry iterator. */
	final class EntryIterator implements Iterator<Entry<K, V>> {
		final Iterator<Node> iterator = data.values().iterator();
		Node current;

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public Entry<K, V> next() {
			current = iterator.next();
			return new WriteThroughEntry(current);
		}

		public void remove() {
			checkState(current != null);
			ConcurrentLinkedHashMap.this.remove(current.key);
			current = null;
		}
	}

	/** An entry that allows updates to write through to the map. */
	final class WriteThroughEntry implements Entry<K, V> {
		static final long serialVersionUID = 1;

		private final K key;
		
		private V value;

		WriteThroughEntry(Node node) {
			this.key = node.key;
			this.value = node.getValue();
		}
		
		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
			put(getKey(), value);
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		private ConcurrentLinkedHashMap<K, V> getOuterType() {
			return ConcurrentLinkedHashMap.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WriteThroughEntry other = (WriteThroughEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		public String toString() {
			return key + "=" + value;
		}

	}

	/** A weigher that enforces that the weight falls within a valid range. */
	static final class BoundedEntryWeigher<K, V> implements EntryWeigher<K, V>,
			Serializable {
		static final long serialVersionUID = 1;
		final EntryWeigher<? super K, ? super V> weigher;

		BoundedEntryWeigher(EntryWeigher<? super K, ? super V> weigher) {
			checkNotNull(weigher);
			this.weigher = weigher;
		}

		public int weightOf(K key, V value) {
			int weight = weigher.weightOf(key, value);
			checkArgument(weight >= 1);
			return weight;
		}

		Object writeReplace() {
			return weigher;
		}
	}

	/** A queue that discards all additions and is always empty. */
	static final class DiscardingQueue extends AbstractQueue<Object> {

		public boolean add(Object e) {
			return true;
		}

		public boolean offer(Object e) {
			return true;
		}

		public Object poll() {
			return null;
		}

		public Object peek() {
			return null;
		}

		public int size() {
			return 0;
		}

		public Iterator<Object> iterator() {
			return emptyList().iterator();
		}
	}

	/** A listener that ignores all notifications. */
	enum DiscardingListener implements EvictionListener<Object, Object> {
		INSTANCE;

		public void onEviction(Object key, Object value) {
		}
	}

	/** An operation that can be lazily applied to the page replacement policy. */
	interface Task extends Runnable {

		/** The priority order. */
		int getOrder();

		/** If the task represents an add, modify, or remove operation. */
		boolean isWrite();

		/** Returns the next task on the link chain. */
		Task getNext();

		/** Sets the next task on the link chain. */
		void setNext(Task task);
	}

	/** A skeletal implementation of the <tt>Task</tt> interface. */
	abstract class AbstractTask implements Task {
		final int order;
		Task task;

		AbstractTask() {
			order = nextOrdering();
		}

		public int getOrder() {
			return order;
		}

		public Task getNext() {
			return task;
		}

		public void setNext(Task task) {
			this.task = task;
		}
	}

	/* ---------------- Serialization Support -------------- */

	static final long serialVersionUID = 1;

	Object writeReplace() {
		return new SerializationProxy<K, V>(this);
	}

	private void readObject(ObjectInputStream stream)
			throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}

	/**
	 * A proxy that is serialized instead of the map. The page-replacement
	 * algorithm's data structures are not serialized so the deserialized
	 * instance contains only the entries. This is acceptable as caches hold
	 * transient data that is recomputable and serialization would tend to be
	 * used as a fast warm-up process.
	 */
	static final class SerializationProxy<K, V> implements Serializable {
		final EntryWeigher<? super K, ? super V> weigher;
		final EvictionListener<K, V> listener;
		final int concurrencyLevel;
		final Map<K, V> data;
		final long capacity;

		SerializationProxy(ConcurrentLinkedHashMap<K, V> map) {
			concurrencyLevel = map.concurrencyLevel;
			data = new HashMap<K, V>(map);
			capacity = map.capacity;
			listener = map.listener;
			weigher = map.weigher;
		}

		Object readResolve() {
			ConcurrentLinkedHashMap<K, V> map = new Builder<K, V>()
					.concurrencyLevel(concurrencyLevel)
					.maximumWeightedCapacity(capacity).listener(listener)
					.weigher(weigher).build();
			map.putAll(data);
			return map;
		}

		static final long serialVersionUID = 1;
	}

	/* ---------------- Builder -------------- */

	/**
	 * A builder that creates {@link ConcurrentLinkedHashMap} instances. It
	 * provides a flexible approach for constructing customized instances with a
	 * named parameter syntax. It can be used in the following manner:
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	ConcurrentMap&lt;Vertex, Set&lt;Edge&gt;&gt; graph = new Builder&lt;Vertex, Set&lt;Edge&gt;&gt;()
	 * 			.maximumWeightedCapacity(5000).weigher(Weighers.&lt;Edge&gt; set())
	 * 			.build();
	 * }
	 * </pre>
	 */
	static final class Builder<K, V> {
		static final int DEFAULT_CONCURRENCY_LEVEL = 16;
		static final int DEFAULT_INITIAL_CAPACITY = 16;

		EvictionListener<K, V> listener;
		EntryWeigher<? super K, ? super V> weigher;

		int concurrencyLevel;
		int initialCapacity;
		long capacity;

		@SuppressWarnings("unchecked")
		public Builder() {
			capacity = -1;
			weigher = Weighers.entrySingleton();
			initialCapacity = DEFAULT_INITIAL_CAPACITY;
			concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
			listener = (EvictionListener<K, V>) DiscardingListener.INSTANCE;
		}

		/**
		 * Specifies the initial capacity of the hash table (default <tt>16</tt>
		 * ). This is the number of key-value pairs that the hash table can hold
		 * before a resize operation is required.
		 * 
		 * @param initialCapacity
		 *			the initial capacity used to size the hash table to
		 *			accommodate this many entries.
		 * @throws IllegalArgumentException
		 *			 if the initialCapacity is negative
		 */
		public Builder<K, V> initialCapacity(int initialCapacity) {
			checkArgument(initialCapacity >= 0);
			this.initialCapacity = initialCapacity;
			return this;
		}

		/**
		 * Specifies the maximum weighted capacity to coerce the map to and may
		 * exceed it temporarily.
		 * 
		 * @param capacity
		 *			the weighted threshold to bound the map by
		 * @throws IllegalArgumentException
		 *			 if the maximumWeightedCapacity is negative
		 */
		public Builder<K, V> maximumWeightedCapacity(long capacity) {
			checkArgument(capacity >= 0);
			this.capacity = capacity;
			return this;
		}

		/**
		 * Specifies the estimated number of concurrently updating threads. The
		 * implementation performs internal sizing to try to accommodate this
		 * many threads (default <tt>16</tt>).
		 * 
		 * @param concurrencyLevel
		 *			the estimated number of concurrently updating threads
		 * @throws IllegalArgumentException
		 *			 if the concurrencyLevel is less than or equal to zero
		 */
		public Builder<K, V> concurrencyLevel(int concurrencyLevel) {
			checkArgument(concurrencyLevel > 0);
			this.concurrencyLevel = concurrencyLevel;
			return this;
		}

		/**
		 * Specifies an optional listener that is registered for notification
		 * when an entry is evicted.
		 * 
		 * @param listener
		 *			the object to forward evicted entries to
		 * @throws NullPointerException
		 *			 if the listener is null
		 */
		public Builder<K, V> listener(EvictionListener<K, V> listener) {
			checkNotNull(listener);
			this.listener = listener;
			return this;
		}

		/**
		 * Specifies an algorithm to determine how many the units of capacity a
		 * value consumes. The default algorithm bounds the map by the number of
		 * key-value pairs by giving each entry a weight of <tt>1</tt>.
		 * 
		 * @param weigher
		 *			the algorithm to determine a value's weight
		 * @throws NullPointerException
		 *			 if the weigher is null
		 */
		public Builder<K, V> weigher(Weigher<? super V> weigher) {
			this.weigher = (weigher == Weighers.singleton()) ? Weighers
					.<K, V> entrySingleton() : new BoundedEntryWeigher<K, V>(
					Weighers.asEntryWeigher(weigher));
			return this;
		}

		/**
		 * Specifies an algorithm to determine how many the units of capacity an
		 * entry consumes. The default algorithm bounds the map by the number of
		 * key-value pairs by giving each entry a weight of <tt>1</tt>.
		 * 
		 * @param weigher
		 *			the algorithm to determine a entry's weight
		 * @throws NullPointerException
		 *			 if the weigher is null
		 */
		public Builder<K, V> weigher(EntryWeigher<? super K, ? super V> weigher) {
			this.weigher = (weigher == Weighers.entrySingleton()) ? Weighers
					.<K, V> entrySingleton() : new BoundedEntryWeigher<K, V>(
					weigher);
			return this;
		}

		/**
		 * Creates a new {@link ConcurrentLinkedHashMap} instance.
		 * 
		 * @throws IllegalStateException
		 *			 if the maximum weighted capacity was not set
		 */
		public ConcurrentLinkedHashMap<K, V> build() {
			checkState(capacity >= 0);
			return new ConcurrentLinkedHashMap<K, V>(this);
		}
	}

	static final class LinkedDeque<E extends Linked<E>> extends
			AbstractCollection<E> implements Queue<E>, Serializable {

		// This class provides a doubly-linked list that is optimized for the
		// virtual
		// machine. The first and last elements are manipulated instead of a
		// slightly
		// more convenient sentinel element to avoid the insertion of null
		// checks with
		// NullPointerException throws in the byte code. The links to a removed
		// element are cleared to help a generational garbage collector if the
		// discarded elements inhabit more than one generation.

		private static final long serialVersionUID = 1L;

		/**
		 * Pointer to first node. Invariant: (first == null && last == null) ||
		 * (first.prev == null)
		 */
		E first;

		/**
		 * Pointer to last node. Invariant: (first == null && last == null) ||
		 * (last.next == null)
		 */
		E last;

		/**
		 * Links the element to the front of the deque so that it becomes the
		 * first element.
		 * 
		 * @param e
		 *			the unlinked element
		 */
		void linkFirst(final E e) {
			final E f = first;
			first = e;

			if (f == null) {
				last = e;
			} else {
				f.setPrevious(e);
				e.setNext(f);
			}
		}

		/**
		 * Links the element to the back of the deque so that it becomes the
		 * last element.
		 * 
		 * @param e
		 *			the unlinked element
		 */
		void linkLast(final E e) {
			final E l = last;
			last = e;

			if (l == null) {
				first = e;
			} else {
				l.setNext(e);
				e.setPrevious(l);
			}
		}

		/** Unlinks the non-null first element. */
		E unlinkFirst() {
			final E f = first;
			final E next = f.getNext();
			f.setNext(null);

			first = next;
			if (next == null) {
				last = null;
			} else {
				next.setPrevious(null);
			}
			return f;
		}

		/** Unlinks the non-null last element. */
		E unlinkLast() {
			final E l = last;
			final E prev = l.getPrevious();
			l.setPrevious(null);
			last = prev;
			if (prev == null) {
				first = null;
			} else {
				prev.setNext(null);
			}
			return l;
		}

		/** Unlinks the non-null element. */
		void unlink(E e) {
			final E prev = e.getPrevious();
			final E next = e.getNext();

			if (prev == null) {
				first = next;
			} else {
				prev.setNext(next);
				e.setPrevious(null);
			}

			if (next == null) {
				last = prev;
			} else {
				next.setPrevious(prev);
				e.setNext(null);
			}
		}

		public boolean isEmpty() {
			return (first == null);
		}

		void checkNotEmpty() {
			if (isEmpty()) {
				throw new NoSuchElementException();
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * Beware that, unlike in most collections, this method is <em>NOT</em>
		 * a constant-time operation.
		 */
		public int size() {
			int size = 0;
			for (E e = first; e != null; e = e.getNext()) {
				size++;
			}
			return size;
		}

		public void clear() {
			for (E e = first; e != null;) {
				E next = e.getNext();
				e.setPrevious(null);
				e.setNext(null);
				e = next;
			}
			first = last = null;
		}

		public boolean contains(Object o) {
			return (o instanceof Linked<?>) && contains((Linked<?>) o);
		}

		// A fast-path containment check
		boolean contains(Linked<?> e) {
			return (e.getPrevious() != null) || (e.getNext() != null)
					|| (e == first);
		}

		/**
		 * Moves the element to the front of the deque so that it becomes the
		 * first element.
		 * 
		 * @param e
		 *			the linked element
		 */
		public void moveToFront(E e) {
			if (e != first) {
				unlink(e);
				linkFirst(e);
			}
		}

		/**
		 * Moves the element to the back of the deque so that it becomes the
		 * last element.
		 * 
		 * @param e
		 *			the linked element
		 */
		public void moveToBack(E e) {
			if (e != last) {
				unlink(e);
				linkLast(e);
			}
		}

		public E peek() {
			return peekFirst();
		}

		public E peekFirst() {
			return first;
		}

		public E peekLast() {
			return last;
		}

		public E getFirst() {
			checkNotEmpty();
			return peekFirst();
		}

		public E getLast() {
			checkNotEmpty();
			return peekLast();
		}

		public E element() {
			return getFirst();
		}

		public boolean offer(E e) {
			return offerLast(e);
		}

		public boolean offerFirst(E e) {
			if (contains(e)) {
				return false;
			}
			linkFirst(e);
			return true;
		}

		public boolean offerLast(E e) {
			if (contains(e)) {
				return false;
			}
			linkLast(e);
			return true;
		}

		public boolean add(E e) {
			return offerLast(e);
		}

		public void addFirst(E e) {
			if (!offerFirst(e)) {
				throw new IllegalArgumentException();
			}
		}

		public void addLast(E e) {
			if (!offerLast(e)) {
				throw new IllegalArgumentException();
			}
		}

		public E poll() {
			return pollFirst();
		}

		public E pollFirst() {
			return isEmpty() ? null : unlinkFirst();
		}

		public E pollLast() {
			return isEmpty() ? null : unlinkLast();
		}

		public E remove() {
			return removeFirst();
		}

		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			return (o instanceof Linked<?>) && remove((E) o);
		}

		// A fast-path removal
		boolean remove(E e) {
			if (contains(e)) {
				unlink(e);
				return true;
			}
			return false;
		}

		public E removeFirst() {
			checkNotEmpty();
			return pollFirst();
		}

		public boolean removeFirstOccurrence(Object o) {
			return remove(o);
		}

		public E removeLast() {
			checkNotEmpty();
			return pollLast();
		}

		public boolean removeLastOccurrence(Object o) {
			return remove(o);
		}

		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for (Object o : c) {
				modified |= remove(o);
			}
			return modified;
		}

		public void push(E e) {
			addFirst(e);
		}

		public E pop() {
			return removeFirst();
		}

		public Iterator<E> iterator() {
			return new AbstractLinkedIterator(first) {

				E computeNext() {
					return cursor.getNext();
				}
			};
		}

		public Iterator<E> descendingIterator() {
			return new AbstractLinkedIterator(last) {

				E computeNext() {
					return cursor.getPrevious();
				}
			};
		}

		abstract class AbstractLinkedIterator implements Iterator<E> {
			E cursor;

			/**
			 * Creates an iterator that can can traverse the deque.
			 * 
			 * @param start
			 *			the initial element to begin traversal from
			 */
			AbstractLinkedIterator(E start) {
				cursor = start;
			}

			public boolean hasNext() {
				return (cursor != null);
			}

			public E next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				E e = cursor;
				cursor = computeNext();
				return e;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Retrieves the next element to traverse to or <tt>null</tt> if
			 * there are no more elements.
			 */
			abstract E computeNext();
		}
	}

	/**
	 * An element that is linked on the {@link Deque}.
	 */
	interface Linked<T extends Linked<T>> {

		/**
		 * Retrieves the previous element or <tt>null</tt> if either the element
		 * is unlinked or the first element on the deque.
		 */
		T getPrevious();

		/** Sets the previous element or <tt>null</tt> if there is no link. */
		void setPrevious(T prev);

		/**
		 * Retrieves the next element or <tt>null</tt> if either the element is
		 * unlinked or the last element on the deque.
		 */
		T getNext();

		/** Sets the next element or <tt>null</tt> if there is no link. */
		void setNext(T next);
	}

	static interface EntryWeigher<K, V> {

		/**
		 * Measures an entry's weight to determine how many units of capacity
		 * that the key and value consumes. An entry must consume a minimum of
		 * one unit.
		 * 
		 * @param key
		 *			the key to weigh
		 * @param value
		 *			the value to weigh
		 * @return the entry's weight
		 */
		int weightOf(K key, V value);
	}

	static interface EvictionListener<K, V> {

		/**
		 * A call-back notification that the entry was evicted.
		 * 
		 * @param key
		 *			the entry's key
		 * @param value
		 *			the entry's value
		 */
		void onEviction(K key, V value);
	}

	static interface Weigher<V> {

		/**
		 * Measures an object's weight to determine how many units of capacity
		 * that the value consumes. A value must consume a minimum of one unit.
		 * 
		 * @param value
		 *			the object to weigh
		 * @return the object's weight
		 */
		int weightOf(V value);
	}

	static final class Weighers {

		private Weighers() {
			throw new AssertionError();
		}

		/**
		 * A entry weigher backed by the specified weigher. The weight of the
		 * value determines the weight of the entry.
		 * 
		 * @param weigher
		 *			the weigher to be "wrapped" in a entry weigher.
		 * @return A entry weigher view of the specified weigher.
		 */
		public static <K, V> EntryWeigher<K, V> asEntryWeigher(
				final Weigher<? super V> weigher) {
			return (weigher == singleton()) ? Weighers.<K, V> entrySingleton()
					: new EntryWeigherView<K, V>(weigher);
		}

		/**
		 * A weigher where an entry has a weight of <tt>1</tt>. A map bounded
		 * with this weigher will evict when the number of key-value pairs
		 * exceeds the capacity.
		 * 
		 * @return A weigher where a value takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <K, V> EntryWeigher<K, V> entrySingleton() {
			return (EntryWeigher<K, V>) SingletonEntryWeigher.INSTANCE;
		}

		/**
		 * A weigher where a value has a weight of <tt>1</tt>. A map bounded
		 * with this weigher will evict when the number of key-value pairs
		 * exceeds the capacity.
		 * 
		 * @return A weigher where a value takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <V> Weigher<V> singleton() {
			return (Weigher<V>) SingletonWeigher.INSTANCE;
		}

		/**
		 * A weigher where the value is a byte array and its weight is the
		 * number of bytes. A map bounded with this weigher will evict when the
		 * number of bytes exceeds the capacity rather than the number of
		 * key-value pairs in the map. This allows for restricting the capacity
		 * based on the memory-consumption and is primarily for usage by
		 * dedicated caching servers that hold the serialized data.
		 * <p>
		 * A value with a weight of <tt>0</tt> will be rejected by the map. If a
		 * value with this weight can occur then the caller should eagerly
		 * evaluate the value and treat it as a removal operation.
		 * Alternatively, a custom weigher may be specified on the map to assign
		 * an empty value a positive weight.
		 * 
		 * @return A weigher where each byte takes one unit of capacity.
		 */
		public static Weigher<byte[]> byteArray() {
			return ByteArrayWeigher.INSTANCE;
		}

		/**
		 * A weigher where the value is a {@link Iterable} and its weight is the
		 * number of elements. This weigher only should be used when the
		 * alternative {@link #collection()} weigher cannot be, as evaluation
		 * takes O(n) time. A map bounded with this weigher will evict when the
		 * total number of elements exceeds the capacity rather than the number
		 * of key-value pairs in the map.
		 * <p>
		 * A value with a weight of <tt>0</tt> will be rejected by the map. If a
		 * value with this weight can occur then the caller should eagerly
		 * evaluate the value and treat it as a removal operation.
		 * Alternatively, a custom weigher may be specified on the map to assign
		 * an empty value a positive weight.
		 * 
		 * @return A weigher where each element takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <E> Weigher<? super Iterable<E>> iterable() {
			return (Weigher<Iterable<E>>) (Weigher<?>) IterableWeigher.INSTANCE;
		}

		/**
		 * A weigher where the value is a {@link Collection} and its weight is
		 * the number of elements. A map bounded with this weigher will evict
		 * when the total number of elements exceeds the capacity rather than
		 * the number of key-value pairs in the map.
		 * <p>
		 * A value with a weight of <tt>0</tt> will be rejected by the map. If a
		 * value with this weight can occur then the caller should eagerly
		 * evaluate the value and treat it as a removal operation.
		 * Alternatively, a custom weigher may be specified on the map to assign
		 * an empty value a positive weight.
		 * 
		 * @return A weigher where each element takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <E> Weigher<? super Collection<E>> collection() {
			return (Weigher<Collection<E>>) (Weigher<?>) CollectionWeigher.INSTANCE;
		}

		/**
		 * A weigher where the value is a {@link List} and its weight is the
		 * number of elements. A map bounded with this weigher will evict when
		 * the total number of elements exceeds the capacity rather than the
		 * number of key-value pairs in the map.
		 * <p>
		 * A value with a weight of <tt>0</tt> will be rejected by the map. If a
		 * value with this weight can occur then the caller should eagerly
		 * evaluate the value and treat it as a removal operation.
		 * Alternatively, a custom weigher may be specified on the map to assign
		 * an empty value a positive weight.
		 * 
		 * @return A weigher where each element takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <E> Weigher<? super List<E>> list() {
			return (Weigher<List<E>>) (Weigher<?>) ListWeigher.INSTANCE;
		}

		/**
		 * A weigher where the value is a {@link Set} and its weight is the
		 * number of elements. A map bounded with this weigher will evict when
		 * the total number of elements exceeds the capacity rather than the
		 * number of key-value pairs in the map.
		 * <p>
		 * A value with a weight of <tt>0</tt> will be rejected by the map. If a
		 * value with this weight can occur then the caller should eagerly
		 * evaluate the value and treat it as a removal operation.
		 * Alternatively, a custom weigher may be specified on the map to assign
		 * an empty value a positive weight.
		 * 
		 * @return A weigher where each element takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <E> Weigher<? super Set<E>> set() {
			return (Weigher<Set<E>>) (Weigher<?>) SetWeigher.INSTANCE;
		}

		/**
		 * A weigher where the value is a {@link Map} and its weight is the
		 * number of entries. A map bounded with this weigher will evict when
		 * the total number of entries across all values exceeds the capacity
		 * rather than the number of key-value pairs in the map.
		 * <p>
		 * A value with a weight of <tt>0</tt> will be rejected by the map. If a
		 * value with this weight can occur then the caller should eagerly
		 * evaluate the value and treat it as a removal operation.
		 * Alternatively, a custom weigher may be specified on the map to assign
		 * an empty value a positive weight.
		 * 
		 * @return A weigher where each entry takes one unit of capacity.
		 */
		@SuppressWarnings({ "cast", "unchecked" })
		public static <A, B> Weigher<? super Map<A, B>> map() {
			return (Weigher<Map<A, B>>) (Weigher<?>) MapWeigher.INSTANCE;
		}

		static final class EntryWeigherView<K, V> implements
				EntryWeigher<K, V>, Serializable {
			static final long serialVersionUID = 1;
			final Weigher<? super V> weigher;

			EntryWeigherView(Weigher<? super V> weigher) {
				checkNotNull(weigher);
				this.weigher = weigher;
			}

			public int weightOf(K key, V value) {
				return weigher.weightOf(value);
			}
		}

		enum SingletonEntryWeigher implements EntryWeigher<Object, Object> {
			INSTANCE;

			public int weightOf(Object key, Object value) {
				return 1;
			}
		}

		enum SingletonWeigher implements Weigher<Object> {
			INSTANCE;

			public int weightOf(Object value) {
				return 1;
			}
		}

		enum ByteArrayWeigher implements Weigher<byte[]> {
			INSTANCE;

			public int weightOf(byte[] value) {
				return value.length;
			}
		}

		enum IterableWeigher implements Weigher<Iterable<?>> {
			INSTANCE;

			public int weightOf(Iterable<?> values) {
				if (values instanceof Collection<?>) {
					return ((Collection<?>) values).size();
				}
				int size = 0;
				for (Iterator<?> i = values.iterator(); i.hasNext();) {
					i.next();
					size++;
				}
				return size;
			}
		}

		enum CollectionWeigher implements Weigher<Collection<?>> {
			INSTANCE;

			public int weightOf(Collection<?> values) {
				return values.size();
			}
		}

		enum ListWeigher implements Weigher<List<?>> {
			INSTANCE;

			public int weightOf(List<?> values) {
				return values.size();
			}
		}

		enum SetWeigher implements Weigher<Set<?>> {
			INSTANCE;

			public int weightOf(Set<?> values) {
				return values.size();
			}
		}

		enum MapWeigher implements Weigher<Map<?, ?>> {
			INSTANCE;

			public int weightOf(Map<?, ?> values) {
				return values.size();
			}
		}
	}
}