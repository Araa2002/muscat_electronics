import java.io.*;
import java.net.*;
import java.sql.*;

public class Muscat_Electronics_Server extends Thread {
    private ServerSocket serverSocket;
    private static final int PORT = 5000;
    private static final String DB_URL = "jdbc:derby://localhost:1527/muscat_electronics";
    private static final String DB_USER = "ap_assignment";
    private static final String DB_PASSWORD = "12345";
    private volatile boolean running = true;

    public Muscat_Electronics_Server() {
        try {
            // No need to load Derby client driver explicitly
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Server Setup Error: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            } catch (IOException e) {
                if (!running) break;
                System.err.println("Error accepting client: " + e.getMessage());
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Socket s = socket;
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

                // Read client input
                String customerId = in.readUTF();
                double amountSpent = in.readDouble();
                System.out.println("Received: customerId=" + customerId + ", amountSpent=" + amountSpent);

                // Validate input
                if (!customerId.matches("CUST\\d{3}")) {
                    out.writeInt(0);
                    out.writeDouble(0);
                    out.writeUTF("Error: Invalid Customer ID format");
                    return;
                }
                if (amountSpent < 0) {
                    out.writeInt(0);
                    out.writeDouble(0);
                    out.writeUTF("Error: Invalid amount");
                    return;
                }

                // Fetch config from database
                try (PreparedStatement stmt = dbConnection.prepareStatement(
                        "SELECT point_rate, baisa_per_point, minimum_purchase FROM rewards_config WHERE customer_id = ?")) {
                    stmt.setString(1, customerId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int pointRate = rs.getInt("point_rate");
                            double baisaPerPoint = rs.getDouble("baisa_per_point");
                            double minimumPurchase = rs.getDouble("minimum_purchase");

                            // Calculate points and discount
                            int points = amountSpent >= minimumPurchase ? (int) (amountSpent * pointRate) : 0;
                            double discount = (points * baisaPerPoint) / 1000;

                            // Send results
                            out.writeInt(points);
                            out.writeDouble(discount);
                            out.writeUTF("Success");
                            System.out.println("Processed for " + customerId + ": Points=" + points + ", Discount=" + discount);
                        } else {
                            out.writeInt(0);
                            out.writeDouble(0);
                            out.writeUTF("Error: Customer ID not found");
                        }
                    }
                } catch (SQLException e) {
                    out.writeInt(0);
                    out.writeDouble(0);
                    out.writeUTF("Error: Database error - " + e.getMessage());
                    System.err.println("Database error: " + e.getMessage());
                }
            } catch (IOException | SQLException e) {
                System.err.println("Error in ClientHandler: " + e.getMessage());
            }
        }
    }

    public void closeConnection() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Muscat_Electronics_Server server = new Muscat_Electronics_Server();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::closeConnection));
    }
}
