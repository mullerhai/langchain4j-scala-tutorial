package example._5_conditional_workflow

import dev.langchain4j.agent.tool.{P, Tool}

import java.util
import java.util.Date

class OrganizingTools {
  @Tool def getCurrentDate = new Date

  @Tool(Array("finds the email addresses and names of people that need to be present at the onsite interview for a given job description ID")) def getInvolvedEmployeesForInterview(@P("job description ID") jobDescriptionId: String): util.List[String] = {
    // dummy implementation for demo
    new util.ArrayList[String](util.List.of("Anna Bolena: hiring.manager@company.com", "Chris Durue: near.colleague@company.com", "Esther Finnigan: vp@company.com"))
  }

  @Tool(Array("creates agenda entries for employees based on email address")) def createCalendarEntry(@P("list of employee email addresses") emailAddress: util.List[String], @P("meeting topic") topic: String, @P("start date and time in format yyyy-mm-dd hh:mm") start: String, @P("end date and time in format yyyy-mm-dd hh:mm") end: String): Unit = {
    // dummy implementation for demo
    System.out.println("*** CALENDAR ENTRY CREATED ***")
    System.out.println("Topic: " + topic)
    System.out.println("Start: " + start)
    System.out.println("End: " + end)
  }

  @Tool def sendEmail(@P("list of recipient email addresses") to: util.List[String], @P("list of CC email addresses") cc: util.List[String], @P("emailsubject") subject: String, @P("body") body: String): Int = {
    // dummy implementation for demo
    System.out.println("*** EMAIL SENT ***")
    System.out.println("To: " + to)
    System.out.println("Cc: " + cc)
    System.out.println("Subject: " + subject)
    System.out.println("Body: " + body)
    1234 // dummy email ID
  }

  @Tool def updateApplicationStatus(@P("job description ID") jobDescriptionId: String, @P("candidate (first name, last name)") candidateName: String, @P("new application status") newStatus: String): Unit = {
    // dummy implementation for demo
    System.out.println("*** APPLICATION STATUS UPDATED ***")
    System.out.println("Job Descirption ID: " + jobDescriptionId)
    System.out.println("Candidate Name: " + candidateName)
    System.out.println("New Status: " + newStatus)
  }
}