package org.example.milkteamanagement.controller.staff;

import org.example.milkteamanagement.entity.Employee;
import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.exception.NotFoundException;
import org.example.milkteamanagement.repository.EmployeeRepository;
import org.example.milkteamanagement.repository.UserAccountRepository;
import org.example.milkteamanagement.repository.WorkShiftRepository;
import org.example.milkteamanagement.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/me")
public class StaffProfileController {

    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final OrderService orderService;

    public StaffProfileController(UserAccountRepository userAccountRepository,
                                  EmployeeRepository employeeRepository,
                                  WorkShiftRepository workShiftRepository,
                                  OrderService orderService) {
        this.userAccountRepository = userAccountRepository;
        this.employeeRepository = employeeRepository;
        this.workShiftRepository = workShiftRepository;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile(Principal principal) {
        UserAccount user = getCurrentUser(principal.getName());
        Employee employee = employeeRepository.findByUserAccount(user)
                .orElseThrow(() -> new NotFoundException("Employee profile not found"));
        return ResponseEntity.ok(Map.of(
                "employeeId", employee.getId(),
                "fullName", employee.getFullName(),
                "phone", employee.getPhone(),
                "status", employee.getStatus(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

    @GetMapping("/shifts")
    public ResponseEntity<List<Map<String, Object>>> shifts(Principal principal) {
        UserAccount user = getCurrentUser(principal.getName());
        return ResponseEntity.ok(workShiftRepository.findByStaff(user).stream()
                .map(shift -> {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("id", shift.getId());
                    payload.put("shiftStart", shift.getShiftStart());
                    payload.put("shiftEnd", shift.getShiftEnd());
                    payload.put("note", shift.getNote());
                    return payload;
                }).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> myOrders(Principal principal) {
        return ResponseEntity.ok(orderService.findByStaff(principal.getName()).stream()
                .map(order -> {
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("id", order.getId());
                    payload.put("orderCode", order.getOrderCode());
                    payload.put("status", order.getStatus());
                    payload.put("total", order.getTotalAmount());
                    payload.put("createdAt", order.getCreatedAt());
                    return payload;
                }).toList());
    }

    private UserAccount getCurrentUser(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}



