package org.team2.cluk.backend.webresources;

import org.team2.cluk.backend.tools.DbConnection;
import org.team2.cluk.backend.tools.JsonTools;
import org.team2.cluk.backend.tools.ServerLog;
import org.team2.cluk.backend.tools.WorkingHours;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/*
* Driver class including information about driver schedules and assigning orders
*/


@Path("/driver")

public class Driver {

	private static int workDuration; //mins.
	private final int breakTime = 45; //mins
	private final int maxWorkDuration = 600;  //10 hours = 600mins, use this to limit assigning order to driver etc
	/*
	 * method to print a driver's phone number using driverId
	 * @param idToken to check is either manager or restaurant
	 * @param driverId
	 * @return phone number of the driver
	 */
	@GET
	@Path("/get-phone-number")
	@Produces("application/json")


	public Response getPhoneNumber(@HeaderParam("Authorisation") String idToken, @HeaderParam("driverId") String driverId) {

		if (!Authorisation.checkAccess(idToken, "manager") || !Authorisation.checkAccess(idToken, "restaurant")) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Cannot get permission").build();
		}

		ServerLog.writeLog("Requested the phone number of driver " + driverId);

		if (driverId == null) {
			ServerLog.writeLog("Rejected request as driverId not specified");
			return Response.status(Response.Status.BAD_REQUEST).entity("ID_BLANK_OR_NOT_PROVIDED").build();
		}

		JsonArrayBuilder responseBuilder = Json.createArrayBuilder();
		// fetch current database connection
		Connection connection = DbConnection.getConnection();

		Statement statement = null;
		String query = "SELECT phoneNumber " +
				"FROM Driver " +
				"WHERE driverId ='" + driverId + "'";

