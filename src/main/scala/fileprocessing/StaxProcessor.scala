package fileprocessing

import java.io.InputStream

import javax.xml.namespace.QName
import javax.xml.stream.events.XMLEvent
import javax.xml.stream.{XMLEventReader, XMLInputFactory, XMLStreamConstants}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try


class StaxProcessor(inputStream: InputStream)  extends Iterator[String] {

  val xif : XMLInputFactory = XMLInputFactory.newInstance()
  xif.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false)
  xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
  xif.setProperty(XMLInputFactory.IS_VALIDATING, false)

  val eventReader: XMLEventReader = xif.createXMLEventReader(inputStream)

  override def hasNext: Boolean = {
    while (eventReader.hasNext) {
      val xmlevent = eventReader.peek()
      if (xmlevent.isStartElement) {
        val name = xmlevent.asStartElement().getName.getLocalPart
        if (name == "table:table" || name == "table:table-row")
          return true
        else
          eventReader.nextEvent()
      } else {
        eventReader.nextEvent()
      }
    }
    false
  }

  override def next(): String = {
    val nextValue = eventReader.nextEvent()
    if(nextValue.isStartElement)
      if(nextValue.asStartElement().getName.getLocalPart == "table:table-row") {
        val a = getStringToEndElement("table:table-row")
        val b = nextValue.toString
        return (b + a)
      }
      else
        return getName(nextValue.toString)

    "--NOT-FOUND--"
  }

  def getName(message : String) : String = {
    val sheetNameRegEx = "(table:name=)\\'(\\w+)\\'".r
    sheetNameRegEx.findFirstMatchIn(message).map(_ group 2).getOrElse("--NOT-FOUND--")
  }

  def getStringToEndElement(endelement: String): String =
  {
    val buffer: StringBuilder = new StringBuilder
    var foundit: Boolean = false

    def foundelement(  event: XMLEvent,elementName: String): Boolean = {
      if(event.isEndElement)
        event.asEndElement().getName.getLocalPart == elementName
      else
        false
    }

    while(eventReader.hasNext)
    {
      val thenextEvent = eventReader.nextEvent()

      buffer.append(thenextEvent.toString)
      if(foundelement(thenextEvent, endelement)) {
        return buffer.toString()
      }
    }

    def printAttributes(xMLEvent: XMLEvent) = {
          if(xMLEvent.isStartElement)
            {
              val attr = xMLEvent.asStartElement().getAttributes
              while(attr.hasNext)
                println("ATTRIBUTE " + attr.next().toString)
            }

    }

    buffer.toString()
  }
}

