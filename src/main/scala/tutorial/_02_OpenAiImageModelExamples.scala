package tutorial

import dev.langchain4j.data.image.Image
import dev.langchain4j.model.image.ImageModel
import dev.langchain4j.model.openai.OpenAiImageModel
import dev.langchain4j.model.openai.OpenAiImageModelName.DALL_E_3
import dev.langchain4j.model.output.Response

object _02_OpenAiImageModelExamples {
  def main(args: Array[String]): Unit = {
    val model = OpenAiImageModel.builder.apiKey(ApiKeys.OPENAI_API_KEY).modelName(DALL_E_3).build
    val response = model.generate("Swiss software developers with cheese fondue, a parrot and a cup of coffee")
    System.out.println(response.content.url)
  }
}