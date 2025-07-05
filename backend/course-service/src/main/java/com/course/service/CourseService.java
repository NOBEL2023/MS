package com.course.service;

import com.course.dto.CourseDTO;
import com.course.dto.CourseWithGymDTO;
import com.course.dto.GymDTO;

import java.util.List;

public interface CourseService {
    CourseDTO create(CourseDTO courseDTO);
    CourseDTO update(Long id, CourseDTO courseDTO);
    void delete(Long id);
    CourseDTO getById(Long id);
    List<CourseDTO> getAll();
    List<CourseWithGymDTO> getAllCoursesWithGymInfo();
    List<CourseDTO> getCoursesByGymId(String gymId);
    List<CourseDTO> getCoursesByGymName(String gymName);
    List<GymDTO> getAvailableGyms();
}