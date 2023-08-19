package com.dghdidi.reports.DataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dghdidi.reports.DataBase.ConnectDataBase.executeQuery;

public class GetInformation {
    public static List<String> getReportInfo(int index) throws SQLException {
        String sql = "SELECT player_name, reported_name, staff_name, server_name, reason FROM report_info WHERE `index` = " + index;
        ResultSet resultSet = executeQuery(sql);
        if (!resultSet.next())
            return null;
        List<String> resultList = new ArrayList<>();
        resultList.add(resultSet.getString("player_name"));
        resultList.add(resultSet.getString("reported_name"));
        resultList.add(resultSet.getString("staff_name"));
        resultList.add(resultSet.getString("server_name"));
        resultList.add(resultSet.getString("reason"));
        return resultList;
    }
}
