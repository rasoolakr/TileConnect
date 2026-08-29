package com.example.tilecommerce.entity;
import com.example.tilecommerce.enumeration.Role;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
@Entity @Table(name="users",
    uniqueConstraints={@UniqueConstraint(name="uk_user_username",columnNames="username"),
                       @UniqueConstraint(name="uk_user_email",columnNames="email")},
    indexes=@Index(name="idx_user_shop",columnList="shop_id"))
@Getter @Setter @NoArgsConstructor
public class User extends BaseEntity {
    @Column(nullable=false,length=80) private String username;
    @Column(nullable=false,length=180) private String email;
    @JsonIgnore @Column(nullable=false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private Role role;
    @JsonIgnore @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="shop_id") private Shop shop;
    private boolean active=true, locked=false, deleted=false;
}
