package example._7_supervisor_orchestration

import example._4_parallel_workflow.{HrCvReviewer, ManagerCvReviewer, TeamMemberCvReviewer}
import example._5_conditional_workflow.{EmailAssistant, InterviewOrganizer, OrganizingTools}
import dev.langchain4j.agentic.AgenticServices
import dev.langchain4j.agentic.supervisor.{SupervisorAgent, SupervisorContextStrategy, SupervisorResponseStrategy}
import dev.langchain4j.model.chat.ChatModel

import java.io.IOException
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

/**
 * Up until now we built deterministic workflows:
 * - sequential, parallel, conditional, loop, and compositions of those.
 * You can also build a Supervisor agentic system, in which an agent will
 * decide dynamically which of his sub-agents to call in which order.
 * In this example, the Supervisor coordinates the hiring workflow:
 * He is supposed to runs HR/Manager/Team reviews and either schedule
 * an interview or send a rejection email.
 * Just like part 2 of the Composed Workflow example, but now 'self-organised'
 * Note that supervisor super-agents can be used in composed workflows just like the other super-agent types.
 * IMPORTANT: this example takes about 50s to run with GPT-4o-mini. You can see what is happening continuously in the PRETTY logs.
 * There are ways to speed up execution, see comments at the end of this file.
 */
object _7a_Supervisor_Orchestration {
  private val CHAT_MODEL = ChatModelProvider.createChatModel

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 1. Define all sub-agents
    val hrReviewer = AgenticServices.agentBuilder(classOf[HrCvReviewer]).chatModel(CHAT_MODEL).outputKey("hrReview").build
    // importantly, if we use the same method names for multiple agents
    // (in this case: 'reviewCv' for all reviewers) we best name our agents, like this:
    // @Agent(name = "managerReviewer", description = "Reviews a CV based on a job description, gives feedback and a score")
    val managerReviewer = AgenticServices.agentBuilder(classOf[ManagerCvReviewer]).chatModel(CHAT_MODEL).outputKey("managerReview").build
    val teamReviewer = AgenticServices.agentBuilder(classOf[TeamMemberCvReviewer]).chatModel(CHAT_MODEL).outputKey("teamMemberReview").build
    val interviewOrganizer = AgenticServices.agentBuilder(classOf[InterviewOrganizer]).chatModel(CHAT_MODEL).tools(new OrganizingTools).build
    val emailAssistant = AgenticServices.agentBuilder(classOf[EmailAssistant]).chatModel(CHAT_MODEL).tools(new OrganizingTools).build
    // 2. Build the Supervisor agent
    val hiringSupervisor = AgenticServices.supervisorBuilder.chatModel(CHAT_MODEL).subAgents(hrReviewer, managerReviewer, teamReviewer, interviewOrganizer, emailAssistant).contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY_AND_SUMMARIZATION).responseStrategy(SupervisorResponseStrategy.SUMMARY).build() // we want a summary of what happened, rather than retrieving a response.supervisorContext("Always use the full panel of available reviewers. Always answer in English. When invoking agent, use pure JSON (no backticks, and new lines as backslash+n).")// optional context for the supervisor on how to behave.build
    // Important to know: the supervisor will invoke 1 agent at a time and then review his plan to choose which agent to invoke next
    // It is not possible to have agents executed in parallel by the supervisor
    // If agents are marked as async, the supervisor will override that (no async execution) and issue a warning
    // 3. Load candidate CV & job description
    val jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    val candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt")
    val candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt")
    val hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt")
    val phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt")
    // start a timer
    val start = System.nanoTime
    // 4. Invoke Supervisor with a natural request
    val result = hiringSupervisor.invoke("Evaluate the following candidate:\n" + "Candidate CV:\n" + candidateCv + "\n\n" + "Candidate Contacts:\n" + candidateContact + "\n\n" + "Job Description:\n" + jobDescription + "\n\n" + "HR Requirements:\n" + hrRequirements + "\n\n" + "Phone Interview Notes:\n" + phoneInterviewNotes).asInstanceOf[String]
    val end = System.nanoTime
    val elapsedSeconds = (end - start) / 1_000_000_000.0
    // in the logs you'll notice a final invocation of agent 'done', this is how the supervisor finishes the invocation series
    System.out.println("=== SUPERVISOR RUN COMPLETED in " + elapsedSeconds + " seconds ===")
    System.out.println(result)
  }

  try CustomLogging.setLevel(LogLevels.PRETTY, 200) // control how much you see from the model calls


  // ADVANCED USE CASES:
  // See _7b_Supervisor_Orchestration_Advanced.java for
  // - typed supervisor,
  // - context engineering,
  // - output strategies,
  // - call chain observation,
  // ON LATENCY:
  // The whole run of this flow typically takes over 60s.
  // A solution for this is to use a fast inference provider like CEREBRAS,
  // which will run the whole flow in 10s but makes more mistakes.
  // To try this example with CEREBRAS, get a key (click get started with free API key)
  // https://inference-docs.cerebras.ai/quickstart
  // and save in env variables as "CEREBRAS_API_KEY"
  // Then change line 38 to:
  // private static final ChatModel CHAT_MODEL = ChatModelProvider.createChatModel("CEREBRAS");
}