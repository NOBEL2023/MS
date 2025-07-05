package com.gym.controller;

import com.gym.dto.GymDTO;
import com.gym.service.GymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gyms")
public class GymController {

    @Autowired
    private GymService gymService;

    @GetMapping
    public List<GymDTO> getAllGyms() {
        return gymService.getAllGyms();
    }

    @GetMapping("/{id}")
    public GymDTO getGymById(@PathVariable String id) {
        return gymService.getGymById(id).orElse(null);
    }

    @PostMapping
    public GymDTO createGym(@RequestBody GymDTO gymDTO) {
        return gymService.createGym(gymDTO);
    }

    @PutMapping("/{id}")
    public GymDTO updateGym(@PathVariable String id, @RequestBody GymDTO gymDTO) {
        return gymService.updateGym(id, gymDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteGym(@PathVariable String id) {
        gymService.deleteGym(id);
    }
}
