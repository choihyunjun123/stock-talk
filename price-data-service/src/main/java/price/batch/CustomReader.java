package price.batch;

import org.springframework.batch.item.ItemReader;
import price.domain.Price;

import java.util.Arrays;
import java.util.List;

public class CustomReader implements ItemReader<String> {

    private final StockCrawlingService stockCrawlingService;

    private final List<String> codes;
    private int index = 0;

    public CustomReader(StockCrawlingService stockCrawlingService, List<String> codes) {
        this.stockCrawlingService = stockCrawlingService;
        this.codes = codes;
    }


    @Override
    public String read() {
        if (index >= codes.size()) {
            return null;
        }
        String code = codes.get(index++);
        String[] data = stockCrawlingService.processCode(code);
        String result = code + ": " + Arrays.toString(data);
        return data != null ? result : null;
    }
}
