package example._1_basic_agent

import dev.langchain4j.agentic.AgenticServices
import dev.langchain4j.model.chat.ChatModel
import utils.*

import java.io.IOException
//import scala.util.{ChatModelProvider, StringLoader}
//import scala.util.log.{CustomLogging, LogLevels}

object _1a_Basic_Agent_Example { // 1. Define the model that will power the agent
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 2. Define the agent behavior in agent_interfaces/CvGenerator.java
    // 3. Create the agent using AgenticServices
    val cvGenerator: CvGenerator = AgenticServices.agentBuilder(classOf[CvGenerator]).chatModel(CHAT_MODEL).build() //.outputKey("masterCv") // we can optionally define the name of the output object.build
    // 4. Load text file from resources/documents/user_life_story.txt
    val lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt")
    // 5. We call the agent to generate the CV
    val cv = cvGenerator.generateCv(lifeStory)
    // 6. and print the generated CV
    System.out.println("=== CV ===")
    System.out.println(cv)
    // In example 1b we'll build the same agent but with structured output
  }

}