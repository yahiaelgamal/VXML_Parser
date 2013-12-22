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
    public String size = null;
    public String toppings = null;
    public String crust = null;
    public String count = null;

    private Recognizer recognizer;
    private Microphone microphone;

    public IVR(){
        this(true);
    }

    public IVR(boolean mic){
        if(mic) {
            ConfigurationManager cm;
            cm = new ConfigurationManager(IVR.class.getResource("ivr.config.xml"));

            this.recognizer = (Recognizer) cm.lookup("recognizer");
            recognizer.allocate();

            this.microphone = (Microphone) cm.lookup("microphone");
        }
    }

    public int price() {
        int countInt = Integer.parseInt(this.count);
        int res = 20;
        if (size.equals("big")) res += 5;
        if (size.equals("regular")) res += 3;
        if (size.equals("small")) res += 1;
        if (toppings.equals("cheese")) res += 1;
        if (toppings.equals("chicken")) res += 4;
        if (toppings.equals("mushrooms")) res += 2;
        if (crust.equals("pan")) res += 3;
        if (crust.equals("thick")) res += 5;
        return res*countInt;
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
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        try {
			return bf.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
    }


    public void interact(){
        String fileName = "dialog1.xml";
        ArrayList<Field> fields = parseXMLFile(fileName);

        System.out.println("Welcome to your pizza ordering service!");

        String answer = "";

        for (int i = 0; i < fields.size(); i++) {
            Field curField = fields.get(i);
            if (!curField.name.startsWith("confirm")) {
                answer = null;
                while (answer == null) {
                    // what the system says
                    System.out.println(curField.prompt);
                    // the answer you get from the user
                    answer = getInput();

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
                    System.out.println(curField.prompt.replaceAll( "<insert_item>", answer));
                    // the answer you get from the user
                    confirm_answer = getInput();
                    if (confirm_answer.toLowerCase().equals("no")) {
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

        System.out.println("Fine. Your total is "
                + price()
                + " pounds. Your order will be ready shortly.");

    }

    public static void main(String[] args) throws IOException {
        IVR instance = new IVR();
        instance.interact();
    }
}
