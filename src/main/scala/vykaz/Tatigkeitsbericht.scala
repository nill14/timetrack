package vykaz

import java.io.FileOutputStream
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataFormat
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import Implicits._
import org.joda.time.DateTime
import org.apache.poi.hssf.util.HSSFColor
import scala.collection.Seq

class Tatigkeitsbericht(xls: String) {


  println(s"TB: $xls")

  val label = "Tätigkeitsbericht"

  // create a new workbook
  val wb: Workbook = new HSSFWorkbook

  lazy val arial10 = {
    val f = wb.createFont
    f.setFontHeightInPoints(10)
    f.setFontName("Arial")
    f
  }

  lazy val arial10b = {
    val f = wb.createFont
    f.setFontHeightInPoints(10)
    f.setFontName("Arial")
    f.setBoldweight(Font.BOLDWEIGHT_BOLD)
    f
  }
  
  lazy val arial10i = {
    val f = wb.createFont
    f.setFontHeightInPoints(10)
    f.setFontName("Arial")
    f.setItalic(true)
    f
  }  

  lazy val arial12b = {
    val f = wb.createFont
    f.setFontHeightInPoints(12)
    f.setFontName("Arial")
    f.setBoldweight(Font.BOLDWEIGHT_BOLD)
    f
  }

  val arial14b = {
    val f = wb.createFont
    f.setFontHeightInPoints(14)
    f.setFontName("Arial")
    f.setBoldweight(Font.BOLDWEIGHT_BOLD)
    f
  }

  def writeProject(date: DateTime, records: Seq[ProjectTuple]) = {

    // create a new sheet
    val sheet = wb.createSheet(label);

    //	for (row <- (0 to 40) map sheet.getRow) {
    //	  val cell = row.getCell(0)
    //	  try {
    //	  	cell.getDateCellValue
    //	  } catch {
    //	    case x: IllegalStateException => println("not a date")
    //	  }
    //	}

    prepare(Props.fullName, date)(sheet)
    insData(records)(sheet)

    //    val comments = collectComments(sheet)(prjName)

    // write the workbook to the output stream
    // close our file (don't blow out our file handles

    //    Sheet sheet = wb.getSheetAt(0);
    //    Row row = sheet.getRow(2);
    //    Cell cell = row.getCell(3);
    //    if (cell == null)
    //        cell = row.createCell(3);
    //    cell.setCellType(Cell.CELL_TYPE_STRING);
    //    cell.setCellValue("a test");

    // Write the output to a file
    val out = new FileOutputStream(xls)
    wb.write(out)
    out.close
  }

  def insData(records: Seq[ProjectTuple])(implicit sheet: Sheet) {
    var start = 5

    val sums = for {
      (projectName, projectCmt, recs) <- records
    } yield {
      val (header, footer) = insProject(projectName, projectCmt, recs, start, records.size > 1)
      start = footer + 1

      footer
    }

    if (records.size > 1) {
      val formula = sums.map(n => s"C${n + 1}").mkString(" + ")
      cell(64, 2).cellFormula(formula)
    }
  }

  def insProject(projectName: String, projectCmt: String, recs: Seq[HourTuple], start: Int, insSum: Boolean)(implicit sheet: Sheet): (Int, Int) = {
    val shift = start + 1
    val end = shift + recs.size

    cell(start, 0).cellValue(projectName).cellFont(arial10b).bgColor(HSSFColor.LIGHT_TURQUOISE.index)
    cell(start, 1).cellValue(projectCmt).cellFont(arial10i).bgColor(HSSFColor.LIGHT_TURQUOISE.index)

    for {
      idx <- 0 until recs.size
      rownum = shift + idx
      (txt, hour) = recs(idx)
    } {
      cell(rownum, 1).cellValue(txt)
      cell(rownum, 2).cellValue(hour)
    }

    if (insSum) {
    	//cell(end, 0).bgColor(HSSFColor.LIGHT_TURQUOISE.index)
    	cell(end, 1).bgColor(HSSFColor.LIGHT_TURQUOISE.index)
    	cell(end, 2).bgColor(HSSFColor.LIGHT_TURQUOISE.index)
    		.cellFormula(s"SUM(C${shift+1}:C${end})").cellFont(arial10b)
    }

    (start, end)
  }

