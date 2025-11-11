package example._3_loop_workflow

import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.io.IOException
import java.util
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

object _3b_Loop_Agent_Example_States_And_Fails {
  /**
   * Here we build the same loop-agent as in 3a, but this time we should see it fail
   * by trying to tailor the CV to a job description that doesn't fit.
   * We will also return the latest score and feedback, on top of the final CV,
   * which will allow us to check if we obtained a good score and if it's worth handing in this CV.
   * We also show a trick to inspect the intermediary states of the review (it gets overwritten in every loop)
   * by storing them in a list each time the exit condition is checked (ie. after every agent invocation).
   */
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls

  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 1. Create all sub-agents (same as before)
    val cvReviewer = AgenticServices
      .agentBuilder(classOf[CvReviewer])
      .chatModel(CHAT_MODEL).outputKey("cvReview").build() // this gets updated in every iteration with new feedback for the next tailoring.build
    val scoredCvTailor = AgenticServices
      .agentBuilder(classOf[ScoredCvTailor])
      .chatModel(CHAT_MODEL).outputKey("cv").build() // this will be updated in every iteration, continuously improving the CV.build
    // 2. Build the sequence and store the reviews on each exit condition check
    // It can be important to know whether the exit condition was met or just the max iterations
    // (eg. John may not even want to bother applying for this job).
    // You can change the output variable to also contain the last score and feedback, and check yourself after the loop finished.
    // You can also store the intermediary values in a mutable list to inspect later.
    // The code below does both things at the same time.
    val reviewHistory = new util.ArrayList[CvReview]
    val reviewedCvGenerator = AgenticServices
      .loopBuilder()
      .subAgents(cvReviewer, scoredCvTailor)
      .outputKey("cvAndReview")
      .output((agenticScope) => {
        val cv = agenticScope.readState("cv").asInstanceOf[String]
        val review = agenticScope.readState("cvReview").asInstanceOf[CvReview]
        util.Map.of("cv", cv, "review", review)
      }).exitCondition((scope) => {
        val review = scope.readState("cvReview").asInstanceOf[CvReview]
        // Add the review to the history
        reviewHistory.add(review)
        review.score >= 0.8
      }).maxIterations(3).build()

    // use UntypedAgent unless you define the resulting composed agent, see below.loopBuilder.subAgents(cvReviewer, scoredCvTailor)// this can be as many as you want, order matters.outputKey("cvAndReview")// this is the final output we want to observe.output((agenticScope) => {
    //    val cvAndReview = util.Map.of("cv", agenticScope.readState("cv"), "finalReview", agenticScope.readState("cvReview"))
    //    cvAndReview
    //  }
    //
    //  ).exitCondition((scope) => {
    //    val review = scope.readState("cvReview").asInstanceOf[CvReview]
    //    reviewHistory.add(review) // capture the score+feedback at every agent invocation
    //    System.out.println("Exit check with score=" + review.score)
    //    review.score >= 0.8
    //  }).maxIterations(3).build // safety to avoid infinite loops, in case exit condition is never met.build
    // 3. Load the original arguments from text files in resources/documents/
    // - master_cv.txt
    // - job_description_backend.txt
    val masterCv: String = StringLoader.loadFromResource("/documents/master_cv.txt")
    val fluteJobDescription = "We are looking for a passionate flute teacher to join our music academy."
    // 4. Because we use an untyped agent, we need to pass a map of arguments
    val arguments: util.Map[String, AnyRef] = util.Map.of("cv", masterCv, // start with the master CV, it will be continuously improved
      "jobDescription", fluteJobDescription)
    // 5. Call the composed agent to generate the tailored CV
    val cvAndReview: util.Map[String, AnyRef] = reviewedCvGenerator.invoke(arguments).asInstanceOf[util.Map[String, AnyRef]]
    // You can observe the steps in the logs, for example:
    // Round 1 output: "content": "{\n  \"score\": 0.0,\n  \"feedback\": \"This CV is not suitable for the flute teacher position at our music academy...
    // Round 2 output: "content": "{\n  \"score\": 0.3,\n  \"feedback\": \"John's CV demonstrates strong soft skills such as communication, patience, and adaptability, which are important in a teaching role. However, the absence of formal music training or ...
    // Round 3 output: "content": "{\n  \"score\": 0.4,\n  \"feedback\": \"John Doe demonstrates strong soft skills and mentoring experience,...
    System.out.println("=== REVIEWED CV FOR FLUTE TEACHER ===")
    System.out.println(cvAndReview.get("cv")) // the final CV after the loop
    // now you get the finalReview in the output map so you can check
    // if the final score and feedback meet your requirements
    val review: CvReview = cvAndReview.get("finalReview").asInstanceOf[CvReview]
    System.out.println("=== FINAL REVIEW FOR FLUTE TEACHER ===")
    System.out.println("CV" + (if (review.score >= 0.8) " passes"
    else " does not pass") + " with score=" + review.score)
    System.out.println("Final feedback: " + review.feedback)
    // in reviewHistory you find the full history of reviews
    System.out.println("=== FULL REVIEW HISTORY FOR FLUTE TEACHER ===")
    System.out.println(reviewHistory)
  }


}

//Exception in thread "main" java.lang.NullPointerException: Cannot invoke "domain.CvReview.score()" because "review" is null
//	at example._3_loop_workflow._3b_Loop_Agent_Example_States_And_Fails$.main(_3b_Loop_Agent_Example_States_And_Fail.scala:86)
//	at example._3_loop_workflow._3b_Loop_Agent_Example_States_And_Fails.main(_3b_Loop_Agent_Example_States_And_Fail.scala)