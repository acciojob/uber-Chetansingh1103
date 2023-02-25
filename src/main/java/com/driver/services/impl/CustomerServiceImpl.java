package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);


		for(Driver driver : driverRepository2.findAll()){
			if(driver.getCab().getAvailable()){
				tripBooking.setDriver(driver);

				tripBooking.setStatus(TripStatus.CONFIRMED);

				int rate = driver.getCab().getPerKmRate();
				int bill = rate * tripBooking.getDistanceInKm();
				tripBooking.setBill(bill);

				Customer customer = customerRepository2.findCustomerById(customerId);
				tripBooking.setCustomer(customer);
				driver.getTripBookingList().add(tripBooking);

				customerRepository2.save(customer);
				driverRepository2.save(driver);
				return tripBooking;
			}
		}
		throw new Exception("No cab available!");
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findTripBookingById(tripId);
		tripBooking.setStatus(TripStatus.CANCELED);

		Driver driver = tripBooking.getDriver();
		Customer customer = tripBooking.getCustomer();

		driver.getTripBookingList().remove(tripBooking);
		customer.getTripBookingList().remove(tripBooking);

		tripBookingRepository2.delete(tripBooking);
		driverRepository2.save(driver);
		customerRepository2.save(customer);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking = tripBookingRepository2.findTripBookingById(tripId);
		if(tripBooking.getStatus().equals(TripStatus.CONFIRMED)){

			Customer customer = tripBooking.getCustomer();
			Driver driver = tripBooking.getDriver();


			tripBooking.setStatus(TripStatus.COMPLETED);

			driverRepository2.save(driver);
			customerRepository2.save(customer);
		}
	}
}
