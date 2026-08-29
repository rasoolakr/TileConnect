package com.example.tilecommerce.entity;
import com.example.tilecommerce.enumeration.OrderStatus;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;
@Entity @Table(name="orders",indexes={@Index(name="idx_order_customer",columnList="customer_id"),@Index(name="idx_order_shop",columnList="shop_id")})
@Getter @Setter @NoArgsConstructor
public class CustomerOrder extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id",nullable=false) private User customer;
    @JsonIgnore @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="shop_id",nullable=false) private Shop shop;
    @Column(nullable=false,unique=true) private String orderNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private OrderStatus status=OrderStatus.CREATED;
    @Column(precision=19,scale=2) private BigDecimal subtotal=BigDecimal.ZERO;
    @Column(precision=19,scale=2) private BigDecimal discount=BigDecimal.ZERO;
    @Column(precision=19,scale=2) private BigDecimal tax=BigDecimal.ZERO;
    @Column(precision=19,scale=2) private BigDecimal deliveryCharge=BigDecimal.ZERO;
    @Column(precision=19,scale=2) private BigDecimal grandTotal=BigDecimal.ZERO;
    private String deliveryNotes;
    @OneToMany(mappedBy="order",cascade=CascadeType.ALL,orphanRemoval=true) private List<OrderItem> items=new ArrayList<>();
}
