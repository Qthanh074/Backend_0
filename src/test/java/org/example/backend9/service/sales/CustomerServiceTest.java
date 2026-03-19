package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.CustomerRequest;
import org.example.backend9.dto.response.sales.CustomerResponse;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.repository.sales.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer mockCustomer;
    private CustomerRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(1);
        mockCustomer.setCode("KH001");
        mockCustomer.setFullName("Nguyễn Văn A");
        mockCustomer.setPhone("0912345678");

        mockRequest = new CustomerRequest();
        mockRequest.setCode("KH001");
        mockRequest.setFullName("Nguyễn Văn A");
        mockRequest.setPhone("0912345678");
    }

    @Test
    @DisplayName("1. Create: Thành công khi thông tin hợp lệ")
    void createCustomer_Success() {
        when(customerRepository.existsByCode(anyString())).thenReturn(false);
        when(customerRepository.existsByPhone(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        CustomerResponse res = customerService.createCustomer(mockRequest);

        assertNotNull(res);
        assertEquals("KH001", res.getCode());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("2. Create: Thất bại do trùng số điện thoại")
    void createCustomer_Fail_DuplicatePhone() {
        when(customerRepository.existsByCode(anyString())).thenReturn(false);
        when(customerRepository.existsByPhone("0912345678")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> customerService.createCustomer(mockRequest));

        assertEquals("Số điện thoại đã được đăng ký!", ex.getMessage());
    }

    @Test
    @DisplayName("3. Loyalty: Tìm kiếm thành viên theo tên hoặc điện thoại")
    void getLoyaltyMembers_WithSearch_Success() {
        String search = "0912";
        when(customerRepository.findByFullNameContainingOrPhoneContaining(search, search))
                .thenReturn(List.of(mockCustomer));

        List<CustomerResponse> result = customerService.getLoyaltyMembers(search);

        assertEquals(1, result.size());
        assertEquals("Nguyễn Văn A", result.get(0).getFullName());
    }

    @Test
    @DisplayName("4. Update: Cập nhật thành công")
    void updateCustomer_Success() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        CustomerResponse res = customerService.updateCustomer(1, mockRequest);

        assertNotNull(res);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("5. Delete: Thành công")
    void deleteCustomer_Success() {
        when(customerRepository.existsById(1)).thenReturn(true);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1));
        verify(customerRepository).deleteById(1);
    }
}