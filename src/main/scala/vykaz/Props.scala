package vykaz

import java.util.Properties
import java.io.FileInputStream

object Props extends Properties {

  //the base folder is ./, the root of the main.properties file  
  val path = "./timetrack.properties"

  //load the file handle for main.properties
  val file = new FileInputStream(path);

  //load all the properties from this file
  //  val inStream = ClassLoader.getSystemClassLoader().getResourceAsStream(path)  
  load(file)

  //we have loaded the properties, so close the file handle
  file.close();

  val sourceFile = readProp("sourceFile")
  val outputDir = readProp("outputDir")
  val year = readProp("year")
  val surname = readProp("surname")
  val fullName = readProp("fullName")
//
  
  
  def readProp(key: String) = {
    val prop = getProperty(key, null)
    if (prop != null) prop
    else throw new Error(s"Missing property $key - see $path")
  }
}