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
import dev.langchain4j.model.openai.{OpenAiChatModel, OpenAiChatModelName}
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.rag.{DefaultRetrievalAugmentor, RetrievalAugmentor}
import dev.langchain4j.rag.content.injector.{ContentInjector, DefaultContentInjector}
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.{EmbeddingStore, EmbeddingStoreIngestor}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore

import java.util.Arrays.asList
import easyrag.shared.{Assistant, Utils}
import tutorial.ApiKeys
object _04_Advanced_RAG_with_Metadata_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example illustrates how to include document source and other metadata into the LLM prompt.
   */
  def main(args: Array[String]): Unit = {
    val assistant = createAssistant("documents/miles-of-smiles-terms-of-use.txt")
    // Ask "What is the name of the file where cancellation policy is defined?".
    // Observe how "file_name" metadata entry was injected into the prompt.
    Utils.startConversationWith(assistant)
  }

  private def createAssistant(documentPath: String) = {
    val document = loadDocument(Utils.toPath(documentPath), new TextDocumentParser)
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    val ingestor = EmbeddingStoreIngestor.builder.documentSplitter(DocumentSplitters.recursive(300, 0)).embeddingModel(embeddingModel).embeddingStore(embeddingStore).build
    ingestor.ingest(document)
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).build
    // Each retrieved segment should include "file_name" and "index" metadata values in the prompt
    val contentInjector = DefaultContentInjector.builder.metadataKeysToInclude(asList("file_name", "index")).build
    val retrievalAugmentor = DefaultRetrievalAugmentor.builder.contentRetriever(contentRetriever).contentInjector(contentInjector).build
    val chatModel = OpenAiChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .temperature(0.3)
//      .timeout(ofSeconds(60))
      .logRequests(true)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .logResponses(true).build
    AiServices.builder(classOf[Assistant]).chatModel(chatModel).retrievalAugmentor(retrievalAugmentor).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
  }
}