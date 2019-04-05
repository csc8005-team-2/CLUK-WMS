package org.team2.cluk.backend.webresources;

import org.team2.cluk.backend.tools.DbConnection;
import org.team2.cluk.backend.tools.JsonTools;
import org.team2.cluk.backend.tools.ServerLog;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Path("/warehouse")
public class Warehouse
{

    @GET
    @Path("/get-total-stock")
    @Produces("application/json")
    //Outputs total stock held at the warehouse(units).
    public void GetTotalStock(@HeaderParam("warehouse") String address)
    {
        Statement statement = null;
        String query = "SELECT stockItem, quantity " +
                       "FROM Inside " +            
                       "WHERE warehouseAddress='"+address+"'";
                       
        try {
        statement = DbConnection.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            String StockItem = rs.getString("StockItem");
            int Quantity = rs.getInt("quantity");
            System.out.println(StockItem + ": " + Quantity);
        }
        } catch (SQLException e ) {
            e.printStackTrace();
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

    /**
     * {stockItem: string, quantity: number}
     * @param address
     * @param requestBody
     * @return
     * @throws SQLException
     */
    @POST
    @Path("/update-stock")
    @Consumes("application/json")
    //Increases warehouse stock of item specified by quantity specified. Takes parameters for stockItem and quantity.
    public Response updateStock(@HeaderParam("warehouse") String address, String requestBody)
    {
        ServerLog.writeLog("Updating warehouse stock at " + address);
        // fetch db connection
        Connection connection = DbConnection.getConnection();

        // parse request body
        JsonArray stockToUpdate = JsonTools.parseArray(requestBody);

        for (JsonValue entry: stockToUpdate) {
            // check if array entry is a JSON
            if (!(entry instanceof JsonObject)) {
                ServerLog.writeLog("Order entry misspecified. Skipping entry.");
                continue;
            }

            JsonObject entryObj = (JsonObject) entry;

            // check if JSON correctly specified
            if (!(entryObj.containsKey("stockItem") || entryObj.containsKey("quantity"))) {
                ServerLog.writeLog("Order entry misspecified. Skipping entry.");
                continue;
            }

            String stockItem = entryObj.getString("stockItem");
            int requestedQuantity = entryObj.getInt("quantity");

            // retrieve current level of given stock
            int currentQuantity = 0;

            Statement statement = null;
            String query = "SELECT stockItem, quantity " +
                    "FROM Inside " +
                    "WHERE stockItem='" + stockItem + "' AND warehouseAddress ='" + address + "'";
            try {
                statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(query);

                while (rs.next()) {
                    currentQuantity = rs.getInt("quantity");
                    System.out.println("Previous stock of " + stockItem + ": " + currentQuantity + " at " + address);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        ServerLog.writeLog("SQL exception occurred when closing SQL statement");
                    }
                }
            }

            // update stock to the new level
            int newQuantity = currentQuantity + requestedQuantity;

            statement = null;
            query = "UPDATE Inside " +
                    "SET quantity ='" + newQuantity +
                    "' WHERE stockItem='" + stockItem + "' AND warehouseAddress ='" + address + "'";
            try {
                statement = connection.createStatement();
                statement.executeUpdate(query);
                ServerLog.writeLog("Updated stock of " + stockItem + ": " + newQuantity + " at " + address);
            } catch (SQLException e) {
                e.printStackTrace();
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

        return Response.status(Response.Status.OK).entity("STOCK_UPDATED").build();
    }

    @GET
    @Path("/send-order")
    //Reduces warehouse stock levels determined by the stock requests in an order. Takes the orderId as a parameter.
    public Response sendOrder(@HeaderParam("address") String address, @HeaderParam("orderId") int orderId)
    {
        // fetch current db connection
        Connection connection = DbConnection.getConnection();

    	//Check if order has already been delivered.
    	boolean orderFulfilled = false;
    	Statement statement = null;
    	String query = "SELECT orderStatus FROM StockOrders WHERE orderId ='"+orderId+"'";
    	try{
    		statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);
			
    		rs.next();
    		String orderStatus = rs.getString("orderStatus");
    		if(!orderStatus.equalsIgnoreCase("Pending")){
    			ServerLog.writeLog("Order has already been fulfilled.");
    			orderFulfilled = true;
    		}
    	} catch (SQLException e ) {
    		e.printStackTrace();
    	} finally {
    		if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    ServerLog.writeLog("SQL exception occurred when closing SQL statement");
                }
            }
    	}

    	if (!orderFulfilled)
    	    return Response.status(Response.Status.FORBIDDEN).entity("ORDER_ALREADY_FULFILLED").build();


