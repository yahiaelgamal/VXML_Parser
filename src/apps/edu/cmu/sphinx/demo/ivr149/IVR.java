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
import com.sun.speech.freetts.*;
import javax.script.*;

import java.io.*;

public class IVR {
    public String size = null;
    public String toppings = null;
    public String crust = null;
    public String count = null;
    
    private ArrayList<Field> fields;
    private Recognizer recognizer;
    private Microphone microphone;
    private Voice voice;
    private static String javaScript;

    public IVR(){
        this(true);
    }
    
    public void stop() {
        this.microphone.stopRecording();
        this.recognizer.deallocate();
    }

    public IVR(boolean mic){
        if(mic) {
            ConfigurationManager cm;
            cm = new ConfigurationManager(IVR.class.getResource("ivr.config.xml"));

            this.recognizer = (Recognizer) cm.lookup("recognizer");
            recognizer.allocate();
            
            VoiceManager voiceManager = VoiceManager.getInstance();
            this.voice = voiceManager.getVoice("kevin16");
            voice.allocate();
            voice.setDumpUtterance(true);
            this.microphone = (Microphone) cm.lookup("microphone");
        }
    }

    public Double price() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        try {
            engine.eval(javaScript);
            System.out.println(javaScript);
            Invocable inv = (Invocable) engine;
			return (Double)inv.invokeFunction("price", size, toppings, count, crust);
		}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		//return "unkown";
		return 20.0;
    }

    public static ArrayList<Field> parseXMLFile(String fileName) {
        ArrayList<Field> fields = new ArrayList<Field>();
        try {
            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document dialog = dBuilder.parse(fXmlFile);

            dialog.getDocumentElement().normalize();

            NodeList nList = dialog.getElementsByTagName("field");
            javaScript = dialog.getElementsByTagName("script").item(0).getFirstChild().getTextContent();
            
            for (int i = 0; i < nList.getLength(); i++) {

                Node nNode = nList.item(i);
                Field f;
                Element eElement = (Element) nNode;

                String prompt;
                if (eElement.getElementsByTagName("prompt").item(0)
                        .getChildNodes().getLength() > 1)
                    prompt = eElement.getElementsByTagName("prompt").item(0)
                            .getFirstChild().getTextContent()
                            + " <insert_item> "
                            + eElement.getElementsByTagName("prompt").item(0)
                                    .getLastChild().getTextContent();
                else
                    prompt = eElement.getElementsByTagName("prompt").item(0)
                            .getTextContent();

                String filled = null;
                if (eElement.getElementsByTagName("filled").item(0) != null)
                    filled = eElement.getElementsByTagName("filled").item(0)
                            .getTextContent();

                if (filled == null)
                    filled = " ";

                String name = eElement.getAttribute("name");

                f = new Field(prompt, filled, name);

                NodeList itemList = eElement.getElementsByTagName("item");

                for (int j = 0; j < itemList.getLength(); j++) {
                    if (itemList.item(j) != null)
                        f.options.add(itemList.item(j).getTextContent());
                }
                fields.add(f);

            }
            return fields;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getInput(){
//        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
//        try {
//			return bf.readLine();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "";
//		}
        this.microphone.clear();
        this.microphone.startRecording();

        System.out.println("Say something to start. Press Ctrl-C to quit.\n");
        Result result = recognizer.recognize();

        if (result != null) {
            String resultText = result.getBestFinalResultNoFiller();
            if(resultText.contains("big"))
            	return "big";
            else if(resultText.contains("small"))
            	return "small";
            else if(resultText.contains("thick"))
            	return "thick";
            else if(resultText.contains("pan"))
            	return "pan";
            else if(resultText.contains("cheese"))
            	return "cheese";
            else if(resultText.contains("mushrooms"))
            	return "mushrooms";
            else if(resultText.contains("chicken"))
            	return "chicken";
            else if(resultText.startsWith("I want"))
            	return resultText.substring(7);
        	else
        		return resultText;
        }else
        	return "NA";
    }
    
    public void produceOutput(String output) {
        voice.speak(output);
        System.out.println(output);
    }


    public void getFieldsFromFile(String fileName) {
        fields = parseXMLFile(fileName);
    }
    
    public void interact(){
        produceOutput("Welcome to your pizza ordering service!");

        String answer = "";

        for (int i = 0; i < fields.size(); i++) {
            Field curField = fields.get(i);
            if (!curField.name.startsWith("confirm")) {
                answer = null;
                while (answer == null) {
                    // what the system says
                    produceOutput(curField.prompt);
                    // the answer you get from the user
                    answer = getInput();
                    System.out.println(answer);

                    if (answer != null && !curField.options.contains(answer))
                        answer = null;
                }
                if (curField.name.equals("size"))
                    size = answer;
                if (curField.name.equals("toppings"))
                    toppings = answer;
                if (curField.name.equals("crust"))
                    crust = answer;
                if (curField.name.equals("count"))
                    count = answer;
            } else {
                String confirm_answer = null;
                while (confirm_answer == null) {
                    // what the system confirms with
                    produceOutput(curField.prompt.replaceAll( "<insert_item>", answer));
                    // the answer you get from the user
                    confirm_answer = getInput();
                    System.out.println(confirm_answer);
                    if (!confirm_answer.toLowerCase().equals("yes")) {
                        // get back to the last field
                        i -= 2;
                        break;
                    }
                }
            }
        }

        if (count.equals("one"))
            count = "1";
        if (count.equals("two"))
            count = "2";
        if (count.equals("three"))
            count = "3";
        
        produceOutput("Fine. Your total is "
                + price().intValue()
                + " pounds. Your order will be ready shortly.");

        this.stop();
    }

    public static void main(String[] args) throws IOException {
    	IVR instance = new IVR();
        instance.getFieldsFromFile("dialog1.xml");
        instance.interact();
    }
}
