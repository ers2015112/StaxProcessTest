package fileprocessing

import java.io.ByteArrayInputStream

import org.scalatest.{FlatSpec, FunSuite, Matchers, WordSpec}



class StaxTableDataProcessorTest extends WordSpec with Matchers {

  val testFile1 = """<?xml version="1.0" encoding="UTF-8"?>
                    |<office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0" xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0" xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0" xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0" xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0" xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0" xmlns:math="http://www.w3.org/1998/Math/MathML" xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0" xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0" xmlns:ooo="http://openoffice.org/2004/office" xmlns:ooow="http://openoffice.org/2004/writer" xmlns:oooc="http://openoffice.org/2004/calc" xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:rpt="http://openoffice.org/2005/report" xmlns:of="urn:oasis:names:tc:opendocument:xmlns:of:1.2" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:grddl="http://www.w3.org/2003/g/data-view#" xmlns:tableooo="http://openoffice.org/2009/table" xmlns:drawooo="http://openoffice.org/2010/draw" xmlns:calcext="urn:org:documentfoundation:names:experimental:calc:xmlns:calcext:1.0" xmlns:loext="urn:org:documentfoundation:names:experimental:office:xmlns:loext:1.0" xmlns:field="urn:openoffice:names:experimental:ooo-ms-interop:xmlns:field:1.0" xmlns:formx="urn:openoffice:names:experimental:ooxml-odf-interop:xmlns:form:1.0" xmlns:css3t="http://www.w3.org/TR/css3-text/" office:version="1.2">
                    |<office:body>
                    |<office:spreadsheet>
                    |<table:table table:name="Other_Grants_V3" table:style-name="ta1">
                    |<table:table-column table:style-name="co1" table:default-cell-style-name="ce4"/>
                    |<table:table-column table:style-name="co2" table:default-cell-style-name="ce6"/>
                    |<table:table-column table:style-name="co3" table:default-cell-style-name="ce8"/>
                    |<table:table-column table:style-name="co4" table:default-cell-style-name="ce10"/>
                    |<table:table-column table:style-name="co5" table:number-columns-repeated="5" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co6" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co5" table:number-columns-repeated="1011" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co7" table:number-columns-repeated="3" table:default-cell-style-name="ce11"/>
                    |<table:table-row table:style-name="ro1">
                    |	<table:table-cell table:style-name="ce1" table:content-validation-name="val1" office:value-type="string">
                    |		<text:p>1.</text:p>
                    |		<text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p>
                    |	</table:table-cell>
                    |	<table:table-cell table:style-name="ce5" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees granted options</text:p></table:table-cell><table:table-cell table:style-name="ce7" table:content-validation-name="val1" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Unrestricted market value of a security at date of grant</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell>
                    |<table:table-cell table:style-name="ce9" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>Number of securities over which options granted</text:p><text:p>e.g. 100.00</text:p></table:table-cell>
                    |<table:table-cell table:number-columns-repeated="1020"/></table:table-row>
                    |<table:table-row table:style-name="ro2">
                    |<table:table-cell table:style-name="ce2" office:value-type="string" calcext:value-type="string"><text:p>ssss</text:p></table:table-cell>
                    |<table:table-cell office:value-type="float" office:value="59" calcext:value-type="float"><text:p>59</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="36.7061" calcext:value-type="float"><text:p>36.7061</text:p></table:table-cell>
                    |<table:table-cell office:value-type="float" office:value="465500" calcext:value-type="float"><text:p>465500.00</text:p></table:table-cell>
                    |<table:table-cell table:number-columns-repeated="1020"/>
                    |</table:table-row>
                    |</table:table><table:named-expressions/></office:spreadsheet></office:body></office:document-content>""".stripMargin

