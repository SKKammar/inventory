package com.example.inventory.init;

import com.example.inventory.entity.Role;
import com.example.inventory.entity.User;
import com.example.inventory.repository.RoleRepository;
import com.example.inventory.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeDefaultUsers();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator with full access")
                    .build();

            Role customerRole = Role.builder()
                    .name("ROLE_CUSTOMER")
                    .description("Customer who can place orders")
                    .build();

            Role warehouseRole = Role.builder()
                    .name("ROLE_WAREHOUSE_STAFF")
                    .description("Warehouse staff for inventory management")
                    .build();

            roleRepository.save(adminRole);
            roleRepository.save(customerRole);
            roleRepository.save(warehouseRole);

            System.out.println("✓ Roles initialized successfully");
        }
    }

    private void initializeDefaultUsers() {
        // Create Admin User
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();

            User admin = User.builder()
                    .username("admin")
                    .email("admin@inventory.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .phoneNumber("9999999999")
                    .address("Admin Address")
                    .isActive(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            userRepository.save(admin);
            System.out.println("✓ Admin user created: username=admin, password=admin123");
        }

        // Create Sample Customer User
        if (!userRepository.existsByUsername("customer1")) {
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();

            User customer = User.builder()
                    .username("customer1")
                    .email("customer1@inventory.com")
                    .password(passwordEncoder.encode("customer123"))
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("9876543210")
                    .address("Customer Address")
                    .isActive(true)
                    .roles(new HashSet<>(Set.of(customerRole)))
                    .build();

            userRepository.save(customer);
            System.out.println("✓ Customer user created: username=customer1, password=customer123");
        }

        // Create Sample Warehouse Staff User
        if (!userRepository.existsByUsername("warehouse1")) {
            Role warehouseRole = roleRepository.findByName("ROLE_WAREHOUSE_STAFF").orElseThrow();

            User warehouse = User.builder()
                    .username("warehouse1")
                    .email("warehouse1@inventory.com")
                    .password(passwordEncoder.encode("warehouse123"))
                    .firstName("Jane")
                    .lastName("Smith")
                    .phoneNumber("9876543211")
                    .address("Warehouse Address")
                    .isActive(true)
                    .roles(new HashSet<>(Set.of(warehouseRole)))
                    .build();

            userRepository.save(warehouse);
            System.out.println("✓ Warehouse staff user created: username=warehouse1, password=warehouse123");
        }
    }
}