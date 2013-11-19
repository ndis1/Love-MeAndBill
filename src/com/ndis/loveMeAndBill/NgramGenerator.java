package com.ndis.loveMeAndBill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.javatuples.Pair;

import android.util.Log;


public class NgramGenerator implements Generator {
	private int n;
	private Generator fallbackGenerator;
	private FreqDistNgram currentNgramFrequencyDistribution;
	private Random rand = new Random();
	private HashMap<String,HashMap<String,Integer>> distribution;

	
	public NgramGenerator(FreqDistNgram _currentNgramFrequencyDistribution, Generator _fallbackGenerator, int _n){
		currentNgramFrequencyDistribution = _currentNgramFrequencyDistribution;
		distribution = currentNgramFrequencyDistribution.getDistribution();
		fallbackGenerator = _fallbackGenerator;
		n = _n;
	}
	public NgramGenerator(ArrayList<LinkedList<String>> _tokens, int _n){
		n = _n;
		if(n > 2){
			fallbackGenerator = new NgramGenerator(_tokens, _n-1);
		}else{
			fallbackGenerator = new UnigramGenerator(_tokens);
		}
		currentNgramFrequencyDistribution = new FreqDistNgram(_tokens, _n);
		distribution = currentNgramFrequencyDistribution.getDistribution();
	}
	public ArrayList<String> generateString(LinkedList<String> startWords, int length){
		if(length == -1){
			return generateStringEndSent(startWords);
		}else{
			return generateStringLimit(length, startWords);
		}
	}
	private ArrayList<String> generateStringEndSent(LinkedList<String> startWords){
		LinkedList<String> prevWords = startWords;
		if(prevWords == null){
			prevWords = new LinkedList<String>();
		}
		while(prevWords.size() < n){
			prevWords.addFirst("<s>");
		}
		ArrayList<String> wordList = new ArrayList<String>();
		boolean sentenceEndFound = false;
		//store probability ranges for words
		while(!sentenceEndFound){

			String next = "";
			for(String prevWord : prevWords){
				next = next + prevWord;
			}
			double count = 0.0;
			HashMap<String, Pair<Double,Double>> ranges = new HashMap<String, Pair<Double,Double>>();
			if(distribution.containsKey(next)){
				HashMap<String,Integer> wordDist = distribution.get(next);
				ArrayList<String> keys = new ArrayList<String>();
				keys.addAll(wordDist.keySet());
				keys.remove("MmMaster__count");
				//change to iterator
				for (String word : keys){
			        double wordFreq = currentNgramFrequencyDistribution.freq(next, word);
			        if(wordFreq != 0){

			        	Pair<Double,Double> p = new Pair<Double,Double>(count, count + wordFreq);
			        	ranges.put(word, p);
			        	count = count + wordFreq;
			        }
				}
				Log.w("count is",count+"");
				double randomDub = rand.nextDouble();
				for (String word : keys){
					if(ranges.containsKey(word)){
						Pair<Double,Double> probRang = ranges.get(word);
						if(probRang.getValue0() < randomDub && probRang.getValue1() > randomDub){
							Log.w("theword", word);
							if(!word.equals("</s>")){
								if(!word.equals("<s>")){
									wordList.add(word);
								}
								prevWords.pop();
								prevWords.add(word);
							}else{
								sentenceEndFound = true;
							}
						}
					}
				}
			}else{
				LinkedList<String> prevMinusOne = new LinkedList<String>();
				prevMinusOne.addAll(prevWords);
				prevMinusOne.pop();
				String word = "";
				ArrayList<String> fallbackWord = fallbackGenerator.generateString(prevMinusOne,1);
				if(fallbackWord.size() >0){
					word = fallbackWord.get(0);
				}else{
					word = "</s>";
				}
				if(!word.equals("</s>")){
					if(!word.equals("<s>")){
						wordList.add(word);
					}
					prevWords.pop();
					prevWords.add(word);
				}else{
					sentenceEndFound = true;
				}
			}
		}
		return wordList;
	}
	
	private ArrayList<String> generateStringLimit(int length, LinkedList<String> startWords){
		LinkedList<String> prevWords = startWords;
		ArrayList<String> wordList = new ArrayList<String>();
		//store probability ranges for words
		for (int i = 0 ; i < length; i++){
			String next = "";
			for(String prevWord : prevWords){
				next = next + prevWord;
			}
			double count = 0.0;
			HashMap<String, Pair<Double,Double>> ranges = new HashMap<String, Pair<Double,Double>>();
			if(distribution.containsKey(next)){
				HashMap<String,Integer> wordDist = distribution.get(next);
				ArrayList<String> keys = new ArrayList<String>();
				keys.addAll(wordDist.keySet());
				keys.remove("MmMaster__count");

				for (String word : keys){
			        double wordFreq = currentNgramFrequencyDistribution.freq(next, word);
			        if(wordFreq != 0){
			        	Pair<Double,Double> p = new Pair<Double,Double>(count, count + wordFreq);
			        	ranges.put(word, p);
			        	count = count + wordFreq;
			        }
				}
				double randomDub = rand.nextDouble();
				for (String word : keys){
					if(ranges.containsKey(word)){
						Pair<Double,Double> probRang = ranges.get(word);
						if(probRang.getValue0() < randomDub && probRang.getValue1() > randomDub){
							if(!word.equals("</s>")){
								if(!word.equals("<s>")){
									wordList.add(word);
								}
								prevWords.pop();
								prevWords.add(word);
							}else{
								prevWords.clear();
								for(int j = 0; j < n; j++){
									prevWords.add("<s>");
								}
							}
						}
					}
				}
			}else{
				LinkedList<String> prevMinusOne = new LinkedList<String>();
				prevMinusOne.addAll(prevWords);
				prevMinusOne.pop();
				String word = "";
					ArrayList<String> fallbackWord = fallbackGenerator.generateString(prevMinusOne,1);
					if(fallbackWord.size() >0){
						word = fallbackWord.get(0);
					}else{
						word = "</s>";
					}
				if(!word.equals("</s>")){
					if(!word.equals("<s>")){
						wordList.add(word);

					}
					prevWords.pop();
					prevWords.add(word);
				}else{
					prevWords.clear();
					for(int j = 0; j < n; j++){
						prevWords.add("<s>");
					}
				}
			}
		}
		return wordList;
	}
	@Override
	public int getNValue() {
		return n;
	}
}