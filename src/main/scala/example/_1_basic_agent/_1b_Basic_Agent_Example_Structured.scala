package example._1_basic_agent

import dev.langchain4j.agentic.AgenticServices
import dev.langchain4j.model.chat.ChatModel
import domain.Cv

import java.io.IOException
import utils.{ChatModelProvider, CustomLogging, CvGeneratorStructuredOutput, LogLevels, StringLoader}

object _1b_Basic_Agent_Example_Structured { // 1. Define the model that will power the agent
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 2. Define the agent behavior in agent_interfaces/CvGeneratorStructuredOutput.java
    // 3. Create the agent using AgenticServices
    val cvGeneratorStructuredOutput = AgenticServices.agentBuilder(classOf[CvGeneratorStructuredOutput]).chatModel(CHAT_MODEL).build
    // 4. Load text file from resources/documents/user_life_story.txt
    val lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt")
    // 5. Retrieve a Cv object from the agent
    val cvStructured = cvGeneratorStructuredOutput.generateCv(lifeStory)
    System.out.println("\n\n=== CV OBJECT ===")
    System.out.println(cvStructured)
  }

}