class StaxTableDataProcessor(inputStream: InputStream)  {
  val xif : XMLInputFactory = XMLInputFactory.newInstance()
  xif.setProperty(XMLInputFactory.SUPPORT_DTD, false)
  xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false)
  xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false)
  xif.setProperty(XMLInputFactory.IS_VALIDATING, false)


  val eventReader = xif.createXMLEventReader(inputStream)
  /**
    *
    * @return - Sheet name and row data from sheet excluding empty rows
    */
  def getSheets() = {
    var sheetName: String = ""
    var sheetMap: mutable.HashMap[String, List[List[String]]] = mutable.HashMap()
    var rowData: ListBuffer[List[String]] = ListBuffer()
    var row: ListBuffer[String] = ListBuffer()
    var inTable = false
    var inRow = false
    var inCell = false
    var inText = false
    var inData = false
    var inRepeatedCols = false
    var sheetRowCount = 0

    while(eventReader.hasNext) {
      val event: XMLEvent = eventReader.nextEvent()
      event.getEventType match {
        case XMLStreamConstants.START_ELEMENT =>
          val qName = event.asStartElement().getName().getLocalPart
          qName match {
            case "table:table" =>
//              println("Start table")
              inTable = true
              val tempName = Try(event.asStartElement().getAttributeByName(new QName("name")).getValue)
              if(tempName.isSuccess)
                sheetName = tempName.get
            case "table:table-row" =>
 //             println("Start row")
              sheetRowCount += 1
              inRow = true
            case "table:table-cell" =>
  //            println("Start cell")
              if(event.asStartElement().getAttributeByName(new QName("number-columns-repeated")) != null)
                  {
                    val numRepeatedCols = event.asStartElement().getAttributeByName(new QName("number-columns-repeated")).getValue().toInt
                    if(numRepeatedCols < 100)
                      if(sheetRowCount > 9)
                        for(i <- 1 to numRepeatedCols)
                          row += ""
                      else
                        row += "HEADER"
                    inCell = false
                  }
              inCell = true
            case "text:p" =>{
  //            println("Start text element")
              inText = true
              inData = false
              }
            case _ =>
          }
        case XMLStreamConstants.CHARACTERS =>
          val characters = event.asCharacters()
//          print("Incharacters " + inCell + " " + inText + " cr " + inRepeatedCols)

          if(inRow) {
            if(inCell && inText) {
                inData = true
                if(sheetRowCount > 9)
                  row += characters.getData.trim
                else
                  row += "HEADER"
//              inCell = false
            }
          }
        case XMLStreamConstants.END_ELEMENT =>
          val qName = event.asEndElement().getName().getLocalPart
          qName match {
            case "table:table" =>
              sheetMap.put(sheetName, rowData.toList)
              sheetRowCount = 0
              rowData = ListBuffer()
              inTable = false
            case "table:table-row" =>
           //   println("**Row " + row.toList.toString())
           //   println("** " + row.toList.filter(_ != "").toString())
              if(!row.toList.filter(_ != "").isEmpty)
                rowData += row.toList
              row = ListBuffer()
              inRow = false
            case "text:p" =>
              inText = false
  //            println("In Data " + inData + " rowCount " + sheetRowCount)
              if(!inData) {
               println("Adding empty cell")
                row += ""
              }
            case "table:table-cell" =>
              inCell = false
            case _ =>
          }
        case _ =>
      }
    }
    sheetMap.toMap
  }

  def getNextSheet() = {
    var sheetName: String = ""
    var rowData: ListBuffer[List[String]] = ListBuffer()
    var row: ListBuffer[String] = ListBuffer()
    var inTable = false
    var inRow = false
    var inCell = false
    var inRepeatedCols = false

    while(eventReader.hasNext) {
      val event: XMLEvent = eventReader.nextEvent()
      event.getEventType match {
        case XMLStreamConstants.START_ELEMENT =>
          val qName = event.asStartElement().getName().getLocalPart
          qName match {
            case "table:table" =>
                  println("Start table")
              inTable = true
              val tempName = Try(event.asStartElement().getAttributeByName(new QName("name")).getValue)
              if(tempName.isSuccess)
                sheetName = tempName.get
            case "table:table-row" =>
                      println("Start row")
              inRow = true
            case "table:table-cell" =>
                   println("Start cell")
              if(event.asStartElement().getAttributeByName(new QName("number-columns-repeated")) != null)
                inRepeatedCols = true
              inCell = true
            case _ =>
          }
        case XMLStreamConstants.CHARACTERS =>
          val characters = event.asCharacters()
          if(inRow) {
            if(inCell) {
              if(!inRepeatedCols) {
                println("Cell Value " + characters.getData)
                row += characters.getData
                println(row.toString())
              }
              else
                inRepeatedCols = false
              inCell = false
            }
          }
        case XMLStreamConstants.END_ELEMENT =>
          val qName = event.asEndElement().getName().getLocalPart
          qName match {
            case "table:table" => inTable = false
            case "table:table-row" =>
              println("**Row " + row.toList.toString())
              println("** " + row.toList.filter(_ != "").toString())
              if(!row.toList.filter(_ != "").isEmpty)
                rowData += row.toList
              row = ListBuffer()
              inRow = false
            case _ =>
          }
        case _ =>
      }
    }
    (sheetName, rowData.toList)
  }

  def getNextSheetOld(): (String, List[List[String]])  = {

  var sheetName: String = ""
    var rowData: ListBuffer[ListBuffer[String]] = ListBuffer()
    var row: ListBuffer[String] = ListBuffer()
    while (eventReader.hasNext()) {
        val event = eventReader.nextEvent()
     event.isStartElement
        match {
          case true =>
            if(event.asStartElement().getName.getLocalPart == "table:table")
              sheetName = event.asStartElement().getAttributeByName(new QName("name")).getValue
//          case true if event.asStartElement().getName.getLocalPart == "table:table-row" =>
//            var rowBuffer = parseTableRow()
//            rowData ++ rowBuffer
          case false =>
        }
//          if(event.isStartElement)
//            if (event.asStartElement().getName.getLocalPart == "table:table") {
//              sheetName = event.asStartElement().getAttributeByName(new QName("name")).getValue
////                  val it = event.asStartElement().getAttributes()
////              while(it.hasNext)
////                println(it.next().)
//          } else if(event.asStartElement().getName.getLocalPart == "table:table-row")
    }
    (sheetName, List(List()))
  }

  def parseTableRow(): ListBuffer[String] ={
    var buffer: ListBuffer[String] = ListBuffer()
    var event = eventReader.nextEvent()
    while(event.isEndElement && (event.asEndElement().getName.getLocalPart != "table:table-row")){
      if(event.isStartElement && (event.asStartElement().getName.getLocalPart == "table:table-cell")){
        var value = event.asStartElement().getAttributeByName(new QName("value"))
        if(value != null){
          buffer ++ value.getValue
        }
      }
      event = eventReader.nextEvent()
    }
    buffer
  }

