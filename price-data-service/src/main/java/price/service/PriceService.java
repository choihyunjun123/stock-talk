package price.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import price.domain.Price;
import price.repository.PriceRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class PriceService {

    private final PriceRepository priceRepository;

    @Autowired
    public PriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public boolean crawl() throws IOException {
        String stockCode = "005930";
        String URL = "https://fchart.stock.naver.com/sise.nhn?symbol="+stockCode+"&timeframe=day&count=10&requestType=0";
        Document a = Jsoup.connect(URL).get();
        Elements elements = a.getElementsByTag("item");
        for (Element item : elements) {
            String data = item.attr("data");
            String[] k = data.split("\\|");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            Price price = new Price();
            price.setStockCode(stockCode);
            LocalDate date = LocalDate.parse(k[0], formatter);
            price.setDate(date);
            price.setOpen(Long.valueOf(k[1]));
            price.setHigh(Long.valueOf(k[2]));
            price.setLow(Long.valueOf(k[3]));
            price.setClose(Long.valueOf(k[4]));
            price.setVolume(Long.valueOf(k[5]));
            price.setCreatedAt(LocalDateTime.now());
            priceRepository.save(price);
        }
        return true;
    }
}
