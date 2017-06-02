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
 * Manages queries concerning the REQUESTS table
 * Created by Tijdelijk on 02/06/2017.
 */

public class RequestsController extends DatabaseController {
    /**
     * Inserts a request into REQUESTS, checks whether the requested user exists and whether a
     * request already exists
     * @return Http result code
     */
    public Result makeRequest(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised
        if(!checkUser(pid2))
            return badRequest(); // checks whether the requested user exists
        String sql = "SELECT * FROM REQUESTS WHERE PID1 = ? AND PID2 = ? " +
                "UNION SELECT * FROM REQUESTS WHERE PID1 = ? AND PID2 = ?";
        String sql2 = "INSERT INTO REQUESTS(PID1, PID2) VALUES(?, ?)";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid2);
            pstmt.setString(3, pid2);
            pstmt.setString(4, pid);
            ResultSet rs = pstmt.executeQuery(); // check whether the request already exists
            if (rs.next()) {
                pstmt.close();
                return badRequest();
            }
            pstmt = conn.prepareStatement(sql2);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid2);
            pstmt.executeUpdate(); // inserts the request into REQUESTS
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
     * Get all requests to the user
     * @return Http result code and if successful an arraynode accompanying it
     */
    public Result getRequests() {
        if (conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if (!checkValidUser(pid, password))
            return unauthorized(); // checks whether the user is authorised

        ArrayNode result = Json.newArray();
        String sql = "SELECT * FROM REQUESTS WHERE PID2 = ?"; // here only pid2 is desired, because pid1 made the request
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ObjectNode request = Json.newObject();
                request.put("pid", rs.getString("PID1"));
                result.add(request); // packs all the rows into ObjectNodes into ArraynNde
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
        return ok(result);
    }
}
