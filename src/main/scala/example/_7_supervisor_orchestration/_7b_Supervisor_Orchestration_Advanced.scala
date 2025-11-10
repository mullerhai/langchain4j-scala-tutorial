package example._7_supervisor_orchestration

import _4_parallel_workflow.{HrCvReviewer, ManagerCvReviewer, TeamMemberCvReviewer}
import _5_conditional_workflow.{EmailAssistant, InterviewOrganizer, OrganizingTools}
import dev.langchain4j.agentic.AgenticServices
import dev.langchain4j.agentic.scope.ResultWithAgenticScope
import dev.langchain4j.agentic.supervisor.{SupervisorContextStrategy, SupervisorResponseStrategy}
import dev.langchain4j.model.chat.ChatModel

import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

/**
 * Advanced Supervisor Example with explicit AgenticScope to inspect evolving context
 */
object _7b_Supervisor_Orchestration_Advanced {
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 200)
  /**
   * In this example we build a similar supervisor as in _7a_Supervisor_Orchestration,
   * but we explore a number of extra features of the Supervisor:
   * - typed supervisor,
   * - context engineering,
   * - output strategies,
   * - call chain observation,
   * - context evolution inspection
   */
  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 1. Define subagents
    val hrReviewer = AgenticServices.agentBuilder(classOf[HrCvReviewer]).chatModel(CHAT_MODEL).build
    val managerReviewer = AgenticServices.agentBuilder(classOf[ManagerCvReviewer]).chatModel(CHAT_MODEL).build
    val teamReviewer = AgenticServices.agentBuilder(classOf[TeamMemberCvReviewer]).chatModel(CHAT_MODEL).build
    val interviewOrganizer = AgenticServices.agentBuilder(classOf[InterviewOrganizer]).chatModel(CHAT_MODEL).tools(new OrganizingTools).outputKey("response").build
    val emailAssistant = AgenticServices.agentBuilder(classOf[EmailAssistant]).chatModel(CHAT_MODEL).tools(new OrganizingTools).outputKey("response").build
    // 2. Build supervisor
    val hiringSupervisor = AgenticServices.supervisorBuilder(classOf[HiringSupervisor]).chatModel(CHAT_MODEL)
      .subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant)
      .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION)
      .responseStrategy(SupervisorResponseStrategy.SCORED).build() // this strategy uses a scorer model to decide weather the LAST response or the SUMMARY solves the user request best.supervisorContext("Policy: Always check HR first, escalate if needed, reject low-fit.").build
    // 3. Load input data
    val jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    val candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt")
    val candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt")
    val hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt")
    val phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt")
    val request = "Evaluate this candidate and either schedule an interview or send a rejection email.\n" + "Candidate CV:\n" + candidateCv + "\n" + "Candidate Contacts:\n" + candidateContact + "\n" + "Job Description:\n" + jobDescription + "\n" + "HR Requirements:\n" + hrRequirements + "\n" + "Phone Interview Notes:\n" + phoneInterviewNotes
    // 4. Invoke supervisor
    val start = System.nanoTime
    val decision = hiringSupervisor.invoke(request, "Manager technical review is most important.")
    val end = System.nanoTime
    System.out.println("=== Hiring Supervisor finished in " + ((end - start) / 1_000_000_000.0) + "s ===")
    System.out.println(decision.result)
    // Print collected contexts
    System.out.println("\n=== Context as Conversation ===")
    System.out.println(decision.agenticScope.contextAsConversation()) // will work in next release
  }


}