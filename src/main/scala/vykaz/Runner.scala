package vykaz

import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object Runner {

  def processParams(args: List[String], map: Map[String, String]): Map[String, String] = {
    args match {
      case Nil => map // true if everything processed successfully
      case "-m" :: (x: String) :: rest => processParams(rest, map + ("sheetName" -> x))
      case "-p" :: (x: String) :: rest => processParams(rest, map + ("projectName" -> x))
      case x => throw new RuntimeException(s"Unrecognized option: $x (${args.mkString(" ")})")
    }
  }

  def main(args: Array[String]) {

    val map = processParams(args.toList, Map())

    val sheetName = map.getOrElse("sheetName", "Undefined sheetName, use -m sheetName (e.g. -m 01)")
    val projectName = map.getOrElse("projectName", "Undefined projectName, use -p projectName")

    val outputFile = s"${Props.outputDir}/TB-${Props.year}-$sheetName-${Props.surname}.xls"
    val month = DateTime.parse(s"${Props.year}-$sheetName-01")

    doMain(sheetName, projectName, outputFile, month)
  }

  def doMain(sheetName: String, projectName: String, outputFile: String, month: DateTime) {

    val vykaz = new Vykaz(Props.sourceFile, sheetName)
    val recs = vykaz.readProject(projectName)
    recs foreach println

    val timesheet = new Tatigkeitsbericht(outputFile)
    timesheet.writeProject(month, projectName, recs)

  }
}