package com.locky.spring.springbatchinaction.job;

import com.locky.spring.springbatchinaction.domain.DailyMovie;
import com.locky.spring.springbatchinaction.domain.DailyMovie2;
import com.locky.spring.springbatchinaction.domain.DailyMovieRepository;
import com.locky.spring.springbatchinaction.domain.DailyMovieRepository2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class testConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private final DailyMovieRepository dailyMovieRepository;
    private final DailyMovieRepository2 dailyMovieRepository2;


    private int chunkSize = 11;

    @Bean
    public Job testjpaPagingItemReaderJob() {
        return jobBuilderFactory.get("testjpaPagingItemReaderJob")
                .start(testjpaPagingItemReaderStep())
                .build();
    }

    @Bean
    public Step testjpaPagingItemReaderStep() {
        return stepBuilderFactory.get("testjpaPagingItemReaderStep")
                .<DailyMovie, DailyMovie2>chunk(chunkSize)
                .reader(testjpaPagingItemReader())
                .processor(testjpaItemProcessor())
                .writer(testjpaPagingItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<DailyMovie> testjpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<DailyMovie>()
                .name("testjpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT d FROM DailyMovie d")
                .build();
    }

    @Bean
    public ItemProcessor<DailyMovie, DailyMovie2> testjpaItemProcessor() {
        return dailyMovie -> new DailyMovie2(dailyMovie.getBoxofficeType(), dailyMovie.getMovieNm());
    }

    @Bean
    public JpaItemWriter<DailyMovie2> testjpaPagingItemWriter(){
        JpaItemWriter<DailyMovie2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
