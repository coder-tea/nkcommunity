package com.codertea.nkcommunity.config;

import com.alibaba.fastjson.JSON;
import com.codertea.nkcommunity.NkcommunityApplication;
import com.codertea.nkcommunity.entity.DiscussPost;
import com.codertea.nkcommunity.service.DiscussPostService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NkcommunityApplication.class)
public class ElasticSearchClientConfigTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private DiscussPostService discussPostService;

    // 把数据库中所有的帖子放到es里
    @Test
    public void addAllDiscussPost() throws IOException {
        BulkRequest request = new BulkRequest();
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, 0, 1000);
        for (DiscussPost discussPost : discussPosts ) {
            request.add(new IndexRequest("discusspost")
                    .id(String.valueOf(discussPost.getId()))
                    .source(JSON.toJSONString(discussPost), XContentType.JSON));
        }
        BulkResponse response = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response.hasFailures());
    }
}
// 创建帖子索引与映射
/**
 * PUT /discusspost
 * {
 *   "mappings": {
 *       "properties": {
 *         "id": {
 *           "type": "integer"
 *         },
 *         "userId": {
 *           "type": "integer"
 *         },
 *         "title": {
 *           "type": "text",
 *           "analyzer": "ik_smart"
 *         },
 *         "content": {
 *           "type": "text",
 *           "analyzer": "ik_smart"
 *         },
 *         "type": {
 * 		  		"type": "integer"
 *         },
 *         "status": {
 * 		  		"type": "integer"
 *         },
 *         "createTime": {
 * 				  "type": "date"
 *         },
 *         "commontCount": {
 * 		  		"type": "integer"
 *         },
 *         "score": {
 * 		  		"type": "double"
 *         }
 *       }
 *   }
 * }
 * */