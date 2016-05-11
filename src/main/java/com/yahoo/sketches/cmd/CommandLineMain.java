package com.yahoo.sketches.cmd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.yahoo.sketches.theta.UpdateSketch;

public class CommandLineMain {
    public static void main(String[] args) throws IOException {
	
	// Parsing command line arguments
    	String sketchType = "uniq"; // default value
    	if (args.length > 0) { 
    	    sketchType = args[0];
    	}
	Integer k = 1024; // default value
    	if (args.length > 1) {
    	    k = Integer.parseInt(args[1]);
    	    k = (k < 32) ? 32 : k; // k is at least 32
    	    k = Integer.highestOneBit((k-1)<<1); // rounded up to the next power of 2
    	}
    	BufferedReader br; 
    	if (args.length > 2) {
    	    br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));	
    	} else {
    	    br = new BufferedReader(new InputStreamReader(System.in));
    	}
    	
    	switch (sketchType) {
        case "uniq":  
            UpdateSketch sketch = UpdateSketch.builder().build(k); 
            String lineStr;
            long lineNum;
            while ((lineStr = br.readLine()) != null) {
    	    	try {
    	    	    lineNum = Long.parseLong(lineStr);
    	    	    //System.out.println(lineNum);
    	    	} catch (NumberFormatException e) {continue;}
    	    	sketch.update(lineNum);
    		}
            //double count = sketch.getEstimate();
            System.out.println(sketch.toString());
        case "freq": break; //TODO: implement frequency counting
        case  "rank": break; //TODO: implement rank sketching
        //$FALL-THROUGH$    
        default: break;
        }
    }
}
