package com.ndis.loveMeAndBill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.javatuples.Pair;

import android.util.Log;

public class UnigramGenerator implements Generator {
	private FreqDistUnig unigramFrequencyDistribution;
	private ArrayList<String> types;
	private Random rand = new Random();

	public UnigramGenerator(ArrayList<LinkedList<String>> _tokens){
		unigramFrequencyDistribution = new FreqDistUnig(_tokens);
		types = unigramFrequencyDistribution.getTypes();
	}
	
	public UnigramGenerator(FreqDistUnig _unigramFrequencyDistribution, ArrayList<String> _types){
		unigramFrequencyDistribution = _unigramFrequencyDistribution;
		types = _types;
	}
	
	public ArrayList<String> generateString(LinkedList<String> empty, int _length){
		int length;
		if(_length == -1){
			length = rand.nextInt(5)+3;
		}else{
			length = _length;
		}
		ArrayList<String> wordList = new ArrayList<String>();
		double count = 0.0;
		//store probability ranges for words
		HashMap<String, Pair<Double,Double>> ranges = new HashMap<String, Pair<Double,Double>>();
		for (String word : types){
	        double wordFreq = unigramFrequencyDistribution.freq(word);
	        Pair<Double,Double> p = new Pair<Double,Double>(count, count + wordFreq);
	        ranges.put(word, p);
	        count = count + wordFreq;
		}
		for (int i = 0 ; i < length; i++){
			double randomDub = rand.nextDouble();
			for (String word : types){
				Pair<Double,Double> probRang = ranges.get(word);
				if(probRang.getValue0() < randomDub && probRang.getValue1() > randomDub){
					wordList.add(word);
				}
			}
		}
		return wordList;
	}

	@Override
	public int getNValue() {
		return 0;
	}
	
	
}