//  def printit(): Unit = {
//    while(eventReader.hasNext) {
//      val thenextEvent = eventReader.nextTag()
//      thenextEvent match {
//        case XMLStreamConstants.CDATA =>
//          print("CDATA" + )
//      }
//      println(eventReader.getText)
//    }
//  }

  def printit(): Unit = {
    while(eventReader.hasNext) {
      val thenextEvent = eventReader.nextEvent()
      if(thenextEvent.getEventType > 0)
      println(getElement(thenextEvent) + " --- " + thenextEvent.toString)
    }
  }



  def printTable(): Unit = {
    var inTable = false
    var inRow = false
    var inCell = false
    var inRepeatedCols = false

    while(eventReader.hasNext) {
      val event: XMLEvent = eventReader.nextEvent()
        event.getEventType match {
          case XMLStreamConstants.START_ELEMENT =>
            val qName = event.asStartElement().getName().getLocalPart
            qName match {
              case "table:table" =>
   //             println("Start table")
                inTable = true
                print(event.asStartElement().toString)
              case "table:table-row" =>
 //               println("Start row")
                inRow = true
              case "table:table-cell" =>
 //               println("Start cell")
                if(event.asStartElement().getAttributeByName(new QName("number-columns-repeated")) != null)
                  inRepeatedCols = true
                inCell = true
              case _ =>
            }
          case XMLStreamConstants.CHARACTERS =>
            val characters = event.asCharacters()
            if(inRow) {
              if(inCell) {
                if(!inRepeatedCols)
                  println("Cell Value " + characters.getData)
                else
                  inRepeatedCols = false
                inCell = false
              }
            }
          case XMLStreamConstants.END_ELEMENT =>
            val qName = event.asEndElement().getName().getLocalPart
            qName match {
              case "table:table" => inTable = false
              case "table:table-row" => inTable = false
              case _ =>
            }
          case _ =>
        }
    }

  }

  def matchType(i: Int) = i match {
    case XMLStreamConstants.START_ELEMENT=> s"START ELEMENT/${i}"
    case XMLStreamConstants.END_ELEMENT=> s"END_ELEMENT/${i}"
    case XMLStreamConstants.SPACE=> s"SPACE/${i}"
    case XMLStreamConstants.CHARACTERS=> s"CHARACTERS/${i}"
    case XMLStreamConstants.PROCESSING_INSTRUCTION=> s"PROCESSING_INSTRUCTION/${i}"
    case XMLStreamConstants.CDATA=> s"CDATA/${i}"
    case XMLStreamConstants.COMMENT=> s"COMMENT/${i}"
    case XMLStreamConstants.ENTITY_REFERENCE=> s"ENTITY_REFERENCE/${i}"
    case XMLStreamConstants.START_DOCUMENT=> s"START_DOCUMENT/${i}"
    case _ => s"IDFK/${i}"
  }

  def getElement(xmlEvent: XMLEvent) = xmlEvent.getEventType match {
    case XMLStreamConstants.START_ELEMENT=> Some(xmlEvent.asStartElement())
    case XMLStreamConstants.END_ELEMENT=> Some(xmlEvent.asEndElement())
    case XMLStreamConstants.SPACE=> Some(xmlEvent.asCharacters())
    case XMLStreamConstants.CHARACTERS=> Some(xmlEvent.asCharacters())
    case XMLStreamConstants.CDATA=> Some(xmlEvent.asCharacters())
    case _ => None
  }
}
