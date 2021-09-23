package com.scottw.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.datastore.*;
import com.google.common.collect.ImmutableList;
import com.scottw.core.wall.Hold;
import com.scottw.core.wall.Point;
import com.scottw.core.wall.Wall;
import com.scottw.core.wall.WallRequest;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class WallsDao {
  private final Datastore datastore;
  private final ObjectMapper objectMapper;

  public WallsDao() {
    this.datastore = DatastoreFactory.fetch();
    this.objectMapper = new ObjectMapper();
  }

  public Wall createWall(WallRequest wallRequest) {
    UUID uuid = UUID.randomUUID();
    Key taskKey = datastore
      .newKeyFactory()
      .setKind("Wall")
      .newKey(uuid.toString());

    Entity.Builder builder = Entity
      .newBuilder(taskKey)
      .set("image", wallRequest.getImage().toString())
      .set("name", wallRequest.getName())
      .set("createdAt", Instant.now().toEpochMilli());

    for (int i = 0; i < wallRequest.getHolds().size(); i++) {
      try {
        builder.set(
          "hold" + i,
          Blob.copyFrom(
            objectMapper.writeValueAsBytes(wallRequest.getHolds().get(i))
          )
        );
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    datastore.put(builder.build());

    return Wall.builder().from(wallRequest).setUuid(uuid).build();
  }

  public Wall createWall(Wall wallRequest) {
    Key taskKey = datastore
      .newKeyFactory()
      .setKind("Wall")
      .newKey(wallRequest.getUuid().toString());

    Entity.Builder builder = Entity
      .newBuilder(taskKey)
      .set("image", wallRequest.getImage().toString())
      .set("name", wallRequest.getName())
      .set("createdAt", Instant.now().toEpochMilli());

    for (int i = 0; i < wallRequest.getHolds().size(); i++) {
      try {
        builder.set(
          "hold" + i,
          Blob.copyFrom(
            objectMapper.writeValueAsBytes(wallRequest.getHolds().get(i))
          )
        );
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    datastore.put(builder.build());

    return wallRequest;
  }

  public List<Wall> getWalls() {
    QueryResults<Entity> results = datastore.run(
      Query
        .newEntityQueryBuilder()
        .setLimit(100)
        .setOffset(0)
        .setKind("Wall")
        .addOrderBy(StructuredQuery.OrderBy.desc("name"))
        .build()
    );

    ArrayList<Wall> walls = new ArrayList<>();
    while (results.hasNext()) {
      Entity entity = results.next();
      Wall x = Wall
        .builder()
        .setUuid(UUID.fromString(entity.getKey().getName()))
        .setImage(URI.create(entity.getString("image")))
        .setHolds(extractHolds(entity))
        .setName(entity.getString("name"))
        .build();
      walls.add(
        Wall
          .builder()
          .setUuid(UUID.fromString(entity.getKey().getName()))
          .setImage(URI.create(entity.getString("image")))
          .setHolds(extractHolds(entity))
          .setName(entity.getString("name"))
          .build()
      );

      if (x.getName().contains("Scott")) {
        createWall(
          x.withHolds(
            ImmutableList
              .<Hold>builder()
              .addAll(x.getHolds())
              .add(
                Hold
                  .builder()
                  .addPoints(Point.builder().setX(373).setY(214).build())
                  .addPoints(Point.builder().setX(358).setY(234).build())
                  .addPoints(Point.builder().setX(369).setY(240).build())
                  .addPoints(Point.builder().setX(385).setY(219).build())
                  .build()
              )
              .add(
                Hold
                  .builder()
                  .addPoints(Point.builder().setX(504).setY(217).build())
                  .addPoints(Point.builder().setX(504).setY(238).build())
                  .addPoints(Point.builder().setX(519).setY(238).build())
                  .addPoints(Point.builder().setX(517).setY(214).build())
                  .build()
              )
              .build()
          )
        );
      }
    }
    return List.copyOf(walls);
  }

  public Optional<Wall> getWall(UUID uuid) {
    Key taskKey = datastore
      .newKeyFactory()
      .setKind("Wall")
      .newKey(uuid.toString());

    Optional<Entity> maybeEntity = Optional.ofNullable(datastore.get(taskKey));

    return maybeEntity.map(
      entity ->
        Wall
          .builder()
          .setUuid(UUID.fromString(entity.getKey().getName()))
          .setImage(URI.create(entity.getString("image")))
          .setHolds(extractHolds(entity))
          .setName(entity.getString("name"))
          .build()
    );
  }

  private Iterable<Hold> extractHolds(Entity entity) {
    return entity
      .getNames()
      .stream()
      .filter(name -> name.startsWith("hold"))
      .map(
        name -> {
          try {
            Hold hold = objectMapper.readValue(
              entity.getBlob(name).asInputStream(),
              Hold.class
            );
            return hold;
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      )
      .collect(Collectors.toList());
  }
}
