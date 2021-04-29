package com.sdec.test;

/**
 * @author Xu Rui
 * @date 2021/3/3 11:34
 */
public class PullTest {

    public static final String pullLogUrl = "http://localhost:28080/log/pull";

    public static void main(String[] args) {

        String originalKey = "dhwxhxhwwptye3yt4hi473tjkka5woor";

        HttpRequest pull = new HttpRequest(pullLogUrl);
        String result = pull.sendGet("key=" + originalKey);
        System.out.println(result);
    }
}
