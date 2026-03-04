package io.quarkiverse.renarde.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;

/**
 * Validation context for storing validation errors that need to be persisted across redirects.
 * This breaks the circular dependency between Flash and Validation.
 *
 * Flash writes errors to this context, and Validation reads from it.
 */
@Default
@RequestScoped
public class ValidationContext {

    /**
     * Flash-scoped errors stored as key-value pairs.
     * Keys are prefixed with "error." to distinguish from regular flash values.
     */
    private Map<String, String> flashErrors = new HashMap<>();

    /**
     * Add an error to be persisted in flash scope.
     *
     * @param field the field name (without "error." prefix)
     * @param message the error message (multiple messages for same field are joined with \f)
     */
    public void addFlashError(String field, String message) {
        String key = "error." + field;
        String existing = flashErrors.get(key);
        if (existing != null) {
            flashErrors.put(key, existing + "\f" + message);
        } else {
            flashErrors.put(key, message);
        }
    }

    /**
     * Get all flash-scoped errors to be stored in the flash cookie.
     *
     * @return map of error keys (with "error." prefix) to error messages
     */
    public Map<String, String> getFlashErrors() {
        return flashErrors;
    }

    /**
     * Load errors from flash values into this context.
     * Called by Flash when handling incoming flash cookie.
     *
     * @param flashValues the flash values map (may contain "error.*" entries)
     */
    public void loadFromFlash(Map<String, Object> flashValues) {
        for (Map.Entry<String, Object> entry : flashValues.entrySet()) {
            if (entry.getKey().startsWith("error.")) {
                flashErrors.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * Check if there are any flash-scoped errors.
     *
     * @return true if there are flash errors
     */
    public boolean hasFlashErrors() {
        return !flashErrors.isEmpty();
    }

    /**
     * Clear all flash-scoped errors.
     */
    public void clear() {
        flashErrors.clear();
    }
}
