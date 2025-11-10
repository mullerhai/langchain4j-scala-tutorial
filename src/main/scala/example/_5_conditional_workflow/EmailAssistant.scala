package example._5_conditional_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.{SystemMessage, UserMessage, V}

trait EmailAssistant {
  @Agent("Sends rejection emails to candidates that didn't pass")
  @SystemMessage(Array(
    """
            You send a kind email to application candidates that did not pass the first review round.
            You also update the application status to 'rejected'.
            You return the sent email ID.
            """))
  @UserMessage(Array(
    """
            Rejected candidate: {{candidateContact}}
            
            For job: {{jobDescription}}
            """)) def send(@V("candidateContact") candidateContact: String, @V("jobDescription") jobDescription: String): Int
}