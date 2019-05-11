package org.team2.cluk.backend.unprocessed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.*;

/*
 * WorkingHours Class which provides information such as start time, end time and date for the Driver's shifts
 */

public class WorkingHours {

       /*
	* instance variables
	*/
	private Connection connection;
	private LocalDate date;
	private Date startTime;
	private Date endTime;
	//private int workingHoursID;
	private final int id;

	/*
	* constructor of the objects for the class WorkingHours
	*/
	public WorkingHours(Connection connection, LocalDate date, Date startTime, Date endTime, int id) {
		// initialize instance variables 
		this.connection = connection;
		this.date = LocalDate.now();
		this.startTime = new Date(startTime.getTime());
		this.endTime = new Date(endTime.getTime());
		this.id = id;
	}

       /*
	* Accessor method to get the start time 
	* @return the start time of the driver's shift
	*/
	public Date getStartTime() {
		return startTime;
	}

       /*
	* Accessor method to get the end time 
	* @return the end time of the driver's shift
  	*/
	public Date getEndTime() {
		return endTime;
	}

       /*
	* Accessor method to get the date
	* @return the date of the driver's shift
	*/
	public LocalDate getLocalDate() { return date; }

       /*
	* Method which prints out the current date in the format dd/MM/yyyy
	* If successful, the system shows "Workday for driver " + id + "is " + LocalDate
	* @param id of the driver
	* @return the driver id and the date of their shift 
	*/
	public void printCurrentDate(int id) throws SQLException{

		// use local date
		LocalDate date = getLocalDate();
		System.out.println(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy").format(date));

		Statement statement1 = null;
		String query1 = "SELECT date " +
				"FROM WorkingHours " +
				"WHERE id ='" + id+"'";

		try {
			// db connection
			statement1 = this.connection.createStatement();
			ResultSet rs = statement1.executeQuery(query1);

			while (rs.next()) {
				String LocalDate = rs.getString("date");
				System.out.println("Workday for driver " + id + "is " + LocalDate + "\n");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement1 != null) {
				statement1.close();
			}
		}
	}

	/*public void updateDate(int id) throws SQLException{

		Statement statement2 = null;
		String query2 = "SELECT date" +
				"FROM WorkingHours " +
				"WHERE id ='" + id +"'";

		try {
			statement2 = connection.createStatement();
			ResultSet rs = statement2.executeQuery(query2);

			rs.next();
			//String Date = rs.getString("date");
			//System.out.println("Previous workday for Driver " + ID + "\t" + "is" + Date + "\n");
			//LocalDate date = getLocalDate();
			//String Date = rs.getString("date");
			//System.out.println(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy").format(Date));

			Statement statement3 = null;
			String query3 = "UPDATE WorkingHours " +
					"SET date ='" +  +
					"'WHERE id='" + id + "'";

			try {
				statement3 = this.connection.createStatement();
				statement3.executeUpdate(query3);
				System.out.println("Updated  for Driver " + id + "\t" + "is" +  + "\n");

			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (statement3 != null) {
					statement3.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement2 != null) {
				statement2.close();
			}
		}
	}*/

	
	/*
	* Method to print a driver's shift start time
	* If successful, the system shows "Driver " + id + "'s start time is " + StartTime
	* @param id of the driver 
	* @return the driver's id and the start time 
	*/
	public void printStartTime(int id) throws SQLException{

			Statement statement3 = null;
			String query3 = "SELECT startTime " +
					"FROM WorkingHours " +
					"WHERE id ='" + id + "'";

			try {
				// db connection
				statement3 = this.connection.createStatement();
				ResultSet rs = statement3.executeQuery(query3);
				while (rs.next()) {
					String StartTime = rs.getString("startTime");
					System.out.println("Driver " + id + "'s start time is " + StartTime + ".\n");

				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (statement3 != null) {
					statement3.close();
				}
			}

	}

	/*
	* Method to update a driver's shift start time
	* If successful, the system shows "Driver " + id + "'s shift start time is updated to " + sqlStartTime
	* @param hour1, min1, sec1  start time of the shift
	* @param id of the driver 
	* @return the driver's id and the start time 
	*/
	public void updateStartTime(int hour1, int min1, int sec1, int id) throws SQLException {

		// use calendar
		Calendar c1 = Calendar.getInstance();

		c1.set(Calendar.HOUR, hour1);
		c1.set(Calendar.MINUTE, min1);
		c1.set(Calendar.SECOND, sec1);
		String startTime = c1.getTime().toString();

		try {

			// date format
			Date dateFormat = new SimpleDateFormat("HH:mm:ss").parse(startTime);
			//System.out.println(dateFormat.format(startTime));
			java.sql.Date sqlStartTime = new java.sql.Date(dateFormat.getTime());
			PreparedStatement p = this.connection.prepareStatement("UPDATE WorkingHours " +
					"SET startTime ='" + sqlStartTime +
					"'WHERE id ='" + id + "'");
			p.setDate(1, sqlStartTime);
			System.out.println("Driver " + id + "'s shift start time is updated to " + sqlStartTime + "\n");

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


	/*
	* Method to print a driver's shift end time
	* If successful, the system shows "Driver " + id + "'s start time is " + EndTime 
	* @param id of the driver 
	* @return the driver's id and the end time 
	*/
	public void printEndTime(int id) throws SQLException{

		Statement statement3 = null;
		String query3 = "SELECT endTime " +
				"FROM WorkingHours " +
				"WHERE id ='" + id + "'";

		try {
			// db connection 
			statement3 = this.connection.createStatement();
			ResultSet rs = statement3.executeQuery(query3);
			while (rs.next()) {
				String EndTime = rs.getString("endTime");
				System.out.println("Driver " + id + "'s start time is " + EndTime + ".\n");

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement3 != null) {
				statement3.close();
			}
		}
	}


       /*
	* Method to update a driver's shift end time
	* If successful, the system shows "Driver " + id + "'s shift end time is updated to " + sqlEndTime
	* @param hour2, min2, sec2  end time of the shift
	* @param id of the driver 
	* @return the driver's id and the end time 
	*/
	public void updateEndTime(int hour2, int min2, int sec2, int id) throws SQLException{

		// use calendar
		Calendar c2 = Calendar.getInstance();

		c2.set(Calendar.HOUR, hour2);
		c2.set(Calendar.MINUTE, min2);
		c2.set(Calendar.SECOND, sec2);
		String endTime = c2.getTime().toString();

		try {

			// date format 
			Date dateFormat = new SimpleDateFormat("HH:mm:ss").parse(endTime);
			//System.out.println(dateFormat.format(endTime));
			java.sql.Date sqlEndTime = new java.sql.Date(dateFormat.getTime());
			PreparedStatement p = this.connection.prepareStatement("UPDATE WorkingHours " +
					"SET endTime ='" + sqlEndTime +
					"'WHERE id ='" + id + "'");
			p.setDate(1, sqlEndTime);
			System.out.println("Driver " + id + "'s shift end time is updated to " + sqlEndTime + "\n");

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
}

