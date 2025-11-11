package example._3_loop_workflow

import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.io.IOException
import java.util
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

object _3a_Loop_Agent_Examples {
  /**
   * This example demonstrates how to implement a CvReviewer agent that we can add to a loop
   * with our CvTailor agent. We will implement two agents:
   * - ScoredCvTailor (takes in a CV and tailors it to a CvReview (feedback/instruction + score))
   * - CvReviewer (takes in the tailored CV and job description, and returns a CvReview object (feedback + score)
   * Additionally, the loop ends when the score is above a certain threshold (e.g. 0.7) (exit condition)
   */
  // 1. Define the model that will power the agents
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  CustomLogging.setLevel(LogLevels.PRETTY, 300)
  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 2. Define the two sub-agents in this package:
    //      - CvReviewer.java
    //      - CvTailor.java
    // 3. Create all agents using AgenticServices
    val cvReviewer = AgenticServices
      .agentBuilder(classOf[CvReviewer])
      .chatModel(CHAT_MODEL).outputKey("cvReview").build() // this gets updated in every iteration with new feedback for the next tailoring.build
    val scoredCvTailor = AgenticServices
      .agentBuilder(classOf[ScoredCvTailor])
      .chatModel(CHAT_MODEL).outputKey("cv").build() // this will be updated in every iteration, continuously improving the CV.build
    // 4. Build the sequence
    val reviewedCvGenerator:UntypedAgent  = AgenticServices
      .loopBuilder()// use UntypedAgent unless you define the resulting composed agent, see _2_Sequential_Agent_Example.loopBuilder
     .subAgents(cvReviewer, scoredCvTailor)// this can be as many as you want, order matters.outputKey("cv")// this is the final output we want to observe (the improved CV).exitCondition((agenticScope) => {
      .outputKey("cv")
      .exitCondition( agenticScope =>{
        val review = agenticScope.readState("cvReview").asInstanceOf[CvReview]
        System.out.println("Checking exit condition with score=" + review.score) // we log intermediary scores
        review.score > 0.8
      }).maxIterations(3).build()

    // 5. Load the original arguments from text files in resources/documents/
    // - master_cv.txt
    // - job_description_backend.txt
    val masterCv: String = StringLoader.loadFromResource("/documents/master_cv.txt")
    val jobDescription: String = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    // 5. Because we use an untyped agent, we need to pass a map of arguments
    val arguments: util.Map[String, AnyRef] = util.Map.of("cv", masterCv, // start with the master CV, it will be continuously improved
      "jobDescription", jobDescription)
    // 5. Call the composed agent to generate the tailored CV
    val tailoredCv: String = reviewedCvGenerator.invoke(arguments).asInstanceOf[String]
    // 6. and print the generated CV
    System.out.println("=== REVIEWED CV UNTYPED ===")
    System.out.println(tailoredCv.asInstanceOf[String])
  }

   // exit condition based on the score given by the CvReviewer agent, when > 0.8 we are satisfied.maxIterations(3)// safety to avoid infinite loops, in case exit condition is never met.build

  // this CV probably passes after the first tailoring + review round
  // if you want to see it fail, try with the flute teacher jobDescription
  // as in example 3b, where we also inspect intermediary states of the CV
  // and retrieve the final review and score as well.


//try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls
}