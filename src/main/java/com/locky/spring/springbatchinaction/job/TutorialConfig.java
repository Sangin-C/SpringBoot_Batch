package com.locky.spring.springbatchinaction.job;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TutorialConfig {

    private final JobBuilderFactory jobBuilderFactory;  //Job 빌더 생성용
    private final StepBuilderFactory stepBuilderFactory; //Step 빌더 생성용

    // JobBuilderFactory를 통해서 tutorialJob을 생성
    @Bean
    public Job tutorialJob(){
        log.info("tutorialJob Start!!!");
        return jobBuilderFactory.get("tutorialJob")
                .start(tutorialStep())
                .build();
    }

    // StepBuilderFactory를 통해서 tutorialStep을 생성
    @Bean
    public Step tutorialStep() {
        log.info("tutorialStep Start!!!");
        return stepBuilderFactory.get("tutorialStep")
                //클래스 생성해서도 가능
                //.tasklet(new TutorialTasklet())
                // Tasklet 인터페이스 안에 excute 메소드 밖에없기때문에 람다식으로 변환 가능
                .tasklet((contribution, chunkContext)->{
                    log.info("excuted tasklet !!");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
