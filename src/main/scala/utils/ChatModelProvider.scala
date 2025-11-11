package utils

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import tutorial.ApiKeys

object ChatModelProvider {
  def createChatModel: ChatModel = OpenAiChatModel.builder
    .apiKey(ApiKeys.OPENAI_API_KEY)
    .baseUrl(ApiKeys.BASE_URL)
    .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
    .logRequests(true)
    .logResponses(true)
    .httpClientBuilder(new SpringRestClientBuilderFactory().create())
    .build

  def createChatModel(enableLogging: Boolean): ChatModel = createChatModel("OPENAI", enableLogging)

  def createChatModel(provider: String): ChatModel = createChatModel(provider, true)

  def createChatModel(provider: String, enableLogging: Boolean): ChatModel = if ("CEREBRAS".equalsIgnoreCase(provider)) OpenAiChatModel.builder.baseUrl("https://api.cerebras.ai/v1").apiKey(System.getenv("CEREBRAS_API_KEY")).modelName("llama-4-scout-17b-16e-instruct").logRequests(enableLogging).logResponses(enableLogging).build
  else OpenAiChatModel.builder.apiKey(System.getenv("OPENAI_API_KEY")).modelName(GPT_4_O_MINI).logRequests(enableLogging).logResponses(enableLogging).build
}