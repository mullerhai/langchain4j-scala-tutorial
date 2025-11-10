package utils

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.{UserMessage, V}

trait CvGenerator {
  @UserMessage(Array(
    """
            Here is information on my life and professional trajectory
            that you should turn into a clean and complete CV.
            Do not invent facts and do not leave out skills or experiences.
            This CV will later be cleaned up, for now, make sure it is complete.
            Return only the CV, no other text.
            My life story: {{lifeStory}}
            """))
  @Agent("Generates a clean CV based on user-provided information") def generateCv(@V("lifeStory") userInfo: String): String
}