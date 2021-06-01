package com.locky.spring.springbatchinaction.job;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.locky.spring.springbatchinaction.domain.*;
import kr.or.kobis.kobisopenapi.consumer.rest.KobisOpenAPIRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TutorialConfig {

    private final JobBuilderFactory jobBuilderFactory;  //Job 빌더 생성용
    private final StepBuilderFactory stepBuilderFactory; //Step 빌더 생성용
    private final DailyMovieRepository dailyMovieRepository;
    private final WeeklyMovieRepository weeklyMovieRepository;
    private final MovieListRepository movieListRepository;

    //발급키키
    String key = "f778bea14d8ca8349bc583598d1288e9";


    // JobBuilderFactory를 통해서 tutorialJob을 생성
    @Bean
    public Job tutorialJob(){
        return jobBuilderFactory.get("tutorialJob")
                .start(tutorialStep(null))
                .build();
    }

    // StepBuilderFactory를 통해서 tutorialStep을 생성
    @Bean
    @JobScope
    public Step tutorialStep(@Value("#{jobParameters[index]}") String index) {
        return stepBuilderFactory.get("tutorialStep")
                //Tasklet 인터페이스 안에 excute 메소드 밖에없기때문에 람다식으로 변환 가능
                //.tasklet(new TutorialTasklet())
                .tasklet((contribution, chunkContext)->{
                    log.info("excuted tasklet !!");
                    log.info("################################index : "+index);
                    //일간 박스오피스 Insert
                    //dailyBoxOffice();
                    //주간 박스오피스 Insert
                    //weeklyBoxOffice();
                    //영화 정보
                    //movieList();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


    public void dailyBoxOffice(){
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
                dailyMovieRepository.save(dailyMovie);
            }
        }catch(Exception e){
            e.getMessage();
        }

    }

    public void weeklyBoxOffice(){
        String weeklyResponse = "";

        //전주 박스오피스 조회 ( 해당주는 안나옴.. )
        LocalDateTime time = LocalDateTime.now().minusDays(7);
        String targetDt =  time.format(DateTimeFormatter.ofPattern("yyyMMdd"));

        //주간/주말/주중 선택
        String weekGb = "0";

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

            // 일일 박스오피스 서비스 호출 (boolean isJson, String targetDt, String itemPerPage, String weekGb, String multiMovieYn, String repNationCd, String wideAreaCd)
            weeklyResponse = service.getWeeklyBoxOffice(true, targetDt, itemPerPage, weekGb, multiMovieYn, repNationCd, wideAreaCd);

            //JSON Parser 객체 생성
            JSONParser jsonParser = new JSONParser();

            //Parser로 문자열 데이터를 객체로 변환
            Object obj = jsonParser.parse(weeklyResponse);

            //파싱한 obj를 JSONObject 객체로 변환
            JSONObject jsonObject = (JSONObject) obj;

            //차근차근 parsing하기
            JSONObject parse_boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");

            //박스오피스 종류
            String boxofficeType = (String) parse_boxOfficeResult.get("boxofficeType");


            //박스오피스 조회 일자
            String showRange = (String) parse_boxOfficeResult.get("showRange");


            //박스오피스 조회 일자
            String yearWeekTime = (String) parse_boxOfficeResult.get("yearWeekTime");


            ObjectMapper objectMapper = new ObjectMapper();
            JSONArray parse_weeklyBoxOfficeList = (JSONArray) parse_boxOfficeResult.get("weeklyBoxOfficeList");
            for(int i=0; i<parse_weeklyBoxOfficeList.size(); i++){
                JSONObject weeklyBoxOffice = (JSONObject) parse_weeklyBoxOfficeList.get(i);
                //JSON object -> Java Object(Entity) 변환
                WeeklyMovie weeklyMovie = objectMapper.readValue(weeklyBoxOffice.toString(), WeeklyMovie.class);
                //DB저장
                weeklyMovie.setBoxofficeType(boxofficeType);
                weeklyMovie.setShowRange(showRange);
                weeklyMovie.setYearWeekTime(yearWeekTime);
                weeklyMovieRepository.save(weeklyMovie);
            }
        }catch(Exception e){
            e.getMessage();
        }
    }


    public void movieList(){
        String movieListResponse = "";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("curPage","1");
        paramMap.put("itemPerPage", "100");
        paramMap.put("movieNm", "");
        paramMap.put("directorNm", "");
        paramMap.put("openStartDt", "");
        paramMap.put("openEndDt", "");
        paramMap.put("prdtStartYear", "");
        paramMap.put("prdtEndYear", "");
        paramMap.put("repNationCd", "");
        String[] movieTypeCdArr = {};
        paramMap.put("movieTypeCdArr", movieTypeCdArr);

        try {
            // KOBIS 오픈 API Rest Client를 통해 호출
            KobisOpenAPIRestService service = new KobisOpenAPIRestService(key);
            movieListResponse = service.getMovieList(true, paramMap);

            //JSON Parser 객체 생성
            JSONParser jsonParser = new JSONParser();

            //Parser로 문자열 데이터를 객체로 변환
            Object obj = jsonParser.parse(movieListResponse);

            //파싱한 obj를 JSONObject 객체로 변환
            JSONObject jsonObject = (JSONObject) obj;

            //차근차근 parsing하기
            JSONObject parse_movieListResult = (JSONObject) jsonObject.get("movieListResult");

            //JSON object -> Java Object(Entity) 변환하기위한 Mapper 선언
            ObjectMapper objectMapper = new ObjectMapper();

            //새로운 JSONObject 선언
            JSONObject movieListResultObject = new JSONObject();

            JSONArray parse_movieList = (JSONArray) parse_movieListResult.get("movieList");
            for(int i=0; i<parse_movieList.size(); i++){
                JSONObject movieListObject = (JSONObject) parse_movieList.get(i);

                String repNationNm = (String) movieListObject.get("repNationNm");
                movieListResultObject.put("repNationNm", repNationNm);

                String nationAlt = (String) movieListObject.get("nationAlt");
                movieListResultObject.put("nationAlt", nationAlt);

                String repGenreNm = (String) movieListObject.get("repGenreNm");
                movieListResultObject.put("repGenreNm", repGenreNm);

                String movieNm = (String) movieListObject.get("movieNm");
                movieListResultObject.put("movieNm", movieNm);

                String movieCd = (String) movieListObject.get("movieCd");
                movieListResultObject.put("movieCd", movieCd);

                String prdtStatNm = (String) movieListObject.get("prdtStatNm");
                movieListResultObject.put("prdtStatNm", prdtStatNm);

                String prdtYear = (String) movieListObject.get("prdtYear");
                movieListResultObject.put("prdtYear", prdtYear);

                String typeNm = (String) movieListObject.get("typeNm");
                movieListResultObject.put("typeNm", typeNm);

                String openDt = (String) movieListObject.get("openDt");
                movieListResultObject.put("openDt", openDt);

                String movieNmEn = (String) movieListObject.get("movieNmEn");
                movieListResultObject.put("movieNmEn", movieNmEn);

                String genreAlt = (String) movieListObject.get("genreAlt");
                movieListResultObject.put("genreAlt", genreAlt);

                //영화감독(directors) Array 추출
                StringBuilder directorsList = new StringBuilder();
                JSONArray parse_directorsList = (JSONArray) movieListObject.get("directors");
                for (int j = 0; j < parse_directorsList.size(); j++) {
                    JSONObject directorsListObject = (JSONObject) parse_directorsList.get(j);
                    String directors = (String) directorsListObject.get("peopleNm");
                    if(j>0)directorsList.append(", ");
                    directorsList.append(directors);
                    movieListResultObject.put("directors", directorsList.toString());
                }

                //제작사(companys) Array 추출
                //제작사 코드 빼고 제작사명만 넣는다.
                StringBuilder companysList = new StringBuilder();
                JSONArray parse_companysList = (JSONArray) movieListObject.get("companys");
                for (int k = 0; k < parse_companysList.size(); k++) {
                    JSONObject companysListObject = (JSONObject) parse_companysList.get(k);
                    String companyNm = (String) companysListObject.get("companyNm");
                    if(k>0) companysList.append(",");
                    companysList.append(companyNm);
                    movieListResultObject.put("companys", companysList.toString());
                }
                //JSON object -> Java Object(Entity) 변환
                MovieList movieList = objectMapper.readValue(movieListResultObject.toString(), MovieList.class);
                movieListRepository.save(movieList);
            }

        }catch(Exception e){
            e.getMessage();
        }

    }

}
