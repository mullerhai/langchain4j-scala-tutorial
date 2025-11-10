package utils

//import com.beishi.StringLoader

import java.io.{IOException, InputStream}
class StringLoader

object StringLoader {
  @throws[IOException]
  def loadFromResource(resourcePath: String): String = try {
    val inputStream = classOf[StringLoader].getResourceAsStream(resourcePath)
    try {
      if (inputStream == null) throw new IOException("Resource not found: " + resourcePath)
      new String(inputStream.readAllBytes)
    } finally if (inputStream != null) inputStream.close()
  }

  @throws[IOException]
  def loadFromResource(clazz: Class[_], resourcePath: String): String = try {
    val inputStream = clazz.getResourceAsStream(resourcePath)
    try {
      if (inputStream == null) throw new IOException("Resource not found: " + resourcePath + " for class: " + clazz.getName)
      new String(inputStream.readAllBytes)
    } finally if (inputStream != null) inputStream.close()
  }
}