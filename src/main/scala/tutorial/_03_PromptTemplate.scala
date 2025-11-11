package tutorial

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.input.structured.{StructuredPrompt, StructuredPromptProcessor}
import dev.langchain4j.model.input.{Prompt, PromptTemplate}
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI

import java.time.Duration.ofSeconds
import java.util
import java.util.Arrays.asList

//object _03_PromptTemplate {
  object Simple_Prompt_Template_Example {
    def main(args: Array[String]): Unit = {
      val model = OpenAiChatModel.builder
        .baseUrl(ApiKeys.BASE_URL)
        .httpClientBuilder(new SpringRestClientBuilderFactory().create())
        .apiKey(ApiKeys.OPENAI_API_KEY).modelName(ApiKeys.MODEL_NAME).timeout(ofSeconds(60)).build
      val template = "Create a recipe for a {{dishType}} with the following ingredients: {{ingredients}}"
      val promptTemplate = PromptTemplate.from(template)
      val variables = new util.HashMap[String, AnyRef]
      variables.put("dishType", "oven dish")
      variables.put("ingredients", "potato, tomato, feta, olive oil")
      val prompt = promptTemplate.apply(variables)
      val response = model.chat(prompt.text)
      System.out.println(response)
    }
  }

  object Structured_Prompt_Template_Example {

    @StructuredPrompt(Array("Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}.", "Structure your answer in the following way:", "Recipe name: ...", "Description: ...", "Preparation time: ...", "Required ingredients:", "- ...", "- ...", "Instructions:", "- ...", "- ..."))
    class CreateRecipePrompt(var dish: String, var ingredients: util.List[String]) {
    }

//    @main
    def main(args: Array[String]): Unit = {
      val model = OpenAiChatModel.builder
        .baseUrl(ApiKeys.BASE_URL)
        .httpClientBuilder(new SpringRestClientBuilderFactory().create())
        .apiKey(ApiKeys.OPENAI_API_KEY)
        .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
        .timeout(ofSeconds(60))
        .build
      val createRecipePrompt = new Structured_Prompt_Template_Example.CreateRecipePrompt("salad", asList("cucumber", "tomato", "feta", "onion", "olives"))
      val prompt = StructuredPromptProcessor.toPrompt(createRecipePrompt)
      val recipe = model.chat(prompt.text)
      System.out.println(recipe)
    }
  }

  class Structured_Prompt_Template_Example {}
//}