  def prepare(username: String, date: DateTime)(implicit sheet: Sheet) {

    sheet.setColumnWidth(0, (256 * 10.67 + 0.7 * 256).toInt)
    sheet.setColumnWidth(1, (256 * 100.67 + 0.7 * 256).toInt)
    sheet.setColumnWidth(2, (256 * 16.78 + 0.7 * 256).toInt)

    cell(0, 1).cellValue(label).cellFont(arial14b).alignment(CellStyle.ALIGN_CENTER)
    cell(0, 2).cellValue(username).cellFont(arial10b)

    cell(1, 1).cellValue(date).alignment(CellStyle.ALIGN_CENTER).dataFormat("mmmm yyyy")
    cell(2, 1).cellFormula("TODAY()").alignment(CellStyle.ALIGN_CENTER).dataFormat("d.m.yyyy")

    cell(4, 0).cellValue("Projekt").cellFont(arial12b)
    cell(4, 1).cellValue("Tätigkeiten").cellFont(arial12b)
    cell(4, 2).cellValue("Stunden").cellFont(arial12b).alignment(CellStyle.ALIGN_CENTER)

    cell(64, 0).bgColor(HSSFColor.LIGHT_TURQUOISE.index)
    cell(64, 1).cellValue("Summe:").cellFont(arial10b).bgColor(HSSFColor.LIGHT_TURQUOISE.index).alignment(CellStyle.ALIGN_RIGHT)
    cell(64, 2).cellFormula("SUM(C6:C64)").cellFont(arial12b).bgColor(HSSFColor.LIGHT_TURQUOISE.index)

    cell(65, 0).bgColor(HSSFColor.GREY_25_PERCENT.index)
    cell(65, 1).cellValue("Gesamtsumme:").cellFont(arial12b).bgColor(HSSFColor.GREY_25_PERCENT.index).alignment(CellStyle.ALIGN_RIGHT)
    cell(65, 2).cellFormula("C65").cellFont(arial14b).bgColor(HSSFColor.GREY_25_PERCENT.index)

    for {
      rownum <- 5 to 65
      colnum <- 0 to 2
      c = cell(rownum, colnum)
    } {
      c.border(CellStyle.BORDER_THIN)
    }

    //     makeBorder("B4:B5", CellStyle.BORDER_THIN)
    {
      val region = CellRangeAddress.valueOf("B4:B5")
      val workbook = sheet.getWorkbook
      RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workbook)
      RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workbook)
    }

    makeBorder("A1:C3", CellStyle.BORDER_MEDIUM)
    makeBorder("A4:C5", CellStyle.BORDER_MEDIUM)
    makeBorder("A6:C65", CellStyle.BORDER_MEDIUM)
    makeBorder("A66:C66", CellStyle.BORDER_MEDIUM)

    for {
      rownum <- 5 to 65
      c = cell(rownum, 2)
    } {
      c.dataFormat("0.00")
    }
  }

  def makeBorder(regionRef: String, borderStyle: Short)(implicit sheet: Sheet) {
    val region = CellRangeAddress.valueOf(regionRef)
    val workbook = sheet.getWorkbook
    RegionUtil.setBorderBottom(borderStyle, region, sheet, workbook)
    RegionUtil.setBorderTop(borderStyle, region, sheet, workbook)
    RegionUtil.setBorderLeft(borderStyle, region, sheet, workbook)
    RegionUtil.setBorderRight(borderStyle, region, sheet, workbook)
  }

  class CellBuilder(cell: Cell) {
    def cellValue(value: String) = {
      cell.setCellValue(value);

    }
  }
}