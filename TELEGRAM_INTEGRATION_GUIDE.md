# Hướng Dẫn Tích Hợp Telegram Bot Xác Thực Số Điện Thoại

## 📋 Tổng Quan

Hệ thống xác thực số điện thoại qua Telegram Bot cho phép người dùng nhận mã OTP trực tiếp trên Telegram thay vì SMS, giúp tiết kiệm chi phí và tăng tính bảo mật.

## 🚀 Cài Đặt

### 1. Tạo Telegram Bot

1. Mở Telegram và tìm **@BotFather**
2. Gửi lệnh `/newbot`
3. Đặt tên cho bot (ví dụ: "Food Delivery Verification")
4. Đặt username cho bot (phải kết thúc bằng "bot", ví dụ: "fooddelivery_verification_bot")
5. Lưu lại **Bot Token** nhận được

### 2. Cấu Hình Application Properties

Mở file `src/main/resources/application.properties` và cập nhật:

```properties
# Telegram Bot Configuration
telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN_HERE
telegram.bot.username=your_bot_username
telegram.bot.enabled=true
```

**Ví dụ:**
```properties
telegram.bot.token=6123456789:AAHdqTcvCH1vGWJxfSeofSAs0K5PALDsaw
telegram.bot.username=fooddelivery_verification_bot
telegram.bot.enabled=true
```

### 3. Build và Chạy Ứng Dụng

```bash
# Build project
./gradlew build

# Run application
./gradlew bootRun
```

Hoặc chạy từ IDE (Run FoodDeliveryApplication)

## 📱 Quy Trình Xác Thực

### Bước 1: Liên Kết Telegram

Người dùng cần liên kết số điện thoại với Telegram trước:

1. Mở Telegram
2. Tìm bot theo username (ví dụ: @fooddelivery_verification_bot)
3. Gửi lệnh: `/start`
4. Gửi lệnh: `/verify 0912345678` (thay số điện thoại thực)

Bot sẽ xác nhận đã liên kết thành công.

### Bước 2: Yêu Cầu Mã OTP

**API Endpoint:** `POST /api/v1/telegram/request-otp`

**Request Body:**
```json
{
  "phoneNumber": "0912345678"
}
```

**Response:**
```json
{
  "message": "Mã OTP đã được gửi đến Telegram của bạn. Vui lòng kiểm tra tin nhắn từ bot.",
  "phoneNumber": "0912345678",
  "expiresAt": "2025-11-16T08:15:00Z",
  "telegramUsername": "Linked"
}
```

Người dùng sẽ nhận được tin nhắn trên Telegram:
```
🔐 Mã xác thực của bạn:

📱 Số điện thoại: 0912345678
🔢 Mã OTP: 123456

⏰ Mã có hiệu lực trong 5 phút.
⚠️ Không chia sẻ mã này với bất kỳ ai!
```

### Bước 3: Xác Thực Mã OTP

**API Endpoint:** `POST /api/v1/telegram/verify-otp`

**Request Body:**
```json
{
  "phoneNumber": "0912345678",
  "otpCode": "123456"
}
```

**Response:**
```json
{
  "message": "Xác thực số điện thoại thành công!",
  "phoneNumber": "0912345678",
  "isVerified": true,
  "verifiedAt": "2025-11-16T08:12:34Z",
  "telegramChatId": 123456789
}
```

Người dùng cũng nhận được thông báo thành công trên Telegram.

## 🔌 API Endpoints

### 1. Request OTP
```http
POST /api/v1/telegram/request-otp
Content-Type: application/json

{
  "phoneNumber": "0912345678"
}
```

### 2. Verify OTP
```http
POST /api/v1/telegram/verify-otp
Content-Type: application/json

{
  "phoneNumber": "0912345678",
  "otpCode": "123456"
}
```

### 3. Check Verification Status
```http
POST /api/v1/telegram/check-verification
Content-Type: application/json

{
  "phoneNumber": "0912345678"
}
```

**Response:** `true` hoặc `false`

### 4. Send Custom Notification
```http
POST /api/v1/telegram/send-notification
Content-Type: application/json

{
  "phoneNumber": "0912345678",
  "message": "Đơn hàng của bạn đã được giao thành công!"
}
```

