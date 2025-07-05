package com.course.service.impl;

import com.course.dto.CourseDTO;
import com.course.dto.CourseWithGymDTO;
import com.course.dto.GymDTO;
import com.course.entity.Course;
import com.course.mapper.CourseMapper;
import com.course.repository.CourseRepository;
import com.course.service.CourseService;
import com.course.service.GymIntegrationService;
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

    @Override
    public CourseDTO create(CourseDTO courseDTO) {
        logger.info("Création d'un nouveau cours: {}", courseDTO.getTitle());

        // Validation de la salle si spécifiée
        if (courseDTO.getGymId() != null && !courseDTO.getGymId().isEmpty()) {
            if (!gymIntegrationService.validateGymExists(courseDTO.getGymId())) {
                throw new RuntimeException("La salle spécifiée n'existe pas ou est indisponible: " + courseDTO.getGymId());
            }
        }

        Course course = mapper.toEntity(courseDTO);
        course = repository.save(course);
        logger.info("Cours créé avec succès, ID: {}", course.getId());
        
        return mapper.toDto(course);
    }

    @Override
    public CourseDTO update(Long id, CourseDTO courseDTO) {
        logger.info("Mise à jour du cours ID: {}", id);

        Course existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        // Validation de la nouvelle salle si changée
        if (courseDTO.getGymId() != null && !courseDTO.getGymId().isEmpty()) {
            if (!courseDTO.getGymId().equals(existing.getGymId())) {
                if (!gymIntegrationService.validateGymExists(courseDTO.getGymId())) {
                    throw new RuntimeException("La nouvelle salle spécifiée n'existe pas: " + courseDTO.getGymId());
                }
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

        existing = repository.save(existing);
        logger.info("Cours mis à jour avec succès");
        
        return mapper.toDto(existing);
    }

    @Override
    public void delete(Long id) {
        logger.info("Suppression du cours ID: {}", id);
        repository.deleteById(id);
        logger.info("Cours supprimé avec succès");
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

        // Enrichissement avec les informations de la salle
        if (course.getGymId() != null && !course.getGymId().isEmpty()) {
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