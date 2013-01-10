package vykaz

import java.io.FileInputStream
import java.util.Date

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import Implicits._

class Vykaz(xls: String, sheetName: String) {

  def readProject(prjName: String): Seq[(String, Double)] = {
    // create a new file
    val inp = new FileInputStream(xls)

    // create a new workbook
    val wb: Workbook = WorkbookFactory.create(inp)

    println(s"Vykaz: $xls")
    
    // create a new sheet
    val sheet = wb.getSheet(sheetName)
    
    if (sheet == null) throw new Error(s"Could not find a sheet, sheetName=$sheetName")
    //	for (row <- (0 to 40) map sheet.getRow) {
    //	  val cell = row.getCell(0)
    //	  try {
    //	  	cell.getDateCellValue
    //	  } catch {
    //	    case x: IllegalStateException => println("not a date")
    //	  }
    //	}

    val comments = collectComments(sheet)(prjName)

    // write the workbook to the output stream
    // close our file (don't blow out our file handles
    inp.close

    comments
  }

  def getRow(sheet: Sheet)(date: DateTime) = {
    val row = sheet.getRow(date.getDayOfMonth)
    val cell = row.getCell(0)
    val dateValue = cell.getDateCellValue //could possibly fail
    Predef.assert(date.toDateMidnight == new DateTime(dateValue).toDateMidnight)
    row
  }

  private def collectComments(sheet: Sheet)(colHeader: String): Seq[(String, Double)] = {
    val colIdx = colIndex(sheet)(colHeader)

    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
    
    for {
      cell <- cellRange(sheet)(colIdx)
    } yield {
      val hours = cell.getNumericCellValue

      cell.comment match {
        case Some(cmt) => (s"${fmt.print(cell.date)} ${cell.header} - ${cmt.trim}", hours)
        case None => (s"${fmt.print(cell.date)} ${cell.header}", hours)
      }
    }
  }

  private def colIndex(sheet: Sheet)(header: String): Int = {
    val row = sheet.getRow(0)

    val result = for {
      index <- (row.getFirstCellNum to row.getLastCellNum)
      cell = row.getCell(index)
      if cell.stringValue == Some(header)
    } yield index

    if (result.isEmpty) throw new Error(s"Sheet ${sheet.getSheetName} does not contain header ${header}")
    
    result.head
  }

  def cellRange(sheet: Sheet)(colIndex: Int): Seq[Cell] = {
    //	  val headerRow = sheet.getRow(0)

    for {
      index <- 1 to 31
      row = sheet.getRow(index)
      dateCell = row.getCell(0)
      if dateCell.dateValue.isDefined
      contentCell = row.getCell(colIndex)
      if contentCell.numValue.isDefined
    } yield contentCell
    //	    } yield {println(contentCell.stringValue.get);contentCell}
  }

}

