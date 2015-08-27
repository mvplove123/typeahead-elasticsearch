package com.jerry.travel.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jerry.travel.model.HotelAnalyse;
import com.jerry.travel.service.HotelAnalyseService;

@Controller
@RequestMapping("hotelAnalyse")
public class HotelAnalyseController extends AbstractController {

    @Resource
    private HotelAnalyseService hotelAnalyseService;

    @ResponseBody
    @RequestMapping(value = "createHotelAnalyseIndex")
    public Map<Object, Object> createHotelAnalyseIndex(
            @RequestParam(value = "limit", required = false, defaultValue = "") String limit) {
        hotelAnalyseService.createHotelAnalyseIndex(Long.valueOf(limit));
        return dataJson("索引完成");
    }

    @ResponseBody
    @RequestMapping(value = "findByCityCodeAndQuery")
    public Map<Object, Object> findByCityCodeAndQuery(
            @RequestParam(value = "cityCode", required = false, defaultValue = "") String cityCode,
            @RequestParam(value = "query", required = false, defaultValue = "") String query) {
        List<HotelAnalyse> result = hotelAnalyseService.searchTypeahead(cityCode, query);
        return dataJson(result);
    }

    @ResponseBody
    @RequestMapping(value = "suggestByCityCodeAndQuery")
    public Map<Object, Object> suggestByCityCodeAndQuery(String cityCode, String query) {
        List<HotelAnalyse> result = hotelAnalyseService.getCompletionSuggest(cityCode, query);
        return dataJson(result);
    }

    @ResponseBody
    @RequestMapping(value = "updateHotelAnalyseIndex")
    public Map<Object, Object> updateHotelAnalyseIndex(@RequestParam(value = "limit", required = false, defaultValue = "") String limit) {
        hotelAnalyseService.updateHotelAnalyseIndex(Long.valueOf(limit));
        return dataJson("索引更新完成");
    }

}
