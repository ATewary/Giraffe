package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.*;

import org.json.simple.JSONArray;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.connect();
        // Don't change this - required for GET and POST requests with the header 'content-type'
        options("/*",
                (req, res) -> {
                    res.header("Access-Control-Allow-Headers", "content-type");
                    res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                    return "OK";
                });

        // Don't change - if required you can reset your database by hitting this endpoint at localhost:4567/reset
        get("/reset", (req, res) -> {
            DatabaseManager.resetDatabase();
            return "OK";
        });

        //TODO: Add your routes here. a couple of examples are below
//        get("/items", (req, res) -> DatabaseManager.getItems(""));
        get("/items", (req, res) -> {
        	JSONArray jarr = DatabaseManager.getItems("");
        	if(jarr.isEmpty()) {
        		res.status(500);
        		return new String("Database is Empty");
        	}
			return jarr;
        });
        get("/items/:next", (req, res) -> {
        	JSONArray jarr = DatabaseManager.getItems(req.params(":next"));
        	if(jarr.isEmpty()) {
        		res.status(404);
        		return new String("Not Found");
        	}
			return jarr;
        });
        get("/distributors", (req, res) -> {
        	JSONArray jarr = DatabaseManager.getDistributors("");
        	if(jarr.isEmpty()) {
        		res.status(500);
        		return new String("Database is Empty");
        	}
			return jarr;
        });
        get("/distributors/:next", (req, res) -> {
        	JSONArray jarr = DatabaseManager.getDistributors(req.params(":next"));
        	if(jarr.isEmpty()) {
        		res.status(404);
        		return new String("Not Found");
        	}
			return jarr;
        });
        get("/distributors/item-id/:next", (req, res) -> {
        	JSONArray jarr = DatabaseManager.getDistributors("/item-id/" + req.params(":next"));
        	if(jarr.isEmpty()) {
        		res.status(404);
        		return new String("Not Found");
        	}
			return jarr;
        });
        post("/add-item", (req,res) -> {
        	boolean bool = DatabaseManager.post(req);
        	if(!bool) {
        		res.status(500);
        		return new String("Error Adding Item");
        	}
        	return "Success";
        });
        post("/add-inventory", (req,res) -> {
        	boolean bool = DatabaseManager.post(req);
    		if(!bool) {
    			res.status(500);
    			return new String("Error Adding Inventory");
    		}
    		return "Success";
        });
        post("/add-distributor", (req,res) -> {
        	boolean bool = DatabaseManager.post(req);
    		if(!bool) {
    			res.status(500);
    			return new String("Error Adding Inventory");
    		}
    		return "Success";
        });
        post("/add-catalog", (req,res) -> {
        	boolean bool = DatabaseManager.post(req);
    		if(!bool) {
    			res.status(500);
    			return new String("Error Adding Inventory");
    		}
    		return "Success";
        });
        put("/modify-inventory/:next", (req,res) -> {
        	boolean bool = DatabaseManager.put(req);
        	if(!bool) {
    			res.status(500);
    			return new String("Error Modifying Inventory");
    		}
    		return "Success";
        });
        put("/modify-price/:next", (req,res) -> {
        	boolean bool = DatabaseManager.put(req);
        	if(!bool) {
        		res.status(500);
        		return new String("Error Modifying Price");
		}
		return "Success";
    });
        delete("/delete-item/:next", (req,res) -> {
        	boolean bool = DatabaseManager.delete(req);{
        	if(!bool) {
            	res.status(500);
            	return new String("Error Deleting Item");
    		}
    		return "Success";
        	}
        });
        delete("/delete-distributor/:next", (req,res) -> {
        	boolean bool = DatabaseManager.delete(req);{
        	if(!bool) {
            	res.status(500);
            	return new String("Error Deleting Distributor");
    		}
    		return "Success";
        	}
        });
        get("/version", (req, res) -> "TopBloc Code Challenge v1.0");
        
        
        
    }
}