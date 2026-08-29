package com.example.tilecommerce.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="categories", uniqueConstraints=@UniqueConstraint(name="uk_category_name",columnNames="name"))
@Getter @Setter @NoArgsConstructor
public class Category extends BaseEntity {
    @Column(nullable=false) private String name;
    private String description;
    private boolean active=true;
}
