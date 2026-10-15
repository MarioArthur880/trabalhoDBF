package com.cefet.liftly.exception;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTests {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    @Test void duplicateIsConflictWithoutSqlLeak() {
        var response = handler.database(new DataIntegrityViolationException("secret sql", new SQLException("secret", "23505")));
        assertEquals(409, response.getStatusCode().value());
        assertEquals("DUPLICATE_DATA", response.getBody().code());
        assertFalse(response.getBody().message().contains("secret"));
    }
    @Test void unavailableDatabaseIsRetryable() {
        var response = handler.database(new DataAccessResourceFailureException("password=secret"));
        assertEquals(503, response.getStatusCode().value());
        assertFalse(response.getBody().message().contains("secret"));
    }
    @Test void foreignKeyIsConflict() {
        var response = handler.database(new DataIntegrityViolationException("sql", new SQLException("fk", "23503")));
        assertEquals("RELATED_RECORD", response.getBody().code());
    }
    @Test void unexpectedFailureDoesNotLeakMessage() {
        var response = handler.unexpected(new RuntimeException("password=secret"));
        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().message().contains("secret"));
    }
    @Test void domainStatusAndMessageArePreserved() {
        var response = handler.status(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensagem vazia."));
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Mensagem vazia.", response.getBody().message());
    }
}
