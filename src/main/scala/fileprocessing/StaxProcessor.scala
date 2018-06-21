package fileprocessing

import java.io.InputStream

import javax.xml.stream.events.XMLEvent
import javax.xml.stream.{XMLEventReader, XMLInputFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


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


  val eventReader = xif.createXMLStreamReader(inputStream)
  /**
    *
    * @return - Sheet name and row data from sheet excluding empty rows
    */
  def getNextSheet(): (String, List[List[String]])  = {
    var rowData: ListBuffer[ListBuffer[String]] = ListBuffer()
    var row: ListBuffer[String] = ListBuffer()
    while(eventReader.hasNext()){
      var data = eventReader.getText
      if(data.isStartElement && data.getValue){

      }

    }



    ("", List(List()))
  }

  def printit(): Unit = {
    while(eventReader.hasNext) {
      val thenextEvent = eventReader.nextEvent()
      println(thenextEvent.toString)
    }
  }
}
