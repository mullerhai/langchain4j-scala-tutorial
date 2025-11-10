package mongodb_movieagent

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V

trait MovieSupervisor {
  @Agent def invoke(@V("request") request: String): String
}