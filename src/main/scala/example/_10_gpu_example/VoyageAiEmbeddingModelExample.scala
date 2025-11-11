package example._10_gpu_example

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.voyageai.{VoyageAiEmbeddingModel, VoyageAiEmbeddingModelName}
import org.junit.jupiter.api.Test

import java.time.Duration
import java.util
import java.util.Arrays.asList

class VoyageAiEmbeddingModelExample {
  @Test def should_embed_single_text(): Unit = {
    val model = VoyageAiEmbeddingModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(VoyageAiEmbeddingModelName.VOYAGE_3_LITE).timeout(Duration.ofSeconds(60)).logRequests(true).logResponses(true).build
    System.out.println(model.embed("Hello World"))
  }

  @Test def should_respect_encoding_format(): Unit = {
    // Using base64 encoding format to compress the embedding
    val model = VoyageAiEmbeddingModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(VoyageAiEmbeddingModelName.VOYAGE_3_LITE).timeout(Duration.ofSeconds(60)).encodingFormat("base64").logRequests(true).logResponses(true).build
    System.out.println(model.embed("Hello World"))
  }

  @Test def should_embed_multiple_segments(): Unit = {
    val model = VoyageAiEmbeddingModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(VoyageAiEmbeddingModelName.VOYAGE_3_LITE).timeout(Duration.ofSeconds(60)).inputType("query").logRequests(true).logResponses(true).build
    val segment1 = TextSegment.from("hello")
    val segment2 = TextSegment.from("hi")
    System.out.println(model.embedAll(asList(segment1, segment2)))
  }

  @Test def should_embed_any_number_of_segments(): Unit = {
    // given
    val model = VoyageAiEmbeddingModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(VoyageAiEmbeddingModelName.VOYAGE_3_LITE).timeout(Duration.ofSeconds(60)).logRequests(true).logResponses(true).build
    val segments = new util.ArrayList[TextSegment]
    val segmentCount = 97
    for (i <- 0 until segmentCount) {
      segments.add(TextSegment.from("text"))
    }
    // when
    System.out.println(model.embedAll(segments))
  }
}