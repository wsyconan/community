package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticSearchService {

    @Autowired
    private DiscussPostRepository postRepository;

    @Autowired
    private ElasticsearchRestTemplate template;

    // 向 ES 服务器中提交新帖子
    public void saveDiscussPost(DiscussPost post) {
        postRepository.save(post);
    }

    // 在 ES 服务器中删除帖子
    public void deleteDiscussPost(int id) {
        postRepository.deleteById(id);
    }

    // 在 ES 服务器中搜索帖子
    public SearchPage<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("<em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("<em>")
                ).build();
        SearchHits<DiscussPost> searchHits = template.search(searchQuery, DiscussPost.class);
        List<SearchHit<DiscussPost>> hits = searchHits.getSearchHits();
        if(searchHits.getTotalHits() <= 0) {
            return null;
        }

        // TODO: 关键字高亮显示（需要研究 ES7 的新API）
        SearchPage<DiscussPost> page = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
        return page;
    }

}
