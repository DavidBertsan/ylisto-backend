package com.ylisto.shift;

import com.ylisto.user.User;
import com.ylisto.user.UserNotFoundException;
import com.ylisto.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import java.util.UUID;

@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final  UserService userService;

    public ShiftService(ShiftRepository shiftRepository, UserService userService) {
        this.shiftRepository = shiftRepository;
        this.userService = userService;
    }
    public Shift assign(UUID businessId, UUID employeeId, Instant startsAt, Instant endsAt) {
        if(!endsAt.isAfter(startsAt)){
            throw new InvalidShiftException("Invalid Shift");
        }
        User user=userService.findByIdAndBusinessId(employeeId,businessId).filter(User::isActive).orElseThrow(()->new UserNotFoundException("User not found: " + employeeId));
        if(shiftRepository.existsByUserIdAndStartsAtLessThanAndEndsAtGreaterThan(employeeId,endsAt,startsAt)){
            throw new ShiftOverlapException("Shift overlaps with an existing one");
        }
        return shiftRepository.save(new Shift(user, startsAt, endsAt));
    }
    public List<Shift> mySchedule(UUID userId, Instant from, Instant to) {
        return  shiftRepository.findAllByUserIdAndStartsAtBetweenOrderByStartsAt(userId,from,to);
    }
    public void remove(UUID businessId, UUID shiftId) {
        Shift shift = shiftRepository.findByIdAndUserBusinessId(shiftId, businessId)
                .orElseThrow(() -> new ShiftNotFoundException("Shift not found: " + shiftId));
        shiftRepository.delete(shift);
    }
}
