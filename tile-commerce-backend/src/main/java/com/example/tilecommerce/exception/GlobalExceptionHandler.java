package com.example.tilecommerce.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<String,Object> base(HttpStatus s,String msg,HttpServletRequest r){
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("timestamp", Instant.now()); m.put("status",s.value()); m.put("error",s.getReasonPhrase());
        m.put("message",msg); m.put("path",r.getRequestURI()); return m;
    }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<?> notFound(NoSuchElementException e,HttpServletRequest r){return ResponseEntity.status(404).body(base(HttpStatus.NOT_FOUND,e.getMessage(),r));}
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<?> state(IllegalStateException e,HttpServletRequest r){return ResponseEntity.status(401).body(base(HttpStatus.UNAUTHORIZED,e.getMessage(),r));}
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<?> upload(Exception e,HttpServletRequest r){return ResponseEntity.badRequest().body(base(HttpStatus.BAD_REQUEST,"Uploaded file is too large",r));}
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> bad(IllegalArgumentException e,HttpServletRequest r){return ResponseEntity.badRequest().body(base(HttpStatus.BAD_REQUEST,e.getMessage(),r));}
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<?> denied(Exception e,HttpServletRequest r){return ResponseEntity.status(403).body(base(HttpStatus.FORBIDDEN,e.getMessage(),r));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e,HttpServletRequest r){
        var m=base(HttpStatus.BAD_REQUEST,"Request validation failed",r);
        Map<String,String> errors=new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));
        m.put("validationErrors",errors); return ResponseEntity.badRequest().body(m);
    }
}
