package example._6_composed_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V

trait CandidateWorkflow {
  @Agent("Based on life story and job description, generates master CV, tailors it to job description with feedback loop until passing score") 
  def processCandidate(@V("lifeStory") userInfo: String, @V("jobDescription") jobDescription: String): String
}