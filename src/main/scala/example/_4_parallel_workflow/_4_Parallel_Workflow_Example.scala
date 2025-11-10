package example._4_parallel_workflow

import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.io.IOException
import java.util
import java.util.concurrent.Executors
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

object _4_Parallel_Workflow_Examples {
  /**
   * This example demonstrates how to implement 3 parallel CvReviewer agents that will
   * evaluate the CV simultaneously. We will implement three agents:
   * - ManagerCvReviewer (judges how well the candidate will likely do the job)
   * input: CV and job description
   * - TeamMemberCvReviewer (judges how well the candidate will fit in the team)
   * input: CV
   * - HrCvReviewer (checks if the candidate qualifies from HR point of view)
   * input: CV, HR requirements
   */
  // 1. Define the model that will power the agents
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 2. Define the three sub-agents in this package:
    //      - HrCvReviewer.java
    //      - ManagerCvReviewer.java
    //      - TeamMemberCvReviewer.java
    // 3. Create all agents using AgenticServices
    val hrCvReviewer = AgenticServices
      .agentBuilder(classOf[HrCvReviewer])
      .chatModel(CHAT_MODEL).outputKey("hrReview").build() // this will be overwritten in every iteration, and also be used as the final output we want to observe.build
    val managerCvReviewer = AgenticServices
      .agentBuilder(classOf[ManagerCvReviewer])
      .chatModel(CHAT_MODEL).outputKey("managerReview").build() // this overwrites the original input instructions, and is overwritten in every iteration and used as new instructions for the CvTailor.build
    val teamMemberCvReviewer = AgenticServices
      .agentBuilder(classOf[TeamMemberCvReviewer])
      .chatModel(CHAT_MODEL).outputKey("teamMemberReview").build() // this overwrites the original input instructions, and is overwritten in every iteration and used as new instructions for the CvTailor.build
    // 4. Build the sequence
    val executor = Executors.newFixedThreadPool(3) // keep a reference for later closing
    val cvReviewGenerator = AgenticServices.parallelBuilder()
      .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer) // this can be as many as you want
      .executor(executor) // optional, by default an internal cached thread pool is used which will automatically shut down after execution is completed
      .outputKey("fullCvReview") // use UntypedAgent unless you define the resulting composed agent, see _2_Sequential_Agent_Example.parallelBuilder.subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer)// this can be as many as you want.executor(executor)// optional, by default an internal cached thread pool is used which will automatically shut down after execution is completed.outputKey("fullCvReview")// this is the final output we want to observe.output((agenticScope) => {
      .output(agenticScope => {

        // read the outputs of each reviewer from the agentic scope
        val hrReview = agenticScope.readState("hrReview").asInstanceOf[CvReview]
        val managerReview = agenticScope.readState("managerReview").asInstanceOf[CvReview]
        val teamMemberReview = agenticScope.readState("teamMemberReview").asInstanceOf[CvReview]
        // return a bundled review with averaged score (or any other aggregation you want here)
        val feedback = String.join("\n", "HR Review: " + hrReview.feedback, "Manager Review: " + managerReview.feedback, "Team Member Review: " + teamMemberReview.feedback)
        val avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0
        new CvReview(avgScore, feedback)
      }).build()


    // 5. Load the original arguments from text files in resources/documents/
    val candidateCv: String = StringLoader.loadFromResource("/documents/tailored_cv.txt")
    val jobDescription: String = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    val hrRequirements: String = StringLoader.loadFromResource("/documents/hr_requirements.txt")
    val phoneInterviewNotes: String = StringLoader.loadFromResource("/documents/phone_interview_notes.txt")
    // 6. Because we use an untyped agent, we need to pass a map of arguments
    val arguments: util.Map[String, AnyRef] = util.Map.of("candidateCv", candidateCv, "jobDescription", jobDescription, "hrRequirements", hrRequirements, "phoneInterviewNotes", phoneInterviewNotes)
    // 7. Call the composed agent to generate the tailored CV
    val review: AnyRef = cvReviewGenerator.invoke(arguments)
    // 8. and print the generated CV
    System.out.println("=== REVIEWED CV ===")
    System.out.println(review)
    // 9. Shutdown executor
    executor.shutdown()
  }

}