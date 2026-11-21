package com.hotelms.utils;

import com.hotelms.config.AppConfig;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatUtils {

    private FormatUtils() {}

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static final DateTimeFormatter INV_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final NumberFormat CURRENCY_FMT;

    static {
        CURRENCY_FMT = NumberFormat.getInstance(Locale.forLanguageTag("en-BD"));
        CURRENCY_FMT.setMinimumFractionDigits(2);
        CURRENCY_FMT.setMaximumFractionDigits(2);
    }

    /** ৳12,500.00 */
    public static String formatCurrency(double amount) {
        return AppConfig.CURRENCY_SYMBOL + CURRENCY_FMT.format(amount);
    }

    /** 16 May 2026 */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "—";
    }

    /** 16 May 2026, 03:45 PM */
    public static String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DATETIME_FMT) : "—";
    }

    /** INV-20260516-0042 */
    public static String generateInvoiceNumber(int sequence) {
        return String.format("%s-%s-%04d",
            AppConfig.INVOICE_PREFIX,
            LocalDate.now().format(INV_DATE_FMT),
            sequence);
    }

    /** Truncates long text with ellipsis for table cells */
    public static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 3) + "...";
    }

    /** Capitalises first letter of each word */
    public static String titleCase(String text) {
        if (text == null || text.isBlank()) return text;
        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /** Formats nights: 1 → "1 night", 3 → "3 nights" */
    public static String formatNights(long nights) {
        return nights + (nights == 1 ? " night" : " nights");
    }
}
