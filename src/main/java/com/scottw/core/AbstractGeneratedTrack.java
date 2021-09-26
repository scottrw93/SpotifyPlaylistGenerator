package com.scottw.core;

import org.immutables.value.Value;

@OurStyle
@Value.Immutable
public abstract class AbstractGeneratedTrack {

  public abstract String getName();

  public abstract GeneratedArtist getArtist();
}
