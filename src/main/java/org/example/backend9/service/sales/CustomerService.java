package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.CustomerRequest;
import org.example.backend9.dto.response.sales.CustomerResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.repository.sales.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    private String calculateRank(BigDecimal totalSpending) {
        if (totalSpending == null) return "Đồng";
        double spending = totalSpending.doubleValue();

        if (spending >= 10000000) return "Vàng";
        if (spending >= 2000000)  return "Bạc";
        return "Đồng";
    }

    // Hàm hỗ trợ chuyển đổi Entity sang Response và áp dụng Rank đồng bộ
    private CustomerResponse convertToResponse(Customer customer) {
        CustomerResponse response = CustomerResponse.fromEntity(customer);
        // Ép hạng thẻ phải theo đúng logic tính toán ở Backend
        response.setRank(calculateRank(customer.getTotalSpending()));
        return response;
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToResponse) // Sử dụng hàm convert đã đồng bộ rank
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã khách hàng đã tồn tại!");
        }
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được đăng ký!");
        }

        Customer customer = new Customer();
        customer.setCode(request.getCode());
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setCanPlaceOrder(request.getCanPlaceOrder() != null ? request.getCanPlaceOrder() : true);
        customer.setTotalSpending(BigDecimal.ZERO); // Mặc định chi tiêu = 0 khi mới tạo

        if (request.getAreaId() != null) {
            Area area = new Area();
            area.setId(request.getAreaId());
            customer.setArea(area);
        }

        return convertToResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (!customer.getCode().equals(request.getCode()) && customerRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã khách hàng đã tồn tại!");
        }
        if (!customer.getPhone().equals(request.getPhone()) && customerRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được đăng ký!");
        }

        customer.setCode(request.getCode());
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        if (request.getCanPlaceOrder() != null) customer.setCanPlaceOrder(request.getCanPlaceOrder());

        return convertToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Integer id) {
        if (!customerRepository.existsById(id)) throw new RuntimeException("Không tìm thấy khách hàng");
        customerRepository.deleteById(id);
    }

    public List<CustomerResponse> getLoyaltyMembers(String search) {
        List<Customer> customers;
        if (search != null && !search.isEmpty()) {
            customers = customerRepository.findByFullNameContainingOrPhoneContaining(search, search);
        } else {
            customers = customerRepository.findAll();
        }

        return customers.stream()
                .map(this::convertToResponse) // 🟢 Đồng bộ hạng thẻ tại đây
                .collect(Collectors.toList());
    }
}