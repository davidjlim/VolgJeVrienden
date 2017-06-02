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
 * Created by s1511432 on 17/05/17.
 */
public class DatabaseController extends Controller {
    Connection conn;

    public Result signin(){
        if(conn == null)
            conn = connect();
        System.out.println("Signing in");
        JsonNode jsonNode = Controller.request().body().asJson();
        System.out.println(jsonNode.toString());
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkUser(pid))
            return notFound();
        if(!checkValidUser(pid, password))
            return unauthorized();
        return ok();
    }

    public Result signup() {
        if(conn == null)
            conn = connect();
        System.out.printf("Signing up");
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();

        if(checkUser(pid))
            return badRequest();
        String sql = "INSERT INTO USERS(PID, PASSWORDHASH, IMAGE) VALUES(?, ?, NULL)";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, DigestUtils.sha1Hex(password));
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

        return ok("Did it work?");
    }

    public Result updateGPS(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        double gpsLong = jsonNode.findPath("gpsLong").asDouble();
        double gpsLat = jsonNode.findPath("gpsLat").asDouble();

        if(!checkValidUser(pid, password))
            return unauthorized();

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
        return ok("Het ging hopelijk goed! (gpsUpdate)");
    }

    public Result getGPS(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

        ArrayNode result = Json.newArray();
        String sql = "SELECT U.PID, U.GPSLONG, U.GPSLAT FROM USERS U, ISFRIENDSWITH F" +
                "WHERE F.PID1 = U.PID AND F.PID2 = ?" +
                "UNION SELECT U.PID, U.GPSLONG, U.GPSLAT FROM USERS U, ISFRIENDSWITH F" +
                "WHERE F.PID2 = U.PID AND F.PID1 = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ObjectNode request = Json.newObject();
                request.put("pid", rs.getString("PID"));
                request.put("gpsLong", rs.getDouble("GPSLONG"));
                request.put("gpsLat", rs.getDouble("GPSLAT"));
                System.out.println(request);
                result.add(request);
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
        System.out.println(result);
        return ok(result);
    }

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

    public Result getRequests(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
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
            if(pstmt != null)
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        System.out.println(result);
        return ok(result);
    }

    public Result addFriend(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        System.out.println(jsonNode.toString());
        String pid = jsonNode.findPath("pid").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

        String sql = "SELECT * FROM REQUESTS WHERE PID1 = ? AND PID2 = ?";
        String sql2 = "DELETE FROM REQUESTS WHERE PID1= ? AND PID2 = ?";
        String sql3 = "INSERT INTO ISFRIENDSWITH VALUES (?,?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid2);
            pstmt.setString(2, pid);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
                pstmt.close();
                return badRequest();
            }
            pstmt = conn.prepareStatement(sql2);
            pstmt.setString(1, pid2);
            pstmt.setString(2, pid);
            pstmt.executeUpdate();
            pstmt = conn.prepareStatement(sql3);
            pstmt.setString(1, pid2);
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
        return ok("Het ging hopelijk goed! (friends)");
    }

    public Result getFriends(){
        if(conn == null)
            conn = connect();

        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();
        ArrayNode result = Json.newArray();
        String sql = "SELECT U.PID, U.IMAGE, U.GPSLONG, U.GPSLAT " +
                "FROM ((SELECT PID1 AS PID FROM ISFRIENDSWITH WHERE PID2 = ? " +
                "UNION SELECT PID2 AS PID FROM ISFRIENDSWITH WHERE PID1 = ?) AS P) " +
                "INNER JOIN USERS U " +
                "ON U.PID = P.PID " +
                "WHERE U.VISIBILITY = 1";
        PreparedStatement pstmt = null;
        System.out.println(jsonNode);
        Integer vis = privateGetVisibility(pid);
        System.out.println(vis);
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
        }
        System.out.println("Friends: " + result);
        return ok(result);
    }

    public Result removeFriend(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String pid2 = jsonNode.findPath("pid2").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

        String sql = "DELETE FROM ISFRIENDSWITH WHERE (PID1=? AND PID2=?) OR (PID1=? AND PID2=?)";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            pstmt.setString(2, pid2);
            pstmt.setString(3, pid2);
            pstmt.setString(4, pid);
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
        return ok("Het ging hopelijk goed! (removeFriend)");
    }

    public Result addImage() throws SQLException {
        if(conn == null)
            conn = connect();
        System.out.printf("Adding image...");
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        String image = jsonNode.findPath("image").asText();
        System.out.println(image.length());
        if(!checkValidUser(pid, password))
            return unauthorized();

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

    public Result getOwnImage(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

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
                return ok(request);
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

    public Result setVisibility(){
        if(conn == null)
            conn = connect();
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        Boolean visibility = jsonNode.findPath("visibility").asBoolean();
        if(!checkValidUser(pid, password))
            return unauthorized();

        String sql = "UPDATE USERS SET VISIBILITY=? WHERE PID=?";
        PreparedStatement pstmt = null;
        try {pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, visibility);
            pstmt.setString(2, pid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
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

    public Result getVisibility(){
        JsonNode jsonNode = Controller.request().body().asJson();
        String pid = jsonNode.findPath("pid").asText();
        String password = jsonNode.findPath("password").asText();
        if(!checkValidUser(pid, password))
            return unauthorized();

        ObjectNode request = Json.newObject();
        Integer visibility = privateGetVisibility(pid);
        if(visibility == null)
            return internalServerError();
        request.put("visibility", visibility);
        return ok(request);
    }

    public Integer privateGetVisibility(String pid){
        if(conn == null)
            conn = connect();

        String sql = "SELECT VISIBILITY FROM USERS WHERE PID = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, pid);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
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
        if(conn == null)
            conn = connect();

        String select = "SELECT * FROM USERS WHERE PID = ?";

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(select);
            pstmt.setString(1,pid);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.next()) {
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
            if(!rs.next()) {
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
    private Configuration configuration;
}
