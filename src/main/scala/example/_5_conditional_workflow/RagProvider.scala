package example._5_conditional_workflow

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.internal.Utils
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore

import java.net.{URISyntaxException, URL}
import java.nio.file.{Path, Paths}
import java.util

object RagProvider {
  def loadHouseRulesRetriever: ContentRetriever = {
    val doc = loadDocument(toPath("documents/house_rules.txt"))
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val store = new InMemoryEmbeddingStore[TextSegment]
    val ingestor = EmbeddingStoreIngestor.builder.documentSplitter(DocumentSplitters.recursive(200, 10)).embeddingModel(embeddingModel).embeddingStore(store).build
    ingestor.ingest(util.List.of(doc))
    EmbeddingStoreContentRetriever.builder.embeddingStore(store).embeddingModel(embeddingModel).maxResults(2).minScore(0.8).build
  }

  def toPath(relativePath: String): Path = try {
    val fileUrl = classOf[Utils].getClassLoader.getResource(relativePath)
    Paths.get(fileUrl.toURI)
  } catch {
    case e: URISyntaxException =>
      throw new RuntimeException(e)
  }
}