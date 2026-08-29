package com.example.tilecommerce.dto;
import jakarta.validation.constraints.*;
public final class AdminDtos { private AdminDtos(){}
 public record ShopRequest(@NotBlank String name,String description,String address,String city,String state,String postalCode,String phoneNumber,String email,String upiId,String paymentPhoneNumber,String bankAccountNumber,String bankIfsc,String qrCodeUrl){}
 public record UserRequest(@NotBlank String username,@NotBlank @Email String email,@NotBlank String password,@NotBlank String role,Long shopId){}
 public record CategoryRequest(@NotBlank String name,String description){}
 public record AddressRequest(@NotBlank String addressLine1,String addressLine2,String city,String state,String postalCode,String country,String phoneNumber,boolean defaultAddress){}
}
