package com.gym.service;

import com.gym.dto.GymDTO;
import com.gym.mapper.GymMapper;
import com.gym.entity.Gym;
import com.gym.repository.GymRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GymService {

    private static final Logger logger = LoggerFactory.getLogger(GymService.class);

    @Autowired
    private GymRepository gymRepository;

    @Autowired
    private GymMapper gymMapper;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public List<GymDTO> getAllGyms() {
        logger.info("Récupération de toutes les salles");
        return gymMapper.toDTOs(gymRepository.findAll());
    }

    public Optional<GymDTO> getGymById(String id) {
        logger.info("Récupération de la salle: {}", id);
        return gymRepository.findById(id).map(gymMapper::toDTO);
    }

    public GymDTO createGym(GymDTO dto) {
        logger.info("Création d'une nouvelle salle: {}", dto.getName());
        
        Gym gym = gymMapper.toEntity(dto);
        Gym savedGym = gymRepository.save(gym);
        GymDTO result = gymMapper.toDTO(savedGym);
        
        // Publier l'événement Kafka
        kafkaProducerService.publishGymCreated(
            result.getId(), 
            result.getName(), 
            result.getEmail(), 
            result.getCapacity()
        );
        
        logger.info("Salle créée avec succès: {}", result.getId());
        return result;
    }

    public GymDTO updateGym(String id, GymDTO dto) {
        logger.info("Mise à jour de la salle: {}", id);
        
        return gymRepository.findById(id).map(existing -> {
            // Sauvegarder l'ancienne capacité pour l'événement
            Integer oldCapacity = existing.getCapacity();
            
            // Mise à jour des champs
            existing.setName(dto.getName());
            existing.setLocation(dto.getLocation());
            existing.setPhone(dto.getPhone());
            existing.setEmail(dto.getEmail());
            existing.setCapacity(dto.getCapacity());
            
            Gym updated = gymRepository.save(existing);
            GymDTO result = gymMapper.toDTO(updated);
            
            // Publier l'événement de mise à jour
            kafkaProducerService.publishGymUpdated(
                result.getId(), 
                result.getName(), 
                result.getEmail(), 
                result.getCapacity()
            );
            
            // Si la capacité a changé, publier un événement spécifique
            if (!oldCapacity.equals(result.getCapacity())) {
                kafkaProducerService.publishGymCapacityChanged(
                    result.getId(), 
                    result.getName(), 
                    oldCapacity, 
                    result.getCapacity()
                );
            }
            
            logger.info("Salle mise à jour avec succès: {}", result.getId());
            return result;
        }).orElse(null);
    }

    public void deleteGym(String id) {
        logger.info("Suppression de la salle: {}", id);
        
        // Récupérer les infos avant suppression pour l'événement
        Optional<Gym> gymOpt = gymRepository.findById(id);
        if (gymOpt.isPresent()) {
            Gym gym = gymOpt.get();
            
            // Supprimer la salle
            gymRepository.deleteById(id);
            
            // Publier l'événement de suppression
            kafkaProducerService.publishGymDeleted(gym.getId(), gym.getName());
            
            logger.info("Salle supprimée avec succès: {}", id);
        } else {
            logger.warn("Tentative de suppression d'une salle inexistante: {}", id);
        }
    }
}