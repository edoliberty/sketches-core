package com.yahoo.sketches.counting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import gnu.trove.map.hash.TLongLongHashMap;


/**
 * This is a utility class that implements (and abstracts) a set of positive counters. 
 * The mapping is  Long key -> Long count.
 * The default value of any key is zero and no negative counters are allowed. 
 * Non-positive mappings are deleted. 
 * 
 * It also support incrementing individual counters and decrementing all counters simultaneously.
 * This is a convenient and efficient modification intended to be used in FrequentDirection sketching.
 * 
 * @author edo 
 */
public class PositiveCountersMap{
  private TLongLongHashMap counters;
  
  //private HashMap<Long,Long> counters;
  private long offset;
  private long nnz;
  final private double MIN_FRACTION_OF_POSITIVES = 0.5;
  final private int MIN_SIZE_TO_REDUCE = 100;

  
  /**
   * Creates empty mappings and default offset = 0.
   */
  public PositiveCountersMap(){
    //counters = new HashMap<Long,Long>();
    counters = new TLongLongHashMap();
    offset = 0L;
  }
  
  /**
   * @return the number of positive counters
   */
  public long nnz(){
    return nnz;
  }
  
  /**
   * @return an iterator over the positive count values 
   */
  public long[] values(){
    removeNegativeCounters();
    return counters.values();
    
  }
  
  /**
   * @return an iterator over the keys corresponding to positive counts only
   */
  public long[] keys(){
    removeNegativeCounters();
    return counters.keys();
  }
  
  /**
   * @param key should not be null.
   * @return the exact count for that key.
   */
  public long get(long key){
    Long val = counters.get(key);
    return (val > offset) ? val - offset: 0L;
  }
  
  /**
   * @param key whose count needs to be set to a different value
   * @param value of new count for the key and cannot be negative.
   */
  public void put(long key, long value){
    if (value < 0) throw new IllegalArgumentException("Received negative value.");
    if (value == 0) counters.remove(key);
    counters.put(key, get(key) + value + offset); 
  }
  
  /**
   * @param key whose count should be incremented. If a counter
   * does not exist for key it is created.
   * @param delta the amount by which the value should be increased.
   * The variable delta cannot be negative.
   */
  public void increment(long key, long delta){
    if (delta < 0) throw new IllegalArgumentException("Received negative value for delta.");
    if (delta == 0) return;
    long value =  get(key);
    if (value == 0) nnz++;
    counters.put(key, value + delta + offset);
  }
  
  /**
   * @param key whose count should be incremented by 1. 
   * If a counter does not exist for key it is created.
   */
  public void increment(long key){
    increment(key, 1L);
  }
  
  /**
   * @param other another PositiveCountersMap
   * All counters of shared keys are summed up.
   * Keys only in the other PositiveCountersMap receive new counts. 
   */
  public void increment(PositiveCountersMap other){
    removeNegativeCounters();
    for (long key : other.counters.keys()) {
      long delta = other.get(key);
      if(delta > 0) {
        increment(key, delta);
      }
    }
    nnz = counters.size();
  }
  
  /**
   * @param delta the value by which all counts should be decremented.
   * The value of delta cannot be negative.
   */
  public void decerementAll(long delta){
    if (delta < 0) throw new IllegalArgumentException("Received negative value for delta.");
    if (delta == 0) return;
    offset += delta;
    int nnzNow = 0;
    for (long value : counters.values()){
      if (value > offset) nnzNow++;
    }
    nnz = nnzNow;
    int sizeNow = counters.size();
    if (sizeNow > MIN_SIZE_TO_REDUCE && (double)nnz/sizeNow < MIN_FRACTION_OF_POSITIVES) {
      removeNegativeCounters();
    }
  }
  
  /**
   * decreases all counts by 1.
   */
  public void decerementAll(){
    decerementAll(1L);
  }
  
  /**
   * This is an internal function that cleans up non-positive counts and frees up space.
   */
  private void removeNegativeCounters(){
    long numKeysToRemove = counters.size() - nnz;
    long[] keysToRemove = new long[(int)numKeysToRemove];
    int i=0;
    for (long key : counters.keys()) {
      if (counters.get(key) <= offset) {
        keysToRemove[i] = key;
        i++;
      }
    }
    for (long key : keysToRemove) {
      counters.remove(key);
    }
    assert(counters.size() == nnz);
  }
  
}
