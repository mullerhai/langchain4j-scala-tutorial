ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "3.6.3"

lazy val root = (project in file("."))
  .settings(
    name := "langchain4j-scala-tutorial"
  )

// https://mvnrepository.com/artifact/com.agentsflex/agents-flex-llm-vllm
libraryDependencies += "com.agentsflex" % "agents-flex-llm-vllm" % "1.4.1"
// https://mvnrepository.com/artifact/com.openai/openai-java
libraryDependencies += "com.openai" % "openai-java" % "4.7.1"
// https://mvnrepository.com/artifact/com.softwaremill.sttp.ai/openai
libraryDependencies += "com.softwaremill.sttp.ai" %% "openai" % "0.4.1"
// https://mvnrepository.com/artifact/io.cequence/openai-scala-core
libraryDependencies += "io.cequence" %% "openai-scala-core" % "1.3.0.RC.1"
// https://mvnrepository.com/artifact/io.cequence/openai-scala-client
libraryDependencies += "io.cequence" %% "openai-scala-client" % "1.3.0.RC.1"
// https://mvnrepository.com/artifact/de.dfki.mary/marytts-common
libraryDependencies += "de.dfki.mary" % "marytts-common" % "5.2.1"
// https://mvnrepository.com/artifact/de.dfki.mary/marytts-runtime
libraryDependencies += "de.dfki.mary" % "marytts-runtime" % "5.2.1"
libraryDependencies += "com.langfuse" % "langfuse-java" % "0.1.0"
// https://mvnrepository.com/artifact/im.arun/toon4j
libraryDependencies += "im.arun" % "toon4j" % "1.1.0"
// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "2.4.240"
// https://mvnrepository.com/artifact/org.postgresql/postgresql
//libraryDependencies += "org.postgresql" % "postgresql" % "42.7.8"
//libraryDependencies += "io.toonformat" %% "toon4s-core" % "0.1.0"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-hugging-face
libraryDependencies += "dev.langchain4j" % "langchain4j-hugging-face" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-embeddings
libraryDependencies += "dev.langchain4j" % "langchain4j-embeddings" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-document-parser-apache-tika
libraryDependencies += "dev.langchain4j" % "langchain4j-document-parser-apache-tika" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-neo4j
libraryDependencies += "dev.langchain4j" % "langchain4j-community-neo4j" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-easy-rag
libraryDependencies += "dev.langchain4j" % "langchain4j-easy-rag" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-agentic
libraryDependencies += "dev.langchain4j" % "langchain4j-agentic" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-kotlin
libraryDependencies += "dev.langchain4j" % "langchain4j-kotlin" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-xinference
libraryDependencies += "dev.langchain4j" % "langchain4j-community-xinference" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-document-parser-apache-pdfbox
libraryDependencies += "dev.langchain4j" % "langchain4j-document-parser-apache-pdfbox" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-neo4j
libraryDependencies += "dev.langchain4j" % "langchain4j-neo4j" % "1.0.0-beta2"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-dashscope
libraryDependencies += "dev.langchain4j" % "langchain4j-community-dashscope" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-opensearch
libraryDependencies += "dev.langchain4j" % "langchain4j-opensearch" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-http-client-spring-restclient
libraryDependencies += "dev.langchain4j" % "langchain4j-http-client-spring-restclient" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-core
libraryDependencies += "dev.langchain4j" % "langchain4j-community-core" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-document-parser-apache-poi
libraryDependencies += "dev.langchain4j" % "langchain4j-document-parser-apache-poi" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-redis
libraryDependencies += "dev.langchain4j" % "langchain4j-community-redis" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-mongodb-atlas
libraryDependencies += "dev.langchain4j" % "langchain4j-mongodb-atlas" % "1.8.0-beta15" //% "runtime"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-jlama
libraryDependencies += "dev.langchain4j" % "langchain4j-jlama" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-onnx-scoring
libraryDependencies += "dev.langchain4j" % "langchain4j-onnx-scoring" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-llm-graph-transformer
libraryDependencies += "dev.langchain4j" % "langchain4j-community-llm-graph-transformer" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-mcp-docker
libraryDependencies += "dev.langchain4j" % "langchain4j-mcp-docker" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-duckdb
libraryDependencies += "dev.langchain4j" % "langchain4j-community-duckdb" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-document-parser-yaml
libraryDependencies += "dev.langchain4j" % "langchain4j-document-parser-yaml" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-document-parser-llamaparse
libraryDependencies += "dev.langchain4j" % "langchain4j-community-document-parser-llamaparse" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-xinference-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-community-xinference-spring-boot-starter" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-mistral-ai-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-mistral-ai-spring-boot-starter" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-memfile
libraryDependencies += "dev.langchain4j" % "langchain4j-community-memfile" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-clickhouse-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-community-clickhouse-spring-boot-starter" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-vearch-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-community-vearch-spring-boot-starter" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-ollama-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-ollama-spring-boot-starter" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-code-execution-engine-graalvm-polyglot
////libraryDependencies += "dev.langchain4j" % "langchain4j-code-execution-engine-graalvm-polyglot" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-milvus-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-milvus-spring-boot-starter" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-document-loader-github
libraryDependencies += "dev.langchain4j" % "langchain4j-document-loader-github" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-elasticsearch
libraryDependencies += "dev.langchain4j" % "langchain4j-elasticsearch" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-yugabytedb
//libraryDependencies += "dev.langchain4j" % "langchain4j-community-yugabytedb" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-neo4j-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-community-neo4j-spring-boot-starter" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-embeddings-all-minilm-l6-v2
libraryDependencies += "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2" % "1.8.0-beta15"
//langchain4j-vespa  langchain4j-weaviate
//langchain4j-voyage-ai
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-voyage-ai
libraryDependencies += "dev.langchain4j" % "langchain4j-voyage-ai" % "1.8.0-beta15"
//langchain4j-reactor
libraryDependencies += "dev.langchain4j" % "langchain4j-reactor" % "1.8.0-beta15"
//langchain4j-open-ai-spring-boot-starter
//libraryDependencies += "dev.langchain4j" % "langchain4j-open-ai-spring-boot-starter" % "1.8.0"
//langchain4j-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-spring-boot-starter" % "1.8.0-beta15"
//langchain4j-experimental-sql
libraryDependencies += "dev.langchain4j" % "langchain4j-experimental-sql" % "1.8.0-beta15"
//langchain4j-embeddings-bge-small-en-v15-q
libraryDependencies += "dev.langchain4j" % "langchain4j-embeddings-bge-small-en-v15-q" % "1.8.0-beta15"
//langchain4j-embedding-store-filter-parser-sql
libraryDependencies += "dev.langchain4j" % "langchain4j-embedding-store-filter-parser-sql" % "1.8.0-beta15"
//langchain4j-web-search-engine-tavily
libraryDependencies += "dev.langchain4j" % "langchain4j-web-search-engine-tavily" % "1.8.0-beta15"
//langchain4j-cohere
libraryDependencies += "dev.langchain4j" % "langchain4j-cohere" % "1.8.0-beta15"



