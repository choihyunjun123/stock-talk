package stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stock.domain.Stocks;
import stock.repository.projection.StockCodeProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface StocksRepository extends JpaRepository<Stocks, Long> {

    List<StockCodeProjection> findAllProjectedByStatus(boolean status);

    List<Stocks> findByName(String name);
}
