package com.example.tilecommerce.service;
import com.example.tilecommerce.entity.*; import com.example.tilecommerce.repository.*; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import com.example.tilecommerce.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException; import java.io.*; import java.math.BigDecimal; import java.time.LocalDate; import com.lowagie.text.*; import com.lowagie.text.pdf.*;
@Service @RequiredArgsConstructor public class InvoiceService {
 private final InvoiceRepository invoices;
 @Transactional public Invoice createInvoice(CustomerOrder o){return invoices.findByOrder_Id(o.getId()).orElseGet(()->{Invoice i=new Invoice();i.setOrder(o);i.setShop(o.getShop());i.setInvoiceNumber("INV-"+o.getOrderNumber());i.setSubtotal(o.getSubtotal());i.setDiscount(o.getDiscount());i.setTax(o.getTax());i.setDeliveryCharge(o.getDeliveryCharge());i.setGrandTotal(o.getGrandTotal());i.setPaymentStatus("VERIFIED");i.setInvoiceDate(LocalDate.now());for(OrderItem oi:o.getItems()){InvoiceItem x=new InvoiceItem();x.setInvoice(i);x.setProductName(oi.getProductName());x.setVariantSize(oi.getVariantSize());x.setQuantity(oi.getQuantity());x.setUnitPrice(oi.getUnitPrice());x.setDiscount(oi.getDiscount());x.setTax(oi.getTax());x.setTotal(oi.getTotal());i.getItems().add(x);}return invoices.save(i);});}
 @Transactional(readOnly=true) public Invoice get(Long orderId){
   Invoice i=invoices.findByOrder_Id(orderId).orElseThrow(()->new java.util.NoSuchElementException("Invoice not found"));
   var u=CurrentUser.get().getUser();
   boolean owner=u.getRole().name().equals("SUPER_ADMIN") ||
       (u.getRole().name().equals("SHOP_OWNER") && u.getShop()!=null && i.getShop()!=null && u.getShop().getId().equals(i.getShop().getId()));
   if(!owner && (i.getOrder().getCustomer()==null || !i.getOrder().getCustomer().getId().equals(u.getId())))
       throw new AccessDeniedException("Invoice access denied");
   i.getItems().forEach(x -> x.getProductName());
   i.getOrder().getOrderNumber();
   i.getShop().getName();
   return i;
 }
 @Transactional(readOnly=true) public byte[] pdf(Long orderId){Invoice i=get(orderId);try{ByteArrayOutputStream out=new ByteArrayOutputStream();Document d=new Document();PdfWriter.getInstance(d,out);d.open();d.add(new Paragraph(i.getShop().getName(),FontFactory.getFont(FontFactory.HELVETICA_BOLD,18)));d.add(new Paragraph("Invoice: "+i.getInvoiceNumber()+"    Date: "+i.getInvoiceDate()));d.add(new Paragraph("Order: "+i.getOrder().getOrderNumber()));d.add(new Paragraph(" "));PdfPTable t=new PdfPTable(5);String[] h={"Product","Size","Qty","Unit Price","Total"};for(String x:h)t.addCell(x);for(InvoiceItem x:i.getItems()){t.addCell(x.getProductName());t.addCell(x.getVariantSize());t.addCell(String.valueOf(x.getQuantity()));t.addCell(x.getUnitPrice().toString());t.addCell(x.getTotal().toString());}d.add(t);d.add(new Paragraph("Grand Total: INR "+i.getGrandTotal()));d.close();return out.toByteArray();}catch(Exception e){throw new IllegalStateException("Unable to generate invoice",e);}}
}
