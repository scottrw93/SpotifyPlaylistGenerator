package com.scottw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.scottw.core.GeneratePlaylistPayload;
import com.scottw.core.GeneratedPlaylist;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SpotifyPlaylistRequestDispatcher implements HttpFunction {

  static {
    System.setProperty("GOOGLE_CLOUD_PROJECT", "heroic-passkey-326916");
    ///Users/scott/projects/wall/homewall-301021-13d254779b63.json
  }

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void service(HttpRequest request, HttpResponse response)
    throws Exception {
    response.appendHeader("Access-Control-Allow-Origin", "*");
    response.appendHeader("Content-Type", "application/json");

    String method = request.getMethod();
    switch (method) {
      case "OPTIONS":
        handleOptions(request, response);
        break;
      case "POST":
        handlePost(request, response);
        break;
      default:
        response.setStatusCode(405);
        break;
    }
  }

  private void handlePost(HttpRequest request, HttpResponse response)
    throws IOException {
    switch (request.getPath()) {
      case "/generate":
        GeneratePlaylistPayload generatePlaylistPayload = objectMapper.readValue(
          request.getInputStream(),
          GeneratePlaylistPayload.class
        );
        try {
          GeneratedPlaylist playlistGenerated = new SpotifyPlaylistGenerator(
            generatePlaylistPayload.getUserId(),
            generatePlaylistPayload.getAccessToken()
          )
          .generateNewPlaylist(
              generatePlaylistPayload.getName(),
              generatePlaylistPayload.getPlaylistId()
            );
          response
            .getWriter()
            .write(objectMapper.writeValueAsString(playlistGenerated));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        break;
      default:
        response.setStatusCode(404);
    }
  }

  private void handleOptions(HttpRequest request, HttpResponse response) {
    response.appendHeader(
      "Access-Control-Allow-Methods",
      "POST, PUT, GET, OPTIONS, DELETE"
    );
    response.appendHeader("Access-Control-Allow-Headers", "Content-Type");
    response.appendHeader("Access-Control-Max-Age", "3600");
    response.setStatusCode(HttpURLConnection.HTTP_NO_CONTENT);
  }
}
