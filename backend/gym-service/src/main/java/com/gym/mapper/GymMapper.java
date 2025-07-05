package com.gym.mapper;

import com.gym.dto.GymDTO;
import com.gym.entity.Gym;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymMapper {
    GymDTO toDTO(Gym gym);
    Gym toEntity(GymDTO gymDTO);
    List<GymDTO> toDTOs(List<Gym> gyms);
}
