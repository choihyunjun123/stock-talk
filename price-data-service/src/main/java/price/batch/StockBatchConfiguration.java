package price.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import price.domain.Price;
import price.repository.PriceRepository;

@Configuration
@EnableBatchProcessing
public class StockBatchConfiguration {

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private StockCrawlingService crawlingService;

    @Bean
    public Job job() {
        return new JobBuilder("stockJob")
                .start(step1())
                .build();
    }

    @Bean
    public Step step1() {
        return new StepBuilder("step1")
                .<Price, Price>chunk(10)
                .reader(new StockItemReader(crawlingService))
                .writer(new ItemWriter<Price>() {
                    @Override
                    public void write(Chunk<? extends Price> chunk) throws Exception {
                        priceRepository.saveAll(chunk);
                    }
                })
                .build();
    }
}
