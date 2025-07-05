package com.course.service.impl;

import com.course.dto.CourseDTO;
import com.course.entity.Course;
import com.course.mapper.CourseMapper;
import com.course.repository.CourseRepository;
import com.course.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository repository;
    private final CourseMapper mapper;

    public CourseServiceImpl(CourseRepository repository, CourseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CourseDTO create(CourseDTO courseDTO) {
        Course course = mapper.toEntity(courseDTO);
        course = repository.save(course);
        return mapper.toDto(course);
    }

    @Override
    public CourseDTO update(Long id, CourseDTO courseDTO) {
        Course existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
        existing.setTitle(courseDTO.getTitle());
        existing.setDescription(courseDTO.getDescription());
        existing = repository.save(existing);
        return mapper.toDto(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public CourseDTO getById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    public List<CourseDTO> getAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
