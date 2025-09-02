package muscat_electronics_ap;

import java.util.*;

public class Muscat_Electronics_Utility {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<String> products = new ArrayList<>();  // Stores product names
        boolean running = true;

        while (running) {
            System.out.println("\n===== Muscat Electronics Utility =====");
            System.out.println("1. Add Product");
            System.out.println("2. Remove Product");
            System.out.println("3. Display All Products");
            System.out.println("4. Search Product");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();
            sc.nextLine();  // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter product name to add: ");
                    String newProduct = sc.nextLine();
                    products.add(newProduct);
                    Collections.sort(products); // sort list after adding
                    System.out.println(newProduct + " added successfully.");
                    break;

                case 2:
                    System.out.print("Enter product name to remove: ");
                    String removeProduct = sc.nextLine();
                    if (products.remove(removeProduct)) {
                        System.out.println(removeProduct + " removed successfully.");
                    } else {
                        System.out.println("Product not found!");
                    }
                    break;

                case 3:
                    System.out.println("\n--- Product List ---");
                    if (products.isEmpty()) {
                        System.out.println("No products available.");
                    } else {
                        for (String p : products) {
                            System.out.println("- " + p);
                        }
                    }
                    break;

                case 4:
                    System.out.print("Enter product name to search: ");
                    String searchProduct = sc.nextLine();
                    int index = Collections.binarySearch(products, searchProduct);
                    if (index >= 0) {
                        System.out.println(searchProduct + " found at position " + (index + 1));
                    } else {
                        System.out.println(searchProduct + " not found.");
                    }
                    break;

                case 5:
                    running = false;
                    System.out.println("Exiting... Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
        sc.close();
    }
}
