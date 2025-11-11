package example._6_composed_workflow

import example._3_loop_workflow.{CvReviewer, ScoredCvTailor}
import example._4_parallel_workflow.{HrCvReviewer, ManagerCvReviewer, TeamMemberCvReviewer}
import example._5_conditional_workflow.{EmailAssistant, InterviewOrganizer, OrganizingTools}
import dev.langchain4j.agentic.scope.AgenticScope
import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.io.IOException
import java.util
import java.util.concurrent.Executors
import utils.{ChatModelProvider, CustomLogging, CvGenerator, LogLevels, StringLoader}

object _6_Composed_Workflow_Example {
  /**
   * Every agent, whether a single-task agent, a sequential workflow,..., is still an Agent object.
   * This makes agents fully composable. You can
   * - bundle smaller agents into super-agents
   * - decompose tasks with sub-agents
   * - mix sequential, parallel, loop, supervisor, ... workflows at any level
   * In this example, we’ll take the composed agents we built earlier (sequential, parallel, etc.)
   * and combine them into two larger composed agents that orchestrate the entire application process.
   */
  // 1. Define the model that will power the agents
  private val CHAT_MODEL = ChatModelProvider.createChatModel

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    ////////////////// CANDIDATE COMPOSED WORKFLOW //////////////////////
    // We'll go from life story > CV > Review > review loop until we pass
    // then email our CV to the company
    // 1. Create all necessary agents for candidate workflow
    val cvGenerator = AgenticServices.agentBuilder(classOf[CvGenerator]).chatModel(CHAT_MODEL).outputKey("cv").build
    val scoredCvTailor = AgenticServices.agentBuilder(classOf[ScoredCvTailor]).chatModel(CHAT_MODEL).outputKey("cv").build
    val cvReviewer = AgenticServices.agentBuilder(classOf[CvReviewer]).chatModel(CHAT_MODEL).outputKey("cvReview").build
    // 2. Create the loop workflow for CV improvement
    val cvImprovementLoop = AgenticServices.loopBuilder.subAgents(scoredCvTailor, cvReviewer).outputKey("cv").exitCondition((agenticScope) => {
      val review = agenticScope.readState("cvReview").asInstanceOf[CvReview]
      System.out.println("CV Review Score: " + review.score)
      if (review.score >= 0.8) System.out.println("CV is good enough, exiting loop.\n")
      review.score >= 0.8
    }).maxIterations(3).build
    // 3. Create the complete candidate workflow: Generate > Review > Improve Loop
    val candidateWorkflow = AgenticServices.sequenceBuilder(classOf[CandidateWorkflow]).subAgents(cvGenerator, cvReviewer, cvImprovementLoop).outputKey("cv").build
    // 4. Load input data
    val lifeStory = StringLoader.loadFromResource("/documents/user_life_story.txt")
    val jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    // 5. Execute the candidate workflow
    val candidateResult = candidateWorkflow.processCandidate(lifeStory, jobDescription)
    // Note that input parameters and intermediate parameters are all stored in one AgenticScope
    // that is available to all agents in the system, no matter how many levels of composition we have
    System.out.println("=== CANDIDATE WORKFLOW COMPLETED ===")
    System.out.println("Final CV: " + candidateResult)
    System.out.println("\n\n\n\n")
    ////////////////// HIRING TEAM COMPOSED WORKFLOW //////////////////////
    // We receive an email with the candidate CV and contacts. We did the phone HR interview.
    // We now go through the 3 parallel reviews then send that result into the conditional flow to invite or reject.
    // 1. Create all necessary agents for hiring team workflow
    val hrCvReviewer = AgenticServices.agentBuilder(classOf[HrCvReviewer]).chatModel(CHAT_MODEL).outputKey("hrReview").build
    val managerCvReviewer = AgenticServices.agentBuilder(classOf[ManagerCvReviewer]).chatModel(CHAT_MODEL).outputKey("managerReview").build
    val teamMemberCvReviewer = AgenticServices.agentBuilder(classOf[TeamMemberCvReviewer]).chatModel(CHAT_MODEL).outputKey("teamMemberReview").build
    val emailAssistant = AgenticServices.agentBuilder(classOf[EmailAssistant]).chatModel(CHAT_MODEL).tools(new OrganizingTools).build
    val interviewOrganizer = AgenticServices.agentBuilder(classOf[InterviewOrganizer]).chatModel(CHAT_MODEL).tools(new OrganizingTools).build
    // 2. Create parallel review workflow
    val parallelReviewWorkflow = AgenticServices.parallelBuilder.subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer).executor(Executors.newFixedThreadPool(3)).outputKey("combinedCvReview").output((agenticScope) => {
      val hrReview = agenticScope.readState("hrReview").asInstanceOf[CvReview]
      val managerReview = agenticScope.readState("managerReview").asInstanceOf[CvReview]
      val teamMemberReview = agenticScope.readState("teamMemberReview").asInstanceOf[CvReview]
      val feedback = String.join("\n", "HR Review: " + hrReview.feedback, "Manager Review: " + managerReview.feedback, "Team Member Review: " + teamMemberReview.feedback)
      val avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0
      System.out.println("Final averaged CV Review Score: " + avgScore + "\n")
      new CvReview(avgScore, feedback)
    }).build
    // 3. Create conditional workflow for final decision
    val decisionWorkflow = AgenticServices.conditionalBuilder.subAgents((agenticScope: AgenticScope) => agenticScope.readState("combinedCvReview").asInstanceOf[CvReview].score >= 0.8, interviewOrganizer).subAgents((agenticScope: AgenticScope) => agenticScope.readState("combinedCvReview").asInstanceOf[CvReview].score < 0.8, emailAssistant).build
    // 4. Create complete hiring team workflow: Parallel Review → Decision
    val hiringTeamWorkflow = AgenticServices.sequenceBuilder(classOf[HiringTeamWorkflow]).subAgents(parallelReviewWorkflow, decisionWorkflow).build
    // 5. Load input data
    val candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt")
    val candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt")
    val hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt")
    val phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt")
    // Put all data in a Map for easy access
    val inputData = util.Map.of("candidateCv", candidateCv, "candidateContact", candidateContact, "hrRequirements", hrRequirements, "phoneInterviewNotes", phoneInterviewNotes, "jobDescription", jobDescription)
    // 6. Execute the hiring team workflow
    hiringTeamWorkflow.processApplication(candidateCv, jobDescription, hrRequirements, phoneInterviewNotes, candidateContact)
    System.out.println("=== HIRING TEAM WORKFLOW COMPLETED ===")
    System.out.println("Parallel reviews completed and decision made")
    // Note: as workflows become more complex, make sure that names of input, intermediate and output parameters
    // are unique to avoid inadvertent overwriting of data in the shared AgenticScope
  }

  try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls
}