package com.example.tilecommerce.config;
import org.springframework.context.annotation.Configuration;import org.springframework.web.servlet.config.annotation.*;import java.nio.file.*;
@Configuration public class WebConfig implements WebMvcConfigurer {public void addResourceHandlers(ResourceHandlerRegistry r){r.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");}}
