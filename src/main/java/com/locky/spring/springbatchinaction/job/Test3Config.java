package com.locky.spring.springbatchinaction.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locky.spring.springbatchinaction.domain.DailyMovie;
import com.locky.spring.springbatchinaction.domain.DailyMovieRepository;
import com.locky.spring.springbatchinaction.domain.MovieList;
import com.locky.spring.springbatchinaction.domain.MovieListRepository;
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
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Test3Config {
    private final JobBuilderFactory jobBuilderFactory;//Job 생성자
    private final StepBuilderFactory stepBuilderFactory;//Step 생성자
    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNKSIZE = 100; //쓰기 단위인 청크사이즈

    @Bean
    public Job test3Job(){
        return jobBuilderFactory.get("test3Job")
                .start(test3Step())
                .build();
    }

    @Bean
    @JobScope
    public Step test3Step(){
        return stepBuilderFactory.get("test3Step")
                .<MovieList, MovieList>chunk(CHUNKSIZE)
                .reader(test3Reader(null))
                .writer(test3Writer())
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<MovieList> test3Reader(@Value("#{jobParameters[index]}") String index) {
        log.info("############################# index : " + index+"#########################################");
        List<MovieList> movieList = movieList(index);
        return new ListItemReader<>(movieList);
    }


    @Bean
    public JpaItemWriter<MovieList> test3Writer(){
        JpaItemWriter<MovieList> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    public List<MovieList> movieList(String index){

        List<MovieList> returncollectData = new ArrayList<>(); //Rest로 가져온 데이터를 리스트에 넣는다.

        //발급키키
        String key = "f778bea14d8ca8349bc583598d1288e9";

        String movieListResponse = "";

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("curPage",index);
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
                //movieListRepository.save(movieList);
                returncollectData.add(movieList);
            }

        }catch(Exception e){
            e.getMessage();
        }
        return returncollectData;
    }
}
