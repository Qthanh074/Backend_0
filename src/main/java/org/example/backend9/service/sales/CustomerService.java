package org.example.backend9.service.sales;

import org.example.backend9.dto.request.sales.CustomerRequest;
import org.example.backend9.dto.response.sales.CustomerResponse;
import org.example.backend9.entity.core.Area;
import org.example.backend9.entity.sales.Customer;
import org.example.backend9.repository.sales.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::fromEntity)
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

        if (request.getAreaId() != null) {
            Area area = new Area();
            area.setId(request.getAreaId());
            customer.setArea(area);
        }

        return CustomerResponse.fromEntity(customerRepository.save(customer));
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

        return CustomerResponse.fromEntity(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Integer id) {
        if (!customerRepository.existsById(id)) throw new RuntimeException("Không tìm thấy khách hàng");
        customerRepository.deleteById(id);
    }
}