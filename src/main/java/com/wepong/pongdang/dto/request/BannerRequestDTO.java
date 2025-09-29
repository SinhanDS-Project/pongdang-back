package com.wepong.pongdang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BannerRequestDTO {
    private String title;
    private String bannerLinkUrl;
    private String description;
}
