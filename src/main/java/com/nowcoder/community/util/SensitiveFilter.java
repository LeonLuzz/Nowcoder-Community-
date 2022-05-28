package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "**";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try ( // 任何路径获取类，然后获取类加载器，再然后通过类加载器获取txt文件
             InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is));
             ) {
                String keyword;
                while ((keyword = reader.readLine()) != null) {
                    // 添加到前缀树
                    this.addKeyWord(keyword);
                }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyWord(String keyword) {
        TrieNode tempNode = rootNode;

        for (int i = 0; i < keyword.length(); i++) {
            char ctr = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(ctr);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(ctr, subNode);
            }
            tempNode = subNode;

            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     *  过滤敏感词
     * @param text 待过滤文本
     * @return  过滤后的文本
     */
    public String sensitiveWordFilter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder result = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                if (tempNode == rootNode) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                result.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd()) {
                result.append(REPLACEMENT);
                begin = ++position;
                tempNode = rootNode;
            } else {
                position++;
            }
        }
        result.append(text.substring(begin));
        return result.toString();
    }

    private boolean isSymbol(Character ctr) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(ctr) && (ctr < 0x2E80 || ctr > 0x9FFF);
    }

    private class TrieNode {

        // 敏感词结束标识
        private boolean isKeyWordEnd = false;

        private Map<Character, TrieNode> map = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character ctr, TrieNode trieNode) {
            map.put(ctr, trieNode);
        }

        // 获取子节点
        public TrieNode getSubNode(Character ctr) {
            return map.get(ctr);
        }
    }
}
