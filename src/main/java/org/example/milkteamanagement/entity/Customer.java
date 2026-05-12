package org.example.milkteamanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    // Use phone number as the natural id / primary key
    @Id
    @Column(length = 20)
    private String phone;

    @Column(length = 120)
    private String name;

    @Column(length = 500)
    private String defaultAddress;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // One customer can have many orders
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CustomerOrder> orders = new ArrayList<>();
}


