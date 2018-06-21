package parsing

trait Validator {

  def validator1(sheetName: String, rowToValidate: (Seq[String], Int), rowNum: Int) = sheetName + " Valid " + rowToValidate._1.headOption.getOrElse("Head") + "- row " + rowNum
  def validator2(sheetName: String, rowToValidate: (Seq[String], Int), rowNum: Int) = sheetName + " InValid " + rowToValidate._1.headOption.getOrElse("Head") + "- row " + rowNum

}

object Validator extends Validator
