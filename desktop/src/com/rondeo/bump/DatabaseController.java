package com.rondeo.bump;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.rondeo.bump.util.Network.Account;
import com.rondeo.bump.util.Network.Message;

public class DatabaseController {
    private Connection connection;
    private Statement statement;

    String[] initSQLStrings = {
        "CREATE TABLE if not exists users"+
            "("+
                " ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                " username VARCHAR, "+
                " password VARCHAR, "+
                " fullname VARCHAR, "+
                " points BIGINT, "+
                " created DATETIME DEFAULT (datetime('now','localtime')) "+
            ")"
    };

    public DatabaseController() throws SQLException {
        connection = DriverManager.getConnection( "jdbc:sqlite:database.db" );
        statement = connection.createStatement();

        for (String string : initSQLStrings) {
            statement.executeUpdate( string );
        }
    }

    public Message registerUser( String username, String password, String fullname ) throws SQLException {
        String sql = String.format( "SELECT * FROM users WHERE username = '%s'", username );
        ResultSet results = statement.executeQuery( sql );
        if( results.next() ) {
            return new Message( "Username already exists!" );
        }
        sql = String.format( "INSERT INTO users (username, password, fullname, points) VALUES ( '%s', '%s', '%s', 0 )", username, password, fullname );
        if( statement.executeUpdate( sql ) > 0 ) {
            return new Message();
        } 
        return new Message( "Sorry. Unable to register!" );
    }

    public Account loginUser( String username, String password ) throws SQLException {
        String sql = String.format( "SELECT * FROM users WHERE username = '%s' AND password = '%s'", username, password );
        //System.out.println( sql );
        ResultSet results = statement.executeQuery( sql );
        while( results.next() ) {
            Account account = new Account();
            account.ID = results.getInt( "ID" );
            account.username = results.getString( "username" );
            account.fullname = results.getString( "fullname" );
            account.points = results.getInt( "points" );
            //System.out.println( account );
            return account;
        }
        return new Account();
    }

    public Account updatePoints( int ID, int points ) throws SQLException {
        String sql = String.format( "UPDATE users SET points = %i WHERE ID = %i", points, ID );
        if( statement.executeUpdate( sql ) > 0 ) {
            sql = String.format( "SELECT * FROM users WHERE ID = %i", ID );
            ResultSet results = statement.executeQuery( sql );
            while( results.next() ) {
                Account account = new Account();
                account.ID = results.getInt( "ID" );
                account.username = results.getString( "username" );
                account.fullname = results.getString( "fullname" );
                account.points = results.getInt( "points" );
                return account;
            }
        }
        return new Account();
    }

    public void close() throws SQLException {
        if( connection != null ) {
            connection.close();
        }
    }

}
