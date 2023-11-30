package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.*;

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
        get("/items", (req, res) -> DatabaseManager.getItems(""));
        get("/items/:next", (req, res) -> DatabaseManager.getItems(req.params(":next")));
        get("/distributors", (req, res) -> DatabaseManager.getDistributors(""));
        get("/distributors/:next", (req, res) -> DatabaseManager.getDistributors(req.params(":next")));
        get("/distributors/item-id/:next", (req, res) -> DatabaseManager.getDistributors("/item-id/" + req.params(":next")));
        post("/add-item", (req,res) -> DatabaseManager.post(req));
        post("/add-inventory", (req,res) -> DatabaseManager.post(req));
        post("/add-distributor", (req,res) -> DatabaseManager.post(req));
        post("/add-catalog", (req,res) -> DatabaseManager.post(req));
        put("/modify-inventory/:next", (req,res) -> DatabaseManager.put(req));
        put("/modify-price/:next", (req,res) -> DatabaseManager.put(req));
        delete("/delete-item/:next", (req,res) -> DatabaseManager.delete(req));
        delete("/delete-distributor/:next", (req,res) -> DatabaseManager.delete(req));


        
        get("/version", (req, res) -> "TopBloc Code Challenge v1.0");
        
        
        
    }
}