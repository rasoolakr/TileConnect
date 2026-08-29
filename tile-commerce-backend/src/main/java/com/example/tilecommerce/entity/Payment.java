package com.example.tilecommerce.entity;
import com.example.tilecommerce.enumeration.PaymentStatus;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="payments")
@Getter @Setter @NoArgsConstructor
public class Payment extends BaseEntity {
    @JsonIgnore
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false,unique=true) private CustomerOrder order;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private PaymentStatus status=PaymentStatus.PENDING;
    @Column(precision=19,scale=2,nullable=false) private BigDecimal amount;
    private String paymentReference;
    private LocalDateTime submittedAt, verifiedAt;
}