libraryDependencies += "dev.langchain4j" % "langchain4j-vespa" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-weaviate
libraryDependencies += "dev.langchain4j" % "langchain4j-weaviate" % "1.8.0-beta15"
//// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-vertex-ai-gemini-spring-boot-starter
libraryDependencies += "dev.langchain4j" % "langchain4j-vertex-ai-gemini-spring-boot-starter" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-document-parser-markdown
libraryDependencies += "dev.langchain4j" % "langchain4j-document-parser-markdown" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-community-neo4j-retriever
libraryDependencies += "dev.langchain4j" % "langchain4j-community-neo4j-retriever" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-oracle
libraryDependencies += "dev.langchain4j" % "langchain4j-oracle" % "1.8.0-beta15"
//langchain4j-infinispan langchain4j-gpu-llama3 agentic-tutorial
//libraryDependencies += "dev.langchain4j" % "agentic-tutorial" % "1.8.0-beta15"
//langchain4j-mistral-ai
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-mistral-ai
libraryDependencies += "dev.langchain4j" % "langchain4j-mistral-ai" % "1.8.0"
//langchain4j-gpu-llama3
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-anthropic
libraryDependencies += "dev.langchain4j" % "langchain4j-anthropic" % "1.8.0"
// https://mvnrepository.com/artifact/org.mapdb/mapdb
libraryDependencies += "org.mapdb" % "mapdb" % "3.1.0" exclude ("androidx.core", "core-ktx")//exclude ("org.mapdb", "mapdb")
// https://mvnrepository.com/artifact/com.github.jenly1314/kvcache
//libraryDependencies += "com.github.jenly1314" % "kvcache" % "1.3.0" exclude ("androidx.core", "core-ktx")
// https://mvnrepository.com/artifact/io.kcache/kcache-mapdb
//libraryDependencies += "io.kcache" % "kcache-mapdb" % "5.2.3" exclude ("androidx.core", "core-ktx")
//langchain4j-code-execution-engine-judge0

