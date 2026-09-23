package com.mybank.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundError.class)
    public ResponseEntity<ApiError> handleAccountNotFoundError(
            AccountNotFoundError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(InsufficientBalanceError.class)
    public ResponseEntity<ApiError> handleInsufficientBalanceError(
            InsufficientBalanceError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }

    @ExceptionHandler(InvalidAmountError.class)
    public ResponseEntity<ApiError> handleInvalidAmountError(
            InvalidAmountError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }

    @ExceptionHandler(AccountBlockedError.class)
    public ResponseEntity<ApiError> handleAccountBlockedError(
            AccountBlockedError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }
    @ExceptionHandler(TransactionNotFoundError.class)
    public ResponseEntity<ApiError> handleTransactionNotFoundError(
            TransactionNotFoundError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }
    @ExceptionHandler(InvalidTransactionAmountError.class)
    public ResponseEntity<ApiError> handleInvalidTransactionAmountError(
            InvalidTransactionAmountError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }
    @ExceptionHandler(InvalidTransactionTypeError.class)
    public ResponseEntity<ApiError> handleInvalidTransactionTypeError(
            InvalidTransactionTypeError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }
    @ExceptionHandler(CardNotFoundError.class)
    public ResponseEntity<ApiError> handleCardNotFoundError(
            CardNotFoundError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }
    @ExceptionHandler(CardBlockedError.class)
    public ResponseEntity<ApiError> handleCardBlockedError(
            CardBlockedError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }
    @ExceptionHandler(CardCancelledError.class)
    public ResponseEntity<ApiError> handleCardCancelledError(
            CardCancelledError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }
    @ExceptionHandler(CardExpiredError.class)
    public ResponseEntity<ApiError> handleCardExpiredError(
            CardExpiredError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }
    @ExceptionHandler(CardAlreadyBlockedError.class)
    public ResponseEntity<ApiError> handleCardAlreadyBlockedError(
            CardAlreadyBlockedError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }
    @ExceptionHandler(CardAlreadyCancelledError.class)
    public ResponseEntity<ApiError> handleCardAlreadyCancelledError(
            CardAlreadyCancelledError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }
    @ExceptionHandler(InvalidCardTypeError.class)
    public ResponseEntity<ApiError> handleInvalidCardTypeError(
            InvalidCardTypeError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(apiError);
    }

    @ExceptionHandler(CreditCardInvoiceNotFoundError.class)
    public ResponseEntity<ApiError> handleCreditCardInvoiceNotFoundError(
            CreditCardInvoiceNotFoundError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }
    @ExceptionHandler(InvoiceAlreadyClosedError.class)
    public ResponseEntity<ApiError> handleInvoiceAlreadyClosedError(
            InvoiceAlreadyClosedError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(apiError);
    }
    @ExceptionHandler(InvoiceAlreadyPaidError.class)
    public ResponseEntity<ApiError> handleInvoiceAlreadyPaidError(
            InvoiceAlreadyPaidError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(apiError);
    }
    @ExceptionHandler(InvoiceNotOpenError.class)
    public ResponseEntity<ApiError> handleInvoiceNotOpenError(
            InvoiceNotOpenError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(apiError);
    }
    @ExceptionHandler(InvoiceNotClosedError.class)
    public ResponseEntity<ApiError> handleInvoiceNotClosedError(
            InvoiceNotClosedError ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(apiError);
    }

}