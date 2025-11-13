package easyrag._3_advanced

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
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
import dev.langchain4j.rag.query.transformer.{CompressingQueryTransformer, QueryTransformer}
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.{EmbeddingStore, EmbeddingStoreIngestor}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import easyrag.shared.{Assistant, Utils}
import utils.ApiKeys

object _01_Advanced_RAG_with_Query_Compression_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example illustrates the implementation of a more sophisticated RAG application
   * using a technique known as "query compression".
   * Often, a query from a user is a follow-up question that refers back to earlier parts of the conversation
   * and lacks all the necessary details for effective retrieval.
   * For example, consider this conversation:
   * User: What is the legacy of John Doe?
   * AI: John Doe was a...
   * User: When was he born?
   * <p>
   * In such scenarios, using a basic RAG approach with a query like "When was he born?"
   * would likely fail to find articles about John Doe, as it doesn't contain "John Doe" in the query.
   * Query compression involves taking the user's query and the preceding conversation, then asking the LLM
   * to "compress" this into a single, self-contained query.
   * The LLM should generate a query like "When was John Doe born?".
   * This method adds a bit of latency and cost but significantly enhances the quality of the RAG process.
   * It's worth noting that the LLM used for compression doesn't have to be the same as the one
   * used for conversation. For instance, you might use a smaller local model trained for summarization.
   */
  def main(args: Array[String]): Unit = {
    val assistant = createAssistant("documents/biography-of-john-doe.txt")
    // First, ask "What is the legacy of John Doe?"
    // Then, ask "When was he born?"
    // Now, review the logs:
    // The first query was not compressed as there was no preceding context to compress.
    // The second query, however, was compressed into something like "When was John Doe born?"
    Utils.startConversationWith(assistant)
  }

  private def createAssistant(documentPath: String) = {
    val document = loadDocument(Utils.toPath(documentPath), new TextDocumentParser)
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    val ingestor = EmbeddingStoreIngestor.builder.documentSplitter(DocumentSplitters.recursive(300, 0)).embeddingModel(embeddingModel).embeddingStore(embeddingStore).build
    ingestor.ingest(document)
    val chatModel = OpenAiChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .temperature(0.3)
//      .timeout(ofSeconds(60))
      .logRequests(true)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .logResponses(true).build
    // We will create a CompressingQueryTransformer, which is responsible for compressing
    // the user's query and the preceding conversation into a single, stand-alone query.
    // This should significantly improve the quality of the retrieval process.
    val queryTransformer = new CompressingQueryTransformer(chatModel)
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).maxResults(2).minScore(0.6).build
    // The RetrievalAugmentor serves as the entry point into the RAG flow in LangChain4j.
    // It can be configured to customize the RAG behavior according to your requirements.
    // In subsequent examples, we will explore more customizations.
    val retrievalAugmentor = DefaultRetrievalAugmentor.builder.queryTransformer(queryTransformer).contentRetriever(contentRetriever).build
    AiServices.builder(classOf[Assistant]).chatModel(chatModel).retrievalAugmentor(retrievalAugmentor).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
  }
}