package com.example.FoodDelivery.domain.res.driverProfile;

import java.math.BigDecimal;

import com.example.FoodDelivery.util.constant.StatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResDriverProfileDTO {
    private Long id;
    private UserDriver user;
    private String vehicleDetails;
    private String status;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private BigDecimal averageRating;
    private BigDecimal codLimit;

    // National ID
    @JsonProperty("national_id_front")
    private String nationalIdFront;

    @JsonProperty("national_id_back")
    private String nationalIdBack;

    @JsonProperty("national_id_status")
    private StatusEnum nationalIdStatus;

    @JsonProperty("national_id_rejection_reason")
    private String nationalIdRejectionReason;

    // Driver License
    @JsonProperty("driver_license_front")
    private String driverLicenseFront;

    @JsonProperty("driver_license_back")
    private String driverLicenseBack;

    @JsonProperty("driver_license_status")
    private StatusEnum driverLicenseStatus;

    @JsonProperty("driver_license_rejection_reason")
    private String driverLicenseRejectionReason;

    // Vehicle Registration
    @JsonProperty("vehicle_registration_front")
    private String vehicleRegistrationFront;

    @JsonProperty("vehicle_registration_back")
    private String vehicleRegistrationBack;

    @JsonProperty("vehicle_registration_status")
    private StatusEnum vehicleRegistrationStatus;

    @JsonProperty("vehicle_registration_rejection_reason")
    private String vehicleRegistrationRejectionReason;

    // Vehicle Insurance
    @JsonProperty("vehicle_insurance_image")
    private String vehicleInsuranceImage;

    @JsonProperty("vehicle_insurance_status")
    private StatusEnum vehicleInsuranceStatus;

    @JsonProperty("vehicle_insurance_rejection_reason")
    private String vehicleInsuranceRejectionReason;

    // Profile Photo
    @JsonProperty("profile_photo")
    private String profilePhoto;

    @JsonProperty("profile_photo_status")
    private StatusEnum profilePhotoStatus;

    @JsonProperty("profile_photo_rejection_reason")
    private String profilePhotoRejectionReason;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDriver {
        private Long id;
        private String name;
        private String email;
    }
}
