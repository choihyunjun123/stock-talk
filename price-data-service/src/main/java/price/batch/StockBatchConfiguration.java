package price.batch;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import price.repository.PriceRepository;

import java.time.LocalDateTime;

@Configuration
public class StockBatchConfiguration {

    private static final String JOB_NAME = "job";
    private static final String STEP_NAME = "step";
    private final StockCrawlingService stockCrawlingService;
    private final PriceRepository priceRepository;

    public StockBatchConfiguration(StockCrawlingService stockCrawlingService, PriceRepository priceRepository) {
        this.stockCrawlingService = stockCrawlingService;
        this.priceRepository = priceRepository;
    }

    @Bean
    Job createJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(createStep(jobRepository, transactionManager))
                .end()
                .build();
    }

    @Bean
    Step createStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<String, String>chunk(10, transactionManager)
                .allowStartIfComplete(true)
                .reader(new CustomReader(stockCrawlingService, stockCrawlingService.crawlStocks()))
                .writer(new CustomWriter(priceRepository))
                .build();
    }
}
