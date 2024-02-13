package price.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import price.domain.Price;

import java.time.LocalDate;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {
    Optional<Price> findByStockCodeAndDate(String stockCode, LocalDate date);
}
