package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.digest.DigestUtils;
import play.mvc.Controller;
import play.mvc.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by s1511432 on 23/05/17.
 */
public class RequestController extends DatabaseController {
    public Result insertRequest(){
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid1 = jsonNode.findPath("pid1").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String sql = "INSERT INTO USERS(PID1, PID2) VALUES(?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pid1);
            pstmt.setString(2, pid2);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ok("Het ging hopelijk goed! (request)");
    }


}
