package stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stock.domain.Stocks;

public interface StocksRepository extends JpaRepository<Stocks, Long> {

}
