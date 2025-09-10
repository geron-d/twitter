package com.twitter.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Исключение, выбрасываемое при попытке деактивации последнего активного администратора
 */
public class LastAdminDeactivationException extends ResponseStatusException {

    private static final String DEFAULT_MESSAGE = "Cannot deactivate the last active administrator";

    public LastAdminDeactivationException() {
        super(HttpStatus.CONFLICT, DEFAULT_MESSAGE);
    }

    public LastAdminDeactivationException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }

    public LastAdminDeactivationException(String reason, Throwable cause) {
        super(HttpStatus.CONFLICT, reason, cause);
    }
}
