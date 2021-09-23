package com.scottw.core.wall;

import com.scottw.core.OurStyle;
import org.immutables.value.Value;

@OurStyle
@Value.Immutable
public abstract class AbstractPoint {

  public abstract int getX();

  public abstract int getY();
}
