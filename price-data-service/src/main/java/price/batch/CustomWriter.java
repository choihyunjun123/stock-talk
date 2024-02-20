package price.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import price.domain.Price;
import price.repository.PriceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CustomWriter implements ItemWriter<String> {

    private static final int DATA_INDEX_DATE = 0;
    private static final int DATA_INDEX_OPEN = 1;
    private static final int DATA_INDEX_HIGH = 2;
    private static final int DATA_INDEX_LOW = 3;
    private static final int DATA_INDEX_CLOSE = 4;
    private static final int DATA_INDEX_VOLUME = 5;
    private static final int CODE_INDEX = 0;
    private static final int DATA_INDEX = 1;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final PriceRepository priceRepository;

    public CustomWriter(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        for (String data : chunk) {
            Price price = parseAndCreatePrice(data);
            priceRepository.save(price);
        }
        System.out.println("Completed writing data.");
    }

    private Price parseAndCreatePrice(String data) {
        String[] parts = data.split(":");
        String stockCode = parts[CODE_INDEX].trim();
        String[] priceData = parts[DATA_INDEX].trim().replaceAll("[\\[\\]]", "").split(",");

        Price price = new Price();
        price.setStockCode(stockCode);
        price.setCreatedAt(LocalDateTime.now());
        price.setDate(LocalDate.parse(priceData[DATA_INDEX_DATE], FORMATTER));
        price.setOpen(Long.parseLong(priceData[DATA_INDEX_OPEN].trim()));
        price.setHigh(Long.parseLong(priceData[DATA_INDEX_HIGH].trim()));
        price.setLow(Long.parseLong(priceData[DATA_INDEX_LOW].trim()));
        price.setClose(Long.parseLong(priceData[DATA_INDEX_CLOSE].trim()));
        price.setVolume(Long.parseLong(priceData[DATA_INDEX_VOLUME].trim()));
        price.setTotalTrade(price.getClose() * price.getVolume());
        setPriceChangeIfPossible(stockCode, price);

        return price;
    }

    private void setPriceChangeIfPossible(String stockCode, Price price) {
        findFirstExistingPriceBeforeDate(stockCode, price.getDate()).ifPresent(beforePrice -> {
            double priceChange = (price.getClose() - beforePrice.getClose()) / (double) beforePrice.getClose() * 100;
            price.setPriceChange(priceChange);
        });
    }

    private Optional<Price> findFirstExistingPriceBeforeDate(String stockCode, LocalDate date) {
        for (int i = 1; i < 10; i++) {
            Optional<Price> existingPrice = priceRepository.findByStockCodeAndDate(stockCode, date.minusDays(i));
            if (existingPrice.isPresent()) {
                return existingPrice;
            }
        }
        return Optional.empty();
    }
}
