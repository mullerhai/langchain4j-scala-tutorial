package easyrag._3_advanced

import dev.langchain4j.data.document.{Document, DocumentParser, DocumentSplitter}
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.rag.{DefaultRetrievalAugmentor, RetrievalAugmentor}
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.rag.query.router.{LanguageModelQueryRouter, QueryRouter}
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import easyrag.shared.{Assistant, Utils}
import tutorial.ApiKeys

import java.nio.file.Path
import java.util

object _02_Advanced_RAG_with_Query_Routing_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example showcases the implementation of a more advanced RAG application
   * using a technique known as "query routing".
   * <p>
   * Often, private data is spread across multiple sources and formats.
   * This might include internal company documentation on Confluence, your project's code in a Git repository,
   * a relational database with user data, or a search engine with the products you sell, among others.
   * In a RAG flow that utilizes data from multiple sources, you will likely have multiple
   * {@link EmbeddingStore}s or {@link ContentRetriever}s.
   * While you could route each user query to all available {@link ContentRetriever}s,
   * this approach might be inefficient and counterproductive.
   * <p>
   * "Query routing" is the solution to this challenge. It involves directing a query to the most appropriate
   * {@link ContentRetriever} (or several). Routing can be implemented in various ways:
   * - Using rules (e.g., depending on the user's privileges, location, etc.).
   * - Using keywords (e.g., if a query contains words X1, X2, X3, route it to {@link ContentRetriever} X, etc.).
   * - Using semantic similarity (see EmbeddingModelTextClassifierExample in this repository).
   * - Using an LLM to make a routing decision.
   * <p>
   * For scenarios 1, 2, and 3, you can implement a custom {@link QueryRouter}.
   * For scenario 4, this example will demonstrate how to use a {@link LanguageModelQueryRouter}.
   */
  def main(args: Array[String]): Unit = {
    val assistant = createAssistant
    // First, ask "What is the legacy of John Doe?"
    // Then, ask "Can I cancel my reservation?"
    // Now, see the logs to observe how the queries are routed to different retrievers.
    Utils.startConversationWith(assistant)
  }

  private def createAssistant = {
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    // Let's create a separate embedding store specifically for biographies.
    val biographyEmbeddingStore = embed(Utils.toPath("documents/biography-of-john-doe.txt"), embeddingModel)
    val biographyContentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(biographyEmbeddingStore).embeddingModel(embeddingModel).maxResults(2).minScore(0.6).build
    // Additionally, let's create a separate embedding store dedicated to terms of use.
    val termsOfUseEmbeddingStore = embed(Utils.toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel)
    val termsOfUseContentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(termsOfUseEmbeddingStore).embeddingModel(embeddingModel).maxResults(2).minScore(0.6).build
    val chatModel = OpenAiChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .temperature(0.3)
//      .timeout(ofSeconds(60))
      .logRequests(true)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .logResponses(true).build
    // Let's create a query router.
    val retrieverToDescription = new util.HashMap[ContentRetriever, String]
    retrieverToDescription.put(biographyContentRetriever, "biography of John Doe")
    retrieverToDescription.put(termsOfUseContentRetriever, "terms of use of car rental company")
    val queryRouter = new LanguageModelQueryRouter(chatModel, retrieverToDescription)
    val retrievalAugmentor = DefaultRetrievalAugmentor.builder.queryRouter(queryRouter).build
    AiServices.builder(classOf[Assistant]).chatModel(chatModel).retrievalAugmentor(retrievalAugmentor).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
  }

  private def embed(documentPath: Path, embeddingModel: EmbeddingModel) = {
    val documentParser = new TextDocumentParser
    val document = loadDocument(documentPath, documentParser)
    val splitter = DocumentSplitters.recursive(300, 0)
    val segments = splitter.split(document)
    val embeddings = embeddingModel.embedAll(segments).content
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    embeddingStore.addAll(embeddings, segments)
    embeddingStore
  }
}