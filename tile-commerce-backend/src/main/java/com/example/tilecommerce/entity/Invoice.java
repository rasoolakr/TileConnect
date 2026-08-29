package com.example.tilecommerce.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity @Table(name="invoices")
@Getter @Setter @NoArgsConstructor
public class Invoice extends BaseEntity {
    @JsonIgnore
    @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="order_id",nullable=false,unique=true) private CustomerOrder order;
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="shop_id",nullable=false) private Shop shop;
    @Column(nullable=false,unique=true) private String invoiceNumber;
    @Column(precision=19,scale=2) private BigDecimal subtotal;
    @Column(precision=19,scale=2) private BigDecimal discount;
    @Column(precision=19,scale=2) private BigDecimal tax;
    @Column(precision=19,scale=2) private BigDecimal deliveryCharge;
    @Column(precision=19,scale=2) private BigDecimal grandTotal;
    private String paymentStatus;
    private LocalDate invoiceDate;
    @OneToMany(mappedBy="invoice",cascade=CascadeType.ALL,orphanRemoval=true) private List<InvoiceItem> items=new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonProperty("orderNumber")
    public String getOrderNumber() { return order == null ? null : order.getOrderNumber(); }
    @com.fasterxml.jackson.annotation.JsonProperty("shopName")
    public String getShopName() { return shop == null ? null : shop.getName(); }
    @com.fasterxml.jackson.annotation.JsonProperty("shopEmail")
    public String getShopEmail() { return shop == null ? null : shop.getEmail(); }
}
