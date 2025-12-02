# Payment System Documentation

## Tổng quan

Hệ thống thanh toán hỗ trợ 3 phương thức:
1. **Wallet Payment** - Thanh toán qua ví trong app
2. **VNPAY** - Thanh toán qua cổng VNPAY (sandbox)
3. **COD (Cash on Delivery)** - Thanh toán khi nhận hàng

## 1. Wallet Payment

### Cách hoạt động:
- Kiểm tra số dư ví của customer
- Trừ tiền từ ví customer
- Cộng tiền vào ví admin
- Tạo transaction record cho cả 2 ví

### API Endpoint:
```http
POST /api/v1/payment/wallet
Content-Type: application/json

{
  "orderId": 123
}
```

### Response Success:
```json
{
  "success": true,
  "message": "Payment successful",
  "transactionAmount": 100000,
  "remainingBalance": 500000
}
```

### Response Failed (Insufficient Balance):
```json
{
  "success": false,
  "message": "Insufficient wallet balance",
  "currentBalance": 50000,
  "requiredAmount": 100000
}
```

## 2. VNPAY Payment

### Cách hoạt động:
1. **Tạo Payment URL**: Backend tạo URL thanh toán VNPAY
2. **Redirect**: Frontend redirect user đến VNPAY
3. **User thanh toán**: User nhập thông tin thẻ/tài khoản
4. **Callback**: VNPAY gọi callback URL với kết quả
5. **Xử lý kết quả**: 
   - Response Code `00` = Success → Cộng tiền vào ví admin
   - Response Code khác = Failed

### API Endpoints:

#### Tạo Payment URL:
```http
POST /api/v1/payment/vnpay/create
Content-Type: application/json

{
  "orderId": 123
}
```

Response:
```json
{
  "success": true,
  "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&...",
  "orderId": 123
}
```

#### Callback Endpoint (VNPAY calls this):
```http
GET /api/v1/payment/vnpay/callback?vnp_ResponseCode=00&vnp_TxnRef=123&...
```

Response:
```json
{
  "success": true,
  "responseCode": "00",
  "orderId": 123,
  "amount": 100000,
  "transactionNo": "15311699",
  "message": "Payment successful"
}
```

### VNPAY Configuration:

Thêm vào file `.env` hoặc environment variables:
```properties
VNPAY_TMN_CODE=CTTVNP01
VNPAY_HASH_SECRET=VNPAY_SECRET_KEY
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:8080/api/v1/payment/vnpay/callback
```

### VNPAY Response Codes:
- `00`: Giao dịch thành công
- `07`: Trừ tiền thành công nhưng giao dịch bị nghi ngờ
- `09`: Thẻ chưa đăng ký Internet Banking
- `10`: Xác thực thông tin sai quá 3 lần
- `11`: Hết hạn chờ thanh toán (timeout)
- `12`: Thẻ/Tài khoản bị khóa
- `13`: Nhập sai OTP
- `24`: Khách hàng hủy giao dịch
- `51`: Tài khoản không đủ số dư
- `65`: Vượt quá hạn mức giao dịch
- `75`: Ngân hàng đang bảo trì
- `79`: Nhập sai mật khẩu quá số lần quy định
- `99`: Các lỗi khác

## 3. COD (Cash on Delivery)

### Cách hoạt động:
- Kiểm tra COD limit (từ driver profile hoặc system config)
- Order amount phải <= COD limit
- Khi order delivered, driver thu tiền và cộng vào ví admin

### API Endpoints:

#### Validate COD:
```http
POST /api/v1/payment/cod/validate
Content-Type: application/json

{
  "orderId": 123,
  "driverId": 456  // optional
}
```

Response Valid:
```json
{
  "valid": true,
  "message": "COD payment is valid"
}
```

Response Invalid:
```json
{
  "valid": false,
  "message": "Order amount exceeds COD limit",
  "orderAmount": 10000000,
  "codLimit": 5000000
}
```

#### Check COD Availability:
```http
GET /api/v1/payment/cod/available?amount=1000000
```

Response:
```json
{
  "available": true,
  "amount": 1000000
}
```

#### Process COD Payment (after delivery):
```http
POST /api/v1/payment/cod/process
Content-Type: application/json

{
  "orderId": 123
}
```

Response:
```json
{
  "success": true,
  "message": "COD payment recorded successfully",
  "amount": 100000
}
```

