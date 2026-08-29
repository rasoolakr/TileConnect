package com.example.tilecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
public class Shop extends BaseEntity {
	@Column(nullable = false, length = 150)
	private String name;
	private String description, address, city, state, postalCode, phoneNumber, email, logoUrl;
	private String upiId, paymentPhoneNumber, bankAccountNumber, bankIfsc, qrCodeUrl;
	private boolean active = true;
}
