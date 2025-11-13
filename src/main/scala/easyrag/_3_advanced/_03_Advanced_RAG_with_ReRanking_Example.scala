package easyrag._3_advanced

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.cohere.CohereScoringModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.rag.{DefaultRetrievalAugmentor, RetrievalAugmentor}
import dev.langchain4j.rag.content.aggregator.{ContentAggregator, ReRankingContentAggregator}
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.{EmbeddingStore, EmbeddingStoreIngestor}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import easyrag.shared.{Assistant, Utils}
import utils.ApiKeys

object _03_Advanced_RAG_with_ReRanking_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example illustrates the implementation of a more advanced RAG application
   * using a technique known as "re-ranking".
   * <p>
   * Frequently, not all results retrieved by {@link ContentRetriever} are truly relevant to the user query.
   * This is because, during the initial retrieval stage, it is often preferable to use faster
   * and more cost-effective models, particularly when dealing with a large volume of data.
   * The trade-off is that the retrieval quality may be lower.
   * Providing irrelevant information to the LLM can be costly and, in the worst case, lead to hallucinations.
   * Therefore, in the second stage, we can perform re-ranking of the results obtained in the first stage
   * and eliminate irrelevant results using a more advanced model (e.g., Cohere Rerank).
   * <p>
   * This example requires "langchain4j-cohere" dependency.
   */
  def main(args: Array[String]): Unit = {
    val assistant = createAssistant("documents/miles-of-smiles-terms-of-use.txt")
    // First, say "Hi". Observe how all segments retrieved in the first stage were filtered out.
    // Then, ask "Can I cancel my reservation?" and observe how all but one segment were filtered out.
    Utils.startConversationWith(assistant)
  }

  private def createAssistant(documentPath: String) = {
    val document = loadDocument(Utils.toPath(documentPath), new TextDocumentParser)
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    val ingestor = EmbeddingStoreIngestor.builder.documentSplitter(DocumentSplitters.recursive(300, 0)).embeddingModel(embeddingModel).embeddingStore(embeddingStore).build
    ingestor.ingest(document)
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).maxResults(5).build() // let's get more results.build
    // To register and get a free API key for Cohere, please visit the following link:
    // https://dashboard.cohere.com/welcome/register
    val scoringModel = CohereScoringModel.builder.apiKey(System.getenv("COHERE_API_KEY")).modelName("rerank-multilingual-v3.0").build
    val contentAggregator = ReRankingContentAggregator.builder.scoringModel(scoringModel).minScore(0.8).build() // we want to present the LLM with only the truly relevant segments for the user's query.build
    val retrievalAugmentor = DefaultRetrievalAugmentor.builder.contentRetriever(contentRetriever).contentAggregator(contentAggregator).build
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
}