//opencsv
libraryDependencies += "com.opencsv" % "opencsv" % "5.9"
// https://mvnrepository.com/artifact/com.dimafeng/testcontainers-scala-redis
//libraryDependencies += "com.dimafeng" %% "testcontainers-scala-redis" % "0.43.6"
// https://mvnrepository.com/artifact/org.mongodb.scala/mongo-scala-driver
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "5.7.0-alpha0" cross CrossVersion.for3Use2_13
libraryDependencies += "dev.langchain4j" % "langchain4j-code-execution-engine-judge0" % "1.8.0-beta15"

libraryDependencies += "dev.langchain4j" % "langchain4j-gpu-llama3" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-redis
libraryDependencies += "dev.langchain4j" % "langchain4j-redis" % "1.0.0-alpha1"
// https://mvnrepository.com/artifact/org.infinispan/infinispan-server-testdriver-core
//libraryDependencies += "org.infinispan" % "infinispan-server-testdriver-core" % "16.0.1"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-infinispan
libraryDependencies += "dev.langchain4j" % "langchain4j-infinispan" % "1.8.0-beta15"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j
libraryDependencies += "dev.langchain4j" % "langchain4j" % "1.8.0"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-open-ai
libraryDependencies += "dev.langchain4j" % "langchain4j-core" % "1.8.0"
// https://mvnrepository.com/artifact/dev.langchain4j/langchain4j-open-ai
libraryDependencies += "dev.langchain4j" % "langchain4j-open-ai" % "1.8.0"
// https://mvnrepository.com/artifact/com.oracle.database.jdbc/ucp
libraryDependencies += "com.oracle.database.jdbc" % "ucp" % "23.26.0.0.0"
// https://mvnrepository.com/artifact/org.openjfx/javafx-controls
libraryDependencies += "org.openjfx" % "javafx-controls" % "24.0.1"
// https://mvnrepository.com/artifact/org.testcontainers/testcontainers-oracle-free
libraryDependencies += "org.testcontainers" % "testcontainers-oracle-free" % "2.0.1"
// https://mvnrepository.com/artifact/org.assertj/assertj-core
libraryDependencies += "org.assertj" % "assertj-core" % "4.0.0-M1" //% Test
libraryDependencies += "org.pytorch" % "executorch-android" % "1.0.0" exclude ("androidx.core", "core-ktx")
// https://mvnrepository.com/artifact/org.pytorch/executorch-android-qnn
//libraryDependencies += "org.pytorch" % "executorch-android-qnn" % "1.0.0"
// https://mvnrepository.com/artifact/androidx.core/core-ktx
//libraryDependencies += "androidx.core" % "core-ktx" % "1.17.0" % "runtime"