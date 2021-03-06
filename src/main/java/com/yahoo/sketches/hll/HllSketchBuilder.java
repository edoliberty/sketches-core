/*
 * Copyright 2015, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */
package com.yahoo.sketches.hll;

import static com.yahoo.sketches.Util.DEFAULT_NOMINAL_ENTRIES;
import static com.yahoo.sketches.Util.LS;
import static com.yahoo.sketches.Util.TAB;

/**
 * 
 */
public class HllSketchBuilder { //TODO will need to add seed and Memory, etc.
  private int logBuckets;
  private Preamble preamble;
  private boolean denseMode = false;
  private boolean hipEstimator = false;
  
  public HllSketchBuilder() {
    logBuckets = Integer.numberOfTrailingZeros(DEFAULT_NOMINAL_ENTRIES);
    preamble = Preamble.createSharedPreamble((byte) logBuckets);
    denseMode = false;
    hipEstimator = false;
  }
  
  public HllSketchBuilder copy() {  //not used.  Do we need this?
    HllSketchBuilder retVal = new HllSketchBuilder();
    retVal.logBuckets = logBuckets;
    retVal.preamble = preamble;
    retVal.denseMode = denseMode;
    retVal.hipEstimator = hipEstimator;
    return retVal;
  }
  
  public HllSketchBuilder setLogBuckets(int logBuckets) {
    this.logBuckets = logBuckets;
    this.preamble = Preamble.createSharedPreamble((byte) logBuckets);
    return this;
  }
  
  public int getLogBuckets() {
    return logBuckets;
  }
  
  public HllSketchBuilder setPreamble(Preamble preamble) {
    this.preamble = preamble;
    return this;
  }
  
  public Preamble getPreamble() {
    return preamble;
  }
  
  public HllSketchBuilder setDenseMode(boolean denseMode) {
    this.denseMode = denseMode;
    return this;
  }
  
  public boolean isDenseMode() {
    return denseMode;
  }
  
  public HllSketchBuilder setHipEstimator(boolean hipEstimator) {
    this.hipEstimator = hipEstimator;
    return this;
  }
  
  public boolean isHipEstimator() {
    return hipEstimator;
  }
  
  public HllSketch build() {
    final Fields fields;
    if (denseMode) {
      fields = new OnHeapFields(preamble);
    } else {
      fields = new OnHeapHashFields(preamble);
    }
    
    if (hipEstimator) {
      return new HipHllSketch(fields);
    } else {
      return new HllSketch(fields);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("HllSketchBuilder configuration:").append(LS).
       append("LgK:").append(TAB).append(logBuckets).append(LS).
       append("K:").append(TAB).append(1 << logBuckets).append(LS).
       append("DenseMode:").append(TAB).append(denseMode).append(LS).
       append("HIP Estimator:").append(TAB).append(hipEstimator).append(LS);
    return sb.toString();
  }
  
}