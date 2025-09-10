package com.bank.unit;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class UnitTests {

    // How to run: mvn test -pl unit-tests
    @Test
    public void shouldReturnCorrectSum() {
        int sum = 2 + 2;
        Assert.assertEquals(sum, 4);
    }

    // Password validator: reject short passwords
    @Test
    public void passwordValidatorShouldRejectShortPasswords() {
        Assert.assertFalse(isPasswordValid("123"));
    }

    // Password validator: accept sufficiently long passwords
    @Test
    public void passwordValidatorShouldAcceptLongPasswords() {
        Assert.assertTrue(isPasswordValid("password123"));
    }

    // Email validator: valid email
    @Test
    public void emailValidatorShouldAcceptValidEmail() {
        Assert.assertTrue(isEmailValid("user@example.com"));
    }

    // Email validator: invalid email
    @Test
    public void emailValidatorShouldRejectInvalidEmail() {
        Assert.assertFalse(isEmailValid("not-an-email"));
    }

    // Amount parsing: parse decimal string to BigDecimal
    @Test
    public void amountParsingShouldWork() {
        BigDecimal amount = parseAmount("100.50");
        Assert.assertEquals(amount, new BigDecimal("100.50"));
    }

    // Currency formatting: two decimal places
    @Test
    public void currencyFormattingShouldProduceTwoDecimals() {
        String formatted = formatCurrency(new BigDecimal("12"));
        Assert.assertEquals(formatted, "12.00");
    }

    // Date parsing: parse ISO date
    @Test
    public void dateParsingShouldWork() {
        LocalDate d = parseDate("2025-09-08");
        Assert.assertEquals(d.getYear(), 2025);
        Assert.assertEquals(d.getMonthValue(), 9);
        Assert.assertEquals(d.getDayOfMonth(), 8);
    }

    // Rounding: half-up to 2 decimals
    @Test
    public void roundingShouldRoundHalfUp() {
        BigDecimal val = new BigDecimal("2.345");
        BigDecimal rounded = round(val, 2);
        Assert.assertEquals(rounded, new BigDecimal("2.35"));
    }

    // String utils: capitalize
    @Test
    public void capitalizeShouldUppercaseFirstLetter() {
        Assert.assertEquals(capitalize("hello"), "Hello");
    }

    // Product name validation: allow letters, numbers, hyphen and space
    @Test
    public void productNameValidationShouldWork() {
        Assert.assertTrue(isProductNameValid("Product_1"));
        Assert.assertTrue(isProductNameValid("Product-2"));
        Assert.assertFalse(isProductNameValid("Product@3"));
    }

    // --- helper methods used by tests ---
    private boolean isPasswordValid(String pwd) {
        return pwd != null && pwd.length() >= 6;
    }

    private boolean isEmailValid(String email) {
        if (email == null) return false;
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(regex, email);
    }

    private BigDecimal parseAmount(String s) {
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private LocalDate parseDate(String isoDate) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        return LocalDate.parse(isoDate, fmt);
    }

    private BigDecimal round(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private boolean isProductNameValid(String name) {
        // Корректная escape-последовательность
        return name.matches("^[A-Za-z0-9_\\-]+$");
    }
}
