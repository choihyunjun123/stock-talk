package price.batch;

import org.springframework.batch.item.ItemReader;
import price.domain.Price;

import java.util.Iterator;

public class StockItemReader implements ItemReader<Price> {

    private final StockCrawlingService crawlingService;
    private Iterator<Price> stockIterator;

    public StockItemReader(StockCrawlingService crawlingService) {
        this.crawlingService = crawlingService;
        this.stockIterator = crawlingService.crawlStocks().iterator();
    }

    @Override
    public Price read() throws Exception {
        if (stockIterator.hasNext()) {
            return stockIterator.next();
        } else {
            return null;
        }
    }
}
