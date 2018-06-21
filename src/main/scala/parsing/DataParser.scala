package parsing

import javax.xml.parsers.SAXParserFactory

import scala.util.Try
import scala.xml._

trait DataParser {

  val repeatColumnsAttr = "table:number-columns-repeated"
  val repeatTableAttr = "table:number-rows-repeated"

  def validateSpecialCharacters(xmlRowData : String )={
    if(xmlRowData.contains("&")){
      //println("Found invalid xml in Data Parser, throwing exception")
      throw new Exception("Invalid")
    }
  }


  def secureSAXParser = {
    val saxParserFactory = SAXParserFactory.newInstance()
    saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    saxParserFactory.newSAXParser()
  }

  def parse(row:String, fileName : String): Either[String, (Seq[String], Int)] = {
    //println("DataParser: Parse: About to parse row: " + row)

    val xmlRow = Try(Option(XML.withSAXParser(secureSAXParser)loadString(row))).getOrElse(None)
    //    Logger.debug("DataParser: Parse: About to match xmlRow: " + xmlRow)

    xmlRow match {
      case None => {
        //print("3.1 Parse row left ")
        validateSpecialCharacters(row)
        Left(row)
      }
      case elem:Option[Elem] => //println("3.2 Parse row right ")
        val cols = Try( Right(xmlRow.get.child.flatMap( parseColumn(_)))).getOrElse{
          //println("Cannot get retrieval")
          throw new Exception("Failure")
        }

        cols match {
          case Right(r: Seq[String]) if !isBlankRow(r) => Right(r, repeated(xmlRow))
          case Right(s: Seq[String]) => Right((s, 1))
        }
      case _  => {
        print("cannot parse file")
        throw new Exception("Parser")
      }
    }
  }

  def repeated(xmlRow: Option[Elem]): Int = {
    val rowsRepeated = xmlRow.get.attributes.asAttrMap.get(repeatTableAttr)
    if (rowsRepeated.isDefined) {
      rowsRepeated.get.toInt
    }
    else {
      1
    }
  }

  def parseColumn(col:scala.xml.Node): Seq[String] = {
    val colsRepeated =  col.attributes.asAttrMap.get(repeatColumnsAttr)

    if(colsRepeated.nonEmpty && colsRepeated.get.toInt < 50) {
      val cols:scala.collection.mutable.MutableList[String]= scala.collection.mutable.MutableList()
      for( i <- 1 to colsRepeated.get.toInt)  cols += col.text
      cols.toSeq
    }
    else  Seq(col.text)
  }

  def isBlankRow(data :Seq[String]) = data.mkString("").trim.length == 0

}

object DataParser extends DataParser
