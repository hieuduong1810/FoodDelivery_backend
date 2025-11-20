package com.example.FoodDelivery.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.FoodDelivery.domain.Permission;
import com.example.FoodDelivery.domain.Role;
import com.example.FoodDelivery.domain.User;
import com.example.FoodDelivery.repository.PermissionRepository;
import com.example.FoodDelivery.repository.RoleRepository;
import com.example.FoodDelivery.repository.UserRepository;
import com.example.FoodDelivery.service.WalletService;
import com.example.FoodDelivery.util.constant.GenderEnum;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(2) // Run after PermissionInitializer (Order 1)
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    public AdminInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder,
            WalletService walletService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========== Starting Admin User Initialization ==========");

        // Check if admin user already exists
        User existingAdmin = userRepository.findByEmail("admin@gmail.com");
        if (existingAdmin != null) {
            log.info("Admin user already exists: admin@gmail.com");
            log.info("=======================================================");
            return;
        }

        // 1. Create or get ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            log.info("Creating ADMIN role...");
            adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Super admin with full access");
            adminRole.setActive(true);

            // Get all permissions from database
            List<Permission> allPermissions = permissionRepository.findAll();
            adminRole.setPermissions(allPermissions);

            adminRole = roleRepository.save(adminRole);
            log.info("✅ ADMIN role created with {} permissions", allPermissions.size());
        } else {
            // Update existing ADMIN role with all permissions
            List<Permission> allPermissions = permissionRepository.findAll();
            adminRole.setPermissions(allPermissions);
            adminRole = roleRepository.save(adminRole);
            log.info("✅ ADMIN role updated with {} permissions", allPermissions.size());
        }

        // 2. Create admin user
        log.info("Creating admin user...");
        User adminUser = new User();
        adminUser.setName("admin");
        adminUser.setEmail("admin@gmail.com");
        adminUser.setPassword(passwordEncoder.encode("123456"));
        adminUser.setGender(GenderEnum.MALE);
        adminUser.setAddress("tp hcm");
        adminUser.setAge(20);
        adminUser.setIsActive(true); // Admin is active by default
        adminUser.setRole(adminRole);

        adminUser = userRepository.save(adminUser);
        log.info("✅ Admin user created successfully");

        // 3. Create wallet for admin user
        walletService.createWalletForUser(adminUser);
        log.info("✅ Wallet created for admin user");

        log.info("   Email: admin@gmail.com");
        log.info("   Password: 123456");
        log.info("   Role: ADMIN");
        log.info("   Permissions: {} endpoints", adminRole.getPermissions().size());
        log.info("=======================================================");
    }
}
