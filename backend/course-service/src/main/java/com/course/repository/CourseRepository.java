package com.course.repository;

import com.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByGymId(String gymId);
    List<Course> findByInstructor(String instructor);
    List<Course> findByLevel(String level);
    List<Course> findByGymName(String gymName);
    
    // Méthode pour mettre à jour le nom de la salle pour tous les cours d'une salle
    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.gymName = :gymName WHERE c.gymId = :gymId")
    void updateGymNameByGymId(@Param("gymId") String gymId, @Param("gymName") String gymName);
    
    // Méthode pour supprimer l'association avec une salle supprimée
    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.gymId = null, c.gymName = null WHERE c.gymId = :gymId")
    void removeGymAssociationByGymId(@Param("gymId") String gymId);
}