package com.elcom.egreen.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.elcom.egreen.beans.VantageProData;

public class DBUtils {
	public static void insertForestLog(Connection conn, VantageProData vantage_pro_data) throws SQLException {
		String sql = "Insert into forecast (barometer, outside_temperature, outside_humidity) values (?,?,?)";
	 
		PreparedStatement pstm = conn.prepareStatement(sql);
	 
		pstm.setFloat(1, vantage_pro_data.barometer);
		pstm.setFloat(2, vantage_pro_data.outside_temperature);
		pstm.setInt(3, vantage_pro_data.outside_humidity);
	 
		pstm.executeUpdate();
	}
}