		try {
			statement = DbConnection.getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while (rs.next()) {
				JsonObjectBuilder arrayEntryBuilder = Json.createObjectBuilder();

				String phoneNumber = rs.getString("phoneNumber");

				arrayEntryBuilder.add("phoneNumber", phoneNumber);

				JsonObject arrayEntry = arrayEntryBuilder.build();
				responseBuilder.add(arrayEntry);

				//System.out.println("Driver " + driverId + "'s phone number is " + PhoneNumber + "\n");
			}
		} catch (SQLException e) {
			ServerLog.writeLog("SQL exception occurred when executing query");
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("SQL Exception occurred when executing query").build();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					ServerLog.writeLog("SQL exception occurred when closing SQL statement");
				}
			}
		}

		JsonArray response = responseBuilder.build();
		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}


	/*
	 * method to update a driver's phone number using the driverId and new phone number
	 * @param idToken
	 * @param driverId
	 * @param phoneNumber
	 * @param phoneNumberObject to get a new phone number
	 * @return updated phone number of the driver
	 */
	@Path("/update-phone-number")
	@POST
	@Consumes("application/json")
	public Response updatePhoneNumber(@HeaderParam("Authorisation") String idToken, @HeaderParam("driverId") String driverId, @HeaderParam("phoneNumber") String phoneNumber, String requestBody) throws SQLException {

		if (!Authorisation.checkAccess(idToken, "manager")) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Cannot get permission").build();
		}
		ServerLog.writeLog("Requested an update of the phone number of driver" + driverId);

		// fetch db connection
		Connection connection = DbConnection.getConnection();
		Statement statement = null;

		JsonObject phoneNumberObject = JsonTools.parseObject(requestBody);
		JsonArrayBuilder responseBuilder = Json.createArrayBuilder();


		if (!(phoneNumberObject.containsKey("phoneNumber"))) {
			return Response.status(Response.Status.BAD_REQUEST).entity("REQUEST_MISSPECIFIED").build();
		}

		String newPhoneNumber = phoneNumberObject.getString("phoneNumber");
		String query = "UPDATE Driver " +
				"SET phoneNumber ='" + newPhoneNumber +
				"'WHERE driverId='" + driverId + "'";
		try {
			statement = DbConnection.getConnection().createStatement();
			statement.executeUpdate(query);
			ServerLog.writeLog("Driver " + driverId + "'s phone number has been updated to: " + newPhoneNumber);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}

		JsonArray response = responseBuilder.build();
		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}


	/*
	 * method to get the current work duration of the driver
	 * @param idToken to check permission is set to either manager or restaurant
	 * @param driverId
	 * @param WorkingHours
	 * @return work duration of a driver
	 */
	/*
	@GET
	@Path("/get-work-duration")
	@Produces("application/json")
	public Response getWorkDuration(@HeaderParam("Authorisation") String idToken, @HeaderParam("driverId") Integer driverId, @HeaderParam("w") WorkingHours w) {

		if (!Authorisation.checkAccess(idToken, "manager") || !Authorisation.checkAccess(idToken, "restaurant")
				|| !Authorisation.checkAccess(idToken, "driver")) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Cannot get permission").build();
		}
		ServerLog.writeLog("Requested information on work duration of driver of " + driverId);

		if (driverId == null) {
			ServerLog.writeLog("Rejected request as driverId not specified");
			return Response.status(Response.Status.BAD_REQUEST).entity("ID_BLANK_OR_NOT_PROVIDED").build();
		}

		JsonArrayBuilder responseBuilder = Json.createArrayBuilder();
		// fetch current database connection
		Connection connection = DbConnection.getConnection();

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		int startTime = Integer.parseInt(formatter.format(w.getStartTime()));
		int endTime = Integer.parseInt(formatter.format(w.getEndTime()));
		workDuration = endTime - startTime;

		Statement statement = null;

		String query = "SELECT date" +
				"FROM WorkingHours" +
				"WHERE driverId = driverId";
		try {
			statement = DbConnection.getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				JsonObjectBuilder arrayEntryBuilder = Json.createObjectBuilder();

				int WorkDuration = rs.getInt(workDuration);

				arrayEntryBuilder.add("workDuration", workDuration);

				//System.out.println(" Driver " + driverId + "'s work duration so far is " + WorkDuration + "\n");
			}
		} catch (SQLException e) {
			ServerLog.writeLog("SQL exception occurred when executing query");
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("SQL Exception occurred when executing query").build();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					ServerLog.writeLog("SQL exception occurred when closing SQL statement");
				}
			}
		}
		JsonArray response = responseBuilder.build();

		return Response.status(Response.Status.OK).entity(response.toString()).build();
	}
*/
	/*
	 * method to make drivers go on break, adding the break time to their work duration
	 *this method would only update the workDuration field while goOnBreak is false and workDuration hasn't exceeded
	 *the max which means that the driver can only take one break per day which happens after the they have worked
	 *270 mins = 4.5 hours, the driver cannot go on break unless their workDuration reaches 270 mins
	 * @param idToken to check access for manager or driver
	 * @param driverId
	 * @param WorkingHours
	 * @return updated work duration for driver after they take a break
	 */
	/* @GET
	@Path("go-on-break")

	public Response goOnBreak(@HeaderParam("Authorisation") String idToken, @HeaderParam("driverId") String driverId, @HeaderParam("w") WorkingHours w) throws SQLException {

		if ((!Authorisation.checkAccess(idToken, "manager") || (!Authorisation.checkAccess(idToken, "driver")))) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Cannot get permission").build();
		}
		// fetch current db connection
		Connection connection = DbConnection.getConnection();

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		int startTime = Integer.parseInt(formatter.format(w.getStartTime()));
		int endTime = Integer.parseInt(formatter.format(w.getEndTime()));
		workDuration = endTime - startTime;

		//check if driver has worked 4.5 hours before going on break
		boolean goOnBreak = false;

		if (goOnBreak && workDuration < maxWorkDuration) {
			//4.5hours = 270mins
			if (workDuration == 270) {

				Statement statement = null;
				String query = "SELECT workDuration" +
						"FROM Driver " +
						"WHERE driverId='" + driverId + "'";

				try {
					statement = DbConnection.getConnection().createStatement();
					ResultSet rs = statement.executeQuery(query);

					rs.next();
					int WorkDuration = rs.getInt(workDuration);
					int newWorkDuration = WorkDuration + breakTime;

					Statement statement2 = null;
					String query2 = "UPDATE Driver " +
							"SET workDuration ='" + newWorkDuration +
							"'WHERE driverId='" + driverId + "'";

					try {
						statement2 = (Statement) DbConnection.getConnection();
						statement2.executeUpdate(query2);
						ServerLog.writeLog("Updated work Duration for Driver " + driverId + "is" + newWorkDuration + " after going on break");
						goOnBreak = true;

					} catch (SQLException e) {
						ServerLog.writeLog("Error when updating work duration of Driver: " + driverId);
						e.printStackTrace();
						JsonObject responseJson = Json.createObjectBuilder().add("message", "UPDATE_WORKDURATION_ERROR").build();
						return Response.status(Response.Status.OK).entity(responseJson.toString()).build();

					} finally {
						if (statement2 != null) {
							try {
								statement2.close();
							} catch (SQLException e) {
								ServerLog.writeLog("SQL exception occurred when closing SQL statement");
							}
						}
					}

				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e) {
							ServerLog.writeLog("SQL exception occurred when closing SQL statement");
						}

					}
				}
			}
		}

		return Response.status(Response.Status.ACCEPTED).build();
	} */


	/* checks access type is manager */
	//connects to database
	//gets today's date and the orderId of approved orders to be delivered in that same day
	//gets the restaurant address of that same orderId from the Order table
	//gets the restaurant address of the  region that matches the restaurant address above
	//returns JsonObject of the method status
	/*@param idToken
	* @return restaurantAddress
	* */

	@Path("/plot-route")
	@GET
	public static Response plotRoute(@HeaderParam("Authorisation") String idToken) {
	    /*
		if (!Authorisation.checkAccess(idToken, "driver")) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Cannot get permission").build();
		} */

		//connects to database
		JsonArrayBuilder responseBuilder = Json.createArrayBuilder();
		Connection connection = DbConnection.getConnection();

		//get today's date
		java.util.Date orderDate = new java.util.Date();
		java.text.SimpleDateFormat date = new java.text.SimpleDateFormat("yyyy-MM-dd");
		String currentDate = date.format(orderDate.getTime());

		//get orderId from stockOrders table where the orderDeliveryDate is today's date and orderSatatus is approved for delivery.

		Statement statement = null;
		String query = "SELECT orderId " +
				"FROM StockOrders " +
				"WHERE orderDeliveryDate='" + currentDate + "' AND orderStatus LIKE 'approved'";
		try {
			statement = DbConnection.getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				int orderIdStockOrders = rs.getInt("orderId");

				//get the restaurantAddress where the orderId from orders table is equal to the orderId from stockOrders table.

				Statement statement1 = null;
				String query1 = "SELECT restaurantAddress " +
						"FROM Orders " +
						"WHERE orderId ='" + orderIdStockOrders + "'";

				try {
					statement1 = DbConnection.getConnection().createStatement();
					ResultSet rs1 = statement1.executeQuery(query1);

					while (rs1.next()) {
						JsonObjectBuilder arrayEntryBuilder = Json.createObjectBuilder();
						String restaurantAddressOrdersTable = rs1.getString("restaurantAddress");
						arrayEntryBuilder.add("address", restaurantAddressOrdersTable);
						JsonObject arrayEntry = arrayEntryBuilder.build();
                   				responseBuilder.add(arrayEntry);
						}

						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}catch (SQLException e) {
			e.printStackTrace();
		}
		
		JsonArray response = responseBuilder.build();
		return Response.status(Response.Status.OK).entity(response.toString()).build();

	}

	//connects to database
	//gets today's date and the orderId of approved orders to be delivered in that same day
	//gets the driverId that has no workDuration i.e available to work
	//assigns the orderId to the driverId
	//returns response of the method status
	/*
	* @param idToken to check permission is manager
	* @returns orderId that has been order to a driverId
	*/

	@Path("/assign-order-to-driver")
	@GET
	public static Response assignOrderToDriver(@HeaderParam("Authorisation") String idToken) {

		if (!Authorisation.checkAccess(idToken, "manager")) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Cannot get access").build();
		}

		Connection connection = DbConnection.getConnection();
		Response.ResponseBuilder res = null;

		//get today's date
		java.util.Date orderDate = new java.util.Date();
		java.text.SimpleDateFormat date = new java.text.SimpleDateFormat("yyyy-MM-dd");
		String currentDate = date.format(orderDate.getTime());

		//get orderId from stockOrders table where the orderDeliveryDate is today's date and orderStatus is approved for delivery.

		Statement statement = null;
		String query = "SELECT orderId" +
				"FROM StockOrders" +
				"WHERE orderDeliveryDate='" + currentDate + " AND orderStatus= approved'";

		int orderId=0;
		String driverId = "";

		try {
			statement = DbConnection.getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				orderId = rs.getInt("orderId");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		//get driverId a random driver that is free i.e (workDuration) from Driver table

		int minWorkDuration = 0;

		Statement statement1 = null;
		String query1 = "SELECT driverId" +
				"FROM Driver" +
				"WHERE workDuration='" + minWorkDuration + "'";
		try {
			statement1 = DbConnection.getConnection().createStatement();
			ResultSet rs1 = statement1.executeQuery(query1);

			while (rs1.next()) {
				driverId = rs1.getString("driverId");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}

		//assign orderId to driverId

		String query3 = "INSERT INTO SentBy(orderId,driverId)VALUES (" + orderId + "," + driverId + ")";
		PreparedStatement statement3 = null;

		try {
			statement3 = connection.prepareStatement(query);
			statement3.setInt(1,orderId);
			statement3.setString(2, driverId);
			ResultSet rs2 = statement3.executeQuery(query3);

			while(rs2.next()){

                //change orderStatus to Pending

                Statement statement4 = null;
                String query4 = "UPDATE StockOrders " +
                        "SET orderStatus = 'Out for delivery' " +
                        "WHERE orderId = '" + orderId + "'";
                try {
                    statement4 = DbConnection.getConnection().createStatement();
                    statement4.executeUpdate(query4);
                    JsonObject responseJson = Json.createObjectBuilder().add("message", "ORDER_OUT_FOR_DELIVERY").build();
                    res = Response.status(Response.Status.OK).entity(responseJson.toString());
                    return res.build();

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (statement4 != null) {
                        statement4.close();
                    }
                }
            }
		}catch (SQLException e ) {
			e.printStackTrace();

			res = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ORDER_ASSIGNMENT_ERROR");
			return res.build();
		}
		finally {
			if (statement3 != null) {
				try {
					statement3.close();
				} catch (SQLException e) {
					ServerLog.writeLog("SQL exception occurred when closing SQL statement");
				}
			}
		}

		JsonObject responseJson = Json.createObjectBuilder().add("message", "ORDER_ASSIGNED_TO_DRIVER").build();
		res = Response.status(Response.Status.OK).entity(responseJson.toString());
		return res.build();
	}

}
