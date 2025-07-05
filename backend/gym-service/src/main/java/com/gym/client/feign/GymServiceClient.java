package com.gym.client.feign;

import com.gym.dto.GymDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gym-service", url = "http://localhost:8081")
public interface GymServiceClient {
    @GetMapping("/gyms")
    List<GymDTO> getAllGyms();

}
