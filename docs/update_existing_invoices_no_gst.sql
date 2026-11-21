-- Run this in phpMyAdmin after selecting hotel_db.
-- Recalculates unpaid/partially paid invoices without GST.
-- Keeps already-paid historical invoices unchanged.
UPDATE invoices
SET tax_percent = 0.00,
    tax_amount = 0.00,
    net_amount = GREATEST(room_charges + service_charges - discount_amount, 0.00)
WHERE payment_status IN ('PENDING', 'PARTIAL');
