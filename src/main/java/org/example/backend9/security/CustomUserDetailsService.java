package org.example.backend9.security;

import org.example.backend9.entity.core.Employee;
import org.example.backend9.repository.core.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        // Chuyển role dạng String (VD: "ADMIN") thành dạng GrantedAuthority ("ROLE_ADMIN")
        String roleName = employee.getRole() != null ? "ROLE_" + employee.getRole().toUpperCase() : "ROLE_STAFF";

        return new User(
                employee.getEmail(),
                employee.getPasswordHash(),
                Collections.singleton(new SimpleGrantedAuthority(roleName))
        );
    }
}