package com.scottw;

import com.google.common.util.concurrent.RateLimiter;
import com.wrapper.spotify.SpotifyApi;
import java.net.URI;

public class RateLimitedSpotifyApi {
  private static final RateLimiter rateLimiter = RateLimiter.create(5);
  private final SpotifyApi spotifyApi;

  public RateLimitedSpotifyApi(String accessToken) {
    this.spotifyApi =
      new SpotifyApi.Builder().setAccessToken(accessToken).build();
  }

  public RateLimitedSpotifyApi(
    String clientId,
    String clientSecret,
    URI redirectUri
  ) {
    this.spotifyApi =
      new SpotifyApi.Builder()
        .setClientId(clientId)
        .setClientSecret(clientSecret)
        .setRedirectUri(redirectUri)
        .build();
  }

  public SpotifyApi fetch() {
    rateLimiter.acquire();
    return spotifyApi;
  }
}
