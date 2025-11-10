package jlama

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.document.{Document, DocumentSplitter}
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.input.{Prompt, PromptTemplate}
import dev.langchain4j.model.jlama.{JlamaChatModel, JlamaEmbeddingModel}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.store.embedding.{EmbeddingMatch, EmbeddingSearchRequest, EmbeddingStore}
import easyrag.shared.Utils

import java.net.{URISyntaxException, URL}
import java.nio.file.{Path, Paths}
import java.util
import java.util.stream.Collectors.joining

class JlamaBasicRagEmbedExamples{}
object JlamaBasicRagEmbedExamples {
  object Chat_Story_From_My_Document {
    def main(args: Array[String]): Unit = {
      // In this very simple example, we are getting data that we want to use for RAG.
      // We will use a history about origin of the Llama by National Geographic https://www.nationalgeographic.es/animales/llama.
      val document = loadDocument(Utils.toPath("example-files/story-about-origin-of-the-llama.txt"), new TextDocumentParser)
      // In a RAG system, it is crucial to split the document into smaller chunks so that it's more effective
      // to identify and retrieve the most relevant information in the retrieval process later
      val splitter = DocumentSplitters.recursive(200, 0)
      val segments = splitter.split(document)
      // Now, for each text segment, we need to create text embeddings, which are numeric representations of the text in the vector space.
      val embeddingModel = JlamaEmbeddingModel.builder.modelName("intfloat/e5-small-v2").build
      val embeddings = embeddingModel.embedAll(segments).content
      // Once we get the text embeddings, we will store them in a vector database for efficient processing and retrieval.
      // For simplicity, this example uses an in-memory store, but you can choose any external compatible store for production environments.
      val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
      embeddingStore.addAll(embeddings, segments)
      // Whenever users ask a question, we also need to create embeddings for this question using the same embedding models as before.
      val question = "Who create the llamas?"
      val questionEmbedding = embeddingModel.embed(question).content
      // We can perform a search on the vector database and retrieve the most relevant text chunks based on the user question.
      val embeddingSearchRequest = EmbeddingSearchRequest.builder.queryEmbedding(questionEmbedding).maxResults(3).minScore(0.7).build
      val relevantEmbeddings = embeddingStore.search(embeddingSearchRequest).matches
      // Now we can offer the relevant information as the context information within the prompt.
      // Here is a prompt template where we can include both the retrieved text and user question in the prompt.
      val promptTemplate = PromptTemplate.from("Context information is below.:\n" + "------------------\n" + "{{information}}\n" + "------------------\n" + "Given the context information and not prior knowledge, answer the query.\n" + "Query: {{question}}\n" + "Answer:")
      val information = relevantEmbeddings.stream.map((`match`: EmbeddingMatch[TextSegment]) => `match`.embedded.text).collect(joining("\n\n"))
      val promptInputs = new util.HashMap[String, AnyRef]
      promptInputs.put("question", question)
      promptInputs.put("information", information)
      val prompt = promptTemplate.apply(promptInputs)
      // Now we can use the Jlama chat model to generate the answer to the user question based on the context information.
      val chatModel = JlamaChatModel.builder.modelName("tjake/Llama-3.2-1B-Instruct-JQ4").temperature(0.2f).build() // expect a more focused and deterministic answer.build
      val aiMessage = chatModel.chat(prompt.toUserMessage).aiMessage
      val answer = aiMessage.text
      System.out.println(answer) // According to Inca legend, the llamas were created by the mythical founders of the Inca Empire....
    }
  }

  def toPath(fileName: String): Path = try {
    val fileUrl = classOf[JlamaBasicRagEmbedExamples].getResource(fileName)
    Paths.get(fileUrl.toURI)
  } catch {
    case e: URISyntaxException =>
      throw new RuntimeException(e)
  }
}