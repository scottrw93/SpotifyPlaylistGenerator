package com.scottw.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.datastore.*;
import com.scottw.core.problem.Problem;
import com.scottw.core.problem.ProblemRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProblemsDao {
  private final Datastore datastore;
  private final ObjectMapper objectMapper;

  public ProblemsDao() {
    this.datastore = DatastoreFactory.fetch();
    this.objectMapper = new ObjectMapper();
  }

  public Problem createProblem(ProblemRequest problemRequest) {
    Problem problem = Problem
      .builder()
      .from(problemRequest)
      .setCreatedAt(Instant.now().toEpochMilli())
      .setUuid(UUID.randomUUID())
      .build();

    Key taskKey = datastore
      .newKeyFactory()
      .setKind("Problem")
      .newKey(problem.getUuid().toString());

    try {
      datastore.put(
        Entity
          .newBuilder(taskKey)
          .set("uuid", problem.getUuid().toString())
          .set("wallUuid", problem.getWallUuid().toString())
          .set("name", problem.getName())
          .set(
            "holds",
            Blob.copyFrom(objectMapper.writeValueAsBytes(problem.getHolds()))
          )
          .set("author", problem.getAuthor())
          .set("grade", problem.getGrade())
          .set("createdAt", problem.getCreatedAt())
          .build()
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return problem;
  }

  public List<Problem> getProblems() {
    QueryResults<Entity> results = datastore.run(
      Query
        .newEntityQueryBuilder()
        .setLimit(100)
        .setOffset(0)
        .setKind("Problem")
        .addOrderBy(StructuredQuery.OrderBy.desc("createdAt"))
        .build()
    );

    ArrayList<Problem> problems = new ArrayList<>();
    while (results.hasNext()) {
      Entity entity = results.next();

      try {
        problems.add(
          Problem
            .builder()
            .setUuid(UUID.fromString(entity.getString("uuid")))
            .setWallUuid(UUID.fromString(entity.getString("wallUuid")))
            .setName(entity.getString("name"))
            .setAuthor(entity.getString("author"))
            .setGrade(entity.getString("grade"))
            .setCreatedAt(entity.getLong("createdAt"))
            .setHolds(
              objectMapper.readValue(
                entity.getBlob("holds").asInputStream(),
                TypeRefs.HOLDS
              )
            )
            .build()
        );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return List.copyOf(problems);
  }

  public void deleteProblem(UUID uuid) {
    Key taskKey = datastore
      .newKeyFactory()
      .setKind("Problem")
      .newKey(uuid.toString());
    datastore.delete(taskKey);
  }
}
