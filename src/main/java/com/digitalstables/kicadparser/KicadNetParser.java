package com.digitalstables.kicadparser;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class KicadNetParser {

	public static final String buildNumber="1";
			
	public static void main(String[] args) {
	
		if(args.length!=2){
			System.out.println("Usage java -jar kicadparser.jar  path_to_dot_net_file  mcuComponentReference");
			System.out.println("e.g.");
			System.out.println("java -jar kicadparser.jar  /Users/john/MyProject/MyProject.net  U5");
			System.exit(0);
		}
		String fileToReadName = args[0];//"/Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/gloria_field_kicad/gloria_field_kicad.net";  //args[0];
		File fileToRead= new File(fileToReadName);
		
		
		if(!fileToRead.isFile()){
			System.out.println(fileToReadName + " is not a valid file");
			System.exit(0);
		}
		
		String dirPath = FilenameUtils.getFullPath(fileToReadName);
		String baseName = FilenameUtils.getBaseName(fileToReadName);
		String mcuComponentReference = args[1];//"U5";
		String gloriaFieldPinDefinitionFileName = dirPath + baseName + ".h" ;///Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/gloria_field_kicad/GloriaPinDefintion.h";
		String componentTypeReportFileName = dirPath + "ComponentReport.csv" ;//"/Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/gloria_field_kicad/ComponentReport.csv";
	
	
		KicadComponent kicadComponent= new KicadComponent();
		KicadComponent existingKicadComponent;
		
		ArrayList<Map.Entry<KicadComponent, String>> kicadComponentArray = new ArrayList();
		MegaPinout megaPinout = new MegaPinout();
		Hashtable valueComponentIndex = new Hashtable();
		
        try (BufferedReader br = new BufferedReader(new FileReader(fileToRead))) 
        {
        	ArrayList<Map.Entry<String, Integer>> pinIndex = new ArrayList();
        	int pinNumber;
            String line, labelName="";
            boolean inNetArea=false;
            boolean insideMainMCUComp=false;
            
            
            boolean insideSpecificNet=false;
            boolean insideComponentDescription=false;
            String modelName="";
            while ((line = br.readLine()) != null) 
            {
            	line = line.trim();
            	if(line.equals("(components")) {
            		insideComponentDescription=true;
            	}else if(line.equals("(libparts")) {
            		insideComponentDescription=false;
            	}
            	
            	if(insideComponentDescription) {
            		
            		if(line.startsWith("(comp (ref")){
            			//
            			// show the last one
            			
            			String csvLine = kicadComponent.getCSVLine();
            			if(!csvLine.equals(",,,,,,,,")){
            				if(valueComponentIndex.containsKey(kicadComponent.getValue())) {
            					existingKicadComponent = (KicadComponent)valueComponentIndex.get(kicadComponent.getValue());
            					existingKicadComponent.addReference(kicadComponent.getReference());
    	                    	valueComponentIndex.put(kicadComponent.getValue(),existingKicadComponent);
		                    }else {
		                    	kicadComponent.addReference(kicadComponent.getReference());
    	                    	valueComponentIndex.put(kicadComponent.getValue(),kicadComponent);
		                    }
            				
                    		
	            			kicadComponentArray.add(new AbstractMap.SimpleEntry<KicadComponent, String>(kicadComponent, kicadComponent.getReference()));
							Collections.sort(kicadComponentArray, new Comparator<Map.Entry<?, String>>(){
								public int compare(Map.Entry<?, String> o1, Map.Entry<?, String> o2) {
									return o1.getValue().compareTo(o2.getValue());
								}});
	            		}
            			
            			
            			kicadComponent = new KicadComponent();
            			
            			String ref = line.substring(10).replace(")", "").trim();
            			kicadComponent.setReference(ref);
            		}else if(line.startsWith("(footprint")){
            			String footprint = line.substring(11).replace(")", "").trim();
            			kicadComponent.setFootprint(footprint);
            		}else if(line.startsWith("(datasheet")){
            			String datasheet = line.substring(11).replace(")", "").trim();
            			kicadComponent.setDatasheet(datasheet);
            		}else if(line.startsWith("(value ")) {
            			String value = line.split(" ")[1].replace(")","").replace("\"","");
            			kicadComponent.setValue(value);
            		}else if(line.startsWith("(libsource")){
            			Pattern regex = Pattern.compile("\\((.*?)\\)");
            			Matcher regexMatcher = regex.matcher(line.substring(10));
            			while (regexMatcher.find()) {//Finds Matching Pattern in String
            				String s = regexMatcher.group(1);
        				   if(s.startsWith("lib")){
        					   String symbolLibrary = s.substring(4).trim();
                   				kicadComponent.setSymbolLibrary(symbolLibrary);
        				   }else if(s.startsWith("part")){
        					   String part = s.substring(5).trim();
                  				kicadComponent.setPart(part);
        				   }else if(s.startsWith("description")){
        					   String description = s.substring(12).replace("\"","").replace(",", " ");
                 				kicadComponent.setDescription(description);
        				   }
        				}
            		}else if(line.contains("(field (name JLCPCB)")){
            			String jlcpcbPart = line.substring(21).replace("\"","").replace(")","").trim();
         				kicadComponent.setJlcpcbPart(jlcpcbPart);
            		}else if(line.contains("(field (name \"JLCPCB Type\")") || line.contains("(field (name JLCPCB_TYPE)")){
            			String jlcpcbType = line.substring(26).replace(")","");
         				kicadComponent.setJlcpcpType(jlcpcbType);
            		}
            		
            		
            		if(line.contains("(comp (ref "+mcuComponentReference)) {
            			insideMainMCUComp=true;
	            	}
            		if(insideMainMCUComp) {
            			if(line.startsWith("(value ")) {
            				modelName = line.split(" ")[1].replace(")","");
            				System.out.println("model:" + modelName);
            				insideMainMCUComp=false;
            			}
            		}
            	}
            	
            	
            	
            //	System.out.println("line " + line);
                if(line.equals("(nets")) {
                	inNetArea=true;
                }else {
                	if(inNetArea) {
                		if(line.contains("(net") && line.contains("(name")) {
                			insideSpecificNet=true;
                			int lineLength= line.length();
                			 labelName = line.substring(line.indexOf("(name")+6, lineLength-1);
                			//System.out.println("found label " + labelName);
                		}else if(insideSpecificNet) {
                			if(line.contains("(ref "+ mcuComponentReference)) {
                				int pinPos = line.indexOf("(pin");
                				
                				String potLine = line.substring(pinPos+5, line.length()).replace("))", "");
                				if(potLine.contains(")"))potLine = potLine.replace(")", "");
                				 pinNumber = Integer.parseInt(potLine);
                				// System.out.println("line contains mcuref labelName "+  labelName +  " pinNumber=" + pinNumber );
                				
                				pinIndex.add(new AbstractMap.SimpleEntry<String, Integer>(labelName, pinNumber));
        						Collections.sort(pinIndex, new Comparator<Map.Entry<?, Integer>>(){
        							public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
        								return o1.getValue().compareTo(o2.getValue());
        							}});
        						
                			}
                			if(line.contains(")))")){
                				insideSpecificNet=false;
                			}
                		}
                	}
                }
            }
            
            
            ArrayList<String> componentLines = new ArrayList();
            componentLines.add("reference , value  , jlcpcbPart , jlcpcpType,footprint , description , part , datasheet , symbolLibrary");
            String csvValue;
            Vector v;
            Enumeration en = valueComponentIndex.keys();
            while(en.hasMoreElements()) {
            	String value = (String) en.nextElement();
            	existingKicadComponent = (KicadComponent)valueComponentIndex.get(value);
            	 csvValue = existingKicadComponent.getCSVLine();
            	// System.out.println(csvValue);
            	componentLines.add(csvValue);
            }
            
    
            FileUtils.writeLines(new File(componentTypeReportFileName), componentLines);
            
           // System.out.println("Physical\tArduino Pin\tLabel Name "  );
            ArrayList<String> lines = new ArrayList();
            for (Map.Entry<String, Integer> entry : pinIndex) {
				labelName = (String)entry.getKey();
				pinNumber = entry.getValue();
			//	System.out.println("line 206 mcuref labelName "+  labelName +  " pinNumber=" + pinNumber );
				if(modelName.contains("ATmega2560")) {
					Pin pin = megaPinout.pinIndex.get(""+pinNumber);
					if(pin!=null && !labelName.contains("Net-(" + mcuComponentReference)) {
						 line = "#define " + labelName +  " " + pin.arduinoPinType+pin.arduinoPinNumber;
						 lines.add(line);
						//System.out.println(pin.pinNumber + "\t\t" +  pin.arduinoPinType+pin.arduinoPinNumber + "\t\t "  + labelName );
						
						//System.out.println(line);
					}
				}
				
            }
            FileUtils.writeLines(new File(gloriaFieldPinDefinitionFileName), lines);
            System.out.println("Saved the following files:");
            System.out.println(gloriaFieldPinDefinitionFileName);
            System.out.println(componentTypeReportFileName);
            
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
	}

}
