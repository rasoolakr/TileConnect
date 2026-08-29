package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.AuthDtos.*;
import com.example.tilecommerce.entity.Address;
import com.example.tilecommerce.entity.Shop;
import com.example.tilecommerce.entity.User;
import com.example.tilecommerce.enumeration.Role;
import com.example.tilecommerce.repository.AddressRepository;
import com.example.tilecommerce.repository.ShopRepository;
import com.example.tilecommerce.repository.UserRepository;
import com.example.tilecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final AddressRepository addresses;
    private final ShopRepository shops;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;

    @Transactional
    public LoginResponse register(RegisterRequest r) {
        validateUniqueUser(r.username(), r.email());

        User u = new User();
        u.setUsername(r.username());
        u.setEmail(r.email());
        u.setPassword(encoder.encode(r.password()));
        u.setRole(Role.CUSTOMER);
        users.save(u);

        if (r.address() != null) {
            saveAddress(u, r.address(), true);
        }

        return login(new LoginRequest(r.username(), r.password()));
    }

    /**
     * Registers a shop owner and creates the shop plus the owner's default address
     * atomically. No Company entity is involved.
     */
    @Transactional
    public LoginResponse registerShop(ShopRegisterRequest r) {
        validateUniqueUser(r.username(), r.email());

        User owner = new User();
        owner.setUsername(r.username());
        owner.setEmail(r.email());
        owner.setPassword(encoder.encode(r.password()));
        owner.setRole(Role.SHOP_OWNER);

        Shop shop = new Shop();
        shop.setName(r.shopName());
        shop.setDescription(r.shopDescription());
        shop.setPhoneNumber(r.shopPhoneNumber());
        shop.setEmail(r.shopEmail());
        shop.setLogoUrl(r.logoUrl());
        shop.setUpiId(r.upiId());
        shop.setPaymentPhoneNumber(r.paymentPhoneNumber());
        shop.setBankAccountNumber(r.bankAccountNumber());
        shop.setBankIfsc(r.bankIfsc());
        shop.setQrCodeUrl(r.qrCodeUrl());
        shop.setActive(true);
        shops.save(shop);

        owner.setShop(shop);
        users.save(owner);

        saveAddress(owner, r.address(), true);

        return login(new LoginRequest(r.username(), r.password()));
    }

    public LoginResponse login(LoginRequest r) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.username(), r.password()));
        User u = users.findByUsername(r.username()).orElseThrow();
        String token = jwt.generate(
                u.getId(),
                u.getUsername(),
                u.getRole().name(),
                u.getShop() == null ? null : u.getShop().getId());
        return new LoginResponse(
                token,
                u.getId(),
                u.getUsername(),
                u.getRole().name(),
                u.getShop() == null ? null : u.getShop().getId());
    }

    private void validateUniqueUser(String username, String email) {
        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private void saveAddress(User user, RegisterAddressRequest request, boolean defaultAddress) {
        Address a = new Address();
        a.setUser(user);
        a.setAddressLine1(request.addressLine1());
        a.setAddressLine2(request.addressLine2());
        a.setCity(request.city());
        a.setState(request.state());
        a.setPostalCode(request.postalCode());
        a.setCountry(request.country());
        a.setPhoneNumber(request.phoneNumber());
        a.setDefaultAddress(defaultAddress);
        addresses.save(a);
    }
}
