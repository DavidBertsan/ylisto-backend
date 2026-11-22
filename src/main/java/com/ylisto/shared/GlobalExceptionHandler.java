package com.ylisto.shared;

import com.ylisto.auth.InvalidCredentialsException;
import com.ylisto.business.BusinessNotFoundException;
import com.ylisto.clocking.AlreadyClockedInException;
import com.ylisto.clocking.NotClockedInException;
import com.ylisto.inventory.InsufficientStockException;
import com.ylisto.inventory.InvalidMovementException;
import com.ylisto.inventory.ProductNotFoundException;
import com.ylisto.shift.InvalidShiftException;
import com.ylisto.shift.ShiftNotFoundException;
import com.ylisto.shift.ShiftOverlapException;
import com.ylisto.team.CannotRemoveYourselfException;
import com.ylisto.user.UserNotFoundException;
import com.ylisto.user.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Two writes raced on the same @Version row: the client should reload and retry
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The resource was modified concurrently, please retry");
    }

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
    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentialsException(InvalidCredentialsException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
    @ExceptionHandler(CannotRemoveYourselfException.class)
    ProblemDetail handleCannotRemoveYourselfException(CannotRemoveYourselfException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleProductNotfoundException(ProductNotFoundException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(InvalidShiftException.class)
    ProblemDetail handleInvalidShiftException(InvalidShiftException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(ShiftOverlapException.class)
    ProblemDetail handleShiftOverlapException(ShiftOverlapException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
    @ExceptionHandler(ShiftNotFoundException.class)
    ProblemDetail handleShiftNotFoundException(ShiftNotFoundException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    @ExceptionHandler(InsufficientStockException.class)
    ProblemDetail handleInsufficientStockException(InsufficientStockException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,ex.getMessage());
    }
    @ExceptionHandler(InvalidMovementException.class)
    ProblemDetail handleInvalidMovementException(InvalidMovementException ex){
        return  ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

}
