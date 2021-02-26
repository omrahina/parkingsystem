package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

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
            if (ticket.isRegular()){
                price = getDiscount(price, Fare.DISCOUNT_RECURRING_USERS);
            }
            ticket.setPrice(price);
        }

    }

    private double getDiscount(double price, double discount){
        return price - (price * discount);
    }

}