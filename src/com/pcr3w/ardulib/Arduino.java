package com.pcr3w.ardulib;

import java.util.ArrayList;
import java.util.List;

import jssc.SerialPort;
import jssc.SerialPortException;

public class Arduino {
	
	public static final byte HIGH = 1;
	public static final byte LOW = 0;
	public static final byte INPUT = 0;
	public static final byte OUTPUT = 1;
	public static final byte INPUT_PULLUP = 2;
	
	private SerialPort serial;
	private volatile StringBuilder data = new StringBuilder();
	private boolean ready = false;
	private List<InputListener<String>> inputListeners = new ArrayList<InputListener<String>>();
	private ReadyListener readyListener = () -> {};
	
	private Arduino(int baudRate, char ack) throws SerialPortException {
		this.serial = new SerialPort("COM3");
		
		this.serial.openPort();
		this.serial.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		
		this.serial.addEventListener(event -> {
			if(event.isRXCHAR() && event.getEventValue() > 0) {
				try {
					if(this.ready) {
						byte[] input = this.serial.readBytes(event.getEventValue());
						
						for(byte dataByte : input) {
							if(dataByte == '\r') {
								String oldData = this.data.toString();
								this.data = new StringBuilder();
								
								new Thread(() -> {
									for(InputListener<String> listener : this.inputListeners) {
										try {
											listener.accept(oldData);
										} catch(Exception e) {
											e.printStackTrace();
										}
									}
								}).start();
								
								break;
							} else if(dataByte != '\n') {
								this.data.append((char) dataByte);
							}
						}
					} else {
						if(this.serial.readBytes(event.getEventValue())[0] == ack) {
							this.ready = true;
							
							this.serial.writeByte((byte) ack);
							
							new Thread(() -> {
								try {
									readyListener.run();
								} catch(Exception e) {
									e.printStackTrace();
								}
							}).start();
						}
					}
				} catch(SerialPortException e) {
					e.printStackTrace();
				}
			}
		});
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				serial.closePort();
			} catch(SerialPortException e) {
				e.printStackTrace();
			}
		}));
	}
	
	public void send(String data) throws SerialPortException {
		this.serial.writeString(data);
	}
	
	public void send(byte[] data) throws SerialPortException {
		this.serial.writeBytes(data);
	}
	
	public void send(byte data) throws SerialPortException {
		this.serial.writeByte(data);
	}
	
	public void send(int[] data) throws SerialPortException {
		this.serial.writeIntArray(data);
	}
	
	public void send(int data) throws SerialPortException {
		this.serial.writeInt(data);
	}
	
	public void onInput(InputListener<String> listener) {
		this.inputListeners.add(listener);
	}
	
	public void onReady(ReadyListener listener) {
		this.readyListener = listener;
	}
	
	public boolean isReady() {
		return this.ready;
	}
	
	public boolean disconnect() throws SerialPortException {
		return this.serial.closePort();
	}
	
	public void pinMode(byte pin, byte mode) throws SerialPortException {
		this.serial.writeBytes(new byte[]{1, pin, mode, '.'});
	}
	
	public void digitalWrite(byte pin, byte mode) throws SerialPortException {
		this.serial.writeBytes(new byte[]{2, pin, mode, '.'});
	}
	
	public void analogWrite(byte pin, byte mode) throws SerialPortException {
		this.serial.writeBytes(new byte[]{3, pin, mode, '.'});
	}
	
	public void delay(long millis) {
		try {
			Thread.sleep(millis);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static Arduino connect(char ack) throws SerialPortException {
		return connect(SerialPort.BAUDRATE_9600, ack);
	}
	
	public static Arduino connect(int baudRate, char ack) throws SerialPortException {
		return new Arduino(baudRate, ack);
	}

	public static interface InputListener<T> {
		
		public void accept(T data) throws Exception;
		
	}
	
	public static interface ReadyListener {
		
		public void run() throws Exception;
		
	}
	
}
