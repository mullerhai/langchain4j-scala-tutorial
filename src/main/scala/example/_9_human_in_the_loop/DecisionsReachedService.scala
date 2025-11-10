package example._9_human_in_the_loop

import dev.langchain4j.service.{SystemMessage, UserMessage, V}

trait DecisionsReachedService {
  @SystemMessage(Array("Given the interaction, return true if a decision has been reached, " + "false if further discussion is needed to find a solution."))
  @UserMessage(Array(
    """
            Interaction so far:
             Secretary: {{proposal}}
             Invitee: {{candidateAnswer}}
    """)) def isDecisionReached(@V("proposal") proposal: String, @V("candidateAnswer") candidateAnswer: String): Boolean
}