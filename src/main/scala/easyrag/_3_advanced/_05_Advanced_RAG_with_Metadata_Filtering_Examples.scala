package easyrag._3_advanced

import dev.langchain4j.data.document.Metadata.metadata
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.rag.query.Query
import dev.langchain4j.service.{AiServices, MemoryId}
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.filter.Filter
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import dev.langchain4j.store.embedding.filter.builder.sql.{LanguageModelSqlFilterBuilder, TableDefinition}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import scala.jdk.FutureConverters.*
import java.util.function.Function
import easyrag.shared.{Assistant, Utils}
import tutorial.ApiKeys

import scala.jdk.FunctionConverters.enrichAsJavaFunction
object _05_Advanced_RAG_with_Metadata_Filtering_Examples {
  private[_3_advanced] trait PersonalizedAssistant {
    def chat(@MemoryId userId: String, @dev.langchain4j.service.UserMessage userMessage: String): String
  }
}

class _05_Advanced_RAG_with_Metadata_Filtering_Examples {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * More information on metadata filtering can be found here: https://github.com/langchain4j/langchain4j/pull/610
   */
  private[_3_advanced] val chatModel = OpenAiChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .temperature(0.3)
//      .timeout(ofSeconds(60))
      .logRequests(true)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .logResponses(true).build
  private[_3_advanced] val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel

  @Test private[_3_advanced] def Static_Metadata_Filter_Example(): Unit = {
    // given
    val dogsSegment = TextSegment.from("Article about dogs ...", metadata("animal", "dog"))
    val birdsSegment = TextSegment.from("Article about birds ...", metadata("animal", "bird"))
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    embeddingStore.add(embeddingModel.embed(dogsSegment).content, dogsSegment)
    embeddingStore.add(embeddingModel.embed(birdsSegment).content, birdsSegment)
    // embeddingStore contains segments about both dogs and birds
    val onlyDogs = metadataKey("animal").isEqualTo("dog")
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).filter(onlyDogs).build() // by specifying the static filter, we limit the search to segments only about dogs.build
    val assistant = AiServices.builder(classOf[Assistant]).chatModel(chatModel).contentRetriever(contentRetriever).build
    // when
    val answer = assistant.answer("Which animal?")
    // then
    assertThat(answer).containsIgnoringCase("dog").doesNotContainIgnoringCase("bird")
  }

  @Test private[_3_advanced] def Dynamic_Metadata_Filter_Example(): Unit = {
    // given
    val user1Info = TextSegment.from("My favorite color is green", metadata("userId", "1"))
    val user2Info = TextSegment.from("My favorite color is red", metadata("userId", "2"))
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    embeddingStore.add(embeddingModel.embed(user1Info).content, user1Info)
    embeddingStore.add(embeddingModel.embed(user2Info).content, user2Info)
    // embeddingStore contains information about both first and second user
    val filterByUserId = (query: Query) => metadataKey("userId").isEqualTo(query.metadata.chatMemoryId.toString)
//    val dynamicFilter: java.util.function.Function[Query, Filter] = filterByUserId.asJava
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .dynamicFilter(filterByUserId.asJava).build
    val personalizedAssistant = AiServices.builder(classOf[_05_Advanced_RAG_with_Metadata_Filtering_Examples.PersonalizedAssistant]).chatModel(chatModel).contentRetriever(contentRetriever).build
    // when
    val answer1 = personalizedAssistant.chat("1", "Which color would be best for a dress?")
    // then
    assertThat(answer1).containsIgnoringCase("green").doesNotContainIgnoringCase("red")
    // when
    val answer2 = personalizedAssistant.chat("2", "Which color would be best for a dress?")
    // then
    assertThat(answer2).containsIgnoringCase("red").doesNotContainIgnoringCase("green")
  }

  @Test private[_3_advanced] def LLM_generated_Metadata_Filter_Example(): Unit = {
    // given
    val forrestGump = TextSegment.from("Forrest Gump", metadata("genre", "drama").put("year", 1994))
    val groundhogDay = TextSegment.from("Groundhog Day", metadata("genre", "comedy").put("year", 1993))
    val dieHard = TextSegment.from("Die Hard", metadata("genre", "action").put("year", 1998))
    // describe metadata keys as if they were columns in the SQL table
    val tableDefinition = TableDefinition.builder.name("movies").addColumn("genre", "VARCHAR", "one of: [comedy, drama, action]").addColumn("year", "INT").build
    val sqlFilterBuilder = new LanguageModelSqlFilterBuilder(chatModel, tableDefinition)
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    embeddingStore.add(embeddingModel.embed(forrestGump).content, forrestGump)
    embeddingStore.add(embeddingModel.embed(groundhogDay).content, groundhogDay)
    embeddingStore.add(embeddingModel.embed(dieHard).content, dieHard)
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).dynamicFilter((query: Query) => sqlFilterBuilder.build(query)).build() // LLM will generate the filter dynamically.build
    val assistant = AiServices.builder(classOf[Assistant]).chatModel(chatModel).contentRetriever(contentRetriever).build
    // when
    val answer = assistant.answer("Recommend me a good drama from 90s")
    // then
    assertThat(answer).containsIgnoringCase("Forrest Gump").doesNotContainIgnoringCase("Groundhog Day").doesNotContainIgnoringCase("Die Hard")
  }
}