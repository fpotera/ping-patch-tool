package io.bluzy.pingidentity.tools.patch.config;

import io.bluzy.pingidentity.tools.patch.auth.Authentication;
import io.bluzy.pingidentity.tools.patch.auth.BasicAuthentication;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RestAPIClient {
    private static final Logger logger = LoggerFactory.getLogger(RestAPIClient.class);

    private final TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }
    };

    private final OkHttpClient client;

    private final String baseUrl;

    private String credential;

    public RestAPIClient(String baseUrl, Authentication auth) throws Exception {
        this.baseUrl = baseUrl;
        BasicAuthentication basicAuth = (BasicAuthentication) auth;
        credential = Credentials.basic(basicAuth.getUser(), basicAuth.getPassword());

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());

        client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) TRUST_ALL_CERTS)
                .hostnameVerifier((hostname, session) -> true)
                .authenticator((route, response) -> {
                    return response.request().newBuilder().build();
                })
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String getConfigJSON(String path) throws IOException {
        logger.info("access url: {}", baseUrl+path);

        Request request = new Request.Builder()
                .url(baseUrl+path)
                .get()
                .addHeader("X-XSRF-Header", "PingFederate")
                .addHeader(Params.AUTHORIZATION_HEADER, credential)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String putConfigJSON(String path, String json) {
        logger.info("access url: {}", baseUrl+path);

        Request request = new Request.Builder()
                .url(baseUrl+path)
                .put(RequestBody.create(json, MediaType.get("application/json")))
                .addHeader("X-XSRF-Header", "PingFederate")
                .addHeader(Params.AUTHORIZATION_HEADER, credential)
                .build();

        try {
            Response response = client.newCall(request).execute();
            try (response) {
                return response.body().string();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


