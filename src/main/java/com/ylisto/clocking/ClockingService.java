package com.ylisto.clocking;


import com.ylisto.user.User;
import com.ylisto.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ClockingService {
    private final ClockingRepository clockingRepository;
    private final UserService  userService;
    private final Clock clock;
    public ClockingService(ClockingRepository clockingRepository,UserService userService,Clock clock) {
        this.clockingRepository = clockingRepository;
        this.userService =userService;
        this.clock =clock;
    }
    public Clocking clockIn(UUID userId) {
       if( clockingRepository.findByUserIdAndClockOutIsNull(userId).isPresent()){
           throw new AlreadyClockedInException("The user is alredy Cloked in");
       }
       User user=userService.findById(userId);
       Clocking clocking=new Clocking(user,Instant.now(clock));
        return clockingRepository.save(clocking);

    }



    public Clocking clockOut(UUID userId) {
        Clocking clocking =  clockingRepository.findByUserIdAndClockOutIsNull(userId).orElseThrow(() ->new NotClockedInException("Not clocked"));
        clocking.clockOut(Instant.now(clock));
        return clockingRepository.save(clocking);
    }

}
