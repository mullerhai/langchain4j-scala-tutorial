package tutorial

import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.service.AiServices
import utils.ApiKeys

object _10_ServiceWithToolsExample {
  // Please also check CustomerSupportApplication and CustomerSupportApplicationTest
  // from spring-boot-example module
  class Calculator {
    @Tool(Array("Calculates the length of a string")) 
    def stringLength(s: String): Int = {
      System.out.println("Called stringLength() with s='" + s + "'")
      s.length
    }

    @Tool(Array("Calculates the sum of two numbers")) 
    def add(a: Int, b: Int): Int = {
      System.out.println("Called add() with a=" + a + ", b=" + b)
      a + b
    }

    @Tool(Array("Calculates the square root of a number")) 
    def sqrt(x: Int): Double = {
      System.out.println("Called sqrt() with x=" + x)
      Math.sqrt(x)
    }
  }

  trait Assistant {
    def chat(userMessage: String): String
  }

  def main(args: Array[String]): Unit = {
    val model = OpenAiChatModel.builder
      .apiKey(ApiKeys.OPENAI_API_KEY) // WARNING! Tools are not supported with "demo" API key.modelName(GPT_4_O_MINI).strictTools(true)// https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-tools.build
      .baseUrl(ApiKeys.BASE_URL)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
//    .timeout(ofSeconds(60))
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
       .build() // WARNING! Tools are not supported with "demo" API key.modelName(GPT_4_O_MINI).strictTools(true)// https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-tools.build
    val assistant = AiServices.builder(classOf[_10_ServiceWithToolsExample.Assistant]).chatModel(model).tools(new _10_ServiceWithToolsExample.Calculator).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
    val question = "What is the square root of the sum of the numbers of letters in the words \"hello\" and \"world\"?"
    val answer = assistant.chat(question)
    System.out.println(answer)
    // The square root of the sum of the number of letters in the words "hello" and "world" is approximately 3.162.
  }
}