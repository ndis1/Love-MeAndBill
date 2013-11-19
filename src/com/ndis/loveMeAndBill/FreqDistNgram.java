package com.ndis.loveMeAndBill;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


public class FreqDistNgram {
	private ArrayList<String> types;
	private ArrayList<LinkedList<String>> tokens;
	private HashMap<String,Integer> typeCounts;
	int tokenCount = 0;
	private HashMap<String,HashMap<String,Integer>> distribution = new HashMap<String,HashMap<String,Integer>>();
	public FreqDistNgram(HashMap<String,HashMap<String,Integer>> _distribution){
		distribution = _distribution;
		types = new ArrayList<String>();
		types.addAll(_distribution.keySet());
	}
	public FreqDistNgram(ArrayList<LinkedList<String>> _tokens, int n){
		tokens = _tokens;
		types = new ArrayList<String>();
		typeCounts = new HashMap<String,Integer>();
		//use linkedlist as fifo queue of previous words
		
		for (LinkedList<String> sent : tokens){
			LinkedList<String> prevWords = new LinkedList<String>();
			for (int w = 0; w < n; w++){
				//add the sentence start character
				prevWords.add("<s>");
			}
			for (int w = 0; w < n; w++){
				sent.add("</s>");
			}
			for(String word : sent){
				if (word != "" && word != "\n"){
					String prev = "";
					for (String prevWord : prevWords){
						prev = prev  + prevWord;
					}
					tokenCount ++;
					int typeCount = 0;
					if(typeCounts.containsKey(prev)){
						typeCount = typeCounts.get(prev);
					}
					if(!types.contains(word)){
						types.add(word);
					}
					typeCount ++;
					typeCounts.put(word, typeCount);
					if(distribution.containsKey(prev)){
						HashMap<String,Integer> dependentFrequency = distribution.get(prev);
						int depFreqCount = 0;
						if(dependentFrequency.containsKey(word)){
							depFreqCount = dependentFrequency.get(word);
						}
						depFreqCount++;
						dependentFrequency.put(word, depFreqCount);
					}else{
						HashMap<String,Integer> tCount = new HashMap<String,Integer>();
						tCount.put(word, 1);
						distribution.put(prev, tCount);
					}
				}			
				prevWords.pop();
				prevWords.add(word);
			}
		}
	}
	
	public int count(String word1, String word2){
		if (distribution.containsKey(word1)){
			HashMap<String,Integer> dependentFrequency = distribution.get(word1);
			if(dependentFrequency.containsKey(word2)){
				return dependentFrequency.get(word2);
			}
		}
		return 0;
	}
	
	public double freq(String word1, String word2){
		if (distribution.containsKey(word1)){
			HashMap<String,Integer> dependentFrequency = distribution.get(word1);
			if(dependentFrequency.containsKey(word2)){
				double count = dependentFrequency.get(word2);
				double length = dependentFrequency.get("MmMaster__count");
				return count/length;
			}
		}
		return 0.0;
	}
	public HashMap<String,HashMap<String,Integer>> getDistribution(){
		return distribution;
	}
	public ArrayList<String> getTypes(){
		return types;
	}
}
