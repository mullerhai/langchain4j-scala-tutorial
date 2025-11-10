package example._5_conditional_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.{SystemMessage, UserMessage, V}
import domain.CvReview

trait InfoRequester {
  @Agent("Emails a candidate to obtain extra info")
  @SystemMessage(Array(
    """
            You send a kind email to candidates to request extra information the company needs
            in order to review the application. Make clear that their application is still being considered.
            """))
  @UserMessage(Array(
    """
            HR review with description of missing info: {{cvReview}}
            
            Candidate contact info: {{candidateContact}}
            
            Job description: {{jobDescription}}
            """)) def send(@V("candidateContact") candidateContact: String, @V("jobDescription") jobDescription: String, @V("cvReview") hrReview: CvReview): String
}