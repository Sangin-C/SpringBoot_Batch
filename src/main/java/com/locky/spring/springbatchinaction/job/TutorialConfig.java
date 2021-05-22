package com.locky.spring.springbatchinaction.job;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.locky.spring.springbatchinaction.domain.Movie;
import com.locky.spring.springbatchinaction.domain.MovieRepository;
import com.locky.spring.springbatchinaction.tasklets.TutorialTasklet;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TutorialConfig {

    private final JobBuilderFactory jobBuilderFactory;  //Job 빌더 생성용
    private final StepBuilderFactory stepBuilderFactory; //Step 빌더 생성용
    private final MovieRepository movieRepository;

    //발급키키
    String key = "f778bea14d8ca8349bc583598d1288e9";
    String dailyResponse = "";


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
                //Tasklet 인터페이스 안에 excute 메소드 밖에없기때문에 람다식으로 변환 가능
                //.tasklet(new TutorialTasklet())
                .tasklet((contribution, chunkContext)->{
                    log.info("excuted tasklet !!");
                    //일간 박스오피스 Insert
                    dailyBoxOfficeApi();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


    public void dailyBoxOfficeApi(){

        //일자 포맷 맞추기
        SimpleDateFormat todayFormat = new SimpleDateFormat("yyyyMMdd");

        //전날 박스오피스 조회 ( 오늘 날짜꺼는 안나옴.. )
        Calendar day = Calendar.getInstance();
        day.add(Calendar.DATE , -1);
        //조회 날짜
        String targetDt = todayFormat.format(day.getTime());
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
            log.info(dailyResponse);

            //JSON Parser 객체 생성
            JSONParser jsonParser = new JSONParser();

            //Parser로 문자열 데이터를 객체로 변환
            Object obj = jsonParser.parse(dailyResponse);
            log.info("obj.toString() : "+obj.toString());
            //파싱한 obj를 JSONObject 객체로 변환
            JSONObject jsonObject = (JSONObject) obj;

            //차근차근 parsing하기
            JSONObject parse_boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");

            //박스오피스 종류
            String boxofficeType = (String) parse_boxOfficeResult.get("boxofficeType");
            log.info("boxofficeType : "+boxofficeType);

            //박스오피스 조회 일자
            String showRange = (String) parse_boxOfficeResult.get("showRange");
            log.info("showRange : "+showRange);

            ObjectMapper objectMapper = new ObjectMapper();
            JSONArray parse_dailyBoxOfficeList = (JSONArray) parse_boxOfficeResult.get("dailyBoxOfficeList");
            for(int i=0; i<parse_dailyBoxOfficeList.size(); i++){
                JSONObject dailyBoxOffice = (JSONObject) parse_dailyBoxOfficeList.get(i);
                //JSON object -> Java Object(Entity) 변환
                Movie movie = objectMapper.readValue(dailyBoxOffice.toString(), Movie.class);
                //DB저장
                movieRepository.save(movie);
            }
        }catch(Exception e){
            e.getMessage();
        }
    }
}
