package tutorial

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.input.structured.StructuredPrompt
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.model.output.structured.Description
import dev.langchain4j.service.*
import tutorial.Hotel_Review_AI_Service_Example.IssueCategory.IssueCategory
import tutorial.Sentiment_Extracting_AI_Service_Example.Sentiment.Sentiment

import java.math.{BigDecimal, BigInteger}
import java.time.Duration.ofSeconds
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util
import java.util.Arrays.asList

//object _08_AIServiceExamples {
  val model: ChatModel = OpenAiChatModel.builder
    .baseUrl(ApiKeys.BASE_URL)
    .apiKey(ApiKeys.OPENAI_API_KEY)
    .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
    .timeout(ofSeconds(60))
    .httpClientBuilder(new SpringRestClientBuilderFactory().create())
    .build

  ////////////////// SIMPLE EXAMPLE //////////////////////
  object Simple_AI_Service_Example {
    trait Assistant {
      def chat(message: String): String
    }

    def main(args: Array[String]): Unit = {
      val assistant = AiServices.create(classOf[Assistant], model)
      val userMessage = "Translate 'Plus-Values des cessions de valeurs mobilières, de droits sociaux et gains assimilés'"
      val answer = assistant.chat(userMessage)
      System.out.println(answer)
    }
  }

  ////////////////// WITH MESSAGE AND VARIABLES //////////////////////
  object AI_Service_with_System_Message_Example {
    trait Chef {
      @SystemMessage(Array("You are a professional chef. You are friendly, polite and concise.")) 
      def answer(question: String): String
    }

    def main(args: Array[String]): Unit = {
      val chef = AiServices.create(classOf[Chef], model)
      val answer = chef.answer("How long should I grill chicken?")
      System.out.println(answer) // Grilling chicken usually takes around 10-15 minutes per side ...
    }
  }

  object AI_Service_with_System_and_User_Messages_Example {
    trait TextUtils {
      @SystemMessage(Array("You are a professional translator into {{language}}"))
      @UserMessage(Array("Translate the following text: {{text}}")) 
      def translate(@V("text") text: String, @V("language") language: String): String

      @SystemMessage(Array("Summarize every message from user in {{n}} bullet points. Provide only bullet points.")) 
      def summarize(@UserMessage text: String, @V("n") n: Int): util.List[String]
    }

    def main(args: Array[String]): Unit = {
      val utils = AiServices.create(classOf[TextUtils], model)
      val translation = utils.translate("Hello, how are you?", "italian")
      System.out.println(translation) // Ciao, come stai?
      val text = "AI, or artificial intelligence, is a branch of computer science that aims to create " + "machines that mimic human intelligence. This can range from simple tasks such as recognizing " + "patterns or speech to more complex tasks like making decisions or predictions."
      val bulletPoints = utils.summarize(text, 3)
      bulletPoints.forEach(System.out.println)
      // [
      // "- AI is a branch of computer science",
      // "- It aims to create machines that mimic human intelligence",
      // "- It can perform simple or complex tasks"
      // ]
    }
  }

  //////////////////// EXTRACTING DIFFERENT DATA TYPES ////////////////////
  object Sentiment_Extracting_AI_Service_Example {
    object Sentiment extends Enumeration {
      type Sentiment = Value
      val POSITIVE, NEUTRAL, NEGATIVE = Value
    }

    trait SentimentAnalyzer {
      @UserMessage(Array("Analyze sentiment of {{it}}")) 
      def analyzeSentimentOf(text: String): Sentiment

      @UserMessage(Array("Does {{it}} have a positive sentiment?")) 
      def isPositive(text: String): Boolean
    }

    def main(args: Array[String]): Unit = {
      val sentimentAnalyzer = AiServices.create(classOf[SentimentAnalyzer], model)
      val sentiment = sentimentAnalyzer.analyzeSentimentOf("It is good!")
      System.out.println(sentiment) // POSITIVE
      val positive = sentimentAnalyzer.isPositive("It is bad!")
      System.out.println(positive) // false
    }
  }

  object Hotel_Review_AI_Service_Example {
    object IssueCategory extends Enumeration {
      type IssueCategory = Value
      val MAINTENANCE_ISSUE, SERVICE_ISSUE, COMFORT_ISSUE, FACILITY_ISSUE, CLEANLINESS_ISSUE, CONNECTIVITY_ISSUE, CHECK_IN_ISSUE, OVERALL_EXPERIENCE_ISSUE = Value
    }

    trait HotelReviewIssueAnalyzer {
      @UserMessage(Array("Please analyse the following review: |||{{it}}|||")) 
      def analyzeReview(review: String): util.List[IssueCategory]
    }

    def main(args: Array[String]): Unit = {
      val hotelReviewIssueAnalyzer = AiServices.create(classOf[HotelReviewIssueAnalyzer], model)
      val review = "Our stay at hotel was a mixed experience. The location was perfect, just a stone's throw away " + "from the beach, which made our daily outings very convenient. The rooms were spacious and well-decorated, " + "providing a comfortable and pleasant environment. However, we encountered several issues during our " + "stay. The air conditioning in our room was not functioning properly, making the nights quite uncomfortable. " + "Additionally, the room service was slow, and we had to call multiple times to get extra towels. Despite the " + "friendly staff and enjoyable breakfast buffet, these issues significantly impacted our stay."
      val issueCategories = hotelReviewIssueAnalyzer.analyzeReview(review)
      // Should output [MAINTENANCE_ISSUE, SERVICE_ISSUE, COMFORT_ISSUE, OVERALL_EXPERIENCE_ISSUE]
      System.out.println(issueCategories)
    }
  }

  object Number_Extracting_AI_Service_Example {
    trait NumberExtractor {
      @UserMessage(Array("Extract number from {{it}}")) 
      def extractInt(text: String): Int

      @UserMessage(Array("Extract number from {{it}}")) 
      def extractLong(text: String): Long

      @UserMessage(Array("Extract number from {{it}}")) 
      def extractBigInteger(text: String): BigInteger

      @UserMessage(Array("Extract number from {{it}}")) 
      def extractFloat(text: String): Float

      @UserMessage(Array("Extract number from {{it}}")) 
      def extractDouble(text: String): Double

      @UserMessage(Array("Extract number from {{it}}")) 
      def extractBigDecimal(text: String): BigDecimal
    }

    def main(args: Array[String]): Unit = {
      val extractor = AiServices.create(classOf[NumberExtractor], model)
      val text = "After countless millennia of computation, the supercomputer Deep Thought finally announced " + "that the answer to the ultimate question of life, the universe, and everything was forty two."
      val intNumber = extractor.extractInt(text)
      System.out.println(intNumber) // 42
      val longNumber = extractor.extractLong(text)
      System.out.println(longNumber) // 42
      val bigIntegerNumber = extractor.extractBigInteger(text)
      System.out.println(bigIntegerNumber) // 42
      val floatNumber = extractor.extractFloat(text)
      System.out.println(floatNumber) // 42.0
      val doubleNumber = extractor.extractDouble(text)
      System.out.println(doubleNumber) // 42.0
      val bigDecimalNumber = extractor.extractBigDecimal(text)
      System.out.println(bigDecimalNumber) // 42.0
    }
  }

  object Date_and_Time_Extracting_AI_Service_Example {
    trait DateTimeExtractor {
      @UserMessage(Array("Extract date from {{it}}")) 
      def extractDateFrom(text: String): LocalDate

      @UserMessage(Array("Extract time from {{it}}")) 
      def extractTimeFrom(text: String): LocalTime

      @UserMessage(Array("Extract date and time from {{it}}")) 
      def extractDateTimeFrom(text: String): LocalDateTime
    }

    def main(args: Array[String]): Unit = {
      val extractor = AiServices.create(classOf[DateTimeExtractor], model)
      val text = "The tranquility pervaded the evening of 1968, just fifteen minutes shy of midnight," + " following the celebrations of Independence Day."
      val date = extractor.extractDateFrom(text)
      System.out.println(date) // 1968-07-04
      val time = extractor.extractTimeFrom(text)
      System.out.println(time) // 23:45
      val dateTime = extractor.extractDateTimeFrom(text)
      System.out.println(dateTime) // 1968-07-04T23:45
    }
  }

  object POJO_Extracting_AI_Service_Example {
    case class Person(@Description(Array("first name of a person"))firstName: String, lastName: String, birthDate: LocalDate){
//      @Description(Array("first name of a person")) // you can add an optional description to help an LLM have a better understanding 
//      val firstName: String = null
//       val lastName = null
//      private val birthDate: LocalDate = null

      override def toString: String = "Person {" + " firstName = \"" + firstName + "\"" + ", lastName = \"" + lastName + "\"" + ", birthDate = " + birthDate + " }"
    }

    trait PersonExtractor {
      @UserMessage(Array("Extract a person from the following text: {{it}}")) 
      def extractPersonFrom(text: String): POJO_Extracting_AI_Service_Example.Person
    }

    def main(args: Array[String]): Unit = {
      val model = OpenAiChatModel.builder.baseUrl(ApiKeys.BASE_URL)
        .apiKey(ApiKeys.OPENAI_API_KEY)
        .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
        .httpClientBuilder(new SpringRestClientBuilderFactory().create())
        .responseFormat("json_schema").strictJsonSchema(true)
        .timeout(ofSeconds(60)).build() // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode.timeout(ofSeconds(60)).build
      val extractor = AiServices.create(classOf[PersonExtractor], model)
      val text = "In 1968, amidst the fading echoes of Independence Day, " + "a child named John arrived under the calm evening sky. " + "This newborn, bearing the surname Doe, marked the start of a new journey."
      val person = extractor.extractPersonFrom(text)
      System.out.println(person) // Person { firstName = "John", lastName = "Doe", birthDate = 1968-07-04 }
    }
  }

  ////////////////////// DESCRIPTIONS ////////////////////////
  object POJO_With_Descriptions_Extracting_AI_Service_Example {
    case class Recipe(@Description(Array("short title, 3 words maximum")) title: String, 
                      @Description(Array("short description, 2 sentences maximum")) description: String,
                      @Description(Array("each step should be described in 6 to 8 words, steps should rhyme with each other"))steps: List[String], 
                      preparationTimeMinutes: Integer) {
//      @Description(Array("short title, 3 words maximum")) private val title = null
//      @Description(Array("short description, 2 sentences maximum")) private val description = null
//      @Description(Array("each step should be described in 6 to 8 words, steps should rhyme with each other")) private val steps = null
//      private val preparationTimeMinutes: Integer = null

      override def toString: String = "Recipe {" + " title = \"" + title + "\"" + ", description = \"" + description + "\"" + ", steps = " + steps + ", preparationTimeMinutes = " + preparationTimeMinutes + " }"
    }

    @StructuredPrompt(Array("Create a recipe of a {{dish}} that can be prepared using only {{ingredients}}")) 
    class CreateRecipePrompt {
       var dish: String = null
       var ingredients: List[String] = null
    }

    trait Chef {
      def createRecipeFrom(ingredients: String*): POJO_With_Descriptions_Extracting_AI_Service_Example.Recipe

      def createRecipe(prompt: POJO_With_Descriptions_Extracting_AI_Service_Example.CreateRecipePrompt): POJO_With_Descriptions_Extracting_AI_Service_Example.Recipe
    }

    def main(args: Array[String]): Unit = {
      val model = OpenAiChatModel.builder
        .baseUrl(ApiKeys.BASE_URL)
        .apiKey(ApiKeys.OPENAI_API_KEY)
        .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
        .responseFormat("json_schema")
        .strictJsonSchema(true)
        .timeout(ofSeconds(60))
        .httpClientBuilder(new SpringRestClientBuilderFactory().create())
        .build() // https://docs.langchain4j.dev/integrations/language-models/open-ai#structured-outputs-for-json-mode.timeout(ofSeconds(60)).build
      val chef = AiServices.create(classOf[Chef], model)
      val recipe = chef.createRecipeFrom("cucumber", "tomato", "feta", "onion", "olives", "lemon")
      System.out.println(recipe)
      // Recipe {
      // title = "Greek Salad",
      // description = "A refreshing mix of veggies and feta cheese in a zesty
      // dressing.",
      // steps = [
      // "Chop cucumber and tomato",
      // "Add onion and olives",
      // "Crumble feta on top",
      // "Drizzle with dressing and enjoy!"
      // ],
      // preparationTimeMinutes = 10
      // }
      val prompt = new CreateRecipePrompt
      prompt.dish = "oven dish"
      prompt.ingredients = List("cucumber", "tomato", "feta", "onion", "olives", "potatoes")
      val anotherRecipe = chef.createRecipe(prompt)
      System.out.println(anotherRecipe)
      // Recipe ...
    }
  }

  ////////////////////////// WITH MEMORY /////////////////////////
  object ServiceWithMemoryExample {
    trait Assistant {
      def chat(message: String): String
    }

    def main(args: Array[String]): Unit = {
      val chatMemory = MessageWindowChatMemory.withMaxMessages(10)
      val assistant = AiServices.builder(classOf[ServiceWithMemoryExample.Assistant]).chatModel(model).chatMemory(chatMemory).build
      val answer = assistant.chat("Hello! My name is Klaus.")
      System.out.println(answer) // Hello Klaus! How can I assist you today?
      val answerWithName = assistant.chat("What is my name?")
      System.out.println(answerWithName) // Your name is Klaus.
    }
  }

  object ServiceWithMemoryForEachUserExample {
    trait Assistant {
      def chat(@MemoryId memoryId: Int, @UserMessage userMessage: String): String
    }

    def main(args: Array[String]): Unit = {
      val assistant = AiServices.builder(classOf[ServiceWithMemoryForEachUserExample.Assistant]).chatModel(model).chatMemoryProvider((memoryId: AnyRef) => MessageWindowChatMemory.withMaxMessages(10)).build
      System.out.println(assistant.chat(1, "Hello, my name is Klaus"))
      // Hi Klaus! How can I assist you today?
      System.out.println(assistant.chat(2, "Hello, my name is Francine"))
      // Hello Francine! How can I assist you today?
      System.out.println(assistant.chat(1, "What is my name?"))
      // Your name is Klaus.
      System.out.println(assistant.chat(2, "What is my name?"))
      // Your name is Francine.
    }
  }
//}