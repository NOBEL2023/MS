package com.gym.client.controller;
import com.gym.dto.GymDTO;
import com.gym.client.feign.GymServiceClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final GymServiceClient gymServiceClient;

    public ClientController(GymServiceClient gymServiceClient) {
        this.gymServiceClient = gymServiceClient;
    }

    @GetMapping("/gyms")
    public List<GymDTO> getGyms() {
        return gymServiceClient.getAllGyms();
    }

    @PostMapping("/gyms")
    public GymDTO createGym(@RequestBody GymDTO gymDTO) {
        return gymServiceClient.createGym(gymDTO);
    }
}
