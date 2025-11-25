package com.example.FoodDelivery.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.FoodDelivery.util.constant.StatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "driver_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String vehicleDetails;
    private String status;

    @Column(precision = 10, scale = 8)
    private BigDecimal currentLatitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal currentLongitude;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(precision = 10, scale = 2)
    private BigDecimal codLimit;

    // National ID (CCCD/CMND/Passport)
    @JsonProperty("national_id_front")
    private String nationalIdFront;

    @JsonProperty("national_id_back")
    private String nationalIdBack;

    @JsonProperty("national_id_number")
    private String nationalIdNumber;

    @JsonProperty("national_id_status")
    private StatusEnum nationalIdStatus;

    @JsonProperty("national_id_rejection_reason")
    private String nationalIdRejectionReason;

    // Driver License (Bằng lái xe)
    @JsonProperty("driver_license_front")
    private String driverLicenseFront;

    @JsonProperty("driver_license_back")
    private String driverLicenseBack;

    @JsonProperty("driver_license_number")
    private String driverLicenseNumber;

    @JsonProperty("driver_license_expiry")
    private LocalDate driverLicenseExpiry;

    @JsonProperty("driver_license_status")
    private StatusEnum driverLicenseStatus;

    @JsonProperty("driver_license_rejection_reason")
    private String driverLicenseRejectionReason;

    // Vehicle Registration (Cà vẹt xe)
    @JsonProperty("vehicle_registration_front")
    private String vehicleRegistrationFront;

    @JsonProperty("vehicle_registration_back")
    private String vehicleRegistrationBack;

    @JsonProperty("vehicle_license_plate")
    private String vehicleLicensePlate;

    @JsonProperty("vehicle_registration_status")
    private StatusEnum vehicleRegistrationStatus;

    @JsonProperty("vehicle_registration_rejection_reason")
    private String vehicleRegistrationRejectionReason;

    // Vehicle Insurance (Bảo hiểm xe)
    @JsonProperty("vehicle_insurance_image")
    private String vehicleInsuranceImage;

    @JsonProperty("vehicle_insurance_expiry")
    private LocalDate vehicleInsuranceExpiry;

    @JsonProperty("vehicle_insurance_status")
    private StatusEnum vehicleInsuranceStatus;

    @JsonProperty("vehicle_insurance_rejection_reason")
    private String vehicleInsuranceRejectionReason;

    // Profile Photo (Ảnh chân dung)
    @JsonProperty("profile_photo")
    private String profilePhoto;

    @JsonProperty("profile_photo_status")
    private StatusEnum profilePhotoStatus;

    @JsonProperty("profile_photo_rejection_reason")
    private String profilePhotoRejectionReason;

    // Criminal Record (Lý lịch tư pháp) - Optional
    @JsonProperty("criminal_record_image")
    private String criminalRecordImage;

    @JsonProperty("criminal_record_issue_date")
    private LocalDate criminalRecordIssueDate;

    @JsonProperty("criminal_record_status")
    private StatusEnum criminalRecordStatus;

    @JsonProperty("criminal_record_rejection_reason")
    private String criminalRecordRejectionReason;

}