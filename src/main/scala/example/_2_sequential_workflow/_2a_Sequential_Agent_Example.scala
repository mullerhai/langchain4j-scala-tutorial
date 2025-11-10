package example._2_sequential_workflow

import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.model.chat.ChatModel
import utils.{ChatModelProvider, CustomLogging, CvGenerator, LogLevels, StringLoader}

import java.io.IOException
import java.util

object _2a_Sequential_Agent_Example {
  /**
   * This example demonstrates how to implement two agents:
   * - CvGenerator (takes in a life story and generates a complete master CV)
   * - CvTailor (takes in the master CV and tailors it to specific instructions (job description, feedback, ...)
   * Then we will call them one after in a fixed workflow
   * using the sequenceBuilder, and demonstrate how to pass a parameter between them.
   * When combining multiple agents, all input, intermediary, and output parameters and the call chain are
   * stored in the AgenticScope, which is accessible for advanced use cases.
   */
  // 1. Define the model that will power the agents
    private val CHAT_MODEL = ChatModelProvider.createChatModel

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 2. Define the two sub-agents in this package:
    //      - CvGenerator.java
    //      - CvTailor.java
    // 3. Create both agents using AgenticServices
    val cvGenerator = AgenticServices.agentBuilder(classOf[CvGenerator]).chatModel(CHAT_MODEL).outputKey("masterCv").build() // if you want to pass this variable from agent 1 to agent 2,.build
    val cvTailor = AgenticServices.agentBuilder(classOf[CvTailor]).chatModel(CHAT_MODEL).outputKey("tailoredCv").build() // note that it is also possible to use a different model for a different agent.outputKey("tailoredCv")// we need to define the name of the output object.build
    ////////////////// UNTYPED EXAMPLE //////////////////////
    // 4. Build the sequence
    val tailoredCvGenerator = AgenticServices // use UntypedAgent unless you define the resulting composed agent, see below
      .sequenceBuilder()
      .subAgents(cvGenerator, cvTailor) // this can be as many as you want, order matters
      .outputKey("tailoredCv") // this is the final output of the composed agent
      // note that you can use as output any field that is part of the AgenticScope
      // for example you could output 'masterCv' instead of tailoredCv (even if in this case that makes no sense)
      .build();
    // use UntypedAgent unless you define the resulting composed agent, see below.sequenceBuilder.subAgents(cvGenerator, cvTailor)// this can be as many as you want, order matters.outputKey("tailoredCv")// this is the final output of the composed agent.build
    // 4. Load the arguments from text files in resources/documents/
    // - user_life_story.txt
    // - job_description_backend.txt
    val lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt")
    val instructions = "Adapt the CV to the job description below." + StringLoader.loadFromResource("/documents/job_description_backend.txt")
    // 5. Because we use an untyped agent, we need to pass a map of arguments
    val arguments = util.Map.of("lifeStory", lifeStory.asInstanceOf[Object], // matches the variable name in agent_interfaces/CvGenerator.java
      "instructions", instructions.asInstanceOf[Object]) // matches the variable name in agent_interfaces/CvTailor.java)
    // 5. Call the composed agent to generate the tailored CV
    val tailoredCv = tailoredCvGenerator.invoke(arguments).asInstanceOf[String]
    // 6. and print the generated CV
    System.out.println("=== TAILORED CV UNTYPED ===")
    System.out.println(tailoredCv.asInstanceOf[String]) // you can observe that the CV looks very different

    // when you'd use job_description_fullstack.txt as input
    // In example 2b we'll build the same sequential agent but with typed output,
    // and we'll inspect the AgenticScope
  }

  try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls
}