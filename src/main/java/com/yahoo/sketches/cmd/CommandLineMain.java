package com.yahoo.sketches.cmd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.yahoo.sketches.theta.UpdateSketch;
import com.yahoo.sketches.frequencies.ErrorType;
import com.yahoo.sketches.frequencies.FrequentItemsSketch;
import com.yahoo.sketches.frequencies.FrequentItemsSketch.Row;
import com.yahoo.sketches.hash.MurmurHash3;
import com.yahoo.sketches.quantiles.QuantilesSketch;

public class CommandLineMain {    
    public static void main(String[] args) throws IOException {
	Boolean verboseModeOn = false;
	int k;
	// Parsing command line arguments
    	String sketchType = "uniq"; // default value
    	if (args.length > 0) { 
    	    sketchType = args[0];
    	}
	double eps = 0.1; 
	//Integer k = 1<<15; // default value
    	if (args.length > 1) {
    	    eps = Float.parseFloat(args[1]);
    	}
    	if (eps <= 0.0 || eps >= 1.0){
    	    throw new IllegalArgumentException("the error tolerance must by greater than 0 and smaller than 1.");
        }
    	
    	BufferedReader br; 
    	if (args.length > 2) {
    	    br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));	
    	} else {
    	    br = new BufferedReader(new InputStreamReader(System.in));
    	}

        String lineStr;
	if (sketchType.equals("uniq")){
            if (eps < 0.001){
        	throw new IllegalArgumentException("uniq requires the error tolerance to be larger than 0.001");
            }
            k = (int)(1.0/(eps*eps));
            k = (k < 32) ? 32 : k; // k is at least 32
    	    k = Integer.highestOneBit((k-1)<<1); // rounded up to the next power of 2
            UpdateSketch sketch = UpdateSketch.builder().build(k); 
            while ((lineStr = br.readLine()) != null) {
        	long hashValue = MurmurHash3.hash(lineStr.getBytes(), 0)[0];
        	sketch.update(hashValue);
            }
            System.out.printf("%d\t%d\t%d\n", (int)(sketch.getEstimate()), 
        	    			      (int)(sketch.getLowerBound(2)),
        	    			      (int)(sketch.getUpperBound(2)));
            if(verboseModeOn)
        	System.out.println(sketch.toString());
        } else if (sketchType.equals("freq")) {
            if (eps < 0.000001){
        	throw new IllegalArgumentException("uniq requires the error tolerance to be at least 0.000001");
            }
            k = (int)(1.0/(eps));
            k = (k < 32) ? 32 : k; // k is at least 32
    	    k = Integer.highestOneBit((k-1)<<1); // rounded up to the next power of 2
            FrequentItemsSketch<String> freqSketch = new FrequentItemsSketch<String>(k);
            while ((lineStr = br.readLine()) != null) {
        	freqSketch.update(lineStr);
            }
            @SuppressWarnings("rawtypes") 
            Row[] rows = freqSketch.getFrequentItems(ErrorType.NO_FALSE_POSITIVES);
            for (@SuppressWarnings("rawtypes") Row row: rows){
        	System.out.printf("%d\t%d\t%d\t%s\n", row.getEstimate(),
        					      row.getLowerBound(),
    			 		              row.getUpperBound(),
        			   		      row.getItem());
            }
        } else if (sketchType.equals("rank")) {
            if (eps < 0.0001){
        	throw new IllegalArgumentException("uniq requires the error tolerance to be at least 0.000001");
            }
            k = (int)(1.0/(eps));
            k = (k < 32) ? 32 : k; // k is at least 32
    	    k = Integer.highestOneBit((k-1)<<1); // rounded up to the next power of 2
    	    QuantilesSketch rankSketch = QuantilesSketch.builder().build(k);
    	    while ((lineStr = br.readLine()) != null) {
    		long value = Long.parseLong(lineStr);
    		rankSketch.update(value);
    	    }
    	    double[] qunatiles = rankSketch.getQuantiles(k);
    	    for (double q : qunatiles) {
    		System.out.println((long)(q));
    	    }
        } else {
            throw new IllegalArgumentException("sketch must recive a setch type [uniq, freq, rank]");
        }
    }
}
