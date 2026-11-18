package com.ylisto.shared;

import com.ylisto.business.BusinessNotFoundException;
import com.ylisto.clocking.AlreadyClockedInException;
import com.ylisto.clocking.NotClockedInException;
import com.ylisto.user.UserNotFoundException;
import com.ylisto.user.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyClockedInException.class)
    ProblemDetail handleAlreadyClockedIn(AlreadyClockedInException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotClockedInException.class)
    ProblemDetail handleNotClockedIn(NotClockedInException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ProblemDetail handleUsernameAlredyExists(UsernameAlreadyExistsException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    ProblemDetail handleBusinessNotFoundException(BusinessNotFoundException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,ex.getMessage());
    }


}
