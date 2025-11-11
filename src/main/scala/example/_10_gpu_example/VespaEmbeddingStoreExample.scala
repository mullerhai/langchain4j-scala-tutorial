package example._10_gpu_example

import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.store.embedding.vespa.VespaEmbeddingStore
import dev.langchain4j.store.embedding.{EmbeddingMatch, EmbeddingSearchRequest, EmbeddingStore}

import java.util
import java.util.Arrays.asList

/**
 * Example of integration with Vespa. You need to configure Vespa server side first, instructions are
 * inside of README.md file.
 */
object VespaEmbeddingStoreExample {
  def main(args: Array[String]): Unit = {
    val embeddingStore = VespaEmbeddingStore.builder.url("url").keyPath("keyPath").certPath("certPath").build
    val embeddingModel = new AllMiniLmL6V2EmbeddingModel
    val segment1 = TextSegment.from("I like football.")
    val embedding1 = embeddingModel.embed(segment1).content
    embeddingStore.add(embedding1, segment1)
    val segment2 = TextSegment.from("I've never been to New York.")
    val embedding2 = embeddingModel.embed(segment2).content
    embeddingStore.add(embedding2, segment2)
    val segment3 = TextSegment.from("But actually we tried our new swimming pool yesterday and it was awesome!")
    val embedding3 = embeddingModel.embed(segment3).content
    embeddingStore.add(embedding3, segment3)
    val ids = embeddingStore.addAll(asList(embedding1, embedding2, embedding3), asList(segment1, segment2, segment3))
    System.out.println("added/updated records count: " + ids.size) // 3
    val segment4 = TextSegment.from("John Lennon was a very cool person.")
    val embedding4 = embeddingModel.embed(segment4).content
    val s4id = embeddingStore.add(embedding4, segment4)
    System.out.println("segment 4 id: " + s4id)
    var queryEmbedding = embeddingModel.embed("What is your favorite sport?").content
    var embeddingSearchRequest = EmbeddingSearchRequest.builder.queryEmbedding(queryEmbedding).maxResults(2).build
    var matches = embeddingStore.search(embeddingSearchRequest).matches
    System.out.println("relevant results count for sport question: " + matches.size) // 2
    System.out.println(matches.get(0).score) // 0.639...
    System.out.println(matches.get(0).embedded.text) // football
    System.out.println(matches.get(1).score) // 0.232...
    System.out.println(matches.get(1).embedded.text) // swimming pool
    queryEmbedding = embeddingModel.embed("And what about musicians?").content
    embeddingSearchRequest = EmbeddingSearchRequest.builder.queryEmbedding(queryEmbedding).maxResults(5).minScore(0.3).build
    matches = embeddingStore.search(embeddingSearchRequest).matches
    System.out.println("relevant results count for music question: " + matches.size) // 1
    System.out.println(matches.get(0).score) // 0.359...
    System.out.println(matches.get(0).embedded.text) // John Lennon
  }
}