    	// retrieve order contents
        HashMap<String, Integer> orderedStock = new HashMap<>();

    	//Check if warehouse has enough stock to fulfil order.
    	boolean stockAvailable = true;

    	statement = null;
    	query = "SELECT quantity, stockItem "+
    			"FROM Contains WHERE orderId='"+orderId+"'";
    	try{
    		statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);
		
    		while(rs.next()){
                int cQuantity=0;

    			cQuantity = rs.getInt("quantity");
    			String cStock= rs.getString("stockItem");

    			orderedStock.put(cStock, cQuantity);
    		}
    	} catch (SQLException e ) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    ServerLog.writeLog("SQL exception occurred when closing SQL statement");
                }
            }
		}

    	// check if enough stock is available to fulfill the order
    	for (Map.Entry<String, Integer> orderedItem: orderedStock.entrySet()) {
    	    String cStock = orderedItem.getKey();
    	    int cQuantity = orderedItem.getValue();

            statement =null;
            query = "SELECT quantity FROM Inside WHERE stockItem ='"+cStock+"' AND warehouseAddress ='"+address+"'";
            try{
                statement = connection.createStatement();
                ResultSet innerRs = statement.executeQuery(query);

                innerRs.next();
                int iQuantity = innerRs.getInt("quantity");

                if(cQuantity > iQuantity){
                    ServerLog.writeLog("Order cannot be fulfilled. Warehouse stock too low.");
                    stockAvailable = false;
                }

            }catch (SQLException e ) {
                e.printStackTrace();
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

    	if (!stockAvailable)
    	    return Response.status(Response.Status.FORBIDDEN).entity("STOCK_TOO_LOW").build();

    	//If passed previous checks update stock levels.

        for (Map.Entry<String, Integer> orderEntry: orderedStock.entrySet()) {
            String stockItem = orderEntry.getKey();
            int quantity = orderEntry.getValue();

            statement = null;
            query = "UPDATE Inside " +
                    "SET quantity = quantity-" + quantity +
                    " WHERE stockItem='" + stockItem + "' AND warehouseAddress ='" + address + "'";
            try {
                statement = connection.createStatement();
                statement.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
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

        //Update database to mark order as out for delivery.
        statement = null;
        query = "UPDATE StockOrders "+
                "SET orderStatus = 'Out for delivery' "+
                "WHERE orderId='"+orderId+"'";
        try{
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e ) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    ServerLog.writeLog("SQL exception occurred when closing SQL statement");
                }
            }
        }
        return Response.status(Response.Status.OK).entity("ORDER_SENT").build();
    }

    /*
    //Checks the warehouse stock is above the minimum level.
    public void minStockCheck(Connection connection) throws SQLException
    {
        Statement statement = null;
        String query = "SELECT stockItem, quantity, minQuantity " +
                       "FROM Inside " +            
                       "WHERE warehouseAddress='"+Address+"'";
                       
        try {
        statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            String StockItem = rs.getString("StockItem");
            int Quantity = rs.getInt("quantity");
            int minQuantity = rs.getInt("minQuantity");

            if(Quantity < minQuantity){
            	int deficit = minQuantity - Quantity;
            	System.out.println("Current stock of "+ StockItem +" is below minimum stock levels by "+deficit+".");
            	this.UpdateStock(connection, StockItem, 100);
            }    
        }
        } catch (SQLException e ) {
            e.printStackTrace();
        } finally {
            if (statement != null) {statement.close();}
        }                 
    }
    
    //Allows the warehouse stock minimums to be set.
    public void updateMinStock(Connection connection, String stockItem, int min) throws SQLException
    {
        Statement statement = null;
        String query = "UPDATE Inside SET minQuantity ="+min+" WHERE stockItem='"+stockItem+"' AND warehouseAddress ='"+Address+"'";
                       
        try {
        	statement = connection.createStatement();
        	statement.executeUpdate(query);
        	System.out.println("Minimum stock levels updated to: " + min);
        
        } catch (SQLException e ) {
            e.printStackTrace();
        } finally {
            if (statement != null) {statement.close();}
        }                 
    }

    //Assigns an order to a driver(basic) may require expanding based on driver class.
    public void assignOrderToDriver(Connection connection, int orderId, String driverId) throws SQLException{
    	try {

		String query = "INSERT INTO SentBy(orderId,driverId)VALUES (?,?)";
    	PreparedStatement statement= connection.prepareStatement(query);
		statement.setInt(1,orderId);
		statement.setString(2, driverId);
		statement.execute();
		
    	}catch (SQLException e ) {
    		e.printStackTrace();
    	}           
    }
	 */
}