package com.scottw.io;

import com.google.cloud.storage.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class UploadWallImage {

  public static String uploadImage(InputStream inputStream, String contentType)
    throws IOException {
    Storage storage = StorageOptions.newBuilder().build().getService();

    BlobId blobId = BlobId.of(
      "home-wall-images",
      UUID.randomUUID().toString() + "." + contentType.split("/")[1]
    );

    BlobInfo blobInfo = BlobInfo
      .newBuilder(blobId)
      .setCacheControl("max-age=3122064000")
      .setContentType(contentType)
      .build();

    Blob blob = storage.createFrom(blobInfo, inputStream);
    return (
      "https://storage.googleapis.com/" +
      blob.getBucket() +
      "/" +
      blob.getName()
    );
  }
}
