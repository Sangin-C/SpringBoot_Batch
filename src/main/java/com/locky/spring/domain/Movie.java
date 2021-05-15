package com.locky.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column
    private String rnum;

    @Column
    private String rank;

    @Column
    private String rankInten;

    @Column
    private String rankOldAndNew;

    @Column
    private String movieCd;

    @Column
    private String movieNm;

    @Column
    private String openDt;

    @Column
    private String salesAmt;

    @Column
    private String salesShare;

    @Column
    private String salesInten;

    @Column
    private String salesChange;

    @Column
    private String salesAcc;

    @Column
    private String audiCnt;

    @Column
    private String audiInten;

    @Column
    private String audiChange;

    @Column
    private String audiAcc;

    @Column
    private String scrnCnt;

    @Column
    private String showCnt;
}
