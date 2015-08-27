package com.jerry.travel.dao;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import com.jerry.travel.model.HotelAnalyse;

@Repository
public class HotelAnalyseDao extends SqlSessionDaoSupport {

    
    /**
     * 获取所有酒店搜索信息
     * @param isInland
     * @return
     */
    public List<HotelAnalyse> getAllHotelAnalyse(Long limit){
        
        
        return this.getSqlSession().selectList("getAllHotelAnalyse",limit);
        
    }
    @Resource
        public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory){
           super.setSqlSessionFactory(sqlSessionFactory);
         }
    
    
}
