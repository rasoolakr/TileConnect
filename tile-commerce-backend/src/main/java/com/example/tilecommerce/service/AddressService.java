package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.AdminDtos.AddressRequest;
import com.example.tilecommerce.entity.Address;
import com.example.tilecommerce.security.CurrentUser;
import com.example.tilecommerce.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository repo;

    public List<Address> list() {
        return repo.findByUser_Id(CurrentUser.id());
    }

    @Transactional
    public Address save(AddressRequest r) {
        if (r.defaultAddress()) {
            repo.findByUser_Id(CurrentUser.id()).forEach(a -> a.setDefaultAddress(false));
        }
        Address a = new Address();
        a.setUser(CurrentUser.get().getUser());
        apply(a, r);
        a.setDefaultAddress(r.defaultAddress());
        return repo.save(a);
    }

    @Transactional
    public Address update(Long id, AddressRequest r) {
        Address a = owned(id);
        if (r.defaultAddress()) {
            repo.findByUser_Id(CurrentUser.id()).forEach(existing -> {
                if (!existing.getId().equals(id)) existing.setDefaultAddress(false);
            });
        }
        apply(a, r);
        a.setDefaultAddress(r.defaultAddress());
        return repo.save(a);
    }

    @Transactional
    public Address setDefault(Long id) {
        Address selected = owned(id);
        repo.findByUser_Id(CurrentUser.id()).forEach(a -> a.setDefaultAddress(a.getId().equals(id)));
        return selected;
    }

    @Transactional
    public void delete(Long id) {
        repo.delete(owned(id));
    }

    private Address owned(Long id) {
        Address a = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!a.getUser().getId().equals(CurrentUser.id())) {
            throw new AccessDeniedException("Address access denied");
        }
        return a;
    }

    private void apply(Address a, AddressRequest r) {
        a.setAddressLine1(r.addressLine1());
        a.setAddressLine2(r.addressLine2());
        a.setCity(r.city());
        a.setState(r.state());
        a.setPostalCode(r.postalCode());
        a.setCountry(r.country());
        a.setPhoneNumber(r.phoneNumber());
    }
}
