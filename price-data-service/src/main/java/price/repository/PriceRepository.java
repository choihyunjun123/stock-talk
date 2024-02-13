package price.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import price.domain.Price;

public interface PriceRepository extends JpaRepository<Price, Long> {

}
