package com.ms.server.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cos")
public class CosConfig {
    private static final Logger log = LoggerFactory.getLogger(CosConfig.class);

    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;
    private String baseUrl;

    @PostConstruct
    public void debug() {
        log.info("========== COS Config Debug ==========");
        log.info("secretId='{}'", secretId);
        log.info("secretKey='{}'", secretKey != null ? "***" + secretKey.substring(Math.max(0, secretKey.length()-4)) : "null");
        log.info("region='{}'", region);
        log.info("bucket='{}'", bucket);
        log.info("baseUrl='{}'", baseUrl);
        log.info("======================================");
    }

    public String getSecretId() { return secretId; }
    public void setSecretId(String secretId) { this.secretId = secretId; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}
