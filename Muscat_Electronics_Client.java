package muscat_electronics_ap;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Muscat_Electronics_Client extends Thread {
    private Scanner scanner;

    public Muscat_Electronics_Client() {
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        /*
            client makes a request to connect to the server.
            This will start communication allowing the transfer of 
            data or any other interaction to occur.
            */
        try (Socket socket = new Socket("localhost", 5000);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            System.out.println("Connected to server.");


            System.out.print("Enter Customer ID (e.g., CUST001): ");
            String customerId = scanner.nextLine();
            if (!customerId.matches("CUST\\d{3}")) {
                System.out.println("Invalid Customer ID. Must be format CUSTxxx (e.g., CUST001).");
                return;
            }

            System.out.print("Enter Amount Spent (OMR): ");
            double amountSpent;
            try {
                amountSpent = Double.parseDouble(scanner.nextLine());
                if (amountSpent < 0) {
                    System.out.println("Amount cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Must be a number.");
                return;
            }

            /*
            Open input data stream to read data.
            Prepare output stream to write data.
            */
            out.writeUTF(customerId);
            out.writeDouble(amountSpent);
            out.flush();

             /*
            Step iv refers to the response the server gave to the customer with the reward
            points and the discount. They are then shown in the application or used in the
            billing or offers.
            */
            int points = in.readInt();
            double discount = in.readDouble();
            String message = in.readUTF();
            if (message.startsWith("Error")) {
                System.out.println("Server Error: " + message);
            } else {
                System.out.println("Reward Points: " + points);
                System.out.println("Discount Value: " + discount + " OMR");
            }
        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
            e.printStackTrace(); 
        } finally {
            scanner.close();
        }
    }

    public static void main(String[] args) {
        Muscat_Electronics_Client mec = new Muscat_Electronics_Client();
        mec.start();
    }
}
