package controllers;

import play.libs.Json;
import play.mvc.*;

import views.html.*;
import com.fasterxml.jackson.databind.node.*;

import java.sql.*;
import play.Configuration;
import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        insert("06|7838449");

        return ok(index.render("Hoi"));
    }

    /**
     * Connect to the test.db database
     *
     * @return the Connection object
     */
    private Connection connect() {
        Connection connection = null;
        try {
            System.out.println(configuration.getString("db.default.url"));
            connection = DriverManager.getConnection(configuration.getString("db.default.url"));
                    //"jdbc:sqlite:/scratch/s1511432/AndroidStudioProjects/volg-je-vrienden/friends.db");
        } catch (SQLException e) {
            System.out.println(e);
        }
        return connection;
    }

    /**
     * Insert a new row into the warehouses table
     *
     * @param pid
     */
    public void insert(String pid) {
        String sql = "INSERT INTO USERS(PID) VALUES(?)";

        try (Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Inject
    private Configuration configuration;

}
