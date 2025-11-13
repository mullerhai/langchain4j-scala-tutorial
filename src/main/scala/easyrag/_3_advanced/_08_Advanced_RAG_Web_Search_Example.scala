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
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever, WebSearchContentRetriever}
import dev.langchain4j.rag.query.router.{DefaultQueryRouter, QueryRouter}
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.web.search.WebSearchEngine
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine

import java.nio.file.Path
import java.util
import easyrag.shared.{Assistant, Utils}
import utils.ApiKeys

object _08_Advanced_RAG_Web_Search_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example demonstrates how to use web search engine as an additional content retriever.
   * <p>
   * This example requires "langchain4j-web-search-engine-tavily" dependency.
   */
  def main(args: Array[String]): Unit = {
    val assistant = createAssistant
    Utils.startConversationWith(assistant)
  }

  private def createAssistant = {
    // Let's create our embedding store content retriever.
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddingStore = embed(Utils.toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel)
    val embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).maxResults(2).minScore(0.6).build
    // Let's create our web search content retriever.
    val webSearchEngine = TavilyWebSearchEngine.builder.apiKey(System.getenv("TAVILY_API_KEY")).build() // get a free key: https://app.tavily.com/sign-in.build
    val webSearchContentRetriever = WebSearchContentRetriever.builder.webSearchEngine(webSearchEngine).maxResults(3).build
    // Let's create a query router that will route each query to both retrievers.
    val queryRouter = new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever)
    val retrievalAugmentor = DefaultRetrievalAugmentor.builder.queryRouter(queryRouter).build
    val model = OpenAiChatModel.builder
        .baseUrl(ApiKeys.BASE_URL)
        .apiKey(ApiKeys.OPENAI_API_KEY)
        .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
        .temperature(0.3)
//      .timeout(ofSeconds(60))
        .logRequests(true)
        .httpClientBuilder(new SpringRestClientBuilderFactory().create())
        .logResponses(true).build
    AiServices.builder(classOf[Assistant]).chatModel(model).retrievalAugmentor(retrievalAugmentor).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
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