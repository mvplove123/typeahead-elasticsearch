package com.jerry.travel.model;

import java.sql.Timestamp;

public class HotelAnalyse {
    private Long id;

    private String cityCode;

    private String query;

    private Long searchCnt;

    private Timestamp lastMod;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Long getSearchCnt() {
        return searchCnt;
    }

    public void setSearchCnt(Long searchCnt) {
        this.searchCnt = searchCnt;
    }

    public Timestamp getLastMod() {
        return lastMod;
    }

    public void setLastMod(Timestamp lastMod) {
        this.lastMod = lastMod;
    }


}