  val testFile2 = """<?xml version="1.0" encoding="UTF-8"?>
                    |<office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0" xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0" xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0" xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0" xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0" xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0" xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0" xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0" xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0" xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0" xmlns:math="http://www.w3.org/1998/Math/MathML" xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0" xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0" xmlns:ooo="http://openoffice.org/2004/office" xmlns:ooow="http://openoffice.org/2004/writer" xmlns:oooc="http://openoffice.org/2004/calc" xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:rpt="http://openoffice.org/2005/report" xmlns:of="urn:oasis:names:tc:opendocument:xmlns:of:1.2" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:grddl="http://www.w3.org/2003/g/data-view#" xmlns:tableooo="http://openoffice.org/2009/table" xmlns:drawooo="http://openoffice.org/2010/draw" xmlns:calcext="urn:org:documentfoundation:names:experimental:calc:xmlns:calcext:1.0" xmlns:loext="urn:org:documentfoundation:names:experimental:office:xmlns:loext:1.0" xmlns:field="urn:openoffice:names:experimental:ooo-ms-interop:xmlns:field:1.0" xmlns:formx="urn:openoffice:names:experimental:ooxml-odf-interop:xmlns:form:1.0" xmlns:css3t="http://www.w3.org/TR/css3-text/" office:version="1.2">
                    |<office:body>
                    |<office:spreadsheet>
                    |<table:table table:name="Other_Grants_V3" table:style-name="ta1">
                    |<table:table-column table:style-name="co1" table:default-cell-style-name="ce4"/>
                    |<table:table-column table:style-name="co2" table:default-cell-style-name="ce6"/>
                    |<table:table-column table:style-name="co3" table:default-cell-style-name="ce8"/>
                    |<table:table-column table:style-name="co4" table:default-cell-style-name="ce10"/>
                    |<table:table-column table:style-name="co5" table:number-columns-repeated="5" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co6" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co5" table:number-columns-repeated="1011" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co7" table:number-columns-repeated="3" table:default-cell-style-name="ce11"/>
                    |<table:table-row table:style-name="ro1">
                    |	<table:table-cell table:style-name="ce1" table:content-validation-name="val1" office:value-type="string">
                    |		<text:p>1.</text:p>
                    |		<text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p>
                    |	</table:table-cell>
                    |	<table:table-cell table:style-name="ce5" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees granted options</text:p></table:table-cell><table:table-cell table:style-name="ce7" table:content-validation-name="val1" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Unrestricted market value of a security at date of grant</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell>
                    |<table:table-cell table:style-name="ce9" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>Number of securities over which options granted</text:p><text:p>e.g. 100.00</text:p></table:table-cell>
                    |<table:table-cell table:number-columns-repeated="1020"/></table:table-row>
                    |<table:table-row table:style-name="ro2">
                    |<table:table-cell table:style-name="ce2" office:value-type="string" calcext:value-type="string"><text:p>2016-09-27</text:p></table:table-cell>
                    |<table:table-cell office:value-type="float" office:value="59" calcext:value-type="float"><text:p>59</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="36.7061" calcext:value-type="float"><text:p>36.7061</text:p></table:table-cell>
                    |<table:table-cell office:value-type="float" office:value="465500" calcext:value-type="float"><text:p>465500.00</text:p></table:table-cell>
                    |<table:table-cell table:number-columns-repeated="1020"/>
                    |</table:table-row>
                    |</table:table><table:named-expressions/>
                    |<table:table table:name="Other_Aquisition_V3" table:style-name="ta1">
                    |<table:table-column table:style-name="co1" table:default-cell-style-name="ce4"/>
                    |<table:table-column table:style-name="co2" table:default-cell-style-name="ce6"/>
                    |<table:table-column table:style-name="co3" table:default-cell-style-name="ce8"/>
                    |<table:table-column table:style-name="co4" table:default-cell-style-name="ce10"/>
                    |<table:table-column table:style-name="co5" table:number-columns-repeated="5" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co6" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co5" table:number-columns-repeated="1011" table:default-cell-style-name="ce11"/>
                    |<table:table-column table:style-name="co7" table:number-columns-repeated="3" table:default-cell-style-name="ce11"/>
                    |<table:table-row table:style-name="ro1">
                    |	<table:table-cell table:style-name="ce1" table:content-validation-name="val1" office:value-type="string">
                    |		<text:p>1.</text:p>
                    |		<text:p>Date of grant</text:p><text:p>(yyyy-mm-dd)</text:p>
                    |	</table:table-cell>
                    |	<table:table-cell table:style-name="ce5" office:value-type="string" calcext:value-type="string"><text:p>2.</text:p><text:p>Number of employees granted options</text:p></table:table-cell><table:table-cell table:style-name="ce7" table:content-validation-name="val1" office:value-type="string" calcext:value-type="string"><text:p>3.</text:p><text:p>Unrestricted market value of a security at date of grant</text:p><text:p>£</text:p><text:p>e.g. 10.1234</text:p></table:table-cell>
                    |<table:table-cell table:style-name="ce9" office:value-type="string" calcext:value-type="string"><text:p>4.</text:p><text:p>Number of securities over which options granted</text:p><text:p>e.g. 100.00</text:p></table:table-cell>
                    |<table:table-cell table:number-columns-repeated="1020"/></table:table-row>
                    |<table:table-row table:style-name="ro2">
                    |<table:table-cell table:style-name="ce2" office:value-type="string" calcext:value-type="string"><text:p></text:p></table:table-cell>
                    |<table:table-cell office:value-type="float" office:value="59" calcext:value-type="float"><text:p>59</text:p></table:table-cell><table:table-cell office:value-type="float" office:value="36.7061" calcext:value-type="float"><text:p>36.7061</text:p></table:table-cell>
                    |<table:table-cell office:value-type="float" office:value="465500" calcext:value-type="float"><text:p>465500.00</text:p></table:table-cell>
                    |<table:table-cell table:number-columns-repeated="1020"/>
                    |</table:table-row>
                    |</table:table><table:named-expressions/></office:spreadsheet></office:body></office:document-content>""".stripMargin

  val testFile1Result = List(List())

  "StaxTableDataProcessor" should {
    "return the correct sheet name with one row of data" in {
      val inputXml: String = testFile1
      //val expectedResult = testFile1Result
      val processor = new StaxTableDataProcessor(new ByteArrayInputStream(inputXml.getBytes("utf-8")))
      val result = processor.getNextSheet()
      result._1 shouldBe "Other_Grants_V3"
      result._2.size shouldBe 2
      print(result._2.toString())
    }
//    "return the correct number of sheets" in {
//      val inputXml: String = testFile2
//      //val expectedResult = testFile1Result
//      val processor = new StaxTableDataProcessor(new ByteArrayInputStream(inputXml.getBytes("utf-8")))
//      val result = processor.getSheets()
//      result.size shouldBe 2
//      val keyList = result.keys.toList
//     println(keyList.toString())
//
//      //processor.printTable()
//    }

  }
}
