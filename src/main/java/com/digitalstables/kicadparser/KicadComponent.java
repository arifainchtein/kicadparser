package com.digitalstables.kicadparser;

import java.util.Vector;

public class KicadComponent {
	String value="";
	String footprint="";
	String description="";
	String part="";
	String reference="";
	String jlcpcbPart="";
	String jlcpcpType="";
	String datasheet="";
	String symbolLibrary="";
	Vector references= new Vector();
	
	public void addReference(String r) {
		references.addElement(r);
	}
	public String getValue() {
		return value;
	}
	public String getFootprint() {
		return footprint;
	}
	public String getDescription() {
		return description;
	}
	public String getPart() {
		return part;
	}
	public String getReference() {
		return reference;
	}
	public String getJlcpcbPart() {
		return jlcpcbPart;
	}
	public String getJlcpcpType() {
		return jlcpcpType;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setFootprint(String footprint) {
		this.footprint = footprint;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setPart(String part) {
		this.part = part;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}
	public void setJlcpcbPart(String jlcpcbPart) {
		this.jlcpcbPart = jlcpcbPart;
	}
	public void setJlcpcpType(String jlcpcpType) {
		this.jlcpcpType = jlcpcpType;
	}
	public String getDatasheet() {
		return datasheet;
	}
	public void setDatasheet(String datasheet) {
		this.datasheet = datasheet;
	}
	public String getSymbolLibrary() {
		return symbolLibrary;
	}
	public void setSymbolLibrary(String symbolLibrary) {
		this.symbolLibrary = symbolLibrary;
	}
	public String getCSVLine() {
		// TODO Auto-generated method stub
		StringBuffer ref= new StringBuffer();
		for(int i=0;i<references.size();i++) {
			if(i>0)ref.append(" ");
			ref.append(references.elementAt(i));
			
		}
		return ref.toString() + "," + value + "," + jlcpcbPart + "," + jlcpcpType + ","+ footprint + "," + description + "," + part + ","  + datasheet + "," + symbolLibrary;
	}
}
