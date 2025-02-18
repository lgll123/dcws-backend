package com.formssi.common.minio.util;

import com.formssi.common.minio.dto.FileInfo;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    /**
     * 创建一个桶
     */
    public void createBucket(String bucket) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * 上传一个文件
     */
    public void uploadFile(InputStream stream, String bucket, String objectName) throws Exception {
        minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName)
                .stream(stream, -1, 10485760).build());
    }

    /**
     * 列出所有的桶
     */
    public List<String> listBuckets() throws Exception {
        List<Bucket> list = minioClient.listBuckets();
        List<String> names = new ArrayList<>();
        list.forEach(b -> {
            names.add(b.name());
        });
        return names;
    }

    /**
     * 列出一个桶中的所有文件和目录
     */
    public List<FileInfo> listFiles(String bucket) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucket).recursive(true).build());

        List<FileInfo> infos = new ArrayList<>();
        results.forEach(r -> {
            FileInfo info = new FileInfo();
            try {
                Item item = r.get();
                info.setFileName(item.objectName());
                info.setDirectory(item.isDir());
                infos.add(info);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return infos;
    }

    /**
     * 下载一个文件
     */
    public InputStream download(String bucket, String objectName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build());
        return stream;
    }

    /**
     * 删除一个桶
     */
    public void deleteBucket(String bucket) throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
    }

    /**
     * 删除一个对象
     */
    public void deleteObject(String bucket, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
    }

    /**
     * 复制文件
     *
     * @Param: [sourceBucket, sourceObject, targetBucket, targetObject]
     */
    public void copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) throws Exception {
        this.createBucket(targetBucket);
        minioClient.copyObject(CopyObjectArgs.builder().bucket(targetBucket).object(targetObject)
                .source(CopySource.builder().bucket(sourceBucket).object(sourceObject).build()).build());
    }

    /**
     * 获取文件信息
     *
     * @Param: [bucket, objectName]
     */
    public String getObjectInfo(String bucket, String objectName) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build()).toString();
    }

    /**
     * 生成一个给HTTP GET请求用的Preview URL。浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。
     *
     * @Param: [bucketName, objectName, expires]
     */
    public String getEffectiveTimePreviewUrl(String bucketName, String objectName, Integer expires) throws Exception {
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder().bucket(bucketName).object(objectName).expiry(expires).method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(build);
    }

    /**
     * 生成一个给HTTP GET请求用的Preview URL。浏览器/移动端的客户端可以用这个URL进行下载，即使其所在的存储桶是私有的。
     * @param bucketName 桶名称
     * @param objectName 文件夹名称
     * @return 返回URL
     * @throws Exception 异常
     */
    public String getPermanentTimePreviewUrl(String bucketName, String objectName) throws Exception {
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder().bucket(bucketName).object(objectName).method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(build);
    }

    /**
     * 获取minio中所有的文件
     *
     * @Param: []
     */
    public List<FileInfo> listAllFile() throws Exception {
        List<String> list = this.listBuckets();
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (String bucketName : list) {
            fileInfoList.addAll(this.listFiles(bucketName));
        }
        return fileInfoList;
    }

}
