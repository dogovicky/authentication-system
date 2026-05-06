package ke.co.legalbridge.authservice.utilities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // Password strength patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;

    /*
     * Hash a plain text password using Bcrypt
     * Mainly for storing passwords in the database
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot me null or empty");
        }
        return passwordEncoder.encode(plainPassword);
    }

    /*
     * Verify if plain password matches the hashed password
     * For login purposes
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    /*
     * Validate password strength
     * Return true if password meets minimum requirements
     */
    public boolean isStrongPassword(String password) {
        if (password == null) {
            return false;
        }

        // Check length
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        // Check for at least one uppercase
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for at least one lowercase
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for one numeric value
        if (!DIGIT_PATTERN.matcher(password).find()) {
            return false;
        }

        // Check for one special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return false;
        }

        return true;

    }

    /*
     * Get detailed password validation result with specific failures
     */
    public PasswordValidationResult validationResult(String password) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null) {
            result.setValid(false);
            result.addError("Password cannot be null");
            return result;
        }

        result.setValid(true);

        // Length check
        if (password.length() < MIN_PASSWORD_LENGTH) {
            result.setValid(false);
            result.addError(String.format("Password must be at least %d characters long", MIN_PASSWORD_LENGTH));
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            result.setValid(false);
            result.addError(String.format("Password must not exceed %d characters", MAX_PASSWORD_LENGTH));
        }

        // Complexity checks
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            result.setValid(false);
            result.addError("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            result.setValid(false);
            result.addError("Password must contain at least one lowercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            result.setValid(false);
            result.addError("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            result.setValid(false);
            result.addError("Password must contain at least one special character (!@#$%^&*...)");
        }

        return result;
    }

    /*
     * Generate a temporary password (for password reset emails)
     */
    public String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";
        StringBuilder password = new StringBuilder();

        SecureRandom random = new SecureRandom();

        // Ensure at least one of each required type
        password.append(chars.charAt(random.nextInt(26))); // uppercase
        password.append(chars.charAt(26 + random.nextInt(24))); // lowercase
        password.append(chars.charAt(50 + random.nextInt(8))); // digit
        password.append(chars.charAt(58 + random.nextInt(chars.length() - 58))); // special

        // Fill remaining with random characters
        for (int i = 4; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Shuffle the password
        return shuffleString(password.toString());
    }

    // =================== PRIVATE HELPER METHODS ================
    private String shuffleString(String str) {
        char[] chars = str.toCharArray();

        SecureRandom random = new SecureRandom();

        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    private boolean containsSequentialChars(String str, int minLength) {
        for (int i = 0; i <= str.length() - minLength; i++) {
            boolean sequential = true;
            for (int j = 1; j < minLength; j++) {
                if (str.charAt(i + j) != str.charAt(i + j - 1) + 1) {
                    sequential = false;
                    break;
                }
            }
            if (sequential) {
                return true;
            }
        }
        return false;
    }

    private boolean containsRepeatedChars(String str, int minLength) {
        for (int i = 0; i <= str.length() - minLength; i++) {
            char firstChar = str.charAt(i);
            boolean repeated = true;
            for (int j = 1; j < minLength; j++) {
                if (str.charAt(i + j) != firstChar) {
                    repeated = false;
                    break;
                }
            }
            if (repeated) {
                return true;
            }
        }
        return false;
    }





    // ==================== INNER CLASS ====================

    /*
     * Result object for detailed password validation
     */
    @Getter
    @Setter
    public static class PasswordValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();

        public void addError(String error) {
            this.errors.add(error);
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

}
