package com.example.minishop.dto.response;

public class UploadImageResponse {

    private String url;
    private String publicId;
    private String format;
    private Long size;

    public UploadImageResponse() {
    }

    public UploadImageResponse(
            String url,
            String publicId,
            String format,
            Long size
    ) {
        this.url = url;
        this.publicId = publicId;
        this.format = format;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}