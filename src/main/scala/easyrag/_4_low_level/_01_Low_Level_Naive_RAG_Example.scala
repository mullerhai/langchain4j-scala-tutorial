package easyrag._4_low_level

import dev.langchain4j.data.document.{Document, DocumentParser, DocumentSplitter}
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel
import dev.langchain4j.model.input.{Prompt, PromptTemplate}
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.model.openai.{OpenAiChatModel, OpenAiTokenCountEstimator}
import dev.langchain4j.store.embedding.{EmbeddingMatch, EmbeddingSearchRequest, EmbeddingStore}
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import easyrag.shared.Utils
import easyrag.shared.Utils.OPENAI_API_KEY
import utils.ApiKeys

import java.time.Duration
import java.util
import java.util.stream.Collectors.joining

object _01_Low_Level_Naive_RAG_Example {
  /**
   * This example demonstrates how to use low-level LangChain4j APIs to implement RAG.
   * Check other packages to see examples of using high-level API (AI Services).
   */
  def main(args: Array[String]): Unit = {
    // Load the document that includes the information you'd like to "chat" about with the model.
    val documentParser = new TextDocumentParser
    val document = loadDocument(Utils.toPath("example-files/story-about-happy-carrot.txt"), documentParser)
    // Split document into segments 100 tokens each
    val splitter = DocumentSplitters.recursive(300, 0, new OpenAiTokenCountEstimator(GPT_4_O_MINI))
    val segments = splitter.split(document)
    // Embed segments (convert them into vectors that represent the meaning) using embedding model
    val embeddingModel = new BgeSmallEnV15QuantizedEmbeddingModel
    val embeddings = embeddingModel.embedAll(segments).content
    // Store embeddings into embedding store for further search / retrieval
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]
    embeddingStore.addAll(embeddings, segments)
    // Specify the question you want to ask the model
    val question = "Who is Charlie?"
    // Embed the question
    val questionEmbedding = embeddingModel.embed(question).content
    // Find relevant embeddings in embedding store by semantic similarity
    // You can play with parameters below to find a sweet spot for your specific use case
    val embeddingSearchRequest = EmbeddingSearchRequest.builder.queryEmbedding(questionEmbedding).maxResults(3).minScore(0.7).build
    val relevantEmbeddings = embeddingStore.search(embeddingSearchRequest).matches
    // Create a prompt for the model that includes question and relevant embeddings
    val promptTemplate = PromptTemplate.from("Answer the following question to the best of your ability:\n" + "\n" + "Question:\n" + "{{question}}\n" + "\n" + "Base your answer on the following information:\n" + "{{information}}")
    val information = relevantEmbeddings.stream.map((`match`: EmbeddingMatch[TextSegment]) => `match`.embedded.text).collect(joining("\n\n"))
    val variables = new util.HashMap[String, AnyRef]
    variables.put("question", question)
    variables.put("information", information)
    val prompt = promptTemplate.apply(variables)
    // Send the prompt to the OpenAI chat model
    val chatModel = OpenAiChatModel.builder
        .baseUrl(ApiKeys.BASE_URL)
        .apiKey(ApiKeys.OPENAI_API_KEY)
        .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
        .temperature(0.3)
        .timeout(Duration.ofSeconds(60))
        .logRequests(true)
        .httpClientBuilder(new SpringRestClientBuilderFactory().create())
        .logResponses(true).build
    val aiMessage = chatModel.chat(prompt.toUserMessage).aiMessage
    // See an answer from the model
    val answer = aiMessage.text
    System.out.println(answer) // Charlie is a cheerful carrot living in VeggieVille...
  }
}