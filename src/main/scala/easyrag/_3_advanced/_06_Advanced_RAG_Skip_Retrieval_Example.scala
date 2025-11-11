package easyrag._3_advanced

import dev.langchain4j.data.document.{Document, DocumentParser, DocumentSplitter}
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.model.input.{Prompt, PromptTemplate}
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.rag.{DefaultRetrievalAugmentor, RetrievalAugmentor}
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.rag.query.Query
import dev.langchain4j.rag.query.router.QueryRouter
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore

import java.nio.file.Path
import java.util
import java.util.Collections.{emptyList, singletonList}
import easyrag.shared.{Assistant, Utils}
import tutorial.ApiKeys
object _06_Advanced_RAG_Skip_Retrieval_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example demonstrates how to conditionally skip retrieval.
   * Sometimes, retrieval is unnecessary, for instance, when a user simply says "Hi".
   * <p>
   * There are multiple ways to implement this, but the simplest one is to use a custom {@link QueryRouter}.
   * When retrieval should be skipped, QueryRouter will return an empty list,
   * meaning that the query will not be routed to any {@link ContentRetriever}.
   * <p>
   * Decision-making can be implemented in various ways:
   * - Using rules (e.g., depending on the user's privileges, location, etc.).
   * - Using keywords (e.g., if a query contains specific words).
   * - Using semantic similarity (see EmbeddingModelTextClassifierExample in this repository).
   * - Using an LLM to make a decision.
   * <p>
   * In this example, we will use an LLM to decide whether a user query should do retrieval or not.
   */
  def main(args: Array[String]): Unit = {
    val assistant = createAssistant
    // First, say "Hi"
    // Notice how this query is not routed to any retrievers.
    // Now, ask "Can I cancel my reservation?"
    // This query has been routed to our retriever.
    Utils.startConversationWith(assistant)
  }

  private def createAssistant = {
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddingStore = embed(Utils.toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel)
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).maxResults(2).minScore(0.6).build
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
    val queryRouter = new QueryRouter() {
      final private val PROMPT_TEMPLATE = PromptTemplate.from("Is the following query related to the business of the car rental company? " + "Answer only 'yes', 'no' or 'maybe'. " + "Query: {{it}}")

      override def route(query: Query): util.Collection[ContentRetriever] = {
        val prompt = PROMPT_TEMPLATE.apply(query.text)
        val aiMessage = chatModel.chat(prompt.toUserMessage).aiMessage
        System.out.println("LLM decided: " + aiMessage.text)
        if (aiMessage.text.toLowerCase.contains("no")) return emptyList
        singletonList(contentRetriever)
      }
    }
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