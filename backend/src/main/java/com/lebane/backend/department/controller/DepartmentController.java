package com.lebane.backend.department.controller;

import com.lebane.backend.common.dto.PageResponse;
import com.lebane.backend.department.dto.*;
import com.lebane.backend.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Departments", description = "Operations for managing real estate departments")
@RestController
@RequestMapping("/api/departamentos")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "Create a department",description = "Creates a department with optional images uploaded to S3-compatible storage.")
    @ApiResponse(responseCode = "202",description = "Department created successfully")
    @ApiResponse(responseCode = "400",description = "Invalid request")
    @ApiResponse(responseCode = "503",description = "Storage service unavailable")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DepartmentResponse> create(
            @Parameter(
            description = "Department data", required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                               schema = @Schema(implementation = DepartmentCreateRequest.class)))
                                        @Valid @RequestPart("departamento")DepartmentCreateRequest request,
            @Parameter(
                    description = "Department images. Maximum 5 files.",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                       array = @ArraySchema(schema = @Schema(type = "string",format = "binary"))))
                                               @RequestPart(value = "imagenes", required = false) List<MultipartFile> images)
    {

        DepartmentResponse response = departmentService.create(request,images);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "List departments",description = "Returns a paginated and filtered list of departments.")
    @ApiResponse(responseCode = "200",description = "Departments retrieved successfully")
    @ApiResponse(responseCode = "400",description = "Invalid filters or pagination")
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

    @Operation(summary = "Get department by id",description = "Returns full department detail including images and inquiries.")
    @ApiResponse(responseCode = "200",description = "Department found")
    @ApiResponse(responseCode = "404",description = "Department not found")
    @GetMapping("/{id}")
    public DepartmentDetailResponse getById(@PathVariable Long id){
        return departmentService.getById(id);
    }

    @Operation(summary = "Update a department",description = "Updates department data using optimistic locking.")
    @ApiResponse(responseCode = "200",description = "Department updated successfully")
    @ApiResponse(responseCode = "404",description = "Department not found")
    @ApiResponse(responseCode = "409",description = "Department version conflict")
    @PutMapping("/{id}")
    public DepartmentResponse update( @PathVariable Long id,@Valid @RequestBody DepartmentUpdateRequest request){
        return departmentService.update(id, request);

    }


}
