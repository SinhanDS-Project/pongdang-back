package com.wepong.pongdang.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wepong.pongdang.model.aws.S3ImagePathDeserializer;
import com.wepong.pongdang.model.aws.S3ImageUrlSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private int price;
    private String description;
    @JsonSerialize(using = S3ImageUrlSerializer.class)
    @JsonDeserialize(using = S3ImagePathDeserializer.class)
    private String img;
}
