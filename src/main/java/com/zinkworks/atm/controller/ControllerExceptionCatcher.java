package com.zinkworks.atm.controller;

import com.zinkworks.atm.exceptions.ATMException;
import com.zinkworks.atm.exceptions.IncorrectAccountInformationException;
import com.zinkworks.atm.exceptions.NoSufficientFundsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionCatcher extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ATMException.class, NoSufficientFundsException.class, IncorrectAccountInformationException.class})
    public ResponseEntity<String> catchATMTasksExceptions(Exception exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> catchUnknownException(Exception exception) {
        return ResponseEntity.internalServerError().body("Something went wrong!");
    }
}
