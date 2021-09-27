package com.scottw;

import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import java.io.IOException;
import java.net.URI;
import org.apache.hc.core5.http.ParseException;

public class SpotifyPlaylistAuth {
  private final RateLimitedSpotifyApi spotifyApi;

  private final String clientId = "a6c082a9f2404c55a2bca89eadf1cc1f";
  private final String clientSecret = "xxx"; // TODO: Figure out secrets in gcp functions
  private final URI redirectUri = SpotifyHttpManager.makeUri(
    "http://localhost:8888/callback"
  );

  public SpotifyPlaylistAuth()
    throws ParseException, SpotifyWebApiException, IOException {
    this.spotifyApi =
      new RateLimitedSpotifyApi(clientId, clientSecret, redirectUri);
  }

  public URI getAuthorizationCodeUri(String state) {
    return spotifyApi
      .fetch()
      .authorizationCodeUri()
      .state(state)
      .scope("user-library-read")
      .scope("playlist-modify-private")
      .show_dialog(true)
      .build()
      .execute();
  }

  public AuthorizationCodeCredentials getAccessToken(String code)
    throws ParseException, SpotifyWebApiException, IOException {
    return spotifyApi
      .fetch()
      .authorizationCode(
        "AQDAb5NmNi_FlZe8eD3SbHq2vYNSjMxX6jbanvqF7jznwxpAf05p1SKG6gB8_-BgUrTiw2hd2RE1poR5bOAMIGYgO4irnHWO-hkTlS5Ngnn5gFCI88wgjN6NzryyLzH8Pvu0EfiE66wFalpbEXx6EQZ_YtTN15hWZfhYPh06vn-zCgRrWqmb3TjNR5LyijiPsP4-sNfgmIZMtmw"
      )
      .build()
      .execute();
  }
}
