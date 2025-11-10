package easyrag._3_advanced

import dev.langchain4j.data.document.{Document, DocumentParser, DocumentSplitter}
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.rag.content.Content
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.service.{AiServices, Result}
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import org.slf4j.{Logger, LoggerFactory}

import java.nio.file.Path
import java.util
import java.util.Scanner
import easyrag.shared.{Assistant, Utils}

import scala.util.control.Breaks.break
object _09_Advanced_RAG_Return_Sources_Example {
  /**
   * Please refer to {@link Naive_RAG_Example} for a basic context.
   * <p>
   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
   * <p>
   * This example demonstrates how to return sources (retrieved contents).
   */
  private[_3_advanced] trait Assistant {
    def answer(query: String): Result[String]
  }

  def main(args: Array[String]): Unit = {
    val assistant = createAssistant
    val log = LoggerFactory.getLogger(classOf[Assistant])
    try {
      val scanner = new Scanner(System.in)
      try while (true) {
        log.info("==================================================")
        log.info("User: ")
        val userQuery = scanner.nextLine
        log.info("==================================================")
        if ("exit".equalsIgnoreCase(userQuery)) break //todo: break is not supported
        val result = assistant.answer(userQuery)
        log.info("==================================================")
        log.info("Assistant: " + result.content)
        log.info("Sources: ")
        val sources = result.sources
        sources.forEach((content: Content) => log.info(content.toString))
      }
      finally if (scanner != null) scanner.close()
    }
  }

  private def createAssistant = {
    // Let's create our embedding store content retriever.
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddingStore = embed(Utils.toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel)
    val contentRetriever = EmbeddingStoreContentRetriever.builder.embeddingStore(embeddingStore).embeddingModel(embeddingModel).maxResults(2).minScore(0.6).build
    val chatModel = OpenAiChatModel.builder.apiKey(Utils.OPENAI_API_KEY).modelName(GPT_4_O_MINI).build
    AiServices.builder(classOf[_09_Advanced_RAG_Return_Sources_Example.Assistant]).chatModel(chatModel).contentRetriever(contentRetriever).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
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