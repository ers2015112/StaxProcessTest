package runner

import fileprocessing.{FileProcessor, StaxProcessor}
import parsing.DataParser

object Runner {

  def main(args: Array[String]): Unit = {
      val stxProcessor: StaxProcessor = new StaxProcessor(FileProcessor.getOdsStream("ERSIssue.ods"))

//      while(stxProcessor.hasNext) {
//        println(DataParser.parse(stxProcessor.next(),"ERSTest.ods"))
//        println("--------------------------------------------")
//      }

    val stxList: List[String] = stxProcessor.toList
    println("List size is " + stxList.size)
 //   stxList.foreach(println(_))
    val stxEither = stxList.map((x) => DataParser.parse(x, "ERSTest.ods"))
    //stxEither.foreach(println(_))
    println("List size is " + stxEither.size)
  }
}
