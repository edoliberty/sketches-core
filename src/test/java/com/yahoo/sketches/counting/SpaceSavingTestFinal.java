package com.yahoo.sketches.counting;

import org.testng.annotations.Test;
import org.testng.Assert;
import java.lang.Math;

/**
 * Tests SpaceSaving class
 * 
 * @author Justin8712
 * 
 */
public class SpaceSavingTestFinal {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void construct() {
    int size = 100;
    SpaceSaving spacesaving = new SpaceSaving(size);
    Assert.assertNotNull(spacesaving);
    // Should throw exception
    spacesaving = new SpaceSaving(-134);
  }

  @Test
  public void updateOneTime() {
    int size = 100;
    SpaceSaving spacesaving = new SpaceSaving(size);
    spacesaving.update(13L);
    Assert.assertEquals(spacesaving.nnz(), 1);
  }
  
  @Test
  public void sizeDoesNotGrow() {
    int maxSize = 100;
    SpaceSaving spacesaving = new SpaceSaving(maxSize);
    for (long key=0L; key<10000L; key++){
      spacesaving.update(key, 1);
      Assert.assertTrue(spacesaving.nnz() <= maxSize);
    }
  }
  
  @Test
  public void estimatesAreCorectBeofreDeletePhase() {
    int maxSize = 100;
    SpaceSaving spacesaving = new SpaceSaving(maxSize);
    for (long key=0L; key<99L; key++){
      spacesaving.update(key);
      Assert.assertTrue(spacesaving.get(key) == 1);
      Assert.assertTrue(spacesaving.getMaxError() == 0);
    }
  }

  /**
   * @param prob the probability of success for the geometric distribution. 
   * @return a random number generated from the geometric distribution.
   */
  static private long randomGeometricDist(double prob){
    assert(prob > 0.0 && prob < 1.0);
    return (long) (Math.log(Math.random()) / Math.log(1.0 - prob));
  }
  
  @Test
  public void testRandomGeometricDist() {
    long maxKey = 0L;
    double prob = .1;
    for (int i=0; i<100; i++){
      long key = randomGeometricDist(prob) ;
      if (key > maxKey) maxKey = key;
      // If you succeed with probability p the probability 
      // of failing 20/p times is smaller than 1/2^20.
      Assert.assertTrue(maxKey < 20.0/prob);
    }
  }
   
  @Test
  public void realCountsInBounds() {
    int n = 4213;
    int maxSize = 50;
    long key;
    double prob = .04; 
    SpaceSaving spacesaving = new SpaceSaving(maxSize);
    PositiveCountersMap realCounts = new PositiveCountersMap();
    for (int i=0; i<n; i++){   
      key = randomGeometricDist(prob);
      spacesaving.update(key);
      realCounts.increment(key);
      long realCount = realCounts.get(key);
      long upperBound = spacesaving.get(key);
      long lowerBound = spacesaving.get(key) - spacesaving.getMaxError();
      Assert.assertTrue(upperBound >=  realCount && realCount >= lowerBound);   
    }
  }
  
  @Test
  public void errorWithinLimits() {
    int n = 100;
    int maxSize = 20;
    long key;
    double prob = .1;

    SpaceSaving spacesaving = new SpaceSaving(maxSize);
    for (int i=0; i<n; i++){
      key = randomGeometricDist(prob);
      spacesaving.update(key);
      long upperBound = spacesaving.get(key);
      long lowerBound = spacesaving.get(key) - spacesaving.getMaxError();
      Assert.assertTrue(upperBound - lowerBound <= i/maxSize);  
      
      key = randomGeometricDist(prob);
      upperBound = spacesaving.get(key);
      lowerBound = spacesaving.get(key) - spacesaving.getMaxError();
      Assert.assertTrue(upperBound - lowerBound <= i/maxSize);  
      
    }
  } 
    
  @Test
  public void realCountsInBoundsAfterUnion() {
    int n = 1000;
    int maxSize1 = 100;
    int maxSize2 = 400;
    double prob1 = .01;
    double prob2 = .005;
   
    PositiveCountersMap realCounts = new PositiveCountersMap();
    SpaceSaving spacesaving1 = new SpaceSaving(maxSize1);
    SpaceSaving spacesaving2 = new SpaceSaving(maxSize2);
    for (int i=0; i<n; i++){
      long key1 = randomGeometricDist(prob1);
      long key2 = randomGeometricDist(prob2);
      
      spacesaving1.update(key1);
      spacesaving2.update(key2);
      
      // Updating the real counters
      realCounts.increment(key1);
      realCounts.increment(key2);
    }
    SpaceSaving spacesaving = spacesaving1.union(spacesaving2);

    for ( long key : realCounts.keys()){
      
      long realCount = realCounts.get(key);
      long upperBound = spacesaving.get(key);
      long lowerBound = spacesaving.get(key) - spacesaving.getMaxError();

      Assert.assertTrue(upperBound >=  realCount && realCount >= lowerBound);
    }
  }
  
  @Test
  public void stressTestUpdateTime() {
    int n = 1000000;
    int maxSize = 1000;  
    SpaceSaving spacesaving = new SpaceSaving(maxSize);
    double prob = 1.0/n;
    final long startTime = System.currentTimeMillis();
    for (int i=0; i<n; i++){
      long key = randomGeometricDist(prob);
      spacesaving.update(key);
    }
    final long endTime = System.currentTimeMillis();
    double timePerUpdate = (double)(endTime-startTime)/(double)n;
    System.out.println("Amortized time per update: " + timePerUpdate);
    Assert.assertTrue(timePerUpdate < 10E-3);
  }

}
