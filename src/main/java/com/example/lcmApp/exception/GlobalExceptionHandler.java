package com.example.lcmApp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionSystemException;
import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== Наши исключения =====
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        log.warn("Ресурс уже существует: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Недостаточно товара: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogic(BusinessLogicException ex) {
        log.warn("Ошибка бизнес-логики: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(DatabaseException ex) {
        log.error("Ошибка базы данных: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleService(ServiceException ex) {
        log.error("Ошибка сервиса: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidStateException ex) {
        log.warn("Некорректное состояние: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Доступ запрещен: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        log.warn("Ошибка аутентификации: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    // ===== Исключения Spring Security =====
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        log.warn("Пользователь не найден: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid credentials", ex.getMessage()));
    }
    

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
        log.warn("Срок действия JWT истек: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Token expired, please log in again", ex.getMessage()));
    }

    // ===== Ошибки валидации Spring =====
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации запроса");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errors.toString(), ex.getMessage()));
    }

    // ===== Ошибки базы данных Spring =====
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex) {
        log.error("Ошибка доступа к данным: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Database error", ex.getMessage()));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransaction(TransactionSystemException ex) {
        log.error("Ошибка транзакции: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Transaction error", ex.getMessage()));
    }

    // ===== Прочие ошибки =====
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Неверный аргумент: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Некорректное состояние: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.debug("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleFileLogger(IOException ex) {
        log.error("Ошибка чтения/записи файла: {}", ex.getMessage());
        if (ex.getCause() != null) {
            log.error("Причина: ", ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ex.getMessage(), getCauseMessage(ex)));
    }

    // ===== Обработка всех остальных исключений =====
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error", ex.getMessage()));
    }

    // ===== Вспомогательный метод =====
    private String getCauseMessage(Throwable ex) {
        if (ex.getCause() != null) {
            return ex.getCause().getMessage();
        }
        return null;
    }
}