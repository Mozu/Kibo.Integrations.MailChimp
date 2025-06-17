package com.mozu.mailchimp.service;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.ecwid.mailchimp.connection.MailChimpConnectionManager;
import com.mozu.api.utils.MozuHttpClientPool;

@Component
public class MozuConnectionManager implements MailChimpConnectionManager {

    @Override
    public void close() throws IOException {
    }

    @Override
    public String post(String url, String payload) throws IOException {
        String responseString = null;
        CloseableHttpResponse httpResponseMessage = null;
        
        CloseableHttpClient httpClient = MozuHttpClientPool.getInstance().getHttpClient();

        try {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(payload, "UTF-8"));
            httpResponseMessage = httpClient.execute(post);
            if (httpResponseMessage.getEntity() != null) {
                responseString = EntityUtils.toString(httpResponseMessage.getEntity(), "UTF-8").trim();
            }
        } finally {
            if (httpResponseMessage!=null) {
                EntityUtils.consume(httpResponseMessage.getEntity());
                httpResponseMessage.close();
            }
        }

        if (responseString==null) {
            throw new IOException(httpResponseMessage.getStatusLine().toString());
        }

        return responseString;
    }
}