## 🎯 Use Cases

### 1. Xác Thực Khi Đăng Ký

```java
// In UserService
public User register(RegisterDTO dto) throws IdInvalidException {
    // Create user
    User user = new User();
    user.setPhoneNumber(dto.getPhoneNumber());
    
    // Request OTP verification
    telegramVerificationService.requestOtp(dto.getPhoneNumber());
    
    // User must verify OTP before account is activated
    user.setIsActive(false);
    
    return userRepository.save(user);
}

public void activateAccount(String phoneNumber, String otpCode) throws IdInvalidException {
    // Verify OTP
    TelegramVerification verification = telegramVerificationService.verifyOtp(phoneNumber, otpCode);
    
    // Update user
    User user = userRepository.findByPhoneNumber(phoneNumber);
    user.setPhoneVerified(true);
    user.setTelegramChatId(verification.getTelegramChatId());
    user.setIsActive(true);
    
    userRepository.save(user);
}
```

### 2. Gửi Thông Báo Đơn Hàng

```java
// In OrderService
public void notifyOrderStatus(Order order, String status) {
    String message = String.format(
        "🎉 Đơn hàng #%d của bạn: %s\n💰 Tổng tiền: %s VNĐ",
        order.getId(),
        status,
        order.getTotalAmount()
    );
    
    telegramVerificationService.sendNotification(
        order.getCustomer().getPhoneNumber(),
        message
    );
}
```

### 3. Xác Thực 2 Lớp

```java
// In AuthService for sensitive operations
public void changePassword(String phoneNumber, String newPassword) throws IdInvalidException {
    // Request OTP for security
    telegramVerificationService.requestOtp(phoneNumber);
    
    // User must provide OTP to proceed
    // ... verify OTP then change password
}
```

## 🗄️ Database Schema

```sql
CREATE TABLE telegram_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    telegram_chat_id BIGINT,
    telegram_username VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL
);

-- Update users table
ALTER TABLE users ADD COLUMN phone_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN telegram_chat_id BIGINT;
```

## 🔐 Bảo Mật

1. **OTP hết hạn sau 5 phút**
2. **Mỗi OTP chỉ sử dụng 1 lần**
3. **Mã OTP 6 chữ số ngẫu nhiên**
4. **Không lưu OTP ở client side**
5. **Rate limiting nên được áp dụng cho API request-otp**

## 🧪 Testing

### Test với Postman

1. Import API collection từ Swagger UI
2. Liên kết Telegram với bot
3. Test các endpoint theo thứ tự:
   - Request OTP
   - Check Telegram for OTP code
   - Verify OTP
   - Check verification status

### Test Bot Commands

Các lệnh có sẵn trong bot:
- `/start` - Bắt đầu sử dụng bot
- `/verify <phone_number>` - Liên kết số điện thoại

## ⚠️ Lưu Ý

1. **Bot Token phải được giữ bí mật** - Không commit vào Git
2. **Sử dụng biến môi trường trong production**
3. **Telegram Bot phải được khởi động cùng ứng dụng**
4. **Người dùng phải liên kết Telegram trước khi yêu cầu OTP**

## 📚 Dependencies

```gradle
implementation("org.telegram:telegrambots:6.8.0")
implementation("org.telegram:telegrambotsextensions:6.8.0")
```

## 🐛 Troubleshooting

### Bot không nhận tin nhắn
- Kiểm tra bot token có đúng không
- Kiểm tra `telegram.bot.enabled=true`
- Xem log khi start application

### Không nhận được OTP
- Kiểm tra đã liên kết Telegram chưa (`/verify`)
- Kiểm tra số điện thoại đúng format
- Xem log của TelegramBotService

### OTP expired
- Mã OTP chỉ có hiệu lực 5 phút
- Yêu cầu mã mới nếu hết hạn

## 📞 Support

Nếu có vấn đề, kiểm tra:
1. Application logs
2. Telegram bot logs
3. Database records trong bảng `telegram_verifications`

---

**Version:** 1.0.0  
**Last Updated:** November 16, 2025
