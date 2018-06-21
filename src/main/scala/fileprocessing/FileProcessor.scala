package fileprocessing

import java.io.{File, InputStream}
import java.util.zip.ZipFile

trait FileProcessor {

  def getOdsStream(fileName: String): InputStream = {
    val zipFile: ZipFile = new ZipFile(fileName)
    zipFile.getInputStream(zipFile.getEntry("content.xml"))
  }


}

object FileProcessor extends FileProcessor
