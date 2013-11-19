package com.ndis.loveMeAndBill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.util.Log;


public class FreqDistUnig {
	private ArrayList<String> types;
	private ArrayList<String> tokens;
	int tokenCount = 0;
	private HashMap<String,Integer> distribution = new HashMap<String,Integer>();
	
	public FreqDistUnig(ArrayList<LinkedList<String>> _tokens){
		tokens = new ArrayList<String>();
		for(LinkedList<String> sent : _tokens){
			tokens.addAll(sent);
		}
		types = new ArrayList<String>();
		for (String t : tokens){
			if (t != "" && t != "\n"){
				tokenCount ++;
				if(!types.contains(t)){
					types.add(t);
				}
				//initialize times
				int timesInDistribution = 0;
				if (distribution.containsKey(t)){
					timesInDistribution = distribution.get(t);
				}
				//increment times in distribution
				timesInDistribution ++;
				distribution.put(t, timesInDistribution);
			}
		}
	}
	public FreqDistUnig(HashMap<String,Integer> _distribution, int _tokenCount, ArrayList<String> _types){
		distribution = _distribution;
		types = _types;
		tokenCount = _tokenCount;
	}
	
	public int count(String word){
		if (distribution.containsKey(word)){
			return distribution.get(word);
		}
		return 0;
	}
	
	public double freq(String word){
		if (distribution.containsKey(word)){
			double wordcount = distribution.get(word);
			double freq = wordcount / tokenCount;
			return freq;
		}
		return 0.0;
	}
	public HashMap<String,Integer> getDistribution(){
		return distribution;
	}
	public ArrayList<String> getTypes(){
		return types;
	}
}
