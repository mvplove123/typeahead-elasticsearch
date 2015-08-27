package com.jerry.travel.service;

import java.util.List;

import com.jerry.travel.model.HotelAnalyse;


public interface HotelAnalyseService {
    
    /**
     * 获取所有酒店搜索信息
     * @return
     */
    public List<HotelAnalyse> getAllHotelAnalyse(Long limit);
    
    public void createHotelAnalyseIndex(Long limit);
    
    public List<HotelAnalyse> searchTypeahead(String cityCode, String query);

    public List<HotelAnalyse> getCompletionSuggest(String cityCode, String query);

    public void updateHotelAnalyseIndex(Long limit);
}
