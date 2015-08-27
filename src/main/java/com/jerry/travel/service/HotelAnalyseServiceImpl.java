package com.jerry.travel.service;


import com.alibaba.fastjson.JSONObject;
import com.jerry.travel.dao.HotelAnalyseDao;
import com.jerry.travel.model.HotelAnalyse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.alibaba.fastjson.JSON.toJSONString;

@Service
public class HotelAnalyseServiceImpl extends AbstractService implements HotelAnalyseService {

    @Resource
    private HotelAnalyseDao hotelAnalyseDao;

    private String indexCluster = "searcher";

    private String type = "hotelAnalyse";

    /**
     * 获取所有酒店搜索信息
     */
    public List<HotelAnalyse> getAllHotelAnalyse(Long limit) {
        return hotelAnalyseDao.getAllHotelAnalyse(limit);
    }

    @Override
    public void createHotelAnalyseIndex(Long limit) {

        
        Client client = getClient();
        recreateIndex(indexCluster);
        XContentBuilder mapping;
        try {
            mapping = XContentFactory.jsonBuilder().startObject().startObject(type).startObject("properties")
                    .startObject("id").field("type", "long").endObject().startObject("cityCode")
                    .field("type", "string").field("store", "yes").endObject().startObject("query")
                    .field("type", "string").field("store", "yes").field("index", "not_analyzed").endObject()
                    .startObject("searchCnt").field("type", "long").endObject().endObject().endObject().endObject();

            PutMappingRequest mappingRequest = Requests.putMappingRequest(indexCluster).type(type).source(mapping);
            client.admin().indices().putMapping(mappingRequest).actionGet();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BulkRequestBuilder bulk = client.prepareBulk();
        List<HotelAnalyse> hotelAnalyseList = getAllHotelAnalyse(limit);
        for (HotelAnalyse analyse : hotelAnalyseList) {

            HotelAnalyse hotelAnalyse = new HotelAnalyse();
            hotelAnalyse.setId(analyse.getId());
            hotelAnalyse.setCityCode(analyse.getCityCode());
            hotelAnalyse.setQuery(analyse.getQuery());
            hotelAnalyse.setSearchCnt(analyse.getSearchCnt());
            IndexRequestBuilder indexbulider = client.prepareIndex(indexCluster, type,
                    String.valueOf(hotelAnalyse.getId()));

            indexbulider.setSource(toJSONString(hotelAnalyse));
            bulk.add(indexbulider);
        }

        BulkResponse response = bulk.execute().actionGet();
        if (response.hasFailures()) {
            System.out.println("index有误！");
        }

    }

    @Override
    public List<HotelAnalyse> searchTypeahead(String cityCode, String query) {

        Client client = getClient();
        
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.prefixQuery("cityCode", cityCode))
                .must(QueryBuilders.prefixQuery("query", query));

        SearchResponse response = client.prepareSearch(indexCluster).setTypes(type).setQuery(queryBuilder).setFrom(0)
                .setSize(10).setExplain(true).addHighlightedField("query")
                .setHighlighterPreTags("<span style=\"color:red\">").setHighlighterPostTags("</span>")
                .addSort("searchCnt", SortOrder.DESC).execute().actionGet();

        SearchHits hits = response.getHits();

        // System.out.println(hits.getTotalHits());
        List<HotelAnalyse> resultList = Lists.newArrayList();
        for (int i = 0; i < hits.getHits().length; i++) {
            HotelAnalyse result = JSONObject.parseObject(hits.getHits()[i].getSourceAsString(), HotelAnalyse.class);
            
            if(result == null){
                continue;
            }
            
            HighlightField hField = hits.getHits()[i].getHighlightFields().get("query");
            
            
            String queryResult = result.getQuery();
            
            String highligterQuery = "<span style=\"color:red\">" + query + "</span>";
            
            
            String highligterResult = queryResult.replace(query, highligterQuery);
            
//            for (Text t : hField.fragments()) {
//                 result.setQuery(t.string());
//                // System.out.println(t.string());
//            }
            
            result.setQuery(highligterResult);

            resultList.add(result);
        }
        return resultList;
    }

    /**
     * 搜索建议，自动补全搜索结结果
     * 
     * @param prefix 搜索前缀词
     * @return 建议列表
     */
    public List<HotelAnalyse> getCompletionSuggest(String cityCode, String prefix) {

        TransportClient client = new TransportClient();
        client.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
        CompletionSuggestionBuilder suggestionsBuilder = new CompletionSuggestionBuilder("complete");
        suggestionsBuilder.text(prefix);
        suggestionsBuilder.field("query");
        suggestionsBuilder.field(cityCode);
        suggestionsBuilder.size(10);
        SuggestResponse resp = client.prepareSuggest(indexCluster).addSuggestion(suggestionsBuilder).execute()
                .actionGet();
        List<? extends Entry<? extends Option>> list = resp.getSuggest().getSuggestion("complete").getEntries();
        List<HotelAnalyse> suggests = new ArrayList<HotelAnalyse>();
        if (list == null) {
            return null;
        } else {
            for (Entry<? extends Option> e : list) {
                for (Option option : e) {
                    System.out.println(option.getText().toString());
                }
            }
            return suggests;
        }
    }

    @Override
    public void updateHotelAnalyseIndex(Long limit) {

        Client client = getClient();


        // 连接客户端，进行更新操作
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        List<HotelAnalyse> hotelAnalyseList = getAllHotelAnalyse(limit);

        for (HotelAnalyse hotelAnalyse : hotelAnalyseList){

//            UpdateRequest updateRequest = client.

            UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(indexCluster, type, String.valueOf(hotelAnalyse.getId()));
            String hotelString = toJSONString(hotelAnalyse);
            System.out.println(hotelString);
            updateRequestBuilder.setDoc(hotelString);
            bulkRequest.add(updateRequestBuilder);
        }
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();

        if (bulkResponse.hasFailures()){




            boolean isAllConflictError = true; // 是否都是版本冲突的错误
            List<Long> failedOrderIdList = new ArrayList<Long>();
            Iterator<BulkItemResponse> itemResponseIterator = bulkResponse.iterator();
            while (itemResponseIterator.hasNext()) {
                BulkItemResponse bulkItemResponse = itemResponseIterator.next();
                if (bulkItemResponse != null && bulkItemResponse.isFailed()) {

                    // 如果不是版本冲突问题。
                    if (bulkItemResponse.getFailure().getStatus() != RestStatus.CONFLICT) {
                        isAllConflictError = false;
                        failedOrderIdList.add(Long.valueOf(bulkItemResponse.getId()));
                    } else {
                        System.out.println("有问题");
                    }
                }
            }

            // 如果非都是版本冲突错误，就抛出异常。都是版本冲突可以忽略
            if (!isAllConflictError) {

                String orderIdListStr = StringUtils.join(failedOrderIdList, ",");

                throw new RuntimeException();
            }


        }



    }

}
