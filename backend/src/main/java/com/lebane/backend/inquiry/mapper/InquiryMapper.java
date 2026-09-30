package com.lebane.backend.inquiry.mapper;

import com.lebane.backend.inquiry.dto.InquiryResponse;
import com.lebane.backend.inquiry.entity.Inquiry;
import org.springframework.stereotype.Component;

@Component
public class InquiryMapper {
    public InquiryResponse toResponse(Inquiry inquiry) {
        return new InquiryResponse(inquiry.getId(), inquiry.getName(), inquiry.getEmail(), inquiry.getMessage(), inquiry.getCreatedAt());
    }
}
