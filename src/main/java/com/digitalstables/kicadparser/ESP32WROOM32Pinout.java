package com.digitalstables.kicadparser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;






public class ESP32WROOM32Pinout {
	public Hashtable<String, Pin> pinIndex = new Hashtable();

	
	public ESP32WROOM32Pinout() {
		String line, pinName;
		int pinNumber;
		 String arduinoPinType = null;
		 String arduinPinFunction="";
		 int arduinoPinNumber=0;
		Pin pin;
		String fileToRead = "/home/ari/Data/DigitalStables/mcu/ESP32WROOM32.txt"; //args[0];
		try (BufferedReader br = new BufferedReader(new FileReader(fileToRead))) {
			while ((line = br.readLine()) != null){
				String[] tokens = line.split("\t");
				if(tokens.length==4) {
					pinNumber = Integer.parseInt(tokens[1]);
					pinName = tokens[2];
					if(tokens[3].startsWith("Digital")) {
						arduinoPinType= "";
						String remaining = tokens[3].substring(12);
						if(remaining.contains("(")){
							arduinoPinNumber = Integer.parseInt(remaining.substring(0,remaining.indexOf("(")).trim());
							arduinPinFunction=remaining.substring(remaining.indexOf("(")+1,remaining.indexOf(")"));;
						}else {
							arduinoPinNumber = Integer.parseInt(remaining.trim());
							arduinPinFunction="";
						}
						pin = new Pin(pinNumber, pinName, arduinoPinType, arduinPinFunction, arduinoPinNumber);
						pinIndex.put(""+pinNumber,pin);
					}else if(tokens[3].startsWith("Analog Reference")) {
						
					}else if(tokens[3].startsWith("Analog")) {
						arduinoPinType= "A";
						arduinPinFunction="";
						String s = tokens[3].substring(11).trim();
				
						arduinoPinNumber = Integer.parseInt(s);
						pin = new Pin(pinNumber, pinName, arduinoPinType, arduinPinFunction, arduinoPinNumber);
						pinIndex.put(""+pinNumber,pin);
					}
					
					
					
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
