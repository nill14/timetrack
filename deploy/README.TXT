usage: 
==================
$ java -jar timetrack.jar -m 01 -p KIT,BLA,ABC (no spaces)

Expected content of timetrack.properties:
==================
sourceFile=path/to/2012-name.xls
outputDir=path/to/tb
year=2012
surname=name
fullName=full name

==================
timetrack.properties a timetrack.jar must be at the same directory (e.g. C:/bin)


build and deploy
==================
$ mvn clean install
$ cd deploy
$ mvn -DsiteUrl=file:///C:/bin clean site-deploy (timetrack.properties will be overwritten)
