package com.example.tilecommerce.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="order_items")
@Getter @Setter @NoArgsConstructor
public class OrderItem extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false) private CustomerOrder order;
    @JsonIgnore @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_id",nullable=false) private Product product;
    @JsonIgnore @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="product_variant_id",nullable=false) private ProductVariant productVariant;
    private String productName, variantSize;
    private int quantity;
    @Column(precision=19,scale=2) private BigDecimal unitPrice;
    @Column(precision=19,scale=2) private BigDecimal discount=BigDecimal.ZERO;
    @Column(precision=19,scale=2) private BigDecimal tax=BigDecimal.ZERO;
    @Column(precision=19,scale=2) private BigDecimal total=BigDecimal.ZERO;
}
