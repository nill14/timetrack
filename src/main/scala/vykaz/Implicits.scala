package vykaz

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.joda.time.DateTime
import org.apache.poi.ss.usermodel.Font

object Implicits {

  implicit def Cell2RichCell(cell: Cell) = new RichCell(cell)
  
    class RichCell(val cell: Cell) {
    
    def stringValue: Option[String] = {
      try {
        val str = cell.getStringCellValue
        if (!str.isEmpty) Some(str)
        else None
      } catch {
        case x: Exception => /*println(cell); */ None
      }
    }

    def value: String = cell.toString

    def numValue: Option[Double] = {
      try {
        if (cell.getCellType != Cell.CELL_TYPE_BLANK) {
          val num = cell.getNumericCellValue
          Some(num)
        } else None
      } catch {
        case x: Exception => /*println(cell); */ None
      }
    }

    def dateValue: Option[DateTime] = {
      try {
        val date = new DateTime(cell.getDateCellValue)
        Some(date)
      } catch {
        case x: Exception => /*println(cell); */ None
      }
    }

    def headerCell: Cell = 
      cell.getSheet.getRow(0).getCell(cell.getColumnIndex)
    
    def header: String =
      headerCell.stringValue.get

    def dateCell: Cell = 
      cell.getRow.getCell(0)
      
    def date: DateTime =
      dateCell.dateValue.get

    def dateString: String =
      date.toString

    def comment: Option[String] = {
      if (cell.getCellComment != null &&
        cell.getCellComment.getString != null &&
        cell.getCellComment.getString.getString != null &&
        !cell.getCellComment.getString.getString.isEmpty)
        Some(cell.getCellComment.getString.getString)
      else None
    }
    
    def cellStyle(style: CellStyle): RichCell = {
      cell.setCellStyle(style)
      this
    }
    
    def cellValue(value: String): RichCell = {
      cell.setCellValue(value)
      this
    }
    
    def cellValue(value: DateTime): RichCell = {
      cell.setCellValue(value.toDate)
      this
    }    
    def cellValue(value: Double): RichCell = {
      cell.setCellValue(value)
      this
    }    
    
    def alignment(align: Short): RichCell = {
      cloneAndSetStyle.setAlignment(align)
      this
    }
    
    def dataFormat(format: String): RichCell = {
      val dataFormat = cell.getSheet.getWorkbook.createDataFormat
      cloneAndSetStyle.setDataFormat(dataFormat.getFormat(format))
      this
    }   
    
    def cellFormula(formula: String): RichCell = {
      cell.setCellFormula(formula)
      this
    }        
    
    def border(border: Short): RichCell = {
      val style = cloneAndSetStyle
      style.setBorderBottom(border)
      style.setBorderTop(border)
      style.setBorderLeft(border)
      style.setBorderTop(border)
      this
    }            
    
    def bgColor(color: Short): RichCell = {
      val style = cloneAndSetStyle
      style.setFillForegroundColor(color)
      style.setFillBackgroundColor(color)
      style.setFillPattern(CellStyle.SOLID_FOREGROUND)
      this
    }
    
    def cellFont(font: Font): RichCell = {
      cloneAndSetStyle.setFont(font)
      this
    }
    
    private def cloneAndSetStyle(): CellStyle = {
      val style = workbook.createCellStyle
      style.cloneStyleFrom(cell.getCellStyle)
      cell.setCellStyle(style)
      style
    }
    
    private def workbook = cell.getSheet.getWorkbook
    
  }


}