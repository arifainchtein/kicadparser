package com.digitalstables.kicadparser;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

public class KicadNetParser {

	public static final String buildNumber="1";
			
	static ArrayList<String> missingDatasheets = new ArrayList();
	static ArrayList<String> missingStock = new ArrayList();
	static ArrayList<String> missingPrice = new ArrayList();
	
	public static void main(String[] args) {
	   long startingTime = System.currentTimeMillis();
		if(args.length!=3){
			System.out.println("Usage java -jar kicadparser.jar  path_to_dot_net_file  mcuComponentReference  path_to_generated_cpl_file");
			System.out.println("e.g.");
			System.out.println("java -jar kicadparser.jar  /Users/john/MyProject/MyProject.net  U5  MyProject.pro_cpl_jlc.csv");
			System.exit(0);
		}
		double durationSeconds=0.0;
		
		
		//String fileToReadName = "/Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/production/valentino_kicad_V2/valentino_2104.net";
		String fileToReadName = args[0];//"/Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/gloria_field_kicad/gloria_field_kicad.net";  //args[0];
		File fileToRead= new File(fileToReadName);
		System.out.println("About to updateJLCPCB database");
		UpdateJLCPCBDatabase u = new UpdateJLCPCBDatabase();
		u.process();
		
		System.out.println("Update JLCPCB database completed");
		
		ArrayList referenceList = new ArrayList();
		
		if(!fileToRead.isFile()){
			System.out.println(fileToReadName + " is not a valid file");
			System.exit(0);
		}
		
		String dirPath = FilenameUtils.getFullPath(fileToReadName);
		String baseName = FilenameUtils.getBaseName(fileToReadName);
		String mcuComponentReference = args[1];
	    //String mcuComponentReference = "U15";
		
	    String cplFileName = args[2];
		//String cplFileName = "/Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/production/valentino_kicad_V2/valentino_2104.pro_cpl_jlc.csv";
		String datasheetDir = dirPath + "datasheets/";
		File dsDir = new File(datasheetDir);
		dsDir.mkdirs();
		
		String gloriaFieldPinDefinitionFileName = dirPath + baseName + ".h" ;///Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/gloria_field_kicad/GloriaPinDefintion.h";
		String componentTypeReportFileName = dirPath + "ComponentReport.csv" ;//"/Users/arifainchtein/Data/DigitalStables/mcu/kicad_designs/gloria_field_kicad/ComponentReport.csv";
		String analysisReportFileName = dirPath + "JLCPCBAnalysisReport.txt" ;
	
		KicadComponent kicadComponent= new KicadComponent();
		KicadComponent existingKicadComponent;
		
		ArrayList<Map.Entry<KicadComponent, String>> kicadComponentArray = new ArrayList();
		MegaPinout megaPinout = new MegaPinout();
		Hashtable valueComponentIndex = new Hashtable();
		Hashtable noJLCPCBomponentIndex = new Hashtable();
		
		double componentPrice=0.0;
        double totalCost=0;
        double extendedPartsCount=0;
        
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
            			//System.out.println("line=" + line);
            			String csvLine = kicadComponent.getCSVLine();
            			
            			if(!csvLine.equals(",,,,,,,,")){
            				if(!kicadComponent.getJlcpcbPart().equals("")) {
            					if(valueComponentIndex.containsKey(kicadComponent.getJlcpcbPart())) {
                					existingKicadComponent = (KicadComponent)valueComponentIndex.get(kicadComponent.getJlcpcbPart());
                					existingKicadComponent.addReference(kicadComponent.getReference());
                					referenceList.add(kicadComponent.getReference());
        	                    	valueComponentIndex.put(kicadComponent.getJlcpcbPart(),existingKicadComponent);
    		                    }else {
    		                    	referenceList.add(kicadComponent.getReference());
    		                    	kicadComponent.addReference(kicadComponent.getReference());
    		                    	valueComponentIndex.put(kicadComponent.getJlcpcbPart(),kicadComponent);
    		                    }
            				}else {
            					noJLCPCBomponentIndex.put(kicadComponent.getReference(),kicadComponent);
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
            	
            	
            	
            
                if(line.equals("(nets")) {
                	inNetArea=true;
                }else {
                	if(inNetArea) {
                		if(line.contains("(net") && line.contains("(name")) {
                			insideSpecificNet=true;
                			int lineLength= line.length();
                			 labelName = line.substring(line.indexOf("(name")+6, lineLength-1);
                			
                		}else if(insideSpecificNet) {
                			if(line.contains("(ref "+ mcuComponentReference)) {
                				int pinPos = line.indexOf("(pin");
                				
                				String potLine = line.substring(pinPos+5, line.length()).replace("))", "");
                				if(potLine.contains(")"))potLine = potLine.replace(")", "");
                				 pinNumber = Integer.parseInt(potLine);
                				
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
            componentLines.add("reference , value  , jlcpcbPart , jlcpcpType,price,total,stock, footprint , description , part , datasheet , symbolLibrary");
            String csvValue;
            Vector v;
            PostgresqlPersistenceManager p = PostgresqlPersistenceManager.instance();
            int stock;
            Enumeration en = valueComponentIndex.keys();
            int totalCompnents =valueComponentIndex.size();
            String encodedPrice="";
            
            String libraryType;
            JSONObject part;
            String datasheet="";
            int counter=0;
            while(en.hasMoreElements()) {
            	String value = (String) en.nextElement();
            	
            	existingKicadComponent = (KicadComponent)valueComponentIndex.get(value);
            	part = p.getRecordByLscspart (existingKicadComponent.getJlcpcbPart());
            	if(part.has("Datasheet")) {
            		
            		datasheet = part.getString("Datasheet");
            	}else {
            		missingDatasheets.add( existingKicadComponent.getJlcpcbPart() );
            		
            	}
            	counter++;
            	System.out.print("\r processing design components " );
				System.out.printf("%.2f%%", ((double)counter/(double)totalCompnents)*100.0);
            	if(datasheet!=null && datasheet.length()>5) {
            		if(!datasheet.startsWith("http://") && !datasheet.startsWith("https://")) {
            			datasheet = "http://" + datasheet;
            		}          		
            		downloadDatasheet( datasheetDir, existingKicadComponent.getJlcpcbPart(),datasheet);          		
            	}else {
            		missingDatasheets.add( existingKicadComponent.getJlcpcbPart() + " -" + datasheet);
            	}
            	
            	
            	
            	if(part.has("Stock")) {
            		stock = part.getInt("Stock");
            	}else {
            		stock=0;
            		missingStock.add(existingKicadComponent.getReference());		
            	}
            	existingKicadComponent.setStock(stock);
            	//
            	// the price is in the format 
            	// 1-999:0.002515152,1000-:0.000848485
            	// assume we are paying the first price
            	if(part.has("Price")) {
            		encodedPrice = part.getString("Price");//.getPriceByLscspart (existingKicadComponent.getJlcpcbPart() );
            		componentPrice=0;
                	if(encodedPrice.contains(",") && encodedPrice.contains(":")) {
                		componentPrice = Double.parseDouble(encodedPrice.split(",")[0].split(":")[1]);
                	}
                	existingKicadComponent.setPrice(componentPrice);
            	}else {
            		missingPrice.add(existingKicadComponent.getReference());
            		existingKicadComponent.setPrice(0);
            	}
            	
            	if(part.has("LibraryType")) {
            		libraryType = part.getString("LibraryType");
            		if(libraryType.equals("Extended"))extendedPartsCount++;
            	}else {
            		
            	}
            	csvValue = existingKicadComponent.getCSVLine();
            	componentLines.add(csvValue);
            	totalCost += existingKicadComponent.getTotalCostPerPart();
            }
            FileUtils.writeLines(new File(componentTypeReportFileName), componentLines); 
            ArrayList<String> lines = new ArrayList();
            for (Map.Entry<String, Integer> entry : pinIndex) {
				labelName = (String)entry.getKey();
				pinNumber = entry.getValue();
				if(modelName.contains("ATmega2560")) {
					Pin pin = megaPinout.pinIndex.get(""+pinNumber);
					if(pin!=null && !labelName.contains("Net-(" + mcuComponentReference)) {
						 line = "#define " + labelName +  " " + pin.arduinoPinType+pin.arduinoPinNumber;
						 lines.add(line);
					}
				}
				
            }
             durationSeconds = .001*(System.currentTimeMillis()-startingTime);
            
            FileUtils.writeLines(new File(gloriaFieldPinDefinitionFileName), lines);
            
           
            
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        
        ArrayList<String> analysisLines = new ArrayList();
        
        totalCost = Math.round(totalCost * 100.0) / 100.0;
        double extendedPartCharge = 3*extendedPartsCount;
        analysisLines.add("Total Part Cost," + totalCost);
        analysisLines.add("Extended Part Cost," + extendedPartCharge);
        double twoBoardsCost = extendedPartCharge + 2*totalCost;
        analysisLines.add("Total Cost 2 Boards," + twoBoardsCost + ", Cost Per Board," +  twoBoardsCost/2 );
        
        double fiveBoardsCost = extendedPartCharge + 5*totalCost;
        analysisLines.add("Total Cost 5 Boards," + fiveBoardsCost + ", Cost Per Board," +  fiveBoardsCost/5 );
       
        double tenBoardsCost = extendedPartCharge + 10*totalCost;
        analysisLines.add("Total Cost 10 Boards," + tenBoardsCost + ", Cost Per Board," +  tenBoardsCost/10 );
       //
        
        try (BufferedReader br = new BufferedReader(new FileReader(cplFileName))) 
        {
        	ArrayList<Map.Entry<String, Integer>> pinIndex = new ArrayList();
        	String cplReference;
        	String line;
        	String tokens;
            while ((line = br.readLine()) != null){
            	cplReference=line.split(",")[0];
            	referenceList.remove(cplReference);
            }
            System.out.println("");
            System.out.println("  ");
            Collections.sort(referenceList);  
            analysisLines.add("Components not in the CPL:" );
            for(int j=0;j<referenceList.size();j++) {
            	analysisLines.add((String) referenceList.get(j));
            	
            }
            
        }catch (IOException e) {
        	e.printStackTrace();
        }
        
        
        Enumeration en2 = noJLCPCBomponentIndex.keys();
       // System.out.println("Components without JLCPCB code:");
        while( en2.hasMoreElements()) {
        	String k = (String) en2.nextElement();
        	kicadComponent = (KicadComponent) noJLCPCBomponentIndex.get(k);
        	//System.out.println("ref:" + kicadComponent.getReference());;
        	 analysisLines.add(kicadComponent.getReference());
        }
        
        
        Collections.sort(missingDatasheets);  
        Iterator it = missingDatasheets.iterator();
        //System.out.println("Components without spreadsheet:");
        analysisLines.add("Components without spreadsheet:");
        String ds;
        while(it.hasNext()) {
        	ds=(String) it.next();
        	
        	analysisLines.add(ds);
        }
        Collections.sort(missingPrice);
        analysisLines.add("Components without price:");
        it = missingPrice.iterator();
        while(it.hasNext()) {
        	ds=(String) it.next();
        	analysisLines.add(ds);
        }
        
        analysisLines.add("Components without stock:");
        it = missingStock.iterator();
        while(it.hasNext()) {
        	ds=(String) it.next();
        	analysisLines.add(ds);
        }
        try {
			FileUtils.writeLines(new File(analysisReportFileName), analysisLines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println("It took " + durationSeconds + " seconds.  Saved the following files:");
        System.out.println(gloriaFieldPinDefinitionFileName);
        System.out.println(componentTypeReportFileName);
        System.out.println(analysisReportFileName);
	}

	
	
	private static void downloadDatasheet(String datasheetDir, String componentCode,String url) {
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			connection.connect();

			BufferedReader r  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String destFileName =datasheetDir + componentCode + ".pdf";
			
			try (InputStream in = connection.getInputStream()) {
				Files.copy(in, Paths.get(destFileName), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				System.out.println("Exc Datasheet=" + e);
			}
		}catch(IOException e) {
			missingDatasheets.add( componentCode + " -" + url);
		}

	}
}
