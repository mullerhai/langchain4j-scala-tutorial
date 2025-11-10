package example._6_composed_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V

trait HiringTeamWorkflow {
  @Agent("Based on CV, phone interview and job description, this agent will either invite or reject the candidate")
  def processApplication(@V("candidateCv") candidateCv: String, @V("jobDescription") jobDescription: String, @V("hrRequirements") hrRequirements: String, @V("phoneInterviewNotes") phoneInterviewNotes: String, @V("candidateContact") candidateContact: String): Unit
}