package com.lebane.backend.department.controller;

import com.lebane.backend.common.dto.PageResponse;
import com.lebane.backend.department.dto.*;
import com.lebane.backend.department.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/departamentos")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DepartmentResponse> create( @Valid @RequestPart("departamento")DepartmentCreateRequest request, @RequestPart(value = "imagenes", required = false) List<MultipartFile> images) {

        DepartmentResponse response =departmentService.create(request,images);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public PageResponse<DepartmentListItemResponse> list (@RequestParam(name = "pagina",defaultValue = "0") int page,
                                                          @RequestParam(name = "cantidad",defaultValue = "20")int size,
                                                          @RequestParam(name = "disponible",required = false)Boolean available,
                                                          @RequestParam(name = "precioMin",required = false)BigDecimal minPrice,
                                                          @RequestParam(name = "precioMax",required = false)BigDecimal maxPrice,
                                                          @RequestParam(name = "metrosCuadradosMin",required = false)BigDecimal minSquareMeters,
                                                          @RequestParam(name = "metrosCuadradosMax",required = false)BigDecimal maxSquareMeters)
    {
        DepartmentFilter filter = new DepartmentFilter(available,minPrice,maxPrice,minSquareMeters,maxSquareMeters);

        return departmentService.list(filter,page,size);
    }

    @GetMapping("/{id}")
    public DepartmentDetailResponse getById(@PathVariable Long id){
        return departmentService.getById(id);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update( @PathVariable Long id,@Valid @RequestBody DepartmentUpdateRequest request){
        return departmentService.update(id, request);

    }


}
