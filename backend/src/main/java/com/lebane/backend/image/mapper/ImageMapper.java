package com.lebane.backend.image.mapper;

import com.lebane.backend.image.dto.ImageResponse;
import com.lebane.backend.image.entity.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    public ImageResponse toResponse(Image image, String url) {

        return new ImageResponse(image.getId(), url, image.getDisplayOrder(), image.isPrimaryImage());
    }
}
