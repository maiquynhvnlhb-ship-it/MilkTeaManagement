package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.UserAccount;
import org.example.milkteamanagement.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkShiftRepository extends JpaRepository<WorkShift, Long> {
    List<WorkShift> findByStaff(UserAccount staff);
}

