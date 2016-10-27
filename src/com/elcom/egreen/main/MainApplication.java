/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.egreen.main;

import com.elcom.egreen.plc.*;
import com.elcom.egreen.utils.DBUtils;
import com.elcom.egreen.conn.ConnectionUtils;
import com.elcom.egreen.davis.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author TuTQ
 */
public class MainApplication {
    
    static Logger logger = Logger.getLogger(MainApplication.class.getName());
    
    public static PLC block1;
    public static VantagePro2 weather_link;
    public static Connection conn;
    
    public static void main(String[] args) throws InterruptedException {
        PropertyConfigurator.configure("log4j.properties");
        
        weather_link = new VantagePro2("COM3", 19200);
        weather_link.start();

        block1 = new PLC("192.168.7.243", 502);
        block1.start();
        block1.auto_light = true;
        
        try {
			conn = ConnectionUtils.getConnection();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        while (true) {
        	if (weather_link.have_new_data) {
        		weather_link.have_new_data = false;
    			try {
					DBUtils.insertForestLog(conn, weather_link.vantage);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}
        	}
        	Thread.sleep(1000);
        }
    }
}
