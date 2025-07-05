package com.course.mapper;

import com.course.dto.CourseDTO;
import com.course.entity.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseDTO toDto(Course course);
    Course toEntity(CourseDTO courseDTO);
}
