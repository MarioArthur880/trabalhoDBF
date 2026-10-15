package com.cefet.liftly.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    public record ApiError(int status, String code, String message, Map<String, String> fieldErrors) {}
    private ResponseEntity<ApiError> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(status, code, message, Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError(400, "VALIDATION_ERROR", "Confira os campos informados.", fields));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> invalid(Exception ex) {
        return error(400, "INVALID_DATA", "Os dados enviados são inválidos. Confira os campos e tente novamente.");
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> status(ResponseStatusException ex) {
        int status = ex.getStatusCode().value();
        String message = status >= 500 ? "Não foi possível concluir a operação. Tente novamente mais tarde."
            : ex.getReason() == null ? "Não foi possível concluir esta operação." : ex.getReason();
        return error(status, "REQUEST_ERROR", message);
    }
    @ExceptionHandler({DataAccessException.class, TransactionException.class, jakarta.persistence.PersistenceException.class})
    public ResponseEntity<ApiError> database(Exception ex) {
        log.error("Falha ao acessar o banco", ex);
        String state = null;
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getSQLState() != null) { state = sql.getSQLState(); break; }
        }
        if ("23505".equals(state)) return error(409, "DUPLICATE_DATA", "Já existe um registro com esses dados.");
        if ("23503".equals(state)) return error(409, "RELATED_RECORD", "Este registro possui vínculos com outros dados. Atualize a página e confira os registros relacionados.");
        if (ex instanceof DataIntegrityViolationException || (state != null && state.startsWith("23")))
            return error(409, "DATA_CONFLICT", "Os dados entram em conflito com registros existentes. Confira as informações.");
        if (state != null && (state.startsWith("40") || "55P03".equals(state)))
            return error(409, "CONCURRENT_UPDATE", "Os dados foram alterados por outra operação. Atualize a página e tente novamente.");
        return error(503, "DATABASE_UNAVAILABLE", "Não foi possível acessar os dados agora. Tente novamente em instantes.");
    }
    @ExceptionHandler(org.springframework.web.ErrorResponseException.class)
    public ResponseEntity<ApiError> framework(org.springframework.web.ErrorResponseException ex) {
        return error(ex.getStatusCode().value(), "REQUEST_ERROR", "Não foi possível processar esta solicitação. Confira os dados enviados.");
    }
    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> parameter(Exception ex) {
        return error(400, "INVALID_PARAMETER", "Confira os parâmetros informados e tente novamente.");
    }
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> upload(Exception ex) {
        return error(413, "FILE_TOO_LARGE", "O arquivo é muito grande. Escolha uma imagem menor.");
    }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> method(Exception ex) {
        return error(405, "METHOD_NOT_ALLOWED", "Esta ação não está disponível para esta solicitação.");
    }
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> media(Exception ex) {
        return error(415, "UNSUPPORTED_MEDIA_TYPE", "O formato dos dados enviados não é aceito.");
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("Falha inesperada na API", ex);
        return error(500, "INTERNAL_ERROR", "Não foi possível concluir a operação. Tente novamente mais tarde.");
    }
}
