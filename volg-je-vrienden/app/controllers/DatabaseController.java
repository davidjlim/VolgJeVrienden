package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.xpath.internal.SourceTree;

import org.apache.commons.codec.digest.DigestUtils;
import play.Configuration;
import play.Logger;
import play.data.DynamicForm;
import static play.data.Form.form;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Parent class to the controllers handling queries to certain tables in the database
 * Created by s1511432 on 17/05/17.
 */
public class DatabaseController extends Controller {
    /**
     * The connection to the database
     */
    protected Connection conn;

    /**
     * For a user with phonenumber pid, returns their visibility value
     * @param pid the requested user
     * @return the VISIBILITY truth value as int
     */
    protected Integer privateGetVisibility(String pid){
        if(conn == null)
            conn = connect();

        String sql = "SELECT VISIBILITY FROM USERS WHERE PID = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { // if the resultset is non-empty
                Integer visibility = rs.getInt("VISIBILITY");
                pstmt.close();
                return visibility;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    /**
     * Makes a connection to the database and returns it
     * @return the connection
     */
    protected Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(configuration.getString("db.default.url"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Checks whether a user exists in the database
     * @param pid the phoneneumber to be checked
     * @return whether the pid is found in the database
     */
    protected Boolean checkUser(String pid){
        if(conn == null)
            conn = connect();

        String select = "SELECT * FROM USERS WHERE PID = ?";

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(select);
            pstmt.setString(1,pid);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) { // if the resultset is empty
                pstmt.close();
                return false;
            }
            pstmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }

        return false;
    }

    /**
     * Checks whether a user with a certain password exists in the database
     * @param pid the phonenumber to be checked
     * @param password the password to be checked
     * @return whether a pid and password combination is found in the database
     */
    protected Boolean checkValidUser(String pid, String password) {
        if(conn == null)
            conn = connect();

        String select = "SELECT * FROM USERS WHERE PID = ? AND PASSWORDHASH = ?";

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(select);
            pstmt.setString(1,pid);
            pstmt.setString(2, DigestUtils.sha1Hex(password));
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) { // if the resultset is empty
                pstmt.close();
                return false;
            }
            pstmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }

        return false;
    }

    @Inject
    protected Configuration configuration;
}
