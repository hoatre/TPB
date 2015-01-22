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
package storm.tpb.testing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class provides per-slot counts of the occurrences of objects.
 * <p/>
 * It can be used, for instance, as a building block for implementing sliding window counting of objects.
 *
 * @param <T> The type of those objects we want to count.
 */
public final class SlotBasedCounterTotal<T> implements Serializable {

  private static final long serialVersionUID = 4858185737378394432L;

  private final Map<Transaction, long[]> objToSummaryTotal = new HashMap<Transaction, long[]>();
  private final int numSlots;

  public SlotBasedCounterTotal(int numSlots) {
    if (numSlots <= 0) {
      throw new IllegalArgumentException("Number of slots must be greater than zero (you requested " + numSlots + ")");
    }
    this.numSlots = numSlots;
  }

  public void incrementCountTotal(Transaction obj, int slot) {
    long[] countsTotal = objToSummaryTotal.get(obj);
    if (countsTotal == null) {
      countsTotal = new long[this.numSlots];
      objToSummaryTotal.put(obj, countsTotal);
    }
    countsTotal[slot]++;
    System.out.println("total count : " + String.valueOf(countsTotal[slot]));
  }

  public long getCount(T obj, int slot) {
    long[] counts = objToSummaryTotal.get(obj);
    if (counts == null) {
      return 0;
    }
    else {
      return counts[slot];
    }
  }

  public Map<Transaction, Long> getCounts() {
    Map<Transaction, Long> result = new HashMap<Transaction, Long>();
    for (Transaction obj : objToSummaryTotal.keySet()) {
      result.put(obj, computeTotalCount(obj));
    }
    return result;
  }

  private long computeTotalCount(Transaction obj) {
    long[] curr = objToSummaryTotal.get(obj);
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
    for (Transaction obj : objToSummaryTotal.keySet()) {
      resetSlotCountToZero(obj, slot);
    }
  }

  private void resetSlotCountToZero(Transaction obj, int slot) {
    long[] counts = objToSummaryTotal.get(obj);
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
    for (Transaction obj : objToSummaryTotal.keySet()) {
      if (shouldBeRemovedFromCounter(obj)) {
        objToBeRemoved.add(obj);
      }
    }
    for (Transaction obj : objToBeRemoved) {
      objToSummaryTotal.remove(obj);
    }
  }

}
