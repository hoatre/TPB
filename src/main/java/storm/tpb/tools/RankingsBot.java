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
package storm.tpb.tools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class RankingsBot implements Serializable {

  private static final long serialVersionUID = -1549827195410578903L;
  private static final int DEFAULT_COUNT = 10;

  private final int maxSize;
  private final List<RankableBot> rankedItemsBot = Lists.newArrayList();

  public RankingsBot() {
    this(DEFAULT_COUNT);
  }

  public RankingsBot(int topN) {
    if (topN < 1) {
      throw new IllegalArgumentException("topN must be >= 1");
    }
    maxSize = topN;
  }

  /**
   * Copy constructor.
   * @param other
   */
  public RankingsBot(RankingsBot other) {
    this(other.maxSize());
    updateWith(other);
  }

  /**
   * @return the maximum possible number (size) of ranked objects this instance can hold
   */
  public int maxSize() {
    return maxSize;
  }

  /**
   * @return the number (size) of ranked objects this instance is currently holding
   */
  public int size() {
    return rankedItemsBot.size();
  }

  /**
   * The returned defensive copy is only "somewhat" defensive.  We do, for instance, return a defensive copy of the
   * enclosing List instance, and we do try to defensively copy any contained Rankable objects, too.  However, the
   * contract of {@link Rankable#copy()} does not guarantee that any Object's embedded within
   * a Rankable will be defensively copied, too.
   *
   * @return a somewhat defensive copy of ranked items
   */
  public List<RankableBot> getRankingsBot() {
    List<RankableBot> copy = Lists.newLinkedList();
    for (RankableBot r: rankedItemsBot) {
      copy.add(r.copy());
    }
    return ImmutableList.copyOf(copy);
  }

  public void updateWith(RankingsBot other) {
    for (RankableBot r : other.getRankingsBot()) {
      updateWith(r);
    }
  }

  public void updateWith(RankableBot r) {
    synchronized(rankedItemsBot) {
      addOrReplace(r);
      rerank();
      shrinkRankingsIfNeeded();
    }
  }

  private void addOrReplace(RankableBot r) {
    Integer rank = findRankOf(r);
    if (rank != null) {
      rankedItemsBot.set(rank, r);
    }
    else {
      rankedItemsBot.add(r);
    }
  }

  private Integer findRankOf(RankableBot r) {
    Object tag = r.getObject();
    for (int rank = 0; rank < rankedItemsBot.size(); rank++) {
      Object cur = rankedItemsBot.get(rank).getObject();
      if (cur.equals(tag)) {
        return rank;
      }
    }
    return null;
  }

  private void rerank() {
    Collections.sort(rankedItemsBot);
    Collections.reverse(rankedItemsBot);
  }

  private void shrinkRankingsIfNeeded() {
    if (rankedItemsBot.size() > maxSize) {
      rankedItemsBot.remove(maxSize);
    }
  }

  /**
   * Removes ranking entries that have a count of zero.
   */
  public void pruneZeroCounts() {
    int i = 0;
    while (i < rankedItemsBot.size()) {
      if (rankedItemsBot.get(i).getCount() == 0) {
        rankedItemsBot.remove(i);
      }
      else {
        i++;
      }
    }
  }

  public String toString() {
    return rankedItemsBot.toString();
  }

  /**
   * Creates a (defensive) copy of itself.
   */
  public RankingsBot copy() {
    return new RankingsBot(this);
  }
}