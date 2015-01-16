package storm.tpb.testing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by HieuLD on 1/15/15.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * This class provides per-slot counts of the occurrences of objects.
 * <p/>
 * It can be used, for instance, as a building block for implementing sliding window counting of objects.
 *
 */
public final class SlotBasedSummary implements Serializable {

    private static final long serialVersionUID = 4858185737378394432L;

    private final Map<Transaction, long[]> objToSummary = new HashMap<Transaction, long[]>();
    private final int numSlots;

    public SlotBasedSummary(int numSlots) {
        if (numSlots <= 0) {
            throw new IllegalArgumentException("Number of slots must be greater than zero (you requested " + numSlots + ")");
        }
        this.numSlots = numSlots;
    }

    public void incrementAmount(Transaction obj, int slot) {
        long[] sums = objToSummary.get(obj);
        if (sums == null) {
            sums = new long[this.numSlots];
            objToSummary.put(obj, sums);
        }
        sums[slot] = sums[slot] + obj.getamount();
    }

    public long getCount(Transaction obj, int slot) {
        long[] counts = objToSummary.get(obj);
        if (counts == null) {
            return 0;
        }
        else {
            return counts[slot];
        }
    }

    public Map<Transaction, Long> getCounts() {
        Map<Transaction, Long> result = new HashMap<Transaction, Long>();
        for (Transaction obj : objToSummary.keySet()) {
            result.put(obj, computeTotalCount(obj));
        }
        return result;
    }

    public Map<Transaction, Long> getTotal(){
        Map<Transaction, Long> result = new HashMap<Transaction, Long>();
        long total = 0;
        for(Transaction obj : objToSummary.keySet())
        {
            total += computeTotalSum(obj);
            result.put(obj, total);
        }

        return result;
    }

    private long computeTotalSum(Transaction obj) {
        long[] curr = objToSummary.get(obj);
        long total = 0;
        for (long l : curr) {
            total += l;
        }
        return total;
    }

    private long computeTotalCount(Transaction obj) {
        long[] curr = objToSummary.get(obj);
        long total = 0;
        for (long l : curr) {
            total += l;
        }
        return total;
    }


    /**
     * Reset the slot count of any tracked objects to zero for the given slot.
     *
     * @param slot
     */
    public void wipeSlot(int slot) {
        for (Transaction obj : objToSummary.keySet()) {
            resetSlotCountToZero(obj, slot);
        }
    }

    private void resetSlotCountToZero(Transaction obj, int slot) {
        long[] counts = objToSummary.get(obj);
        counts[slot] = 0;
    }

    private boolean shouldBeRemovedFromCounter(Transaction obj) {
        return computeTotalCount(obj) == 0;
    }

    /**
     * Remove any object from the counter whose total count is zero (to free up memory).
     */
    public void wipeZeros() {
        Set<Transaction> objToBeRemoved = new HashSet<Transaction>();
        for (Transaction obj : objToSummary.keySet()) {
            if (shouldBeRemovedFromCounter(obj)) {
                objToBeRemoved.add(obj);
            }
        }
        for (Transaction obj : objToBeRemoved) {
            objToSummary.remove(obj);
        }
    }

}
