package com.example.FoodDelivery.domain.req.telegram;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqSendNotificationDTO {
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String message;
}
