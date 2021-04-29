package com.sdec.test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


/**
 * Java sends HTTP get and post requests
 */
public class HttpRequest {

    private String url;

    HttpRequest(){

    }

    HttpRequest(String url){
        this.url = url;
    }
    /**
     * Send a get request to the specified URL
     * @param url  The URL to send the request
     * @param param Request parameters
     * @return URL A response that represents a remote resource
     */
    public String sendGet(String url, String param){
        StringBuilder result = new StringBuilder();
        String urlName = url + "?" + param;
        try{
            URL realUrl = new URL(urlName);
            //Open the connection between and URL
            URLConnection conn = realUrl.openConnection();
            //Set common request properties
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //Make the actual connection
            conn.connect();
            //Get all response header fields
            Map<String,List<String>> map = conn.getHeaderFields();
            //Traverse all response header fields
            for (String key : map.keySet()) {
                System.out.println(key + "-->" + map.get(key));
            }
            // Define the BufferedReader input stream to read the response of the URL
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("Exception in sending get request" + e);
            e.printStackTrace();
        }
        return result.toString();


    }

    public String sendGet(String param){
        return sendGet(this.url, param);
    }

    /**
     * Send a post request to the specified URL
     * @param url  The URL to send the request
     * @param param Request parameters
     * @return URL A response that represents a remote resource
     */
    public String sendPost(String url, String param) throws IOException {
        StringBuilder result = new StringBuilder();
        try{
            URL realUrl = new URL(url);
            //Open the connection between and URL
            URLConnection conn =  realUrl.openConnection();
            //Set common request properties
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("contentType", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //Gets the output stream corresponding to the urlconnection object

            PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
            out.print(param);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append("\n").append(line);
            }
        } catch (Exception e) {
            System.out.println("Exception in sending post request" + e);
            throw e;
        }
        return result.toString();
    }

    public String sendPost(String param) throws IOException {
        return sendPost(this.url, param);
    }

    //test get and post
    public static void main(String[] args) throws Exception{
        HttpRequest httpRequest = new HttpRequest();
        //send get
        String s = httpRequest.sendGet("http://127.0.0.1:28080/log/pull","key=mknwef0z578l3qw4p5ru1brl42agvn49");
        System.out.println(s);
        //send post
        String s1 = httpRequest.sendPost("http://localhost:28080/log/push", "1751|@|21|@|20210302|@|204155|@|1|@|6|@|CTN20171215000016|@|20210302_204155|@|mknwef0z578l3qw4p5ru1brl42agvn49|@|0002|@|2|@|1|@|0|@|031010|@|3|@|1|@|0000149|@|1|@||@|2|@||@|1|@||@|3|@|0000149|@|1|@|");
        System.out.println(s1);
    }
}