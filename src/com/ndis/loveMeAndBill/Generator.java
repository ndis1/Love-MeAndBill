package com.ndis.loveMeAndBill;

import java.util.ArrayList;
import java.util.LinkedList;

public interface Generator {
	public ArrayList<String> generateString(LinkedList<String> startWords, int length);
	public int getNValue();
}
