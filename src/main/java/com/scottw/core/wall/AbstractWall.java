package com.scottw.core.wall;

import com.scottw.core.OurStyle;
import java.util.UUID;
import org.immutables.value.Value;

@OurStyle
@Value.Immutable
public abstract class AbstractWall extends AbstractWallRequest {

  public abstract UUID getUuid();
}
