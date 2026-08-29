package com.example.tilecommerce.security;
import com.example.tilecommerce.entity.User;
import lombok.Getter;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
@Getter
public class SecurityUser implements UserDetails {
    private final User user;
    public SecurityUser(User user) { this.user=user; }
    @Override public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
    }
    @Override public String getPassword(){return user.getPassword();}
    @Override public String getUsername(){return user.getUsername();}
    @Override public boolean isAccountNonExpired(){return true;}
    @Override public boolean isAccountNonLocked(){return !user.isLocked();}
    @Override public boolean isCredentialsNonExpired(){return true;}
    @Override public boolean isEnabled(){return user.isActive()&&!user.isDeleted();}
}
