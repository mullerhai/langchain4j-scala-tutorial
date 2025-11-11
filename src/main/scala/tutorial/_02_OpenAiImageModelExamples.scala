package tutorial

import dev.langchain4j.data.image.Image
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.image.ImageModel
import dev.langchain4j.model.openai.OpenAiImageModel
import dev.langchain4j.model.openai.OpenAiImageModelName.DALL_E_3
import dev.langchain4j.model.output.Response

object _02_OpenAiImageModelExamples {
  def main(args: Array[String]): Unit = {
    val model = OpenAiImageModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .modelName(ApiKeys.MODEL_NAME) //DALL_E_3)
      .build
    val response = model.generate("请帮我生成一份早餐烹饪攻略")//"Swiss software developers with cheese fondue, a parrot and a cup of coffee")
    System.out.println(response.content.url)
  }
}