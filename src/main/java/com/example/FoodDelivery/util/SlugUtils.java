package com.example.FoodDelivery.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {

    /**
     * Generate slug from Vietnamese text
     * Example: "Nhà Hàng Phở Hà Nội" -> "nha-hang-pho-ha-noi"
     * 
     * @param text Input text (Vietnamese or English)
     * @return URL-friendly slug
     */
    public static String generateSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // Convert Vietnamese characters to ASCII
        String slug = convertVietnameseToAscii(text);

        // Convert to lowercase
        slug = slug.toLowerCase();

        // Replace spaces and special characters with hyphens
        slug = slug.replaceAll("[^a-z0-9]+", "-");

        // Remove leading/trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        // Replace multiple consecutive hyphens with single hyphen
        slug = slug.replaceAll("-+", "-");

        return slug;
    }

    /**
     * Convert Vietnamese characters to ASCII equivalents
     */
    private static String convertVietnameseToAscii(String text) {
        // Vietnamese character mappings
        text = text.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        text = text.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        text = text.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        text = text.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        text = text.replaceAll("[ìíịỉĩ]", "i");
        text = text.replaceAll("[ÌÍỊỈĨ]", "I");
        text = text.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        text = text.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        text = text.replaceAll("[ùúụủũưừứựửữ]", "u");
        text = text.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        text = text.replaceAll("[ỳýỵỷỹ]", "y");
        text = text.replaceAll("[ỲÝỴỶỸ]", "Y");
        text = text.replaceAll("đ", "d");
        text = text.replaceAll("Đ", "D");

        // Remove any remaining diacritical marks
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        text = pattern.matcher(text).replaceAll("");

        return text;
    }

    /**
     * Generate unique slug by appending number suffix if needed
     * Example: "nha-hang-pho" -> "nha-hang-pho-2" if original exists
     * 
     * @param baseSlug Base slug to make unique
     * @param counter  Counter for uniqueness (start with 2)
     * @return Unique slug with counter suffix
     */
    public static String makeUniqueSlug(String baseSlug, int counter) {
        return baseSlug + "-" + counter;
    }

    /**
     * Generate slug with ID suffix for guaranteed uniqueness
     * Example: "nha-hang-pho-123" where 123 is restaurant ID
     * 
     * @param text Input text
     * @param id   Entity ID
     * @return Slug with ID suffix
     */
    public static String generateSlugWithId(String text, Long id) {
        String baseSlug = generateSlug(text);
        return baseSlug + "-" + id;
    }
}
