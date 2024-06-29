package com.workintech.spring17challenge.controller;

import com.workintech.spring17challenge.dto.ApiResponse;
import com.workintech.spring17challenge.entity.Course;
import com.workintech.spring17challenge.entity.CourseGpa;
import com.workintech.spring17challenge.exceptions.ApiException;
import com.workintech.spring17challenge.validation.CourseValidation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseGpa lowCourseGpa;
    private final CourseGpa mediumCourseGpa;
    private final CourseGpa highCourseGpa;

    private List<Course> courses;

    public CourseController(@Qualifier("lowCourseGpa") CourseGpa lowCourseGpa,
                            @Qualifier("mediumCourseGpa") CourseGpa mediumCourseGpa,
                            @Qualifier("highCourseGpa") CourseGpa highCourseGpa){
        this.lowCourseGpa = lowCourseGpa;
        this.mediumCourseGpa = mediumCourseGpa;
        this.highCourseGpa = highCourseGpa;
    }

    @PostConstruct
    public void init(){
        this.courses = new ArrayList<>();
    }

    @GetMapping
    public List<Course> getAll(){
        return this.courses;
    }

    @GetMapping("/{name}")
    public Course getByName(@PathVariable String name){
        CourseValidation.checkName(name);
        return courses.stream()
                .filter(course -> course.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(()-> new ApiException("course not found with given name: "+ name,
                        HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> create(@RequestBody Course course){
        CourseValidation.checkName(course.getName());
        CourseValidation.checkCredit(course.getCredit());
        courses.add(course);
        Integer totalGpa = getTotalGpa(course);
        return  new ResponseEntity<>(new ApiResponse(course,totalGpa),HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable Integer id,@RequestBody Course course){
        CourseValidation.checkId(id);
        CourseValidation.checkCredit(course.getCredit());
        CourseValidation.checkName(course.getName());
        course.setId(id);
        Course existingCourse = getExistingCourseById(id);
        int indexOfExisting = courses.indexOf(existingCourse);
        courses.set(indexOfExisting,course);
        return new ResponseEntity<>(new ApiResponse(course,getTotalGpa(course)),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        courses.remove(getExistingCourseById(id));
    }


    private Course getExistingCourseById(Integer id) {
        return courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApiException("course not found with id: "+ id,HttpStatus.NOT_FOUND));
    }


    private Integer getTotalGpa(Course course) {
        Integer totalGpa = null;
        if(course.getCredit()<=2){
            totalGpa = course.getGrade().getCoefficient()*course.getCredit()*lowCourseGpa.getGpa();
        }
        else if(course.getCredit() == 3){
            totalGpa = course.getGrade().getCoefficient()*course.getCredit()* mediumCourseGpa.getGpa();
        }
        else{
            totalGpa = course.getGrade().getCoefficient()*course.getCredit()* highCourseGpa.getGpa();
        }
        return totalGpa;
    }
}
