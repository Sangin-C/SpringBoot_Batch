package com.locky.spring.springbatchinaction.job;


import com.locky.spring.springbatchinaction.domain.DailyMovie;
import com.locky.spring.springbatchinaction.domain.DailyMovieRepository;
import com.locky.spring.springbatchinaction.domain.DailyMovieRepository2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RPWCofing {

    private final JobBuilderFactory jobBuilderFactory;  //Job 빌더 생성용
    private final StepBuilderFactory stepBuilderFactory; //Step 빌더 생성용
    private final DailyMovieRepository dailyMovieRepository;
    private final DailyMovieRepository2 dailyMovieRepository2;

    @Bean
    public Job myJob() {
        return jobBuilderFactory.get("myJob")
                .start(mystep())
                .build();
    }

    @Bean
    public Step mystep() {
        return stepBuilderFactory.get("mystep")
                .<DailyMovie, DailyMovie> chunk(30)
                .reader(myrReader())
                .writer(myWriter())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<DailyMovie> myrReader() {
        List<DailyMovie> oldMember = dailyMovieRepository.findAll();
        return new ListItemReader<>(oldMember);
    }

/*    public ItemProcessor<DailyMovie, DailyMovie> myProcessor() {
        return DailyMovie::setInactive;
    }*/

    @Bean
    public ItemWriter<DailyMovie> myWriter() {
        return ((List<? extends DailyMovie> DailyMovieList) ->
                dailyMovieRepository2.saveAll(DailyMovieList));
    }

}
