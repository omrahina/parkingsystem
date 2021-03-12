package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.commons.math3.util.Precision;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //TODO: Some tests are failing here. Need to check if this logic is correct
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

    public double roundPrice(double price){
        return Precision.round(price, 2);
    }

    public void getDiscount(Ticket ticket){
        double initialPrice = ticket.getPrice();
        double discounted = initialPrice * Fare.DISCOUNT_RECURRING_USERS;
        ticket.setPrice(initialPrice - discounted);
    }

    public double getDiscount(double price){
        double discounted = price * Fare.DISCOUNT_RECURRING_USERS;
        return (price - discounted);
    }

}