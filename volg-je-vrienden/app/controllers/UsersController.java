package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.codec.digest.DigestUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Manages queries concerning the USERS table
 * Created by s1511432 on 02/06/2017.
 */

public class UsersController extends DatabaseController {
    /**
     * Searches for the user in the database, returns http result accordingly
     * @return Http result code
     */
    public Result signin(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkUser(pid))
            return notFound(); // the user doesn't exist
        if(!checkValidUser(pid, password))
            return unauthorized(); // the password doesn't belong to this pid
        return ok();
    }

    /**
     * If the user doesn't already exist, insert them into USERS
     * @return Http result code
     */
    public Result signup() {
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();

        if(checkUser(pid))
            return badRequest(); // a user with this pid already exists
        String sql = "INSERT INTO USERS(PID, PASSWORDHASH, IMAGE) VALUES(?, ?, NULL)";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, DigestUtils.sha1Hex(password)); // insert hashed password into the db
            pstmt.executeUpdate();
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

        return ok();
    }

    /**
     * Updates a user's gps location
     * @return Http result code
     */
    public Result updateGPS(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        double gpsLong = jsonNode.findPath("gpsLong").asDouble();
        double gpsLat = jsonNode.findPath("gpsLat").asDouble();

        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        String sql = "UPDATE USERS SET GPSLONG=?, GPSLAT=? WHERE PID=?";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, gpsLong);
            pstmt.setDouble(2, gpsLat);
            pstmt.setString(3, pid);
            pstmt.executeUpdate();
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
        return ok();
    }

    /**
     * Adds an image as base64 string to the user in USERS
     * @return Http result code
     * @throws SQLException
     */
    public Result addImage() throws SQLException {
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        String image = jsonNode.findPath("image").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        String sql = "UPDATE USERS SET IMAGE=? WHERE PID=?";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, image);
            pstmt.setString(2, pid);
            pstmt.executeUpdate();
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
        return ok();
    }

    /**
     * Gets the user's image from USERS
     * @return Http result code and if successful an ObjectNode accompanying it
     */
    public Result getOwnImage(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        String sql = "SELECT IMAGE FROM USERS WHERE PID = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ObjectNode request = Json.newObject();
                request.put("image", rs.getString("IMAGE"));
                pstmt.close();
                return ok(request); // returns the row in an ObjectNode
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
        return internalServerError();
    }

    /**
     * Sets the user's VISIBILITY boolean int in the table USERS to the desired value
     * @return Http result code
     */
    public Result setVisibility(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        Boolean visibility = jsonNode.findPath("visibility").asBoolean();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        String sql = "UPDATE USERS SET VISIBILITY=? WHERE PID=?";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, visibility);
            pstmt.setString(2, pid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            try {
                pstmt.close();
            } catch (SQLException e1) {
                return internalServerError();
            }
            return internalServerError();
        } finally {
            if(pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    return internalServerError();
                }
        }
        return ok();
    }

    /**
     * Gets the user's visibility
     * @return Http result code and if successful an ObjectNode accompanying it
     */
    public Result getVisibility(){
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        ObjectNode request = Json.newObject();
        Integer visibility = privateGetVisibility(pid);
        if(visibility == null)
            return internalServerError();
        request.put("visibility", visibility);
        return ok(request);
    }


}
