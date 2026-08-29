package com.example.tilecommerce.service;

import com.example.tilecommerce.entity.*;
import com.example.tilecommerce.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class InvoiceServiceTest {
	@Test
	void createInvoiceCopiesTotals() {
		InvoiceRepository repo = Mockito.mock(InvoiceRepository.class);
		InvoiceService s = new InvoiceService(repo);
		CustomerOrder o = new CustomerOrder();
		o.setOrderNumber("ORD-1");
		o.setSubtotal(new BigDecimal("100"));
		o.setDiscount(BigDecimal.ZERO);
		o.setTax(BigDecimal.ZERO);
		o.setDeliveryCharge(BigDecimal.ZERO);
		o.setGrandTotal(new BigDecimal("100"));
		Shop sh = new Shop();
		sh.setName("Demo");
		o.setShop(sh);
		Mockito.when(repo.findByOrder_Id(null)).thenReturn(java.util.Optional.empty());
		Mockito.when(repo.save(Mockito.any())).thenAnswer(x -> x.getArgument(0));
		Invoice i = s.createInvoice(o);
		assertEquals("INV-ORD-1", i.getInvoiceNumber());
		assertEquals(new BigDecimal("100"), i.getGrandTotal());
	}
}
