package com.example.tilecommerce.service;

import com.example.tilecommerce.dto.OrderDtos.*;
import com.example.tilecommerce.entity.*;
import com.example.tilecommerce.enumeration.*;
import com.example.tilecommerce.repository.*;
import com.example.tilecommerce.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final CartRepository carts;
	private final CustomerOrderRepository orders;
	private final AddressRepository addresses;
	private final ProductVariantRepository variants;
	private final PaymentRepository payments;
	private final InvoiceService invoiceService;

	@Transactional
	public CustomerOrder checkout(CheckoutRequest r) {
		Cart c = carts.findByUser_IdAndStatus(CurrentUser.id(), CartStatus.ACTIVE)
				.orElseThrow(() -> new IllegalArgumentException("Cart is empty"));
		if (c.getItems().isEmpty())
			throw new IllegalArgumentException("Cart is empty");
		Address a = addresses.findById(r.addressId())
				.orElseThrow(() -> new NoSuchElementException("Address not found"));
		if (!a.getUser().getId().equals(CurrentUser.id()))
			throw new org.springframework.security.access.AccessDeniedException("Address access denied");
		CustomerOrder o = new CustomerOrder();
		o.setCustomer(CurrentUser.get().getUser());
		o.setShop(c.getShop());
		o.setOrderNumber("ORD-" + System.currentTimeMillis());
		o.setDeliveryNotes(r.deliveryNotes());
		BigDecimal sub = BigDecimal.ZERO;
		for (CartItem ci : c.getItems()) {
			ProductVariant v = variants.findById(ci.getProductVariant().getId()).orElseThrow();
			if (v.getStockQuantity() < ci.getQuantity())
				throw new IllegalArgumentException("Insufficient stock for " + v.getProduct().getName());
			v.setStockQuantity(v.getStockQuantity() - ci.getQuantity());
			variants.save(v);
			OrderItem oi = new OrderItem();
			oi.setOrder(o);
			oi.setProduct(v.getProduct());
			oi.setProductVariant(v);
			oi.setProductName(v.getProduct().getName());
			oi.setVariantSize(v.getSize());
			oi.setQuantity(ci.getQuantity());
			oi.setUnitPrice(v.getPrice());
			oi.setTotal(v.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
			o.getItems().add(oi);
			sub = sub.add(oi.getTotal());
		}
		o.setSubtotal(sub);
		o.setDiscount(BigDecimal.ZERO);
		o.setTax(BigDecimal.ZERO);
		o.setDeliveryCharge(BigDecimal.ZERO);
		o.setGrandTotal(sub);
		o.setStatus(OrderStatus.PAYMENT_PENDING);
		CustomerOrder saved = orders.save(o);
		Payment p = new Payment();
		p.setOrder(saved);
		p.setAmount(saved.getGrandTotal());
		payments.save(p);
		c.setStatus(CartStatus.CHECKED_OUT);
		carts.save(c);
		return saved;
	}

	@Transactional(readOnly = true)
	public List<CustomerOrder> myOrders() {
		List<CustomerOrder> result = orders.findByCustomer_IdOrderByCreatedAtDesc(CurrentUser.id());
		result.forEach(o -> o.getItems().forEach(i -> i.getProductName()));
		return result;
	}

	@Transactional(readOnly = true)
	public CustomerOrder get(Long id) {
		CustomerOrder o = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
		if (!o.getCustomer().getId().equals(CurrentUser.id()) && !CurrentUser.get().getAuthorities().stream().anyMatch(
				a -> a.getAuthority().equals("ROLE_SHOP_OWNER") || a.getAuthority().equals("ROLE_SUPER_ADMIN")))
			throw new org.springframework.security.access.AccessDeniedException("Order access denied");
		o.getItems().forEach(i -> i.getProductName());
		return o;
	}

	@Transactional
	public CustomerOrder status(Long id, String status) {
		CustomerOrder o = orders.findById(id).orElseThrow();
		try {
			o.setStatus(OrderStatus.valueOf(status));
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid order status");
		}
		return orders.save(o);
	}

	@Transactional
	public Payment submitPayment(Long id, String ref) {
		CustomerOrder o = get(id);
		if (o.getStatus() != OrderStatus.PAYMENT_PENDING)
			throw new IllegalArgumentException("Order is not awaiting payment");
		Payment p = payments.findByOrder_Id(id).orElseThrow();
		p.setPaymentReference(ref);
		p.setStatus(PaymentStatus.OFFLINE_PAYMENT_SUBMITTED);
		p.setSubmittedAt(LocalDateTime.now());
		o.setStatus(OrderStatus.PAYMENT_SUBMITTED);
		orders.save(o);
		return payments.save(p);
	}

	@Transactional
	public Payment verifyPayment(Long id) {
		CustomerOrder o = orders.findById(id).orElseThrow();
		Payment p = payments.findByOrder_Id(id).orElseThrow();
		p.setStatus(PaymentStatus.VERIFIED);
		p.setVerifiedAt(LocalDateTime.now());
		o.setStatus(OrderStatus.CONFIRMED);
		orders.save(o);
		Payment saved = payments.save(p);
		invoiceService.createInvoice(o);
		return saved;
	}
}
