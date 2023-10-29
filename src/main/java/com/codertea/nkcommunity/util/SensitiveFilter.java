package com.codertea.nkcommunity.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    // 和谐词
    private static final String REPLACEMENT = "***";
    // 根节点
    private TrieNode root = new TrieNode();
    // 根据敏感词列表，在容器启动时，初始化trie树
    @PostConstruct
    public void init() {
        // 类加载器是从类路径下去加载资源，类路径就是target/classes
        // 程序编译(maven compile)后所有代码和资源都放到classes下了
        // 字节流
        try(
            // 这里开启或者创建对象，编译时会自动加上finally把他关闭掉，相当于在finally里手动关闭
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            // 从字节流中读取不太方便，需要转化成字符流，字符流也不方便，所以再转换为缓冲流
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        ) {
            String keyword;
            while ((keyword=bufferedReader.readLine())!=null) {
                // 添加到前缀树
                this.insert(keyword);
            }

        } catch (IOException e) {
            logger.error("加载敏感词文件失败:" + e.getMessage());
        }
    }
    // 把一个敏感词添加到前缀树
    private void insert(String keyword) {
        // 指针
        TrieNode cur = root;
        for(int i=0; i<keyword.length(); i++) {
            char c = keyword.charAt(i);
            if(cur.getSubNode(c) == null) {
                // 初始化子节点
                cur.addSubNode(c, new TrieNode());
            }
            // 指针指向子节点
            cur = cur.getSubNode(c);
            // 设置结束的标识
            if(i==keyword.length()-1) cur.setKeywordEnd(true);
        }
    }

    // 找keyword在不在字典树里
    private boolean find(String keyword) {
        TrieNode cur = root;
        for(int i=0; i<keyword.length(); i++) {
            char c = keyword.charAt(i);
            if(cur.getSubNode(c)==null) return false;
            cur = cur.getSubNode(c);
        }
        return cur.isKeywordEnd;
    }

    // 把原文和谐掉.返回和谐后的字符串
    public String filter(String origin) {
        if(StringUtils.isBlank(origin)) return null;
        StringBuilder res = new StringBuilder();
        // 指针 指向trie树节点
        TrieNode cur = root;
        // 指针 指向子字符串
        int position = 0;
        while(position < origin.length()) {
            Character c = origin.charAt(position);
            // 跳过符号 #你开&票#哈哈
            if(isSymbol(c)) {
                if(cur == root) res.append(c);
                position++;
                continue;
            }
            cur = cur.getSubNode(c);
            if(cur == null) {
                res.append(origin.charAt(position));
                position++;
                cur = root;
                continue;
            }
            if(cur.isKeywordEnd()) {
                res.append(REPLACEMENT);
                position++;
                cur = root;
            } else {
                position++;
            }
        }
        return res.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 字典树结构只在这个类内部用，所以可以定位为内部类
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeywordEnd = false;
        // 子节点(key是下级节点字符，value是下级节点)
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }
        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
