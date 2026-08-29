package com.example.tilecommerce.controller;
import com.example.tilecommerce.entity.Shop;
import com.example.tilecommerce.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/shops") @RequiredArgsConstructor
public class ShopController {
    private final ShopRepository shops;
    @GetMapping("/public") public List<Shop> publicShops(){return shops.findAll().stream().filter(Shop::isActive).toList();}
}
