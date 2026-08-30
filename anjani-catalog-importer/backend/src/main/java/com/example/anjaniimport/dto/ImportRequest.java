package com.example.anjaniimport.dto;

public record ImportRequest(

        // Application-level collection name
        // Example: "Wall Collections"
        String collection,


        // Optional size filter
        // Example: "300 x 600 mm"
        String size,

        // Optional finish filter
        String finish,

        // Optional color filter
        String color,

        // Maximum number of products to fetch
        Integer maxProducts

) {
}
