package example._8_non_ai_agents

import example._4_parallel_workflow.{HrCvReviewer, ManagerCvReviewer, TeamMemberCvReviewer}
import example._5_conditional_workflow.{EmailAssistant, InterviewOrganizer, OrganizingTools}
import dev.langchain4j.agentic.scope.AgenticScope
import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.agentic.supervisor.{SupervisorAgent, SupervisorContextStrategy, SupervisorResponseStrategy}
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.io.IOException
import java.util
import java.util.concurrent.Executors
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

object _8_Non_AI_Agents {
  /**
   * Here we how to use non-AI agents (plain Java operators) within agentic workflows.
   * Non-AI agents are simply methods, but can be used as any other type of agent.
   * They are perfect for deterministic operations like calculations, data transformations,
   * and aggregations, where you rather have no LLM involvement.
   * The more steps you can outsource to non-AI agents, the faster, correcter and cheaper your workflows will be.
   * Non-AI agents are preferred over tools for workflows where you want to enforce determinism for certain steps.
   * In this case we want the aggregated score of the reviewers to be calculated deterministically, not by an LLM.
   * We also update the application status in the database deterministically based on the aggregated score.
   */
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 100) // control how much you see from the model calls

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 1. Define the ScoreAggregator non-AI agents in this pacckage
    // 2. Build the AI sub-agents for the parallel review step
    val hrReviewer = AgenticServices.agentBuilder(classOf[HrCvReviewer]).chatModel(CHAT_MODEL).outputKey("hrReview").build
    val managerReviewer = AgenticServices.agentBuilder(classOf[ManagerCvReviewer]).chatModel(CHAT_MODEL).outputKey("managerReview").build
    val teamReviewer = AgenticServices.agentBuilder(classOf[TeamMemberCvReviewer]).chatModel(CHAT_MODEL).outputKey("teamMemberReview").build
    // 3. Build the composed parallel agent
    val executor = Executors.newFixedThreadPool(3) // keep a reference for later closing
    val parallelReviewWorkflow = AgenticServices.parallelBuilder.subAgents(hrReviewer, managerReviewer, teamReviewer).executor(executor).build
    // 4. Build the full workflow incl. non-AI agent
    val collectFeedback = AgenticServices.sequenceBuilder.subAgents(parallelReviewWorkflow, new ScoreAggregator, // no AgenticServices builder needed for non-AI agents. outputname 'combinedCvReview' is defined in the class
      new StatusUpdate, // takes 'combinedCvReview' as input, no output needed
      AgenticServices.agentAction((agenticScope: AgenticScope) => {
        // another way to add non-AI agents that can operate on the AgenticScope
        val review = agenticScope.readState("combinedCvReview").asInstanceOf[CvReview]
        agenticScope.writeState("scoreAsPercentage", review.score * 100) // when agents from different systems communicate, output conversion is often needed
      })).outputKey("scoreAsPercentage").build() // outputName defined on the non-AI agent annotation in ScoreAggregator.java.build
    // 5. Load input data
    val candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt")
    val candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt")
    val hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt")
    val phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt")
    val jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    val arguments = util.Map.of("candidateCv", candidateCv.asInstanceOf[Object], "candidateContact", candidateContact.asInstanceOf[Object], "hrRequirements", hrRequirements.asInstanceOf[Object], "phoneInterviewNotes", phoneInterviewNotes.asInstanceOf[Object], "jobDescription", jobDescription.asInstanceOf[Object])
    // 6. Invoke the workflow
    val scoreAsPercentage = collectFeedback.invoke(arguments).asInstanceOf[Double]
    executor.shutdown()
    System.out.println("=== SCORE AS PERCENTAGE ===")
    System.out.println(scoreAsPercentage)
    // as we can see in the logs, the application status has also been updated accordingly
  }

}