package com.scottw;

import com.google.common.util.concurrent.RateLimiter;
import com.wrapper.spotify.SpotifyApi;

public class RateLimitedSpotifyApi {
  private static final RateLimiter rateLimiter = RateLimiter.create(5);
  private final SpotifyApi spotifyApi;

  public RateLimitedSpotifyApi(String accessToken) {
    this.spotifyApi =
      new SpotifyApi.Builder().setAccessToken(accessToken).build();
  }

  public SpotifyApi fetch() {
    rateLimiter.acquire();
    return spotifyApi;
  }
}
