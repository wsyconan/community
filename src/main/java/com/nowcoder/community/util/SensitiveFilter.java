package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换词
    private static final String REPLACEMENT = "**";

    // 根节点
    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive_words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 构建前缀树
                this.addKeyWord(keyword);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if(StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode temp = root;
        int begin = 0;
        int position = 0;
        // 过滤结果
        StringBuffer sb = new StringBuffer();

        while (position < text.length()) {
            char c = text.charAt(position);
            // 当前字符为符号
            if(checkSymbol(c)) {
                // 若temp处于根节点
                if(temp == root) {
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            // 当前字符不是符号,检查子节点
            temp = temp.getChildNode(c);
            // 无子节点
            if(temp == null) {
                sb.append(text.charAt(begin));
                position = ++begin;
                temp = root;
            } else if(temp.isKeywordEnd()) {
                // 发现敏感词
                sb.append(REPLACEMENT);
                begin = ++position;
                temp = root;
            } else {
                // 检查下一个字符
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean checkSymbol(Character c) {
        // 0x2E80 ~ 0x9FFF 东亚字符编码
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private void addKeyWord(String keyword) {
        TrieNode temp = root;
        for(char c: keyword.toCharArray()) {
            TrieNode chileNode = temp.getChildNode(c);
            if(chileNode == null) {
                // 初始化子节点
                chileNode = new TrieNode();
                temp.addChildNode(c, chileNode);
            }
            temp = chileNode;
        }
        temp.setKeywordEnd(true);
    }

    // 前缀树
    private class TrieNode {

        // 子节点
        private Map<Character, TrieNode> childNodes = new HashMap<>();

        // 是否为结尾节点
        private boolean isKeywordEnd = false;

        // 添加子节点
        public void addChildNode(Character c, TrieNode node) {
            childNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getChildNode(Character c) {
            return childNodes.get(c);
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }

}
