package edu.cmu.sphinx.demo.ivr149;

import java.util.ArrayList;

public class Field {
	String prompt;
	ArrayList<String> options;
	String filled;
	String name;
	
	public Field(String p, String f, String n){
		prompt = p;
		options = new ArrayList<String>();
		filled = f;
		name = n;
	}
}
