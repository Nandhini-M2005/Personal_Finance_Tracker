package login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.loginandregister.db.DBConnection; // Import the database connection class

public class RegisterServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set CORS headers to allow requests from any origin
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Read request body
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        try {
            // Parse JSON input
            JSONObject inputData = new JSONObject(body.toString());
            String email = inputData.optString("email", "");
            String password = inputData.optString("password", "");

            // Prepare JSON response
            JSONObject responseObj = new JSONObject();

            if (email.length() < 3 || password.length() < 4) {
                responseObj.put("status", false);
                responseObj.put("message", "Email must be at least 3 characters and password at least 4 characters");
            } else {
                if (registerUser(email, password)) {
                    responseObj.put("status", true);
                    responseObj.put("message", "Registration Successful!");
                } else {
                    responseObj.put("status", false);
                    responseObj.put("message", "User already exists!");
                }
            }

            // Send JSON response
            PrintWriter out = response.getWriter();
            out.print(responseObj.toString());
            out.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("status", false);
            errorResponse.put("message", "Invalid request format: " + e.getMessage());
            PrintWriter out = response.getWriter();
            out.print(errorResponse.toString());
            out.flush();
        }
    }

    // Method to register a new user in the database
    private boolean registerUser(String email, String password) {
        boolean isRegistered = false;
        try (Connection conn = DBConnection.getConnection()) {
            // Check if the user already exists
            String checkUserQuery = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return false; // User already exists
                    }
                }
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (email, password) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, password);
                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted > 0) {
                    isRegistered = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isRegistered;
    }
}
