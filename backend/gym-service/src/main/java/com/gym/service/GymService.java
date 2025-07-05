package com.gym.service;

import com.gym.dto.GymDTO;
import com.gym.mapper.GymMapper;
import com.gym.entity.Gym;
import com.gym.repository.GymRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GymService {

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private GymMapper gymMapper;

    public List<GymDTO> getAllGyms() {
        return gymMapper.toDTOs(gymRepository.findAll());
    }

    public Optional<GymDTO> getGymById(String id) {
        return gymRepository.findById(id).map(gymMapper::toDTO);
    }

    public GymDTO createGym(GymDTO dto) {
        Gym gym = gymMapper.toEntity(dto);
        return gymMapper.toDTO(gymRepository.save(gym));
    }

    public GymDTO updateGym(String id, GymDTO dto) {
        return gymRepository.findById(id).map(existing -> {
            existing.setName(dto.getName());
            existing.setLocation(dto.getLocation());
            return gymMapper.toDTO(gymRepository.save(existing));
        }).orElse(null);
    }

    public void deleteGym(String id) {
        gymRepository.deleteById(id);
    }
}
