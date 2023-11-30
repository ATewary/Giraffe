package com.topbloc.codechallenge.db;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.QueryParamsMap;
import spark.Request;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static spark.Spark.*;

public class DatabaseManager {
    private static final String jdbcPrefix = "jdbc:sqlite:";
    private static final String dbName = "challenge.db";
    
    private static final String getAllItemsQuery = "SELECT items.id, items.name, inventory.stock, inventory.capacity FROM items JOIN inventory ON items.id = inventory.item";

    
    private static String connectionString;
    private static Connection conn;

    static {
        File dbFile = new File(dbName);
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
    }

    public static void connect() {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            System.out.println("Connection to SQLite has been established.");
            conn = connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // Schema function to reset the database if needed - do not change
    public static void resetDatabase() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
        connect();
        applySchema();
        seedDatabase();
    }

    // Schema function to reset the database if needed - do not change
    private static void applySchema() {
        String itemsSql = "CREATE TABLE IF NOT EXISTS items (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String inventorySql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + "id integer PRIMARY KEY,\n"
                + "item integer NOT NULL UNIQUE references items(id) ON DELETE CASCADE,\n"
                + "stock integer NOT NULL,\n"
                + "capacity integer NOT NULL\n"
                + ");";
        String distributorSql = "CREATE TABLE IF NOT EXISTS distributors (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String distributorPricesSql = "CREATE TABLE IF NOT EXISTS distributor_prices (\n"
                + "id integer PRIMARY KEY,\n"
                + "distributor integer NOT NULL references distributors(id) ON DELETE CASCADE,\n"
                + "item integer NOT NULL references items(id) ON DELETE CASCADE,\n"
                + "cost float NOT NULL\n" +
                ");";

        try {
            System.out.println("Applying schema");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Schema applied");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Schema function to reset the database if needed - do not change
    private static void seedDatabase() {
        String itemsSql = "INSERT INTO items (id, name) VALUES (1, 'Licorice'), (2, 'Good & Plenty'),\n"
            + "(3, 'Smarties'), (4, 'Tootsie Rolls'), (5, 'Necco Wafers'), (6, 'Wax Cola Bottles'), (7, 'Circus Peanuts'), (8, 'Candy Corn'),\n"
            + "(9, 'Twix'), (10, 'Snickers'), (11, 'M&Ms'), (12, 'Skittles'), (13, 'Starburst'), (14, 'Butterfinger'), (15, 'Peach Rings'), (16, 'Gummy Bears'), (17, 'Sour Patch Kids')";
        String inventorySql = "INSERT INTO inventory (item, stock, capacity) VALUES\n"
                + "(1, 22, 25), (2, 4, 20), (3, 15, 25), (4, 30, 50), (5, 14, 15), (6, 8, 10), (7, 10, 10), (8, 30, 40), (9, 17, 70), (10, 43, 65),\n" +
                "(11, 32, 55), (12, 25, 45), (13, 8, 45), (14, 10, 60), (15, 20, 30), (16, 15, 35), (17, 14, 60)";
        String distributorSql = "INSERT INTO distributors (id, name) VALUES (1, 'Candy Corp'), (2, 'The Sweet Suite'), (3, 'Dentists Hate Us')";
        String distributorPricesSql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES \n" +
                "(1, 1, 0.81), (1, 2, 0.46), (1, 3, 0.89), (1, 4, 0.45), (2, 2, 0.18), (2, 3, 0.54), (2, 4, 0.67), (2, 5, 0.25), (2, 6, 0.35), (2, 7, 0.23), (2, 8, 0.41), (2, 9, 0.54),\n" +
                "(2, 10, 0.25), (2, 11, 0.52), (2, 12, 0.07), (2, 13, 0.77), (2, 14, 0.93), (2, 15, 0.11), (2, 16, 0.42), (3, 10, 0.47), (3, 11, 0.84), (3, 12, 0.15), (3, 13, 0.07), (3, 14, 0.97),\n" +
                "(3, 15, 0.39), (3, 16, 0.91), (3, 17, 0.85)";

        try {
            System.out.println("Seeding database");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Database seeded");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper methods to convert ResultSet to JSON - change if desired, but should not be required
    private static JSONArray convertResultSetToJson(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<String> colNames = IntStream.range(0, columns)
                .mapToObj(i -> {
                    try {
                        return md.getColumnName(i + 1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        while (rs.next()) {
            jsonArray.add(convertRowToJson(rs, colNames));
        }
        return jsonArray;
    }

    private static JSONObject convertRowToJson(ResultSet rs, List<String> colNames) throws SQLException {
        JSONObject obj = new JSONObject();
        for (String colName : colNames) {
            obj.put(colName, rs.getObject(colName));
        }
        return obj;
    }

    // Controller functions - add your routes here. getItems is provided as an example
    public static JSONArray getItems(String req) {
        String sql = getAllItemsQuery;
        try {
        	switch(req) {
        		case "":
        			break;
        		case "outofstock":
        			sql = sql + " WHERE inventory.stock = 0";
        			break;
        		case "overstock":
        			sql = sql + " WHERE inventory.stock > inventory.capacity";
        			break;
        		case "lowstock":
        			sql = sql + " WHERE inventory.stock < 0.35 * inventory.capacity";
        			break;
        		default:
        			sql = "SELECT items.id, items.name, inventory.stock, inventory.capacity FROM items JOIN inventory ON items.id = inventory.id WHERE items.name = '" + req + "'";
        	}
        	
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    public static JSONArray getDistributors(String req) {
        String sql = "";
        try {
        	switch(req) {
        		case "":
        			sql = "SELECT * FROM distributors";
        			break;
        		default:
        			System.out.println(req.length() >= 4 && req.substring(0,4).equals("/id/"));
        			if(req.length() >= 9 && req.substring(0,9).equals("/item-id/")) {
        				sql = "SELECT items.id, items.name, distributor_prices.cost FROM distributors JOIN distributor_prices ON distributors.id = distributor_prices.distributor JOIN items ON distributor_prices.item = items.id WHERE distributors.id IN (SELECT distributor FROM distributor_prices WHERE item = '" + req.substring(9) + "');";
        			}else {
            			sql = "SELECT items.id, items.name, distributor_prices.cost, distributors.id, distributors.name FROM distributor_prices JOIN items ON distributor_prices.item = items.id JOIN distributors ON distributor_prices.distributor = distributors.id WHERE distributor_prices.distributor = '" + req + "';";
        			}
        			break;
        	}
        	
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    public static boolean post(Request req) {
    	QueryParamsMap map = req.queryMap();
    	
        try {
        	switch(req.pathInfo()) {
        	case "/add-item": 
        		System.out.println("Map GET ID:" + map.get("id").value());
        		System.out.println("Map GET NAME:" + map.get("name").value());
            	conn.createStatement().execute("INSERT INTO items (id, name) VALUES ('" + map.get("id").value() + "', '" + map.get("name").value() + "');");
            	break;
        	case "/add-inventory":
            	conn.createStatement().execute("INSERT INTO inventory (item, stock, capacity) VALUES ('" + map.get("id").value() +"' , '" + map.get("stock").value() +"', '" + map.get("capacity").value() + "');");
            	break;
        	case "/add-distributor":
        		conn.createStatement().execute("INSERT INTO distributors (id, name) VALUES ('" + map.get("id").value() + "', '" + map.get("name").value() + "')");
            	break;
        	case "/add-catalog":
            	conn.createStatement().execute("INSERT INTO distributor_prices (distributor, item, cost) VALUES ('" + map.get("distributor").value()+ "', '" + map.get("item").value() + "', '" + map.get("cost").value() + "')");
            	break;	
        	}
        	return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
     }
    
    public static boolean put(Request req) {
    	QueryParamsMap map = req.queryMap();
    	String param = req.params(":next");
    	String path = req.pathInfo().substring(0, req.pathInfo().lastIndexOf('/'));
        try {
        	switch(path) {
        	case "/modify-inventory":
        		PreparedStatement ps = conn.prepareStatement("UPDATE inventory SET stock = ?, capacity = ? WHERE item = (SELECT id FROM items WHERE name = ?)");
        		ps.setObject(1, map.get("stock").value());
                ps.setObject(2, map.get("capacity").value());
                ps.setString(3, param);
                ps.executeUpdate();
//            	conn.createStatement().execute("UPDATE inventory SET stock = '" + map.get("stock").value() + "', capacity = '" + map.get("capacity").value() + "' WHERE item = (SELECT id FROM items WHERE name = '" + param + "')");
            	return true;
        	case "/modify-price":
            	conn.createStatement().execute("UPDATE distributor_prices SET cost = " + map.get("cost").value() + " WHERE distributor = " + map.get("distributor").value() + " AND item = " + param);
            	return true;
        	}
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            
        }
        return false;
     }
    
    public static boolean delete(Request req) {
    	String param = req.params(":next");
    	String path = req.pathInfo().substring(0, req.pathInfo().lastIndexOf('/'));
        try {
        	switch(path) {
        	case "/delete-item":
        		System.out.println("DELETE: " + param);
        		PreparedStatement ps = conn.prepareStatement("DELETE FROM items WHERE name = ?");
        		ps.setObject(1, param);
                ps.executeUpdate();

//            	conn.createStatement().execute();
            	break;
        	case "/delete-distributor":
            	conn.createStatement().execute("DELETE FROM distributors WHERE name = '" + param + "'");
            	conn.createStatement().execute("DELETE FROM distributors WHERE id = " + param);
            	break;
        	}
        	return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
     }
    
    
}
