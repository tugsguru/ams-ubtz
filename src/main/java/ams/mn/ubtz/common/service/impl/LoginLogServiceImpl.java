package ams.mn.ubtz.common.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ams.mn.ubtz.common.service.LoginLogService;
import ams.mn.ubtz.config.DataSourceProperties;

@Service
public class LoginLogServiceImpl implements LoginLogService{
	
	private final DataSourceProperties dataSourceProperties;
	 
	public LoginLogServiceImpl(DataSourceProperties dataSourceProperties) {
	   this.dataSourceProperties = dataSourceProperties;
	}

	@Override
	public List<Object> loginLogList() {
		
		List<Object> list = new ArrayList<>();
        String sql = "select u.uname,u.wname,l.date,l.mac_address from log_in l, users u where u.uid=l.uid order by date";
      
        try (
            Connection conn = DriverManager.getConnection(dataSourceProperties.getUrl());
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("uname", rs.getString("uname"));
                row.put("wname", rs.getString("wname"));
                row.put("date", rs.getTimestamp("date"));
                row.put("mac_address", rs.getString("mac_address"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
	}

}
