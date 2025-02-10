package com.example.layeredarchitecture.CrudUtill;

import com.example.layeredarchitecture.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBMGT {
    public static <t>t execute(String sql,Object... params)throws SQLException,ClassNotFoundException {
        Connection connection = DBConnection.getDbConnection().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }

        if (sql.startsWith("select") || sql.startsWith("SELECT")){
            ResultSet resultSet = preparedStatement.executeQuery();
            return (t) resultSet;

        }else{
            int i=preparedStatement.executeUpdate();

            boolean isSaved=i>0;

            return (t)((Boolean) isSaved);
        }
    }
}
