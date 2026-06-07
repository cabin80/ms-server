package com.ms.admin.service;

import com.ms.admin.config.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class CosService {

    private static final Logger log = LoggerFactory.getLogger(CosService.class);

    private final CosConfig cosConfig;
    private final COSClient cosClient;

    public CosService(CosConfig cosConfig) {
        this.cosConfig = cosConfig;
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        this.cosClient = new COSClient(cred, clientConfig);
    }

    /**
     * 上传文件到 COS
     * @param file 上传的文件
     * @param prefix 路径前缀，如 "covers" / "songs"
     * @return 可公开访问的 URL
     */
    public String upload(MultipartFile file, String prefix) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String key = prefix + "/" + dateDir + "/" + fileName;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(file.getContentType());

        try (InputStream is = file.getInputStream()) {
            PutObjectRequest request = new PutObjectRequest(cosConfig.getBucket(), key, is, meta);
            cosClient.putObject(request);
        }

        String url = cosConfig.getBaseUrl() + "/" + key;
        log.info("COS 上传成功: {}", url);
        return url;
    }

    public String uploadSong(MultipartFile file) throws IOException {
        return upload(file, "songs");
    }

    public String uploadCover(MultipartFile file) throws IOException {
        return upload(file, "covers");
    }
}
