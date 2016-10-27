package com.elcom.egreen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elcom.egreen.beans.VantageProData;

public class DBUtils {
	
	public static void insertForestLog(Connection conn, VantageProData vantage_pro_data) throws SQLException {
		String sql = "Insert into forecast (next_record, barometer, outside_temperature, outside_humidity, wind_speed, wind_direction, uv, solar_radiation)"
				+ " values (?,?,?,?,?,?,?,?)";
	 
		PreparedStatement pstm = conn.prepareStatement(sql);
	 
		pstm.setInt(1, vantage_pro_data.next_record);
		pstm.setFloat(2, vantage_pro_data.barometer);
		pstm.setFloat(3, vantage_pro_data.outside_temperature);
		pstm.setInt(4, vantage_pro_data.outside_humidity);
		pstm.setInt(5, vantage_pro_data.wind_speed);
		pstm.setInt(6, vantage_pro_data.wind_direction);
		pstm.setInt(7, vantage_pro_data.uv);
		pstm.setInt(8, vantage_pro_data.solar_radiation);
	 
		pstm.executeUpdate();
	}
}
