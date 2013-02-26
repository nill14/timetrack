package vykaz

import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object Runner {

  def processParams(args: List[String], map: Map[String, String]): Map[String, String] = {
    args match {
      case Nil => map // true if everything processed successfully
      case "-m" :: (x: String) :: rest => processParams(rest, map + ("sheetName" -> x))
      case "-p" :: (x: String) :: rest => processParams(rest, map + ("projectNames" -> x))
      case x => throw new RuntimeException(s"Unrecognized option: $x (${args.mkString(" ")})")
    }
  }

  def main(args: Array[String]) {

    val map = processParams(args.toList, Map())

    val sheetName = map.getOrElse("sheetName", 
        throw new Error("Undefined sheetName, use -m sheetName (e.g. -m 01)"))
    val projectNames = map.getOrElse("projectNames", 
        throw new Error("Undefined projectName, use -p projectName1[,projectName2]")).split(",")

    val outputFile = s"${Props.outputDir}/TB-${Props.year}-$sheetName-${Props.surname}.xls"
    val month = DateTime.parse(s"${Props.year}-$sheetName-01")

    doMain(sheetName, projectNames, outputFile, month)
  }

  def doMain(sheetName: String, projectNames: Array[String], outputFile: String, month: DateTime) {

    val vykaz = new Vykaz(Props.sourceFile, sheetName)
    
    val records = for {
      projectName <- projectNames
      (_, projectCmt, recs) <- vykaz.readProject(projectName)
    } yield {
    	println (s"Project ${projectName}, comment ${projectCmt}")
    	recs foreach println
      (projectName, projectCmt, recs)
    }

    
    val timesheet = new Tatigkeitsbericht(outputFile)
    timesheet.writeProject(month, records)

  }
}