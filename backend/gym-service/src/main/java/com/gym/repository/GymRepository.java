package com.gym.repository;

import com.gym.entity.Gym;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GymRepository extends MongoRepository<Gym, String> {
}
