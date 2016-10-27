package com.elcom.egreen.davis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.elcom.egreen.beans.VantageProData;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class VantagePro2 extends Thread{
	private Logger logger = Logger.getLogger(VantagePro2.class);
	
	private String port_name = "COM3";
	private int baud_rate = 19200;
	private Boolean connected = false;
	private SerialPort serial_port;
	private OutputStream serial_out;
	private InputStream serial_in;
	
	public int sample_time = 20000;
	
	private final byte ACK = 0x06;
	public VantageProData vangtage = new VantageProData();
	
	public VantagePro2() {
		// TODO Auto-generated constructor stub
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public VantagePro2(String port_name, int baud_rate) {
		this();
		this.port_name = port_name;
		this.baud_rate = baud_rate;
	}
	
	private void connect() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		logger.debug("connect " + port_name);
		CommPortIdentifier comm_port_indentifier = CommPortIdentifier.getPortIdentifier(port_name);
		CommPort comm_port = comm_port_indentifier.open(this.getClass().getName(), 2000);
		if (comm_port instanceof SerialPort)
        {
            serial_port = (SerialPort) comm_port;
            serial_port.setSerialPortParams(baud_rate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
    		serial_port.notifyOnDataAvailable(true);
    		
            serial_out = serial_port.getOutputStream();
            serial_in = serial_port.getInputStream();
            connected = true;
        }
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		byte[] data_in_buff = new byte[1024];
		int data_in_buff_size = 0;
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!connected) {
				try {
					connect();
					serial_out.write("\r".getBytes());
				} catch (NoSuchPortException e) {
					// TODO Auto-generated catch block
					logger.error("NoSuchPortException: " + e.getMessage());
				} catch (PortInUseException e) {
					// TODO Auto-generated catch block
					logger.error("PortInUseException: " + e.getMessage());
				} catch (UnsupportedCommOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			try {
				serial_out.write("LOOP 1\n".getBytes());
				Thread.sleep(500);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			try {
				if (serial_in.available() != 0) {
					data_in_buff_size = serial_in.read(data_in_buff);
					if (data_in_buff_size == 100) {
						if (data_in_buff[0] == ACK && data_in_buff[1] == 'L' && data_in_buff[2] == 'O' && data_in_buff[3] == 'O') {	
							vangtage.next_record = (data_in_buff[7] << 8) + data_in_buff[6];
							vangtage.barometer = ((data_in_buff[9] << 8) + data_in_buff[8])/1000f;
							vangtage.inside_temperature = ((data_in_buff[11] << 8) + data_in_buff[10])/10f;
							vangtage.inside_humidity = data_in_buff[12];
							vangtage.outside_temperature = ((data_in_buff[14] << 8) + data_in_buff[13])/10f;
							vangtage.wind_speed = data_in_buff[15];
							vangtage.avg_wind_speed_10min = data_in_buff[16];
							vangtage.wind_direction = (data_in_buff[18] << 8) + data_in_buff[17];
							vangtage.outside_humidity = data_in_buff[34];
							vangtage.rain_rate = (data_in_buff[43] << 8) + data_in_buff[42];
							vangtage.day_rain = (data_in_buff[52] << 8) + data_in_buff[51];
							vangtage.month_rain = (data_in_buff[54] << 8) + data_in_buff[53];
							vangtage.year_rain = (data_in_buff[56] << 8) + data_in_buff[55];
							vangtage.rain_alarms = data_in_buff[72];
							vangtage.outside_alarms = data_in_buff[73];
							vangtage.forecast_icons = data_in_buff[90];
							vangtage.time_of_sunrise = (data_in_buff[93] << 8) + data_in_buff[92];
							vangtage.time_of_sunset = (data_in_buff[95] << 8) + data_in_buff[94];
							vangtage.solar_radiation = (data_in_buff[46] << 8) + data_in_buff[45];
							vangtage.uv = data_in_buff[44];

							logger.debug("----------- next_record: " + vangtage.next_record + " -----------");
							logger.debug("barometer: " + vangtage.barometer + " Hg");
							logger.debug("inside_temperature: " + vangtage.inside_temperature + " F");
							logger.debug("inside_humidity: " + vangtage.inside_humidity + " %");
							logger.debug("outside_temperature: " + vangtage.outside_temperature + " F");
							logger.debug("wind_speed: " + vangtage.wind_speed + " mph");
							logger.debug("avg_wind_speed_10min: " + vangtage.avg_wind_speed_10min + " mph");
							logger.debug("wind_direction: " + vangtage.wind_direction + " degrees");
							logger.debug("outside_humidity: " + vangtage.outside_humidity + " %");
							logger.debug("solar_radiation: " + vangtage.solar_radiation + " watt/meter2");
							logger.debug("uv: " + vangtage.uv + " index");
							logger.debug("rain_rate: " + vangtage.rain_rate + " clicks");
							logger.debug("day_rain: " + vangtage.day_rain + " clicks");
							logger.debug("month_rain: " + vangtage.month_rain + " clicks");
							logger.debug("year_rain: " + vangtage.year_rain + " clicks");
							logger.debug("rain_alarms: " + vangtage.rain_alarms);
							logger.debug("outside_alarms: " + vangtage.outside_alarms);
							logger.debug("forecast_icons: " + vangtage.forecast_icons);
							logger.debug("time_of_sunrise: " + vangtage.time_of_sunrise/100 + ":" + vangtage.time_of_sunrise%100);
							logger.debug("time_of_sunset: " + vangtage.time_of_sunset/100 + ":" + vangtage.time_of_sunset%100);
							Thread.sleep(sample_time);
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
}