### COD Configuration:

Thêm vào `system_configuration` table:
```sql
INSERT INTO system_configuration (config_key, config_value, description) 
VALUES ('default_cod_limit', '5000000', 'Default COD limit in VND (5,000,000)');
```

Hoặc set cho từng driver:
```sql
UPDATE driver_profiles SET cod_limit = 10000000 WHERE user_id = 123;
```

## 4. Commission Calculation

### Cách hoạt động:
- Lấy commission percentage từ `system_configuration`
- Driver commission: % của delivery_fee
- Restaurant commission: % của subtotal

### API Endpoint:
```http
GET /api/v1/payment/commission/123
```

Response:
```json
{
  "driverCommission": 15000,
  "restaurantCommission": 20000,
  "driverEarnings": 85000,
  "restaurantEarnings": 80000
}
```

### Commission Configuration:

Thêm vào `system_configuration` table:
```sql
INSERT INTO system_configuration (config_key, config_value, description) 
VALUES 
('driver_commission_percentage', '15', 'Driver commission percentage (15%)'),
('restaurant_commission_percentage', '20', 'Restaurant commission percentage (20%)');
```

## Flow tích hợp vào Order

### 1. Khi tạo order (Checkout):

```java
// Customer chọn payment method
order.setPaymentMethod("WALLET"); // or "VNPAY" or "COD"
order.setPaymentStatus("PENDING");

// Nếu COD, validate COD limit
if ("COD".equals(order.getPaymentMethod())) {
    Map<String, Object> codValidation = paymentService.validateCODPayment(order, null);
    if (!(Boolean) codValidation.get("valid")) {
        throw new IdInvalidException("COD not available for this order amount");
    }
}

// Nếu WALLET, process payment ngay
if ("WALLET".equals(order.getPaymentMethod())) {
    Map<String, Object> result = paymentService.processWalletPayment(order);
    if ((Boolean) result.get("success")) {
        order.setPaymentStatus("PAID");
    } else {
        throw new IdInvalidException("Wallet payment failed");
    }
}

// Nếu VNPAY, tạo payment URL và trả về cho frontend
if ("VNPAY".equals(order.getPaymentMethod())) {
    String paymentUrl = vnPayService.createPaymentUrl(order, ipAddress);
    // Frontend redirect user to paymentUrl
    // Sau khi thanh toán, VNPAY sẽ callback và update payment status
}
```

### 2. Khi order DELIVERED (COD):

```java
if ("COD".equals(order.getPaymentMethod()) && "DELIVERED".equals(order.getOrderStatus())) {
    paymentService.processCODPaymentOnDelivery(order);
    order.setPaymentStatus("PAID");
}
```

### 3. Khi order DELIVERED (Calculate Commission):

```java
if ("DELIVERED".equals(order.getOrderStatus())) {
    Map<String, BigDecimal> commissions = paymentService.calculateCommissions(order);
    // Save to OrderEarningsSummary
    // Update driver and restaurant wallets
}
```

## Testing

### Test Wallet Payment:
1. Tạo user với wallet có balance đủ
2. Tạo order
3. Call `/api/v1/payment/wallet` với orderId
4. Check balance đã bị trừ và order payment_status = "PAID"

### Test VNPAY (Sandbox):
1. Call `/api/v1/payment/vnpay/create` để lấy payment URL
2. Mở URL trong browser
3. Nhập thông tin test card:
   - Card Number: 9704198526191432198
   - Card Holder: NGUYEN VAN A
   - Issue Date: 07/15
   - OTP: 123456
4. VNPAY sẽ redirect về callback URL
5. Check order payment_status đã update

### Test COD:
1. Tạo order với amount <= COD limit
2. Call `/api/v1/payment/cod/validate`
3. Update order status to "DELIVERED"
4. Call `/api/v1/payment/cod/process`
5. Check admin wallet đã nhận tiền

## Lưu ý

1. **Security**: VNPAY hash_secret phải được bảo mật, không commit vào git
2. **Transaction**: Tất cả payment operations đều dùng `@Transactional` để đảm bảo consistency
3. **Wallet Balance**: Luôn check balance trước khi trừ tiền
4. **COD Limit**: Driver có thể có COD limit riêng, override default limit
5. **Commission**: Commission được calculate khi order delivered và save vào `order_earnings_summary`
