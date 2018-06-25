package runner

import fileprocessing.{FileProcessor, StaxProcessor, StaxTableDataProcessor}
import parsing.DataParser

object Runner {

  def main(args: Array[String]): Unit = {
//      val stxProcessor: StaxProcessor = new StaxProcessor(FileProcessor.getOdsStream("ERSIssue.ods"))
//
////      while(stxProcessor.hasNext) {
////        println(DataParser.parse(stxProcessor.next(),"ERSTest.ods"))
////        println("--------------------------------------------")
////      }
//
//    val stxList: List[String] = stxProcessor.toList
//    println("List size is " + stxList.size)
// //   stxList.foreach(println(_))
//    val stxEither = stxList.map((x) => DataParser.parse(x, "ERSTest.ods"))
//    //stxEither.foreach(println(_))
//    println("List size is " + stxEither.size)
    val stxProc: StaxTableDataProcessor = new StaxTableDataProcessor(FileProcessor.getOdsStream("ERSIssue2.ods"))
    val res = stxProc.getSheets()
    //res.foreach((x) => println("**************" + x.toString()))
    println("Map size" + res.size)
    res.keys.toList.foreach((x) => println(x))
    val otherop = res.get("Other_Acquisition_V3").get
    println("No of rows " + otherop.size)
    otherop.foreach((x) => println("**************" + x.toString()))
  }
}
