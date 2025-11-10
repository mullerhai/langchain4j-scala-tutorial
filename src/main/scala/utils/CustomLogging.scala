package utils

object CustomLogging {
  private var currentLevel = LogLevels.NONE
  private var charLimit = 100

  def setLevel(level: LogLevels): Unit = {
    currentLevel = level
    configureLogging()
  }

  def setLevel(level: LogLevels, charLimit: Int): Unit = {
    currentLevel = level
    CustomLogging.charLimit = charLimit
    configureLogging()
  }

  def getLevel: LogLevels = currentLevel

  def getCharLimit: Int = charLimit

  private def configureLogging(): Unit = {
    System.setProperty("logback.statusListenerClass", "ch.qos.logback.core.status.NopStatusListener")
    currentLevel match {
      case LogLevels.NONE =>
        System.setProperty("logback.configurationFile", "log/logback-none.xml")
      case LogLevels.PRETTY =>
        System.setProperty("logback.configurationFile", "log/logback-beautiful.xml")
        System.out.println("Pretty logging enabled - showing clean agent conversations")
      case LogLevels.DEBUG =>
        System.setProperty("logback.configurationFile", "log/logback-full.xml")
        System.out.println("Debug logging enabled - showing complete HTTP logs")
      case LogLevels.INFO =>
        System.setProperty("logback.configurationFile", "log/logback-info.xml")
        System.out.println("Info logging enabled - showing basic information")
    }
  }

  def isPrettyLogging: Boolean = currentLevel eq LogLevels.PRETTY

  def isDebugLogging: Boolean = currentLevel eq LogLevels.DEBUG
}