/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

package edu.cmu.sphinx.demo.ivr149;

import java.util.ArrayList;
import java.util.HashMap;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.*;

public class IVR {
	
	  public static int price(String size, String toppings, int count, String crust)
	  {
	    int res = 20;
	    if (size.equals("big")) res += 5;
		if (size.equals("regular")) res += 3;
	    if (toppings.equals("cheese")) res += 1;
		if (toppings.equals("chicken")) res += 4;
	    if (toppings.equals("mushrooms")) res += 2;
		if (crust.equals("pan")) res += 3;
		if (crust.equals("thick")) res += 5;
	    return res*count;
	  }
	
    public static void main(String[] args) {
    	boolean voice = false;
    	if(voice){
	        ConfigurationManager cm;
	        double ms = 0.0;
	        double lastValue = 0.0;
	        HashMap<String, Integer> map = new HashMap<String, Integer>();
	
	        if (args.length > 0) {
	            cm = new ConfigurationManager(args[0]);
	        } else {
	            cm = new ConfigurationManager(IVR.class.getResource("ivr.config.xml"));
	        }
	
	        Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
	        recognizer.allocate();
	
	        // start the microphone or exit if the programm if this is not possible
	        Microphone microphone = (Microphone) cm.lookup("microphone");
	        if (!microphone.startRecording()) {
	            System.out.println("Cannot start microphone.");
	            recognizer.deallocate();
	            System.exit(1);
	        }
	
	       // System.out.println("Say: (Good morning | Hello) ( Bhiksha | Evandro | Paul | Philip | Rita | Will )");
	        System.out.println("2ool ell enta 3awzoo" );
	        // loop the recognition until the programm exits.
	        while (true) {
	            System.out.println("Start speaking. Press Ctrl-C to quit.\n");
	
	            Result result = recognizer.recognize();
	
	            if (result != null) {
	                String resultText = result.getBestFinalResultNoFiller();
	                String[] equation = resultText.split(" ");
	                if (equation.length == 0) {
	                	System.out.println("You said nothing");
	                }
	                else{
	                	if(equation[0].equalsIgnoreCase("store"))
	                	{
	                		String var = equation[1];
	                		if(var.equalsIgnoreCase("last")) {
	                			ms = lastValue;
	                		} else {
	                			//int nr = getNumber(equation,2,equation.length-1);
	                    		//map.put(var, nr);
	                		}
	                	}
	                	else if(equation[0].equalsIgnoreCase("Define")) {
	                	} else if(equation[0].equalsIgnoreCase("retrieve")) {
	                		System.out.println("MS value is " + ms);
	                	} else {
	                		//lastValue = findResult(equation);
	                	}
	                }
	                System.out.println("You said: " + resultText + '\n');
	            } else {
	                System.out.println("I can't hear what you said.\n");
	            }
	        }
    	}else{
    	    try {
    	    	File fXmlFile = new File("dialog1.xml");
    	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    	    	Document dialog = dBuilder.parse(fXmlFile);
    	     
    	    	dialog.getDocumentElement().normalize();
    	    	
    	    	NodeList nList = dialog.getElementsByTagName("field");
    	    	
    	    	ArrayList<Field> fields = new ArrayList<Field>();
    	    	
    	    	for (int i = 0; i < nList.getLength(); i++) {
    	     
    	    		Node nNode = nList.item(i);
    	    		
    	    		Field f;
    	    		
	    			Element eElement = (Element) nNode;  
	    			
	    			String prompt;
	    			if(eElement.getElementsByTagName("prompt").item(0).getChildNodes().getLength() > 1)
	    				prompt = eElement.getElementsByTagName("prompt").item(0).getFirstChild().getTextContent() + " <insert_item> "
	    				+ eElement.getElementsByTagName("prompt").item(0).getLastChild().getTextContent();
	    			else
	    				prompt = eElement.getElementsByTagName("prompt").item(0).getTextContent();
	    			
	    			String filled = null;
	    			if(eElement.getElementsByTagName("filled").item(0) != null)
	    				filled = eElement.getElementsByTagName("filled").item(0).getTextContent();
	    			
	    			if(filled == null)
	    				filled = " ";
	    			
	    			String name = eElement.getAttribute("name");
	    			
	    			f = new Field(prompt, filled, name);
	    			
	    			NodeList itemList = eElement.getElementsByTagName("item");
	    			
	    			for (int j = 0; j < itemList.getLength(); j++) {
	    				if(itemList.item(j) != null)
	    					f.options.add(itemList.item(j).getTextContent());
	    			}
	    			fields.add(f);
    	    		
	        	}
    	    	
	    	    String size = null;
	    	    String toppings = null;
	    	    String crust = null;
	    	    String count = null;
	    	    
	    	    System.out.println("Welcome to your pizza ordering service!");
	    	    
    	    	BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
    	    	String answer = "";
	    	    for(int i = 0; i < fields.size(); i++){
	    	    	Field curField = fields.get(i);
	    	    	if(!curField.name.startsWith("confirm")){
		    	    	answer = null;
		    	    	while(answer == null){
		    	    		System.out.println(curField.prompt);
		    	    		answer = bf.readLine();
		    	    	}
		    	    	if(curField.name.equals("size"))
		    	    		size = answer;
		    	    	if(curField.name.equals("toppings"))
		    	    		toppings = answer;
		    	    	if(curField.name.equals("crust"))
		    	    		crust = answer;
		    	    	if(curField.name.equals("count"))
		    	    		count = answer;
	    	    	}else{   	
		    	    	String confirm_answer = null;
		    	    	while(confirm_answer == null){
		    	    		System.out.println(curField.prompt.replaceAll("<insert_item>", answer));
		    	    		confirm_answer = bf.readLine();
		    	    		if(confirm_answer.toLowerCase().equals("no")){
		    	    			i -= 2;
		    	    			break;
		    	    		}
		    	    	}
	    	    	}
	    	    }
	    	    
	    	    System.out.println("Fine. Your total is " + price(size, toppings, Integer.parseInt(count), crust) + " pounds. Your order will be ready shortly.");
    	    	
	    	} catch (Exception e) {
	        	e.printStackTrace();
	        }
    	}
    }
}
