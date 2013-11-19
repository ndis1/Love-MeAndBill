package com.ndis.loveMeAndBill;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.util.SparseArray;

public class GeneratorBuilder {
	private AssetManager assetManager;
	private Resources resources;
	public GeneratorBuilder(AssetManager _assetManager, Resources _resources){
		assetManager = _assetManager;
		resources = _resources;
	}
	public UnigramGenerator getUnigramDistribution(String filename){
		UnigramGenerator unigramGenerator = null;
    	try {
    		HashMap<String,Integer> unigDistribution = new HashMap<String,Integer>();

    		int unigTokenCount = 0;
    		ArrayList<String> types = new ArrayList<String>();
			String sCurrentLine;
			InputStream instream = assetManager.open(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(instream));
			boolean firstLine = true;
			while ((sCurrentLine = br.readLine()) != null) {
				if(firstLine){
					unigTokenCount = Integer.parseInt(sCurrentLine);
					firstLine = false;
				}
				String[] tokensInThisLine = sCurrentLine.split(" ");
				if(tokensInThisLine.length > 1){
					unigDistribution.put(tokensInThisLine[0], Integer.parseInt(tokensInThisLine[1]));
				}
			}
			types = new ArrayList<String>();
			types.addAll(unigDistribution.keySet());
	        FreqDistUnig unigFD = new FreqDistUnig(unigDistribution, unigTokenCount, types);
	        unigramGenerator = new UnigramGenerator(unigFD,types);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return unigramGenerator;
    }
	
	private SparseArray<String> getSourceFiles(int resourceId, int n){
		ArrayList<String> sourceFiles =new ArrayList(Arrays.asList(resources.getStringArray(resourceId)));
		SparseArray<String> sourceSparse = new SparseArray<String>();

		for(String s : sourceFiles){
			if(s.contains("UNI")){
				sourceSparse.put(1, s);
			}else if(s.contains("BIG") && n >= 2){
				sourceSparse.put(2, s);
			}else if(s.contains("TRI")&& n >= 3){
				sourceSparse.put(3, s);
			}
			else if(s.contains("QUA")&& n >= 4){
				sourceSparse.put(4, s);
			}
		}
		return sourceSparse;
   }
	 public Generator getNgramDistribution(int resourceId, int n){
		SparseArray<String> sourceFiles = getSourceFiles(resourceId, n);
		
		return getNgramDistribution(sourceFiles);
	 }
	 public Generator getNgramDistribution(SparseArray<String> _sourceFiles){

		NgramGenerator ngramGenerator = null;
		SparseArray<String> sourceFiles = _sourceFiles;
		int thisLevel = sourceFiles.size();
		String thisLevelFile = sourceFiles.get(thisLevel);
		Generator fallbackGenerator;
		if(thisLevel == 1){
			return getUnigramDistribution(sourceFiles.get(1));
		}
		if(thisLevel == 2){
			fallbackGenerator = getUnigramDistribution(sourceFiles.get(1));
		}else{
			sourceFiles.remove(thisLevel);
			fallbackGenerator = getNgramDistribution(sourceFiles);
		}
		try {
			HashMap<String,HashMap<String,Integer>> distribution = new HashMap<String,HashMap<String,Integer>>();
			String sCurrentLine;
			InputStream instream = assetManager.open(thisLevelFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(instream));
			while ((sCurrentLine = br.readLine()) != null) {
				HashMap<String,Integer> dependant = new HashMap<String,Integer>();
				String[] tokensInThisLine = sCurrentLine.split(" ");
			String condition = tokensInThisLine[0];
			String total = tokensInThisLine[1];
			dependant.put("MmMaster__count", Integer.parseInt(total));
				for (int v = 2; v < tokensInThisLine.length-1; v+=2){
					dependant.put(tokensInThisLine[v], Integer.parseInt(tokensInThisLine[v+1]));
				}
				distribution.put(condition, dependant);
			}
			FreqDistNgram freqDistNgram = new FreqDistNgram(distribution);
			Log.w("perendprob", freqDistNgram.freq(".", "</s>")+"");
			ngramGenerator = new NgramGenerator(freqDistNgram, fallbackGenerator, thisLevel-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return ngramGenerator;
	 }
}