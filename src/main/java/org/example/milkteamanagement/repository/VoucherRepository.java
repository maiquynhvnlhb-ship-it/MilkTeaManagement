package org.example.milkteamanagement.repository;

import org.example.milkteamanagement.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCaseAndActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String code,
            LocalDate todayStart,
            LocalDate todayEnd
    );
}

