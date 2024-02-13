package stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stock.domain.Stocks;

@Repository
public interface StocksRepository extends JpaRepository<Stocks, Long> {

}
