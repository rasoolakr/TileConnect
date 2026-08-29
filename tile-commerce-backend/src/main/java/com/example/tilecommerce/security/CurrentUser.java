package com.example.tilecommerce.security;
import org.springframework.security.core.context.SecurityContextHolder;
public final class CurrentUser {
    private CurrentUser(){}
    public static SecurityUser get(){
        var a=SecurityContextHolder.getContext().getAuthentication();
        if(a==null || !(a.getPrincipal() instanceof SecurityUser u)) throw new IllegalStateException("Unauthenticated");
        return u;
    }
    public static Long id(){return get().getUser().getId();}
    public static Long shopId(){return get().getUser().getShop()==null?null:get().getUser().getShop().getId();}
}
