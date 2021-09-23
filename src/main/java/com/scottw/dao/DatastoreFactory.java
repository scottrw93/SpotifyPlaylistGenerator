package com.scottw.dao;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

public class DatastoreFactory {
  private static final Datastore datastore = DatastoreOptions
    .newBuilder()
    .build()
    .getService();

  private DatastoreFactory() {}

  protected static Datastore fetch() {
    return datastore;
  }
}
