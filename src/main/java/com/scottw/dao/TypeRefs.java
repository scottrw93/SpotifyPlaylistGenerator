package com.scottw.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.scottw.core.wall.Hold;
import java.util.List;

public class TypeRefs {
  public static final TypeReference<List<Hold>> HOLDS = new TypeReference<>() {};

  private TypeRefs() {}
}
