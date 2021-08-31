package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.apache.lucene.search.DisjunctionDISIApproximation;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper mapper;

    @Autowired
    private DiscussPostRepository repository;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Test
    public void testInsert() {
        repository.save(mapper.selectDiscussPostById(241));
        repository.save(mapper.selectDiscussPostById(242));
        repository.save(mapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        repository.saveAll(mapper.selectDiscussPosts(101,0,100));
        repository.saveAll(mapper.selectDiscussPosts(102,0,100));
        repository.saveAll(mapper.selectDiscussPosts(103,0,100));
        repository.saveAll(mapper.selectDiscussPosts(111,0,100));
        repository.saveAll(mapper.selectDiscussPosts(112,0,100));
        repository.saveAll(mapper.selectDiscussPosts(131,0,100));
        repository.saveAll(mapper.selectDiscussPosts(132,0,100));
    }

    @Test
    public void testDelete() {
        repository.deleteAll();
    }

    @Test
    public void testSearch() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("<em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("<em>")
                ).build();
        SearchHits<DiscussPost> searchHits = template.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> hits = searchHits.getSearchHits();
        if(searchHits.getTotalHits() <= 0) {
            return;
        }
        List<DiscussPost> list = new ArrayList<>();
        for(SearchHit<DiscussPost> hit: hits) {
            DiscussPost post = new DiscussPost();
            post.setId(hit.getContent().getId());
            post.setUserId(hit.getContent().getUserId());
            post.setStatus(hit.getContent().getStatus());
            post.setCreateTime(hit.getContent().getCreateTime());
            post.setCommentCount(hit.getContent().getCommentCount());


            List titleField =  hit.getHighlightFields().get("title");
            if(titleField == null) {
                post.setTitle(hit.getContent().getTitle());
            } else {
                post.setTitle(titleField.get(0).toString());
            }

            List contentField =  hit.getHighlightFields().get("content");
            if(contentField == null) {
                post.setContent(hit.getContent().getContent());
            } else {
                post.setContent(contentField.get(0).toString());
            }

            list.add(post);
        }
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        page.getContent().forEach(post->post.getContent().getId());
    }

}
