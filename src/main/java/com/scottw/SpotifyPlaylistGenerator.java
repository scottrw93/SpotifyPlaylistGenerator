package com.scottw;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.scottw.core.GeneratedArtist;
import com.scottw.core.GeneratedPlaylist;
import com.scottw.core.GeneratedTrack;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.BadGatewayException;
import com.wrapper.spotify.model_objects.specification.*;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SpotifyPlaylistGenerator {
  private static final int LIMIT = 10;

  private final Retryer<Void> retryer;
  private final RateLimitedSpotifyApi spotifyApi;
  private final String userId;

  public SpotifyPlaylistGenerator(String userId, String accessToken) {
    this.retryer =
      RetryerBuilder
        .<Void>newBuilder()
        .retryIfExceptionOfType(BadGatewayException.class)
        .withStopStrategy(StopStrategies.stopAfterAttempt(3))
        .build();
    this.userId = userId;
    this.spotifyApi = new RateLimitedSpotifyApi(accessToken);
  }

  public GeneratedPlaylist generateNewPlaylist(
    String newPlaylistName,
    Optional<String> playlistId
  )
    throws ParseException, SpotifyWebApiException, IOException, ExecutionException, RetryException {
    List<Track> tracksOnCurrentPlaylist = playlistId.isPresent()
      ? getPlaylistTracks(playlistId.orElseThrow())
      : getLikedTracks();

    return generateNewPlaylist(newPlaylistName, tracksOnCurrentPlaylist);
  }

  private GeneratedPlaylist generateNewPlaylist(
    String newPlaylistName,
    List<Track> tracksOnCurrentPlaylist
  )
    throws ParseException, SpotifyWebApiException, IOException, ExecutionException, RetryException {
    List<Track> tracksOnNewPlaylist = new ArrayList<>();

    for (Track track : tracksOnCurrentPlaylist) {
      Optional<Track> toAdd = getNextMostPopularTrackOnAlbum(
        track,
        ImmutableSet
          .<String>builder()
          .addAll(
            tracksOnCurrentPlaylist
              .stream()
              .map(Track::getId)
              .collect(Collectors.toSet())
          )
          .addAll(
            tracksOnNewPlaylist
              .stream()
              .map(Track::getId)
              .collect(Collectors.toSet())
          )
          .build()
      );

      if (toAdd.isPresent()) {
        tracksOnNewPlaylist.add(toAdd.orElseThrow());
      }
    }

    return createPlaylist(newPlaylistName, tracksOnNewPlaylist);
  }

  private GeneratedPlaylist createPlaylist(String name, List<Track> tracks)
    throws ParseException, SpotifyWebApiException, IOException, ExecutionException, RetryException {
    Playlist createdPlaylist = spotifyApi
      .fetch()
      .createPlaylist(userId, name)
      .build()
      .execute();

    GeneratedPlaylist.Builder generatedPlaylistBuilder = GeneratedPlaylist
      .builder()
      .setName(name)
      .setHref(
        createdPlaylist
          .getExternalUrls()
          .getExternalUrls()
          .values()
          .iterator()
          .next()
      );

    for (List<Track> chunk : Lists.partition(tracks, LIMIT)) {
      retryer.call(
        () -> {
          spotifyApi
            .fetch()
            .addItemsToPlaylist(
              createdPlaylist.getId(),
              chunk
                .stream()
                .map(Track::getUri)
                .collect(Collectors.toList())
                .toArray(new String[0])
            )
            .build()
            .execute();

          return null;
        }
      );

      generatedPlaylistBuilder.addAllTracks(
        chunk
          .stream()
          .map(
            track ->
              GeneratedTrack
                .builder()
                .setArtist(
                  GeneratedArtist
                    .builder()
                    .setName(track.getArtists()[0].getName())
                    .build()
                )
                .setName(track.getName())
                .build()
          )
          .collect(Collectors.toSet())
      );
    }

    return generatedPlaylistBuilder.build();
  }

  private Optional<Track> getNextMostPopularTrackOnAlbum(
    Track track,
    Set<String> tracksToExclude
  )
    throws ParseException, SpotifyWebApiException, IOException {
    Optional<String> albumId = Optional.ofNullable(track.getAlbum().getId());

    if (albumId.isPresent()) {
      List<Track> tracks = new ArrayList<>();

      int offset = 0;

      while (true) {
        Paging<TrackSimplified> album = spotifyApi
          .fetch()
          .getAlbumsTracks(albumId.orElseThrow())
          .limit(LIMIT)
          .offset(offset)
          .build()
          .execute();

        String[] idsToFetch = Arrays
          .stream(album.getItems())
          .map(TrackSimplified::getId)
          .filter(trackId -> !trackId.equals(track.getId()))
          .filter(trackId -> !tracksToExclude.contains(trackId))
          .collect(Collectors.toList())
          .toArray(new String[0]);

        if (idsToFetch.length == 0) {
          break;
        }

        tracks.addAll(
          Arrays
            .stream(
              spotifyApi.fetch().getSeveralTracks(idsToFetch).build().execute()
            )
            .collect(Collectors.toList())
        );

        if (album.getItems().length < LIMIT) {
          break;
        } else {
          offset += LIMIT;
        }
      }

      return tracks.stream().max(Comparator.comparing(Track::getPopularity));
    }

    return Optional.empty();
  }

  private List<Track> getLikedTracks()
    throws ParseException, SpotifyWebApiException, IOException {
    ImmutableList.Builder<Track> tracksOnPlaylist = ImmutableList.builder();
    int offset = 0;

    while (true) {
      Paging<SavedTrack> tracks = spotifyApi
        .fetch()
        .getUsersSavedTracks()
        .limit(LIMIT)
        .offset(offset)
        .build()
        .execute();

      for (SavedTrack track : tracks.getItems()) {
        tracksOnPlaylist.add(track.getTrack());
      }

      if (tracks.getItems().length < LIMIT) {
        break;
      } else {
        offset += LIMIT;
      }
    }

    return tracksOnPlaylist.build();
  }

  private List<Track> getPlaylistTracks(String playlistId)
    throws ParseException, SpotifyWebApiException, IOException {
    ImmutableList.Builder<Track> tracksOnPlaylist = ImmutableList.builder();
    int offset = 0;

    while (true) {
      Paging<PlaylistTrack> tracks = spotifyApi
        .fetch()
        .getPlaylistsItems(playlistId)
        .limit(LIMIT)
        .offset(offset)
        .build()
        .execute();

      for (PlaylistTrack track : tracks.getItems()) {
        tracksOnPlaylist.add((Track) track.getTrack());
      }

      if (tracks.getItems().length < LIMIT) {
        break;
      } else {
        offset += LIMIT;
      }
    }

    return tracksOnPlaylist.build();
  }
}
