package MovieTicketBooking;

import java.util.List;

import MovieTicketBooking.MovieTicketBookingSystem.Seat;

class Booking {
    private String name;
    private String mobileNumber;
    private String movieName;
    private List<Seat> seatNumbers;

    public Booking(String name, String mobileNumber, String movieName, List<Seat> seatNumbers) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.movieName = movieName;
        this.seatNumbers = seatNumbers;
    }

    public String getName() {
        return name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getMovieName() {
        return movieName;
    }

    public List<Seat> getSeatNumbers() {
        return seatNumbers;
    }
}