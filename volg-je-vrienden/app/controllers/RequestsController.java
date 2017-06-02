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
            return unauthorized();
        if(!checkUser(pid2))
            return badRequest();
        String sql = "SELECT * FROM REQUESTS WHERE PID1 = ? AND PID2 = ? " +
                "UNION SELECT * FROM REQUESTS WHERE PID1 = ? AND PID2 = ?";
        String sql2 = "INSERT INTO REQUESTS(PID1, PID2) VALUES(?, ?)";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid2);
            pstmt.setString(3, pid2);
            pstmt.setString(4, pid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                pstmt.close();
                return badRequest();
            }
            pstmt = conn.prepareStatement(sql2);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid2);
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
        return ok("Het ging hopelijk goed! (request)");
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
            return unauthorized();

        ArrayNode result = Json.newArray();
        String sql = "SELECT * FROM REQUESTS WHERE PID2 = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ObjectNode request = Json.newObject();
                request.put("pid", rs.getString("PID1"));
                System.out.println(request);
                result.add(request);
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
        System.out.println(result);
        return ok(result);
    }
}
