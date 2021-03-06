/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.titus.ext.cassandra.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.spectator.api.Gauge;
import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;

/**
 * This manager distributes items in a bucket by returning what bucket should be used based on the max bucket size. The balancing strategy
 * of the manager is to return the bucket with the least amount of items in it. The max bucket size is a best effort and can be breached.
 */
public class BalancedBucketManager<T> {
    private final Object mutex = new Object();
    private final int maxBucketSize;
    private Registry registry;
    private final Map<T, Integer> itemToBucket;
    private final Map<Integer, Integer> bucketSizes;

    private final AtomicInteger highestBucketIndex;
    private final AtomicInteger bucketWithLeastItems;
    private final Id bucketSizeId;

    public BalancedBucketManager(int initialBucketCount, int maxBucketSize, String metricNameRoot, Registry registry) {
        this.maxBucketSize = maxBucketSize;
        this.registry = registry;

        this.itemToBucket = new ConcurrentHashMap<>();
        this.bucketSizes = new ConcurrentHashMap<>();
        this.highestBucketIndex = new AtomicInteger();
        this.bucketWithLeastItems = new AtomicInteger();
        bucketSizeId = registry.createId(metricNameRoot + ".bucketSize");

        createInitialBuckets(initialBucketCount);
    }

    /**
     * Returns the next bucket that should be used.
     *
     * @return bucket index
     */
    public int getNextBucket() {
        return bucketWithLeastItems.get();
    }

    /**
     * Add an item to the bucket
     *
     * @param bucket
     * @param item
     */
    public void addItem(int bucket, T item) {
        addItems(bucket, Collections.singletonList(item));
    }

    /**
     * Add a list of items to the bucket
     *
     * @param bucket
     * @param items
     */
    public void addItems(int bucket, List<T> items) {
        synchronized (mutex) {
            int currentBucketSize = bucketSizes.getOrDefault(bucket, 0);
            for (T item : items) {
                itemToBucket.put(item, bucket);
            }

            int newBucketSize = currentBucketSize + items.size();
            bucketSizes.put(bucket, newBucketSize);
            updateBucketCounters();
        }
    }

    /**
     * Delete an item
     *
     * @param item
     */
    public void deleteItem(T item) {
        synchronized (mutex) {
            Integer bucket = itemToBucket.get(item);
            if (bucket != null) {
                itemToBucket.remove(item);
                Integer currentBucketSize = bucketSizes.get(bucket);
                bucketSizes.replace(bucket, currentBucketSize, currentBucketSize - 1);
                updateBucketCounters();
            }
        }
    }

    /**
     * Get all items in all buckets.
     *
     * @return unmodifiable list of the items in all buckets
     */
    public List<T> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(itemToBucket.keySet()));
    }

    /**
     * Check to see if an item exists in any of the buckets.
     *
     * @param item
     * @return true if an item exists in any bucket.
     */
    public boolean itemExists(T item) {
        return itemToBucket.containsKey(item);
    }

    /**
     * Get the bucket of an item.
     *
     * @param item
     * @return the bucket of the item or null if the item does not exist.
     */
    public int getItemBucket(T item) {
        return itemToBucket.get(item);
    }

    private void createInitialBuckets(int initialBucketCount) {
        for (int i = 0; i < initialBucketCount; i++) {
            bucketSizes.put(i, 0);
        }
        updateBucketCounters();
    }

    private void updateBucketCounters() {
        int maxBucket = 0;
        int smallestBucket = 0;
        int smallestBucketSize = maxBucketSize;
        boolean allBucketsFull = true;
        for (Map.Entry<Integer, Integer> entry : bucketSizes.entrySet()) {
            Integer bucket = entry.getKey();
            if (bucket > maxBucket) {
                maxBucket = bucket;
            }
            Integer bucketSize = entry.getValue();
            if (bucketSize < smallestBucketSize) {
                smallestBucketSize = bucketSize;
                smallestBucket = bucket;
            }
            if (bucketSize < maxBucketSize) {
                allBucketsFull = false;
            }
            updateBucketMetrics(bucket, bucketSize);
        }

        if (allBucketsFull) {
            // only create a new bucket if all existing buckets are full
            int newBucket = maxBucket + 1;
            bucketSizes.put(newBucket, 0);
            highestBucketIndex.getAndSet(newBucket);
            bucketWithLeastItems.getAndSet(newBucket);
        } else {
            highestBucketIndex.getAndSet(maxBucket);
            bucketWithLeastItems.getAndSet(smallestBucket);
        }
    }

    private void updateBucketMetrics(int bucket, int bucketSize) {
        Gauge bucketSizeGauge = registry.gauge(bucketSizeId.withTag("bucket", Integer.toString(bucket)));
        bucketSizeGauge.set(bucketSize);
    }
}
