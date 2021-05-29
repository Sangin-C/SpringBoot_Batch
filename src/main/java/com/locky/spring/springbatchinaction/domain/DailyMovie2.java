package com.locky.spring.springbatchinaction.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@ToString
@Getter
@Setter
@NoArgsConstructor
@Entity
public class DailyMovie2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String boxofficeType;
    private String movieNm;

    public DailyMovie2(String boxofficeType, String movieNm) {
        this.boxofficeType = boxofficeType;
        this.movieNm = movieNm;
    }
}
