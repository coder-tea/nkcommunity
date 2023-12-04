package com.codertea.nkcommunity.service;

import com.alibaba.fastjson.JSON;
import com.codertea.nkcommunity.entity.DiscussPost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DiscussPostService discussPostService;

    // 增加一个文档
    public void addDiscussPost(DiscussPost discussPost) throws IOException {
        IndexRequest request = new IndexRequest("discusspost");
        request.id(String.valueOf(discussPost.getId()));
        request.source(JSON.toJSONString(discussPost), XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    // 删除一个文档
    public void deleteDiscussPost(int id) throws IOException {
        DeleteRequest request = new DeleteRequest("discusspost", String.valueOf(id));
        DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    // 搜索文档关键字高亮
    public List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws IOException {
        SearchRequest request = new SearchRequest("discusspost");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(current); // es中的页码从0开始
        searchSourceBuilder.size(limit);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword); // 精确匹配
        searchSourceBuilder.query(termQueryBuilder);
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        request.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<DiscussPost> list = new ArrayList<>();
        for(SearchHit hit : response.getHits().getHits()) {
            DiscussPost discussPost = JSON.parseObject(hit.getSourceAsString(), DiscussPost.class);
            // 处理高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                // 获取高亮字段结果
                HighlightField titleHighlight = highlightFields.get("title");
                if(titleHighlight!=null) {
                    // 取出高亮结果数组中的第一个
                    String title = titleHighlight.getFragments()[0].string();
                    discussPost.setTitle(title);
                }
                list.add(discussPost);
            }
        }
        return list;
    }

    // 搜索文档,看看有多少,方便分页
    public int findDiscussPostRows(String keyword) throws IOException {
        SearchRequest request = new SearchRequest("discusspost");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword); // 精确匹配
        searchSourceBuilder.query(termQueryBuilder);
        request.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return (int) response.getHits().getTotalHits().value;
    }
}
