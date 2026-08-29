package com.example.tilecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TileCommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TileCommerceApplication.class, args);
    }
}
