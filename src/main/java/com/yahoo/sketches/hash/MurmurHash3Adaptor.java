/*
 * Copyright 2015, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */
package com.yahoo.sketches.hash;

import static com.yahoo.sketches.Util.ceilingPowerOf2;
import static com.yahoo.sketches.hash.MurmurHash3.hash;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;

/**
 * A general purpose wrapper for the MurmurHash3.
 * <ul>
 * <li>Inputs can be long, long[], int[], byte[], double or String.</li>
 * <li>Returns null if arrays or String is null or empty.</li>
 * <li>Provides methods for returning the 128-bit result as either an array of 2 longs or as a byte
 * array of 16 bytes.</li>
 * <li>Provides modulo, asDouble and asInt functions.</li>
 * </ul>
 * 
 * @author Lee Rhodes
 */
public final class MurmurHash3Adaptor {
  private static final long BIT62 = 1L << 62;
  private static final long MAX_LONG = Long.MAX_VALUE;
  private static final long INT_MASK = 0x7FFFFFFFL;
  private static final long PRIME = 9219741426499971445L; //from P. L'Ecuyer and R. Simard 
  
  private MurmurHash3Adaptor() {}
  
  /**
   * Hash a long and long seed.
   * 
   * @param datum the input long value
   * @param seed A long valued seed.
   * @return The 128-bit hash as a byte[16] as 2 64-bit longs in Big Endian order.
   */
  public static byte[] hashToBytes(long datum, long seed) {
    long[] data = { datum };
    return toByteArray(hash(data, seed));
  }
  
