package com.example.tilecommerce.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
@Entity @Table(name="addresses")
@Getter @Setter @NoArgsConstructor
public class Address extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
    @Column(nullable=false) private String addressLine1;
    private String addressLine2, city, state, postalCode, country, phoneNumber;
    private boolean defaultAddress;
}
