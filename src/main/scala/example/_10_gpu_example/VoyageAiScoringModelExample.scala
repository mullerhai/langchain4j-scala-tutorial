package example._10_gpu_example

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.model.voyageai.VoyageAiScoringModel
import dev.langchain4j.model.voyageai.VoyageAiScoringModelName.RERANK_LITE_1
import org.junit.jupiter.api.Test

import java.time.Duration
import java.util
import java.util.Arrays.asList

class VoyageAiScoringModelExample {
  @Test def should_score_single_text(): Unit = {
    val model = VoyageAiScoringModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(RERANK_LITE_1).timeout(Duration.ofSeconds(60)).logRequests(true).logResponses(true).build
    val text = "labrador retriever"
    val query = "tell me about dogs"
    System.out.println(model.score(text, query))
  }

  @Test def should_score_multiple_segments_with_all_parameters(): Unit = {
    val model = VoyageAiScoringModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(RERANK_LITE_1).timeout(Duration.ofSeconds(60)).logRequests(true).logResponses(true).build
    val catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.")
    val dogSegment = TextSegment.from("The sweet-faced, lovable Labrador Retriever is one of America's most popular dog breeds, year after year.")
    val segments = asList(catSegment, dogSegment)
    val query = "tell me about dogs"
    System.out.println(model.scoreAll(segments, query))
  }

  @Test def should_respect_top_k(): Unit = {
    val model = VoyageAiScoringModel.builder.apiKey(System.getenv("VOYAGE_API_KEY")).modelName(RERANK_LITE_1).timeout(Duration.ofSeconds(60)).topK(1).logRequests(true).logResponses(true).build
    val catSegment = TextSegment.from("The Maine Coon is a large domesticated cat breed.")
    val dogSegment = TextSegment.from("The sweet-faced, lovable Labrador Retriever is one of America's most popular dog breeds, year after year.")
    val segments = asList(catSegment, dogSegment)
    val query = "tell me about dogs"
    // when
    System.out.println(model.scoreAll(segments, query))
  }
}