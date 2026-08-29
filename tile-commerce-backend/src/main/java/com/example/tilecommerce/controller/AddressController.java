package com.example.tilecommerce.controller;

import com.example.tilecommerce.dto.AdminDtos.AddressRequest;
import com.example.tilecommerce.entity.Address;
import com.example.tilecommerce.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService service;

    @GetMapping
    public List<Address> list() { return service.list(); }

    @PostMapping
    public Address create(@Valid @RequestBody AddressRequest request) { return service.save(request); }

    @PutMapping("/{id}")
    public Address update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/default")
    public Address setDefault(@PathVariable Long id) { return service.setDefault(id); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
