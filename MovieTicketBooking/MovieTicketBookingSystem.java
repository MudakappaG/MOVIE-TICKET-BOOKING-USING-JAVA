package MovieTicketBooking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MovieTicketBookingSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movie_ticket_booking";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        Connection connection = null;
        try {
            // Establish database connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Display movie list
            while(true) {
            List<Movie> movies = getMovieList(connection);
            displayMovieList(movies);

            // Select movie
            int movieIndex = getMovieIndex(movies.size());
            Movie selectedMovie = movies.get(movieIndex);

            // Get user details
            String name = getUserName();
            String mobileNumber = getUserMobileNumber();

            // Display seat layout
            displaySeatLayout(selectedMovie);

            // Select seats
            List<Seat> selectedSeats = getSelectedSeats(selectedMovie);

            // Calculate total amount
            double totalAmount = calculateTotalAmount(selectedSeats);

            // Make payment
            boolean paymentSuccessful = makePayment(totalAmount);

            if (paymentSuccessful) {
                // Store booking information in database
                saveBooking(connection, name, mobileNumber, selectedMovie, selectedSeats);

                // Print receipt
                printReceipt(name, mobileNumber, selectedMovie, selectedSeats, totalAmount);
            } else {
                System.out.println("Payment was not successful. Booking canceled.");
            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close database connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Movie> getMovieList(Connection connection) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM movies";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int rows = resultSet.getInt("rows");
                int columns = resultSet.getInt("columns");
                Movie movie = new Movie(name, rows, columns);
                movies.add(movie);
            }
        }
        return movies;
    }

    private static void displayMovieList(List<Movie> movies) {
        System.out.println("----- Movie List -----");
        for (int i = 0; i < movies.size(); i++) {
            System.out.println(i + 1 + ". " + movies.get(i).getName());
        }
        System.out.println();
    }

    private static int getMovieIndex(int movieCount) {
        Scanner scanner = new Scanner(System.in);
        int movieIndex;
        do {
            System.out.print("Enter the movie number: ");
            movieIndex = scanner.nextInt();
        } while (movieIndex < 1 || movieIndex > movieCount);

        return movieIndex - 1;
    }

    private static String getUserName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        return scanner.nextLine();
    }

    private static String getUserMobileNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your mobile number: ");
        return scanner.nextLine();
    }

    private static void displaySeatLayout(Movie movie) {
        System.out.println("----- Seat Layout -----");
        
        // Iterate over rows and columns
        for (int row = 0; row < movie.getRows(); row++) {
            for (int column = 0; column < movie.getColumns(); column++) {
                // Check seat availability
                Seat seat = new Seat(row, column);
                boolean isAvailable = isSeatAvailable(movie, seat);
                
                // Display seat status
                if (isAvailable) {
                    System.out.print("O "); // Available seat
                } else {
                    System.out.print("X "); // Booked seat
                }
            }
            System.out.println(); // Move to the next row
        }
        
        System.out.println();
    }

    private static boolean isSeatAvailable(Movie movie, Seat seat) {
        // Retrieve the seat status from the database or any other data source
        // For now, assume all seats are available initially
        boolean isAvailable = true;

        // Implement your logic to check seat availability based on your requirements
        // You can use the movie and seat information to determine the availability
        // For example, you might query the database to check if the seat is booked

        // Replace the following placeholder code with your logic
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT * FROM bookings WHERE movie_name = ? AND seat_row = ? AND seat_column = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, movie.getName());
            preparedStatement.setInt(2, seat.getRow());
            preparedStatement.setInt(3, seat.getColumn());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Seat is already booked
                isAvailable = false;
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isAvailable;
    }



    private static List<Seat> getSelectedSeats(Movie movie) {
        List<Seat> selectedSeats = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        int totalSeats = movie.getRows() * movie.getColumns();

        System.out.print("Enter the seat numbers (comma-separated): ");
        String seatInput = scanner.nextLine();
        String[] seatNumbers = seatInput.split(",");

        for (String seatNumber : seatNumbers) {
            int seatIndex = Integer.parseInt(seatNumber.trim());
            if (seatIndex >= 1 && seatIndex <= totalSeats) {
                int row = (seatIndex - 1) / movie.getColumns();
                int column = (seatIndex - 1) % movie.getColumns();
                Seat seat = new Seat(row, column);
                selectedSeats.add(seat);
            }
        }

        return selectedSeats;
    }

    private static double calculateTotalAmount(List<Seat> selectedSeats) {
        // Implement your logic to calculate the total amount based on selected seats
        // For simplicity, assume a fixed price per seat
        double seatPrice = 100.0; // Price per seat
        return seatPrice * selectedSeats.size();
    }

    private static boolean makePayment(double totalAmount) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Total Amount: INR " + totalAmount);
        System.out.print("Enter 'Y' to confirm payment: ");
        String confirmPayment = scanner.nextLine();
        return confirmPayment.equalsIgnoreCase("Y");
    }

    private static void saveBooking(Connection connection, String name, String mobileNumber, Movie movie, List<Seat> selectedSeats) throws SQLException {
        String query = "INSERT INTO bookings (name, mobile_number, movie_name, seat_row, seat_column) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (Seat seat : selectedSeats) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, mobileNumber);
                preparedStatement.setString(3, movie.getName());
                preparedStatement.setInt(4, seat.getRow());
                preparedStatement.setInt(5, seat.getColumn());
                preparedStatement.executeUpdate();
            }
        }
    }

    private static void printReceipt(String name, String mobileNumber, Movie movie, List<Seat> selectedSeats, double totalAmount) {
        System.out.println("----- Receipt -----");
        System.out.println("Name: " + name);
        System.out.println("Mobile Number: " + mobileNumber);
        System.out.println("Movie: " + movie.getName());
        System.out.print("Seat Numbers: ");
        for (Seat seat : selectedSeats) {
            System.out.print("(" + (seat.getRow() + 1) + "," + (seat.getColumn() + 1) + ") ");
        }
        System.out.println();
        System.out.println("Total Amount: INR " + totalAmount);
    }

    private static class Movie {
        private String name;
        private int rows;
        private int columns;

        public Movie(String name, int rows, int columns) {
            this.name = name;
            this.rows = rows;
            this.columns = columns;
        }

        public String getName() {
            return name;
        }

        public int getRows() {
            return rows;
        }

        public int getColumns() {
            return columns;
        }
    }

    public static class Seat {
        private int row;
        private int column;

        public Seat(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }
    }
}
