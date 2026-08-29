package com.example.tilecommerce.entity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="invoice_items")
@Getter @Setter @NoArgsConstructor
public class InvoiceItem extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="invoice_id",nullable=false) private Invoice invoice;
    private String productName, variantSize;
    private int quantity;
    @Column(precision=19,scale=2) private BigDecimal unitPrice;
    @Column(precision=19,scale=2) private BigDecimal discount;
    @Column(precision=19,scale=2) private BigDecimal tax;
    @Column(precision=19,scale=2) private BigDecimal total;
}
