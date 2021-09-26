package com.scottw.core;

import java.util.Optional;
import org.immutables.value.Value;

@OurStyle
@Value.Immutable
public abstract class AbstractGeneratePlaylistPayload {

  public abstract String getName();

  public abstract String getUserId();

  public abstract Optional<String> getPlaylistId();

  public abstract String getAccessToken();
}
