package com.pcr3w.ardulib.test;

import java.util.Arrays;

import com.pcr3w.ardulib.Arduino;

import jssc.SerialPortException;

public class Test {
	
	public static void main(String[] args) throws SerialPortException {	
		Arduino arduino = Arduino.connect((char) 66);
		
		arduino.onInput(data -> System.out.println(Arrays.toString(data.getBytes())));
		arduino.onReady(() -> {
			arduino.pinMode((byte) 13, Arduino.OUTPUT);
			
			while(true) {
				arduino.digitalWrite((byte) 13, Arduino.HIGH);
				arduino.delay(500);
				arduino.digitalWrite((byte) 13, Arduino.LOW);
				arduino.delay(500);
			}
		});
	}
	
}
