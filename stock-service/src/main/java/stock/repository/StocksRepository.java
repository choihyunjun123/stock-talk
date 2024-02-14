package stock.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stock.domain.Stocks;
import stock.repository.projection.StockCodeProjection;

import java.util.List;

@Repository
public interface StocksRepository extends JpaRepository<Stocks, Long> {

    List<StockCodeProjection> findAllProjectedByStatus(boolean status);

    List<Stocks> findAllByStatus(Sort sort, boolean status);

    List<Stocks> findByName(String name);

    List<Stocks> findByCode(String code);

    Page<Stocks> findByMarketType(int type, Pageable pageable);
}
