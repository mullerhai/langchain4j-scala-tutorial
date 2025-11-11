package example._2_sequential_workflow

import dev.langchain4j.agentic.AgenticServices
import dev.langchain4j.agentic.scope.{AgenticScope, ResultWithAgenticScope}
import dev.langchain4j.model.chat.ChatModel
import utils.{AgenticScopePrinter, ChatModelProvider, CustomLogging, CvGenerator, LogLevels, StringLoader}

import java.io.IOException
import java.util

object _2b_Sequential_Agent_Example_Typed {
  /**
   * We'll implement the same sequential workflow as in 2a, but this time we'll
   * - use a typed interface for the composed agent (SequenceCvGenerator)
   * - which will allow us to use its method with arguments instead of .invoke(argsMap)
   * - collect the output in a custom way
   * - retrieve and inspect the AgenticScope after invocation, for debugging or testing purposes
   */
  // 1. Define the model that will power the agents


  val CHAT_MODEL = ChatModelProvider.createChatModel
  CustomLogging.setLevel(LogLevels.PRETTY, 150)
  @throws[IOException]
  def main(args: Array[String]): Unit = {

    // 2. Define the sequential agent interface in this package:
    //      - SequenceCvGenerator.java
    // with method signature:
    // ResultWithAgenticScope<Map<String, String>> generateTailoredCv(@V("lifeStory") String lifeStory, @V("instructions") String instructions);
    // 3. Create both sub-agents using AgenticServices like before
    val cvGenerator: CvGenerator = AgenticServices.agentBuilder(classOf[CvGenerator]).chatModel(CHAT_MODEL).outputKey("masterCv").build() //.outputKey("masterCv") // if you want to pass this variable from agent 1 to agent 2,.build
    val cvTailor = AgenticServices.agentBuilder(classOf[CvTailor]).chatModel(CHAT_MODEL).outputKey("tailoredCv").build() // note that it is also possible to use a different model for a different agent.outputKey("tailoredCv")// we need to define the name of the output object.build
    // 4. Load the arguments from text files in resources/documents/
    // (no need to put them in a Map this time)
    // - user_life_story.txt
    // - job_description_backend.txt
    val lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt")
    val instructions = "Adapt the CV to the job description below." + StringLoader.loadFromResource("/documents/job_description_backend.txt")
    // 5. Build the typed sequence with custom output handling
//    val bothCvsAndLifeStory2 = Map("lifeStory"-> agenticScope.readState("lifeStory", ""), "masterCv" -> agenticScope.readState("masterCv", ""), "tailoredCv" -> agenticScope.readState("tailoredCv", ""))

    val sequenceCvGenerator = AgenticServices.sequenceBuilder(classOf[SequenceCvGenerator]).subAgents(cvGenerator, cvTailor).outputKey("bothCvsAndLifeStory")
      .output(
        (agenticScope) => {
          // any method is possible, but we collect some internal variables.
//          bothCvsAndLifeStory2
          Map("lifeStory"-> agenticScope.readState("lifeStory", ""), "masterCv" -> agenticScope.readState("masterCv", ""), "tailoredCv" -> agenticScope.readState("tailoredCv", ""))
        }
      ).
      build() // here we specify the typed interface.subAgents(cvGenerator, cvTailor).outputKey("bothCvsAndLifeStory").output((agenticScope) => {
    // any method is possible, but we collect some internal variables.

//    bothCvsAndLifeStory
  

//  ).build()
  // 6. Call the typed composed agent
    val bothCvsAndScope: ResultWithAgenticScope[util.Map[String, String]] = sequenceCvGenerator.generateTailoredCv(lifeStory, instructions)
  // 7. Extract result and agenticScope
    val agenticScope: AgenticScope = bothCvsAndScope.agenticScope
    val bothCvsAndLifeStory: util.Map[String, String] = bothCvsAndScope.result
    println("=== USER INFO (input) ===")
    val userStory: String = bothCvsAndLifeStory.get("lifeStory")
    System.out.println(if (userStory.length > 100) userStory.substring(0, 100) + " [truncated...]"
    else lifeStory)
    System.out.println("=== MASTER CV TYPED (intermediary variable) ===")
    val masterCv: String = bothCvsAndLifeStory.get("masterCv")
    System.out.println(if (masterCv.length > 100) masterCv.substring(0, 100) + " [truncated...]"
    else masterCv)
    System.out.println("=== TAILORED CV TYPED (output) ===")
    val tailoredCv: String = bothCvsAndLifeStory.get("tailoredCv")
    System.out.println(if (tailoredCv.length > 100) tailoredCv.substring(0, 100) + " [truncated...]"
    else tailoredCv)
  // Both untyped and typed agents give the same tailoredCv result
  // (any differences are due to the non-deterministic nature of LLMs),
  // but the typed agent is more elegant to use and safer because of compile-time type checking
    System.out.println("=== AGENTIC SCOPE ===")
    System.out.println(AgenticScopePrinter.printPretty(agenticScope, 100))
  // this will return this object (filled out):
  // AgenticScope {
  //     memoryId = "e705028d-e90e-47df-9709-95953e84878c",
  //             state = {
  //                     bothCvsAndLifeStory = { // output
  //                             masterCv = "...",
  //                            lifeStory = "...",
  //                            tailoredCv = "..."
  //                     },
  //                     instructions = "...", // inputs and intermediary variables
  //                     tailoredCv = "...",
  //                     masterCv = "...",
  //                     lifeStory = "..."
  //             }
  // }
    System.out.println("=== CONTEXT AS CONVERSATION (all messages in the conversation) ===")
    System.out.println(AgenticScopePrinter.printConversation(agenticScope.contextAsConversation(), 100))
  }

 // control how much you see from the model calls
}