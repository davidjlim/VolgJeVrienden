package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
    public Result signin(){
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();
        return ok();

    }

    public Result signup() {
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();

        if(checkUser(pid))
            return badRequest();
        String sql = "INSERT INTO USERS(PID, PASSWORDHASH, IMAGE) VALUES(?, ?, NULL)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pid);
            pstmt.setString(2, DigestUtils.sha1Hex(password));
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ok();
    }

    public Result updateGPS(){
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        double gpsLong = jsonNode.findPath("gpsLong").asDouble();
        double gpsLat = jsonNode.findPath("gpsLat").asDouble();

        if(!checkValidUser(pid, password))
            return unauthorized();

        String sql = "UPDATE USERS GPSLONG=?, GPSLAT=? WHERE PID=?";
        Connection conn = connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gpsLong);
            pstmt.setInt(2, gpsLat);
            pstmt.setString(3, pid);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok("Het ging hopelijk goed! (gpsUpdate)");
    }

    public Result getGPS(){
        Connection conn = connect();

        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

        ArrayNode result = Json.newArray();
        String sql = "SELECT U.PID, U.GPSLONG, U.GPSLAT FROM U.USERS, F.ISFRIENDSWITH " +
                "WHERE F.PID1 = U.PID AND F.PID2 = ?" +
                "UNION SELECT U.PID, U.GPSLONG, U.GPSLAT FROM U.USERS, F.ISFRIENDSWITH " +
                "WHERE F.PID2 = U.PID AND F.PID1 = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ObjectNode request = Json.newObject();
                request.put("pid", rs.getString("PID"));
                request.put("gpsLong", rs.getString("GPSLONG"));
                request.put("gpsLat", rs.getString("GPSLAT"));
                System.out.println(request);
                result.add(request);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return ok(result);
    }

    public Result makeRequest(){

        JsonNode jsonNode = Controller.request().body().asJson();
        String pid1 = jsonNode.findPath("pid1").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid1, password))
            return unauthorized();

        String sql = "INSERT INTO REQUESTS(PID1, PID2) VALUES(?, ?)";
        Connection conn = connect();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pid1);
            pstmt.setString(2, pid2);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ok("Het ging hopelijk goed! (request)");
    }

    public Result getRequests(){
        Connection conn = connect();

        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

        ArrayNode result = Json.newArray();
        String sql = "SELECT * FROM REQUESTS WHERE PID2 = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ObjectNode request = Json.newObject();
                request.put("pid", rs.getString("PID1"));
                System.out.println(request);
                result.add(request);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return ok(result);
    }

    public Result addFriend(){

        return ok("Het ging hopelijk goed! (friends)");
    }

    protected Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(configuration.getString("db.default.url"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    protected Boolean checkUser(String pid){
        String select = "SELECT * FROM USERS WHERE PID = ?";

        try {
            Connection connection = connect();
            PreparedStatement pstmt = connection.prepareStatement(select);
            pstmt.setString(1,pid);
            ResultSet rs = pstmt.executeQuery();
            connection.close();
            if(!rs.next())
                return false;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    protected Boolean checkValidUser(String pid, String password){
        String select = "SELECT * FROM USERS WHERE PID = ? AND PASSWORDHASH = ?";

        try {
            Connection connection = connect();
            PreparedStatement pstmt = connection.prepareStatement(select);
            pstmt.setString(1,pid);
            pstmt.setString(2, DigestUtils.sha1Hex(password));
            ResultSet rs = pstmt.executeQuery();
            connection.close();
            if(!rs.next())
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
