package mongodb_movieagent

import dev.langchain4j.agentic.Agent
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.service.V
import dev.langchain4j.store.embedding.{EmbeddingSearchRequest, EmbeddingSearchResult}
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore

class MongoDbMovieSearchTool(private val embeddingModel: EmbeddingModel, private val store: MongoDbEmbeddingStore) {
  @Agent(value = "Search movies in MongoDB by semantic description", outputKey = "movieTitle") 
  def search(@V("query") query: String): String = try {
    val queryEmbedding = embeddingModel.embed(query).content
    val result = store.search(EmbeddingSearchRequest.builder.queryEmbedding(queryEmbedding).maxResults(1).build)
    if (!result.matches.isEmpty) {
      val doc = result.matches.getFirst.embedded
      System.out.println(doc.toString)
      return doc.metadata.getString("title")
    }
    "No matching movie found in MongoDB."
  } catch {
    case e: Exception =>
      "Error searching MongoDB: " + e.getMessage
  }
}