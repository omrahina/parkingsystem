package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    /**
     * This method calculates a vehicle's parking fare according to its type and a duration.
     * When the duration is under 30 minutes, the ticket maintains its initial price (0)
     * @param ticket the ticket generated
     */
    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double duration = ticket.getOutTime().getTime() - ticket.getInTime().getTime(); //Duration in ms

        if(duration > Fare.MAXIMUM_NO_CHARGE_TIME){
            double price = 0.0;
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    price = (duration * Fare.CAR_RATE_PER_HOUR) / (60 * 60 * 1000);
                    break;
                }
                case BIKE: {
                    price = (duration * Fare.BIKE_RATE_PER_HOUR) / (60 * 60 * 1000);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
            ticket.setPrice(price);
        }

    }

    /**
     * This method applies a discount of a certain percentage on a given ticket
     * @param ticket having the initial price
     */
    public void getDiscount(Ticket ticket){
        double initialPrice = ticket.getPrice();
        double discounted = initialPrice * Fare.DISCOUNT_RECURRING_USERS;
        ticket.setPrice(initialPrice - discounted);
    }
}