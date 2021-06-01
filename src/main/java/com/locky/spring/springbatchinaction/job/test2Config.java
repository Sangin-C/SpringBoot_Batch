package com.locky.spring.springbatchinaction.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locky.spring.springbatchinaction.domain.DailyMovie;
import com.locky.spring.springbatchinaction.domain.DailyMovie2;
import com.locky.spring.springbatchinaction.domain.DailyMovieRepository;
import kr.or.kobis.kobisopenapi.consumer.rest.KobisOpenAPIRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class test2Config {
    private final JobBuilderFactory jobBuilderFactory;//Job 생성자
    private final StepBuilderFactory stepBuilderFactory;//Step 생성자
    private final EntityManagerFactory entityManagerFactory;
    private final DailyMovieRepository dailyMovieRepository;
    private final DataSource dataSource;//데이터 소스, 오라클 jdbc 사용
    private static final int CHUNKSIZE = 5; //쓰기 단위인 청크사이즈

    private List<DailyMovie> collectData = new ArrayList<>(); //Rest로 가져온 데이터를 리스트에 넣는다.
    private boolean checkRestCall = false; //RestAPI 호출여부 판단
    private int nextIndex = 0;//리스트의 데이터를 하나씩 인덱스를 통해 가져온다.

    @Bean
    public Job testJob(){
        return jobBuilderFactory.get("testJob")
                .start(collectStep())
                .build();
    }

    @Bean
    public Step collectStep(){
        return stepBuilderFactory.get("collectStep")
                .<DailyMovie, DailyMovie>chunk(5)
                .reader(restItCollectReader())
                .writer(itCollectWriter())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<DailyMovie> restItCollectReader() {
        List<DailyMovie> dailyMovie = dailyBoxOffice();
        return new ListItemReader<>(dailyMovie);
    }

/*    //Rest API로 데이터를 가져온다.
    @Bean
    public ItemReader<DailyMovie> restItCollectReader(){
        return new ItemReader<DailyMovie>(){
            @Override
            public DailyMovie read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                collectData = dailyBoxOffice();//배열을 리스트로 변환
                DailyMovie nextCollect = null; //ItemReader는 반복문으로 동작한다. 하나씩 Writer로 전달해야 한다.
                if (nextIndex < collectData.size()) {//전체 리스트에서 하나씩 추출해서, 하나씩 Writer로 전달
                    nextCollect = collectData.get(nextIndex);
                    nextIndex++;
                }
                return nextCollect;//DTO 하나씩 반환한다. Rest 호출시 데이터가 없으면 null로 반환.
            }
        };
    }*/


/*    @Bean // beanMapped()를 사용할때는 필수
    public JdbcBatchItemWriter<DailyMovie> itCollectWriter(){// 오라클 db에 데이터를 쓴다.
        return new JdbcBatchItemWriterBuilder<DailyMovie>()
                .dataSource(dataSource)
                .sql("INSERT INTO DailyMovie (audiAcc) values (:audiAcc)")
                .beanMapped()
                .build();
    }*/

@Bean
public JpaItemWriter<DailyMovie> itCollectWriter(){
    JpaItemWriter<DailyMovie> jpaItemWriter = new JpaItemWriter<>();
    jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
    return jpaItemWriter;
}

/*@Bean
public ItemWriter<DailyMovie> itCollectWriter(){
    return ((List<? extends DailyMovie> DailyMovieList) ->
            dailyMovieRepository.saveAll(DailyMovieList));
}*/



    public List<DailyMovie> dailyBoxOffice(){

        List<DailyMovie> returncollectData = new ArrayList<>(); //Rest로 가져온 데이터를 리스트에 넣는다.

        //발급키키
        String key = "f778bea14d8ca8349bc583598d1288e9";

        String dailyResponse = "";

        //전날 박스오피스 조회 ( 오늘 날짜꺼는 안나옴.. )
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        String targetDt =  time.format(DateTimeFormatter.ofPattern("yyyMMdd"));

        //ROW 개수
        String itemPerPage = "10";

        //다양성영화(Y)/상업영화(N)/전체(default)
        String multiMovieYn = "";

        //한국영화(K)/외국영화(F)/전체(default)
        String repNationCd = "";

        //상영지역별 코드/전체(default)
        String wideAreaCd = "";

        try {
            // KOBIS 오픈 API Rest Client를 통해 호출
            KobisOpenAPIRestService service = new KobisOpenAPIRestService(key);

            // 일일 박스오피스 서비스 호출 (boolean isJson, String targetDt, String itemPerPage,String multiMovieYn, String repNationCd, String wideAreaCd)
            dailyResponse = service.getDailyBoxOffice(true, targetDt, itemPerPage, multiMovieYn, repNationCd, wideAreaCd);

            //JSON Parser 객체 생성
            JSONParser jsonParser = new JSONParser();

            //Parser로 문자열 데이터를 객체로 변환
            Object obj = jsonParser.parse(dailyResponse);

            //파싱한 obj를 JSONObject 객체로 변환
            JSONObject jsonObject = (JSONObject) obj;

            //차근차근 parsing하기
            JSONObject parse_boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");

            //박스오피스 종류
            String boxofficeType = (String) parse_boxOfficeResult.get("boxofficeType");


            //박스오피스 조회 일자
            String showRange = (String) parse_boxOfficeResult.get("showRange");


            ObjectMapper objectMapper = new ObjectMapper();
            JSONArray parse_dailyBoxOfficeList = (JSONArray) parse_boxOfficeResult.get("dailyBoxOfficeList");
            for(int i=0; i<parse_dailyBoxOfficeList.size(); i++){
                JSONObject dailyBoxOffice = (JSONObject) parse_dailyBoxOfficeList.get(i);
                //JSON object -> Java Object(Entity) 변환
                DailyMovie dailyMovie = objectMapper.readValue(dailyBoxOffice.toString(), DailyMovie.class);
                //DB저장
                dailyMovie.setBoxofficeType(boxofficeType);
                dailyMovie.setShowRange(showRange);
                returncollectData.add(dailyMovie);
                //dailyMovieRepository.save(dailyMovie);
            }
        }catch(Exception e){
            e.getMessage();
        }
        return returncollectData;
    }
}
