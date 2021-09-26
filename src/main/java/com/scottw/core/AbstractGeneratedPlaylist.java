package com.scottw.core;

import java.util.List;
import org.immutables.value.Value;

@OurStyle
@Value.Immutable
public abstract class AbstractGeneratedPlaylist {

  public abstract String getName();

  public abstract String getHref();

  public abstract List<GeneratedTrack> getTracks();
}