  /**
   * Hash a long[] and long seed.
   * 
   * @param data the input long array
   * @param seed A long valued seed.
   * @return The 128-bit hash as a byte[16] as 2 64-bit longs in Big Endian order.
   */
  public static byte[] hashToBytes(long[] data, long seed) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    return toByteArray(hash(data, seed));
  }
  
  /**
   * Hash an int[] and long seed.
   * 
   * @param data the input int array
   * @param seed A long valued seed.
   * @return The 128-bit hash as a byte[16] as 2 64-bit longs in Big Endian order.
   */
  public static byte[] hashToBytes(int[] data, long seed) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    return toByteArray(hash(data, seed));
  }
  
  /**
   * Hash a byte[] and long seed.
   * 
   * @param data the input byte array
   * @param seed A long valued seed.
   * @return The 128-bit hash as a byte[16] as 2 64-bit longs in Big Endian order.
   */
  public static byte[] hashToBytes(byte[] data, long seed) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    return toByteArray(hash(data, seed));
  }
  
  /**
   * Hash a double and long seed.
   * 
   * @param datum the input double
   * @param seed A long valued seed.
   * @return The 128-bit hash as a byte[16] as 2 64-bit longs in Big Endian order.
   */
  public static byte[] hashToBytes(double datum, long seed) {
    double d = (datum == 0.0) ? 0.0 : datum; //canonicalize -0.0, 0.0
    long[] data = { Double.doubleToLongBits(d) }; //canonicalize all NaN forms
    return toByteArray(hash(data, seed));
  }
  
  /**
   * Hash a String and long seed.
   * 
   * @param datum the input String
   * @param seed A long valued seed.
   * @return The 128-bit hash as a byte[16] as 2 64-bit longs in Big Endian order.
   */
  public static byte[] hashToBytes(String datum, long seed) {
    if ((datum == null) || datum.isEmpty()) {
      return null;
    }
    byte[] data = datum.getBytes(UTF_8);
    return toByteArray(hash(data, seed));
  }
  
  /**
   * Hash a long and long seed.
   * 
   * @param datum the input long
   * @param seed A long valued seed.
   * @return The 128-bit hash as a long[2].
   */
  public static long[] hashToLongs(long datum, long seed) {
    long[] data = { datum };
    return hash(data, seed);
  }
  
  /**
   * Hash a long[] and long seed.
   * 
   * @param data the input long array.
   * @param seed A long valued seed.
   * @return The 128-bit hash as a long[2].
   */
  public static long[] hashToLongs(long[] data, long seed) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    return hash(data, seed);
  }
  
  /**
   * Hash a int[] and long seed.
   * 
   * @param data the input int array.
   * @param seed A long valued seed.
   * @return The 128-bit hash as a long[2].
   */
  public static long[] hashToLongs(int[] data, long seed) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    return hash(data, seed);
  }
  
  /**
   * Hash a byte[] and long seed.
   * 
   * @param data the input byte array.
   * @param seed A long valued seed.
   * @return The 128-bit hash as a long[2].
   */
  public static long[] hashToLongs(byte[] data, long seed) {
    if ((data == null) || (data.length == 0)) {
      return null;
    }
    return hash(data, seed);
  }
  
  /**
   * Hash a double and long seed.
   * 
   * @param datum the input double.
   * @param seed A long valued seed.
   * @return The 128-bit hash as a long[2].
   */
  public static long[] hashToLongs(double datum, long seed) {
    double d = (datum == 0.0) ? 0.0 : datum; //canonicalize -0.0, 0.0
    long[] data = { Double.doubleToLongBits(d) };//canonicalize all NaN forms
    return hash(data, seed);
  }
  
  /**
   * Hash a String and long seed.
   * 
   * @param datum the input String.
   * @param seed A long valued seed.
   * @return The 128-bit hash as a long[2].
   */
  public static long[] hashToLongs(String datum, long seed) {
    if ((datum == null) || datum.isEmpty()) {
      return null;
    }
    byte[] data = datum.getBytes(UTF_8);
    return hash(data, seed);
  }
  
  //As Integer functions
  
  /**
   * Returns a deterministic uniform random integer between zero (inclusive) and
   * n (exclusive) given the input data.
   * @param data the input long array.
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer
   */
  public static int asInt(long[] data, int n) {
    if ((data == null) || (data.length == 0)) {
      throw new IllegalArgumentException("Input is null or empty.");
    }
    return asInteger(data, n); //data is long[]
  }
  
  /**
   * Returns a deterministic uniform random integer between zero (inclusive) and
   * n (exclusive) given the input data.
   * @param data the input int array.
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer
   */
  public static int asInt(int[] data, int n) {
    if ((data == null) || (data.length == 0)) {
      throw new IllegalArgumentException("Input is null or empty.");
    }    
    return asInteger(toLongArray(data), n); //data is int[]
  }
  
  /**
   * Returns a deterministic uniform random integer between zero (inclusive) and
   * n (exclusive) given the input data.
   * @param data the input byte array.
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer.
   */
  public static int asInt(byte[] data, int n) {
    if ((data == null) || (data.length == 0)) {
      throw new IllegalArgumentException("Input is null or empty.");
    }
    return asInteger(toLongArray(data), n); //data is byte[]
  }
  
  /**
   * Returns a deterministic uniform random integer between zero (inclusive) and
   * n (exclusive) given the input datum.
   * @param datum the input long
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer
   */
  public static int asInt(long datum, int n) {
    long[] data = { datum };
    return asInteger(data, n); //data is long[]
  }
  
  /**
   * Returns a deterministic uniform random integer between zero (inclusive) and
   * n (exclusive) given the input double.
   * @param datum the given double.
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer
   */
  public static int asInt(double datum, int n) {
    double d = (datum == 0.0) ? 0.0 : datum; //canonicalize -0.0, 0.0
    long[] data = { Double.doubleToLongBits(d) };//canonicalize all NaN forms
    return asInteger(data, n); //data is long[]
  }
  
  /**
   * Returns a deterministic uniform random integer between zero (inclusive) and
   * n (exclusive) given the input datum.
   * @param datum the given String.
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer
   */
  public static int asInt(String datum, int n) {
    if ((datum == null) || datum.isEmpty()) {
      throw new IllegalArgumentException("Input is null or empty.");
    }
    byte[] data = datum.getBytes(UTF_8);
    return asInteger(toLongArray(data), n); //data is byte[]
  }
  
  /**
   * Returns a deterministic uniform random integer with a minimum inclusive value of zero and a 
   * maximum exclusive value of n given the input data.
   * <p>
   * The integer values produced are only as random as the MurmurHash3 algorithm, which may be
   * adequate for many applications. However, if you are looking for high guarantees of randomness
   * you should turn to more sophisticated random generators such as Mersenne Twister or Well19937c
   * algorithms.
   * 
   * @param data The input data (key)
   * @param n The upper exclusive bound of the integers produced. Must be &gt; 1.
   * @return deterministic uniform random integer
   */
  private static int asInteger(long[] data, int n) {
    int t;
    int cnt = 0;
    long seed = 0;
    if (n < 2) throw new IllegalArgumentException("Given value of n must be &gt; 1.");
    if (n > (1 << 30)) {
      while (++cnt < 10000) {
        long[] h = MurmurHash3.hash(data, seed);
        t = (int) (h[0] & INT_MASK);
        if (t < n) {
          return t;
        }
        t = (int) ((h[0] >>> 33));
        if (t < n) {
          return t;
        }
        t = (int) (h[1] & INT_MASK);
        if (t < n) {
          return t;
        }
        t = (int) ((h[1] >>> 33));
        if (t < n) {
          return t;
        }
        seed += PRIME;
      } // end while
      throw new IllegalStateException(
          "Internal Error: Failed to find integer &lt; n within 10000 iterations.");
    }
    long mask = ceilingPowerOf2(n) - 1;
    while (++cnt < 10000) {
      long[] h = MurmurHash3.hash(data, seed);
      t = (int) (h[0] & mask);
      if (t < n) {
        return t;
      }
      t = (int) ((h[0] >>> 33) & mask);
      if (t < n) {
        return t;
      }
      t = (int) (h[1] & mask);
      if (t < n) {
        return t;
      }
      t = (int) ((h[1] >>> 33) & mask);
      if (t < n) {
        return t;
      }
      seed += PRIME;
    } // end while
    throw new IllegalStateException(
        "Internal Error: Failed to find integer &lt; n within 10000 iterations.");
  }
  
  /**
   * Returns a uniform random double with a minimum inclusive value of zero and a maximum exclusive
   * value of 1.0.
   * <p>
   * The double values produced are only as random as the MurmurHash3 algorithm, which may be
   * adequate for many applications. However, if you are looking for high guarantees of randomness
   * you should turn to more sophisticated random generators such as Mersenne Twister or Well
   * algorithms.
   * 
   * @param hash The output of the MurmurHash3.
   * @return the uniform random double.
   */
  public static double asDouble(long[] hash) {
    return (hash[0] >>> 12) * 0x1.0p-52d;
  }
  
  /**
   * Returns the remainder from the modulo division of the 128-bit output of the murmurHash3 by the
   * divisor.
   * 
   * @param h0 The lower 64-bits of the 128-bit MurmurHash3 hash.
   * @param h1 The upper 64-bits of the 128-bit MurmurHash3 hash.
   * @param divisor Must be positive and greater than zero.
   * @return the modulo result.
   */
  public static int modulo(long h0, long h1, int divisor) {
    long d = divisor;
    long modH0 = (h0 < 0L) ? addRule(mulRule(BIT62, 2L, d), (h0 & MAX_LONG), d) : h0 % d;
    long modH1 = (h1 < 0L) ? addRule(mulRule(BIT62, 2L, d), (h1 & MAX_LONG), d) : h1 % d;
    long modTop = mulRule(mulRule(BIT62, 4L, d), modH1, d);
    return (int) addRule(modTop, modH0, d);
  }
  
  /**
   * Returns the remainder from the modulo division of the 128-bit output of the murmurHash3 by the
   * divisor.
   * 
   * @param hash The size 2 long array from the MurmurHash3.
   * @param divisor Must be positive and greater than zero.
   * @return the modulo result
   */
  public static int modulo(long[] hash, int divisor) {
    return modulo(hash[0], hash[1], divisor);
  }
  
  private static long addRule(long a, long b, long d) {
    return ((a % d) + (b % d)) % d;
  }
  
  private static long mulRule(long a, long b, long d) {
    return ((a % d) * (b % d)) % d;
  }
  
  private static byte[] toByteArray(long[] hash) { //Assumes Big Endian
    byte[] bArr = new byte[16];
    ByteBuffer bb = ByteBuffer.wrap(bArr);
    bb.putLong(hash[0]);
    bb.putLong(hash[1]);
    return bArr;
  }
  
  private static long[] toLongArray(byte[] data) {
    int dataLen = data.length;
    int longLen = (dataLen+7)/8;
    long[] longArr = new long[longLen];
    for (int bi=0; bi<dataLen; bi++) {
      int li = bi/8;
      longArr[li] |= (((long)data[bi]) << (bi*8) % 64);
    }
    return longArr;
  }
  
  private static long[] toLongArray(int[] data) {
    int dataLen = data.length;
    int longLen = (dataLen+1)/2;
    long[] longArr = new long[longLen];
    for (int ii=0; ii<dataLen; ii++) {
      int li = ii/2;
      longArr[li] |= (((long)data[ii]) << (ii*32) % 64);
    }
    return longArr;
  }
  
}