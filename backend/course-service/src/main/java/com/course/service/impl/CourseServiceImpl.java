package com.course.service.impl;

import com.course.dto.CourseDTO;
import com.course.dto.CourseWithGymDTO;
import com.course.dto.GymDTO;
import com.course.entity.Course;
import com.course.mapper.CourseMapper;
import com.course.repository.CourseRepository;
import com.course.service.CourseService;
import com.course.service.GymIntegrationService;
import com.course.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Autowired
    private CourseRepository repository;

    @Autowired
    private CourseMapper mapper;

    @Autowired
    private GymIntegrationService gymIntegrationService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Override
    public CourseDTO create(CourseDTO courseDTO) {
        logger.info("Création d'un nouveau cours: {}", courseDTO.getTitle());

        String gymName = null;
        
        // Validation et récupération du nom de la salle si spécifiée
        if (courseDTO.getGymId() != null && !courseDTO.getGymId().isEmpty()) {
            if (!gymIntegrationService.validateGymExists(courseDTO.getGymId())) {
                throw new RuntimeException("La salle spécifiée n'existe pas ou est indisponible: " + courseDTO.getGymId());
            }
            
            // Récupérer le nom de la salle
            try {
                GymDTO gym = gymIntegrationService.getGymInfoForCourse(courseDTO.getGymId());
                gymName = gym.getName();
                courseDTO.setGymName(gymName); // Définir le nom dans le DTO
            } catch (Exception e) {
                logger.warn("Impossible de récupérer le nom de la salle {}: {}", courseDTO.getGymId(), e.getMessage());
                gymName = "Salle inconnue";
                courseDTO.setGymName(gymName);
            }
        }

        Course course = mapper.toEntity(courseDTO);
        course = repository.save(course);
        CourseDTO result = mapper.toDto(course);
        
        // Publier l'événement Kafka de création
        kafkaProducerService.publishCourseCreated(
            result.getId(),
            result.getTitle(),
            result.getInstructor(),
            result.getGymId(),
            result.getGymName(),
            result.getMaxParticipants(),
            result.getPrice(),
            result.getLevel()
        );
        
        // Si le cours est assigné à une salle, publier l'événement d'assignation
        if (result.getGymId() != null && result.getGymName() != null) {
            kafkaProducerService.publishCourseAssignedToGym(
                result.getId(),
                result.getTitle(),
                result.getGymId(),
                result.getGymName()
            );
        }
        
        logger.info("Cours créé avec succès, ID: {}, Salle: {}", course.getId(), result.getGymName());
        return result;
    }

    @Override
    public CourseDTO update(Long id, CourseDTO courseDTO) {
        logger.info("Mise à jour du cours ID: {}", id);

        Course existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        String oldGymId = existing.getGymId();
        String oldGymName = existing.getGymName();
        String newGymId = courseDTO.getGymId();
        String newGymName = null;

        // Validation et récupération du nom de la nouvelle salle si changée
        if (newGymId != null && !newGymId.isEmpty()) {
            if (!newGymId.equals(oldGymId)) {
                if (!gymIntegrationService.validateGymExists(newGymId)) {
                    throw new RuntimeException("La nouvelle salle spécifiée n'existe pas: " + newGymId);
                }
                
                // Récupérer le nom de la nouvelle salle
                try {
                    GymDTO newGym = gymIntegrationService.getGymInfoForCourse(newGymId);
                    newGymName = newGym.getName();
                } catch (Exception e) {
                    logger.warn("Impossible de récupérer la nouvelle salle {}: {}", newGymId, e.getMessage());
                    newGymName = "Salle inconnue";
                }
            } else {
                // Même salle, garder le nom existant ou le mettre à jour si fourni
                newGymName = courseDTO.getGymName() != null ? courseDTO.getGymName() : oldGymName;
            }
        }

        // Mise à jour des champs
        existing.setTitle(courseDTO.getTitle());
        existing.setDescription(courseDTO.getDescription());
        existing.setInstructor(courseDTO.getInstructor());
        existing.setDuration(courseDTO.getDuration());
        existing.setMaxParticipants(courseDTO.getMaxParticipants());
        existing.setPrice(courseDTO.getPrice());
        existing.setSchedule(courseDTO.getSchedule());
        existing.setLevel(courseDTO.getLevel());
        existing.setGymId(courseDTO.getGymId());
        existing.setGymName(newGymName);

        existing = repository.save(existing);
        CourseDTO result = mapper.toDto(existing);
        
        // Publier l'événement de mise à jour
        kafkaProducerService.publishCourseUpdated(
            result.getId(),
            result.getTitle(),
            result.getInstructor(),
            result.getGymId(),
            result.getGymName(),
            result.getMaxParticipants(),
            result.getPrice(),
            result.getLevel()
        );
        
        // Gérer les changements d'assignation de salle
        if (!java.util.Objects.equals(oldGymId, newGymId)) {
            // Désassignation de l'ancienne salle
            if (oldGymId != null) {
                kafkaProducerService.publishCourseUnassignedFromGym(
                    result.getId(),
                    result.getTitle(),
                    oldGymId,
                    oldGymName
                );
            }
            
            // Assignation à la nouvelle salle
            if (newGymId != null) {
                kafkaProducerService.publishCourseAssignedToGym(
                    result.getId(),
                    result.getTitle(),
                    newGymId,
                    newGymName
                );
            }
        }
        
        logger.info("Cours mis à jour avec succès: {} - Salle: {}", result.getTitle(), result.getGymName());
        return result;
    }

    @Override
    public void delete(Long id) {
        logger.info("Suppression du cours ID: {}", id);
        
        // Récupérer les infos avant suppression pour l'événement
        Course course = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Supprimer le cours
        repository.deleteById(id);
        
        // Publier l'événement de suppression
        kafkaProducerService.publishCourseDeleted(course.getId(), course.getTitle());
        
        // Si le cours était assigné à une salle, publier l'événement de désassignation
        if (course.getGymId() != null) {
            kafkaProducerService.publishCourseUnassignedFromGym(
                course.getId(),
                course.getTitle(),
                course.getGymId(),
                course.getGymName()
            );
        }
        
        logger.info("Cours supprimé avec succès: {} - Salle: {}", course.getTitle(), course.getGymName());
    }

    @Override
    public CourseDTO getById(Long id) {
        logger.info("Récupération du cours ID: {}", id);
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    public List<CourseDTO> getAll() {
        logger.info("Récupération de tous les cours");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseWithGymDTO> getAllCoursesWithGymInfo() {
        logger.info("Récupération de tous les cours avec informations des salles");
        
        List<Course> courses = repository.findAll();
        return courses.stream()
                .map(this::enrichCourseWithGymInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesByGymId(String gymId) {
        logger.info("Récupération des cours pour la salle: {}", gymId);
        return repository.findByGymId(gymId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GymDTO> getAvailableGyms() {
        logger.info("Récupération des salles disponibles");
        return gymIntegrationService.getAllGymsWithCircuitBreaker();
    }

    /**
     * Enrichit un cours avec les informations de la salle
     */
    private CourseWithGymDTO enrichCourseWithGymInfo(Course course) {
        CourseWithGymDTO enrichedCourse = new CourseWithGymDTO();
        
        // Copie des informations du cours
        enrichedCourse.setId(course.getId());
        enrichedCourse.setTitle(course.getTitle());
        enrichedCourse.setDescription(course.getDescription());
        enrichedCourse.setInstructor(course.getInstructor());
        enrichedCourse.setDuration(course.getDuration());
        enrichedCourse.setMaxParticipants(course.getMaxParticipants());
        enrichedCourse.setPrice(course.getPrice());
        enrichedCourse.setSchedule(course.getSchedule());
        enrichedCourse.setLevel(course.getLevel());
        enrichedCourse.setGymId(course.getGymId());

        // Utiliser le nom stocké en base ou enrichir si nécessaire
        if (course.getGymName() != null && !course.getGymName().isEmpty()) {
            enrichedCourse.setGymName(course.getGymName());
            
            // Enrichir avec les informations supplémentaires de la salle si disponible
            if (course.getGymId() != null) {
                try {
                    GymDTO gym = gymIntegrationService.getGymInfoForCourse(course.getGymId());
                    enrichedCourse.setGymLocation(gym.getLocation());
                    enrichedCourse.setGymCapacity(gym.getCapacity());
                } catch (Exception e) {
                    logger.warn("Impossible d'enrichir le cours {} avec les infos détaillées de la salle {}: {}", 
                        course.getId(), course.getGymId(), e.getMessage());
                    enrichedCourse.setGymLocation("Information indisponible");
                    enrichedCourse.setGymCapacity(0);
                }
            }
        } else if (course.getGymId() != null) {
            // Fallback: récupérer les infos via OpenFeign si le nom n'est pas stocké
            try {
                GymDTO gym = gymIntegrationService.getGymInfoForCourse(course.getGymId());
                enrichedCourse.setGymName(gym.getName());
                enrichedCourse.setGymLocation(gym.getLocation());
                enrichedCourse.setGymCapacity(gym.getCapacity());
            } catch (Exception e) {
                logger.warn("Impossible d'enrichir le cours {} avec les infos de la salle {}: {}", 
                    course.getId(), course.getGymId(), e.getMessage());
                enrichedCourse.setGymName("Information indisponible");
                enrichedCourse.setGymLocation("Information indisponible");
                enrichedCourse.setGymCapacity(0);
            }
        }

        return enrichedCourse;
    }
}