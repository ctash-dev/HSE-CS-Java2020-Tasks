package ru.hse.cs.java2020.task03;

import java.sql.*;
import java.util.Optional;

public class PostgrBD {

    private static Connection connection;
    private static Statement statement;

    public PostgrBD() {
        setConnection();
    }

    public void setConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/JavaDB", "postgres", "1001");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Optional<BDInfo> getData(String chatId) {

        String token = "";
        String orgid = "";
        String username = "";

        try {
            setConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT \"token\", \"orgid\", \"username\" "
                    + "FROM public.\"BotTable\" WHERE \"chatid\" = '"
                    + chatId + "'");

            if (!resultSet.isBeforeFirst()) {
                return Optional.empty();
            }
            while (resultSet.next()) {
                token = resultSet.getString("token");
                orgid = resultSet.getString("orgid");
                username = resultSet.getString("username");
            }
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return Optional.of(new BDInfo(token, orgid, username));
    }

    public void updateCounter(String chatid, int counter) throws SQLException {
        setConnection();
        statement = connection.createStatement();
        String req = "UPDATE public.\"BotTable\"SET counter ="
                + "'" + counter + "'" + " WHERE \"chatid\" = '"
                + chatid + "'";
        statement.executeUpdate(req);
        connection.close();
    }

    public int getCounter(String chatid) throws SQLException {

        int counter = 0;

        try {
            setConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT \"counter\""
                    + "FROM public.\"BotTable\" WHERE \"chatid\" = '"
                    + chatid + "'");

            if (!resultSet.isBeforeFirst()) {
                return 0;
            }
            while (resultSet.next()) {
                counter = resultSet.getInt("counter");
            }

            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return counter;
    }

    public void insertData(String chatid, BDInfo userInfo) throws SQLException {
        setConnection();
        statement = connection.createStatement();
        String req = "INSERT INTO public.\"BotTable\"(username, chatid, orgid, token) VALUES ("
                + "'" + userInfo.getUsername() + "'" + ", '" + chatid + "',"
                + "'" + userInfo.getOrg() + "'" + "," + "'" + userInfo.getToken() + "')";
        statement.executeUpdate(req);
        connection.close();
    }
}
