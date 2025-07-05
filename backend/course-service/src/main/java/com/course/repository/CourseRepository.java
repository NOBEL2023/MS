package com.course.repository;

import com.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByGymId(String gymId);
    List<Course> findByInstructor(String instructor);
    List<Course> findByLevel(String level);
}