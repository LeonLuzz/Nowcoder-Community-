package com.nowcoder.community;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;


import java.io.IOException;
import java.util.*;

@SpringBootTest
@ContextConfiguration(classes = NowCoderApplication.class)
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    @Qualifier("client")
    private RestHighLevelClient restHighLevelClient;

    //判断某id的文档（数据库中的行）是否存在
    @Test
    public void testExist() {
        boolean exists = discussPostRepository.existsById(1101);
        System.out.println(exists);
    }

    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    //修改一下
    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我在干嘛》？赶紧灌水");
        // post.setTitle(null);//es中的title会设为null
        discussPostRepository.save(post);
    }

    //修改一条数据
    //覆盖es里的原内容 与 修改es中的内容 的区别：String类型的title被设为null，覆盖的话，会把es里的该对象的title也设为null；UpdateRequest，修改后该对象的title不变
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("discusspost", "109");
        request.timeout("1s");
        DiscussPost post = discussPostMapper.selectDiscussPostById(230);
        post.setContent("我是新人,使劲灌水.");
        post.setTitle(null);//es中的title会保存原内容不变
        request.doc(JSON.toJSONString(post), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }

    // 删除数据
    @Test
    public void testDelete() {
    //  discussPostRepository.deleteById(231);//删除一条数据
        discussPostRepository.deleteAll();//删除所有数据
    }

    //不带高亮的查询
    @Test
    public void noHighlightQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                //在discusspost索引的title和content字段中都查询“互联网寒冬”
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)// 指定从哪条开始查询
                .size(10);// 需要查出的总记录条数

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //  System.out.println(JSONObject.toJSON(searchResponse));

        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
        //  System.out.println(discussPost);
            list.add(discussPost);
        }
        System.out.println(list.size());
        for (DiscussPost post : list) {
            System.out.println(post);
        }
    }

    // 高亮显示
    @Test
    public void highlightQuery() throws Exception{
        SearchRequest searchRequest = new SearchRequest("discusspost");//discusspost是索引名，就是表名
        Map<String,Object> res = new HashMap<>();

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)// 指定从哪条开始查询
                .size(10)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//高亮
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            // System.out.println(discussPost);
            list.add(discussPost);
        }
        res.put("list",list);
        res.put("total",total);
        if(res.get("list")!= null){
            for (DiscussPost post : list = (List<DiscussPost>) res.get("list")) {
                System.out.println(post);
            }
            System.out.println(res.get("total"));
        }
    }
}



    // 7.15 直接没了 discussRepository.search方法，放弃
//    @Test
//    public void testSearchByRepository() {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .withPageable(PageRequest.of(0, 10))
//                .withHighlightFields(
//                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                ).build();
//
//        // elasticTemplate.queryForPage(searchQuery, class, SearchResultMapper)
//        // 底层获取得到了高亮显示的值, 但是没有返回.
//
//        Page<DiscussPost> page = discussRepository.search(searchQuery);
//        System.out.println(page.getTotalElements());
//        System.out.println(page.getTotalPages());
//        System.out.println(page.getNumber());
//        System.out.println(page.getSize());
//        for (DiscussPost post : page) {
//            System.out.println(post);
//        }
//    }

    // 7.x版本弃用 template ,因此放弃
//    @Test
//    public void testSearchByTemplate() {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .withPageable(PageRequest.of(0, 10))
//                .withHighlightFields(
//                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
//                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
//                ).build();
//
//        Page<DiscussPost> page = elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
//            @Override
//            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
//                SearchHits hits = response.getHits();
//                if (hits.getTotalHits() <= 0) {
//                    return null;
//                }
//
//                List<DiscussPost> list = new ArrayList<>();
//                for (SearchHit hit : hits) {
//                    DiscussPost post = new DiscussPost();
//
//                    String id = hit.getSourceAsMap().get("id").toString();
//                    post.setId(Integer.valueOf(id));
//
//                    String userId = hit.getSourceAsMap().get("userId").toString();
//                    post.setUserId(Integer.valueOf(userId));
//
//                    String title = hit.getSourceAsMap().get("title").toString();
//                    post.setTitle(title);
//
//                    String content = hit.getSourceAsMap().get("content").toString();
//                    post.setContent(content);
//
//                    String status = hit.getSourceAsMap().get("status").toString();
//                    post.setStatus(Integer.valueOf(status));
//
//                    String createTime = hit.getSourceAsMap().get("createTime").toString();
//                    post.setCreateTime(new Date(Long.valueOf(createTime)));
//
//                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
//                    post.setCommentCount(Integer.valueOf(commentCount));
//
//                    // 处理高亮显示的结果
//                    HighlightField titleField = hit.getHighlightFields().get("title");
//                    if (titleField != null) {
//                        post.setTitle(titleField.getFragments()[0].toString());
//                    }
//
//                    HighlightField contentField = hit.getHighlightFields().get("content");
//                    if (contentField != null) {
//                        post.setContent(contentField.getFragments()[0].toString());
//                    }
//
//                    list.add(post);
//                }
//
//                return new AggregatedPageImpl(list, pageable,
//                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
//            }
//        });
//
//        System.out.println(page.getTotalElements());
//        System.out.println(page.getTotalPages());
//        System.out.println(page.getNumber());
//        System.out.println(page.getSize());
//        for (DiscussPost post : page) {
//            System.out.println(post);
//        }
//    }
