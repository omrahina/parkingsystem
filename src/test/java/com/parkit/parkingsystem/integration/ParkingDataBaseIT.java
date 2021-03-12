package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        parkingService.processIncomingVehicle();
        assertNotNull(ticketDAO.getTicket("ABCDEF"));
        assertTrue(parkingSpotDAO.updateParking(parkingSpot));

    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        updateInTime(ticket, new Date(System.currentTimeMillis() - (60*60*1000)) );
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        Ticket updatedTicket = ticketDAO.getTicket("ABCDEF");
        assertTrue(ticketDAO.updateTicket(updatedTicket));
        assertEquals(1.5, updatedTicket.getPrice(), 0.001);
    }

    @Test
    void testFreeThirtyMinuteParking(){
        testParkingACar();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        updateInTime(ticket, new Date(System.currentTimeMillis() - (29*60*1000)) );
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        assertEquals(0, ticketDAO.getTicket("ABCDEF").getPrice());
    }

    @Test
    void testParkingARegularCar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        updateInTime(ticket, new Date(System.currentTimeMillis() - (60*60*1000)) );
        parkingService.processExitingVehicle();

        parkingService.processIncomingVehicle();
        assertTrue(ticketDAO.countTicket("ABCDEF") > 0);
    }

    @Test
    void testParkingLotExit_RegularCar(){
        testParkingARegularCar();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        updateInTime(ticket, new Date(System.currentTimeMillis() - (45*60*1000)) );
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR * 0.95), ticketDAO.getTicket("ABCDEF").getPrice(), 0.001);
    }

    boolean updateInTime(Ticket ticket, Date date){
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection();
            PreparedStatement ps = con.prepareStatement("update ticket set IN_TIME=? where ID=?");
            ps.setTimestamp(1, new Timestamp(date.getTime()));
            ps.setInt(2,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            System.out.println("Error updating inTime " + ex);
        }finally {
            dataBaseTestConfig.closeConnection(con);
        }
        return false;
    }

}
