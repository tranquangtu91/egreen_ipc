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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author TuTQ
 */
public class MainApplication {
    
    static Logger log = Logger.getLogger(MainApplication.class.getName());
    
    public static PLC block1;
    public static VantagePro2 weather_link;
    public static Connection conn;
    
    public static void main(String[] args) throws InterruptedException {
        BasicConfigurator.configure();
        
        weather_link = new VantagePro2("COM3", 19200);
        weather_link.start();

        block1 = new PLC("192.168.7.243", 502);
        block1.start();
        block1.auto_light = true;
        
        try {
			conn = ConnectionUtils.getConnection();
			DBUtils.insertForestLog(conn, weather_link.vangtage);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        while (true) {
        	
        }
    }
}
