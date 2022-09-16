package com.digitalstables.kicadparser;

public class Pin{
		public int pinNumber;
		public String pinName;
		public String arduinoPinType;
		public String arduinPinFunction;
		public int arduinoPinNumber;
		
		Pin(int pinNumber,String pinName,String arduinoPinType,String arduinPinFunction,int arduinoPinNumber){
			this.pinNumber=pinNumber;
			this.pinName=pinName;
			this.arduinoPinType=arduinoPinType;
			this.arduinPinFunction=arduinPinFunction;
			this.arduinoPinNumber=arduinoPinNumber;
		}
	}