package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
 * Created by s1511432 on 17/05/17.
 */
public class DatabaseController extends Controller {
    protected Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(configuration.getString("db.default.url"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    protected Boolean checkValidUser(String pid, String password){
        String select = "SELECT * FROM USERS WHERE PID = ? AND PASSWORD = ?";

        try {
            Connection connection = connect();
            PreparedStatement pstmt = connection.prepareStatement(select);
            pstmt.setString(1,pid);
            pstmt.setString(2, DigestUtils.sha1Hex(password));
            if(!pstmt.executeQuery().next())
                return false;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Inject
    private Configuration configuration;
}
