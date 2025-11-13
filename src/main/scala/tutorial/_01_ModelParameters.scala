package tutorial

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import utils.ApiKeys

import java.time.Duration.ofSeconds

object _01_ModelParameters {
  def main(args: Array[String]): Unit = {
    // OpenAI parameters are explained here: https://platform.openai.com/docs/api-reference/chat/create
    val model = OpenAiChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .temperature(0.3)
      .timeout(ofSeconds(60))
      .logRequests(true)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .logResponses(true).build
    val prompt = "读一首李白的唐诗" // "Explain in three lines how to make a beautiful painting"
    val response = model.chat(prompt)
    System.out.println(response)
  }
}