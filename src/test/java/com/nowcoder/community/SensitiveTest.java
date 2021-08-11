package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter filter;

    @Test
    public void testFilter() {
        String text = "苟利国家生死以，岂因祸福避趋之";
        text = filter.filter(text);
        System.out.println(text);
    }

    @Test
    public void testFilter2() {
        String text = "可以嫖娼，の吸毒⭐，无丝竹之乱耳，无案牍之劳形。";
        text = filter.filter(text);
        System.out.println(text);
    }
}
