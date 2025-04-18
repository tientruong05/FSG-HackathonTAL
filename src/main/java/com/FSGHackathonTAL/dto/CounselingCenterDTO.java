package com.FSGHackathonTAL.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CounselingCenterDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String district;
    private String phone;
    private String email;
    private String website;
    private String description;
    private String imageUrl;
    private double rating;
    private boolean verified;
    private String openingHours;
} 