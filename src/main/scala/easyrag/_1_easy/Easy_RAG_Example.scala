package easyrag._1_easy

import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.rag.content.retriever.{ContentRetriever, EmbeddingStoreContentRetriever}
import dev.langchain4j.service.AiServices
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import easyrag.shared.{Assistant, Utils}
import tutorial.ApiKeys

import java.time.Duration.ofSeconds
import java.util

object Easy_RAG_Example {
  private val CHAT_MODEL = OpenAiChatModel.builder
    .baseUrl(ApiKeys.BASE_URL)
    .apiKey(ApiKeys.OPENAI_API_KEY)
    .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
    .temperature(0.3)
    .timeout(ofSeconds(60))
    .logRequests(true)
    .httpClientBuilder(new SpringRestClientBuilderFactory().create())
    .logResponses(true).build//  OpenAiChatModel.builder.apiKey(Utils.OPENAI_API_KEY).modelName(GPT_4_O_MINI).build

  /**
   * This example demonstrates how to implement an "Easy RAG" (Retrieval-Augmented Generation) application.
   * By "easy" we mean that we won't dive into all the details about parsing, splitting, embedding, etc.
   * All the "magic" is hidden inside the "langchain4j-easy-rag" module.
   * <p>
   * If you want to learn how to do RAG without the "magic" of an "Easy RAG", see {@link Naive_RAG_Example}.
   */
  def main(args: Array[String]): Unit = {
    // First, let's load documents that we want to use for RAG
    val documents = loadDocuments(Utils.toPath("documents/"), Utils.glob("*.txt"))
    // Second, let's create an assistant that will have access to our documents
    val assistant = AiServices.builder(classOf[Assistant]).chatModel(CHAT_MODEL)
      .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
      .contentRetriever(createContentRetriever(documents)).build() // it should use OpenAI LLM.chatMemory(MessageWindowChatMemory.withMaxMessages(10))// it should remember 10 latest messages.contentRetriever(createContentRetriever(documents))// it should have access to our documents.build
    // Lastly, let's start the conversation with the assistant. We can ask questions like:
    // - Can I cancel my reservation?
    // - I had an accident, should I pay extra?
    Utils.startConversationWith(assistant)
  }

  private def createContentRetriever(documents: util.List[Document]) = {
    // Here, we create an empty in-memory store for our documents and their embeddings.
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]()
    // Here, we are ingesting our documents into the store.
    // Under the hood, a lot of "magic" is happening, but we can ignore it for now.
    EmbeddingStoreIngestor.ingest(documents, embeddingStore)
    // Lastly, let's create a content retriever from an embedding store.
    EmbeddingStoreContentRetriever.from(embeddingStore)
  }
}