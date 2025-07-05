package com.course.service;

import com.course.dto.CourseDTO;
import java.util.List;

public interface CourseService {
    CourseDTO create(CourseDTO courseDTO);
    CourseDTO update(Long id, CourseDTO courseDTO);
    void delete(Long id);
    CourseDTO getById(Long id);
    List<CourseDTO> getAll();
}
