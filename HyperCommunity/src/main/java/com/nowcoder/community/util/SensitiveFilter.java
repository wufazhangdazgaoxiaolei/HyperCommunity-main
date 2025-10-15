package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class TrieNode {

        private boolean isKeywordEnd = false;

        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

    //add sensitive word to Trie
    private void addKeyword(String keyword) {
        TrieNode tmpNode = rootNode;
        keyword = keyword.toLowerCase();
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tmpNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tmpNode.addSubNode(c, subNode);
            }
            tmpNode = subNode;
            if (i == keyword.length() - 1) {
                tmpNode.setKeywordEnd(true);
            }
        }
    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        TrieNode tmpNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()) {
            if (position < text.length()) {
                char c = text.charAt(position);
                char lowerC = Character.toLowerCase(c);
                if (!Character.isLetterOrDigit(lowerC)) {
                    if (tmpNode == rootNode) {
                        sb.append(lowerC);
                        begin++;
                    }
                    position++;
                    continue;
                }

                tmpNode = tmpNode.getSubNode(lowerC);
                if (tmpNode == null) {
                    sb.append(text.charAt(begin));
                    position = ++begin;
                    tmpNode = rootNode;
                } else if (tmpNode.isKeywordEnd) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tmpNode = rootNode;
                } else {
                    position++;
                }
            } else {
                sb.append(text.charAt(begin));
                position = ++begin;
                tmpNode = rootNode;
            }
        }
        return sb.toString();
    }


}


