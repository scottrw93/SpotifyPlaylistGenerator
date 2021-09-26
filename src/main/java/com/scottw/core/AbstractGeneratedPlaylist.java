package com.scottw.core;

import org.immutables.value.Value;

import java.util.List;

@OurStyle
@Value.Immutable
public abstract class AbstractGeneratedPlaylist {

  public abstract String getName();

  public abstract String getHref();

  public abstract List<GeneratedTrack> getTracks();
}
