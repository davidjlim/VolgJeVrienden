package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Manages queries concerning the ISFRIENDSWITH table
 * Created by s1511432 on 02/06/2017.
 */

public class FriendsController extends DatabaseController {
    /**
     * If a request exists, remove the request from REQUESTS and add the friend pair to ISFRIENDSWITH
     * @return Http result code
     */
    public Result addFriend(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // check whether the user is authorised

        String sql = "SELECT * FROM REQUESTS WHERE PID1 = ? AND PID2 = ?";
        String sql2 = "DELETE FROM REQUESTS WHERE PID1= ? AND PID2 = ?";
        String sql3 = "INSERT INTO ISFRIENDSWITH VALUES (?,?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid2);
            pstmt.setString(2, pid);
            ResultSet rs = pstmt.executeQuery(); // checks whether the request exists
            if(!rs.next()) { // if the request doesn't exist
                pstmt.close();
                return badRequest();
            }
            pstmt = conn.prepareStatement(sql2);
            pstmt.setString(1, pid2);
            pstmt.setString(2, pid);
            pstmt.executeUpdate(); // remove the request from REQUESTS
            pstmt = conn.prepareStatement(sql3);
            pstmt.setString(1, pid2);
            pstmt.setString(2, pid);
            pstmt.executeUpdate(); // add the friendship to ISFRIENDSWITH
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
     * Gets all friends' pid, gps coordinates and image from USERS belonging to pid in ISFRIENDSWITH,
     * which is passed by a json object by post request
     * @return Http result code and if successful an arraynode accompanying it
     */
    public Result getFriends(){
        if(conn == null)
            conn = connect();

        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised
        ArrayNode result = Json.newArray();
        String sql = "SELECT U.PID, U.IMAGE, U.GPSLONG, U.GPSLAT " +
                "FROM ((SELECT PID1 AS PID FROM ISFRIENDSWITH WHERE PID2 = ? " +
                "UNION SELECT PID2 AS PID FROM ISFRIENDSWITH WHERE PID1 = ?) AS P) " +
                "INNER JOIN USERS U " +
                "ON U.PID = P.PID " +
                "WHERE U.VISIBILITY = 1"; // Finds the pids from all friends, then
        // cross reference it with all visible users and select the desired info
        PreparedStatement pstmt = null;
        if(privateGetVisibility(pid) == 1) {
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, pid);
                pstmt.setString(2, pid);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    ObjectNode request = Json.newObject();
                    request.put("pid", rs.getString("PID"));
                    request.put("image", rs.getString("IMAGE"));
                    request.put("gpsLong", rs.getObject("GPSLONG") != null ? rs.getDouble("GPSLONG") : null);
                    request.put("gpsLat", rs.getObject("GPSLAT") != null ? rs.getDouble("GPSLAT") : null);
                    result.add(request); // packs the rows into ObjectNodes and insert into ArrayNode
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (pstmt != null)
                    try {
                        pstmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
        }
        return ok(result);
    }

    /**
     * Removes a friend pair in ISFRIENDSWITH; pid, password and pid2 are passed by json object
     * by post request.
     * @return Http result code
     */
    public Result removeFriend(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        String sql = "DELETE FROM ISFRIENDSWITH WHERE (PID1=? AND PID2=?) OR (PID1=? AND PID2=?)";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid2);
            pstmt.setString(3, pid2);
            pstmt.setString(4, pid); // Being friends is bidirectional, so both orders are possible
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
}
