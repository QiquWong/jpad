package sandbox.adm.groovy.test

import groovy.text.SimpleTemplateEngine

import javafx.scene.web.WebEngine
import javafx.scene.web.WebView

import com.dexvis.dex.DexData
import com.dexvis.dex.wf.DexEnvironment
import com.dexvis.dex.wf.DexTask
import com.dexvis.dex.wf.DexTaskState
import com.dexvis.javafx.scene.control.DexFileChooser
import org.apache.commons.io.FileUtils

import au.com.bytecode.opencsv.CSVReader

import com.thoughtworks.xstream.annotations.XStreamOmitField

class TestGroovy_02 {

	// !!!!! see WebTask.groovy

	public TestGroovy_02() {

		String templatePath = "web/c3/LineChart.gtmpl"

		String csvFilePath = "in/groovy/quartet.csv" // "in/groovy/gc.csv"

		println "Template file: ${templatePath}"

		String templateCode = FileUtils.readFileToString(new File(templatePath))

		println "-----------------------------------------------"
		println "Template Code:\n\n'$templateCode'"

		println "-----------------------------------------------"

		CSVReader reader = new CSVReader(new FileReader(csvFilePath))

		// read header row and the rest of CSV data
		def List<String>       header
		def List<List<String>> data
		header = reader.readNext().collect{ it }
		data = []
		List<String> row
		int rowLimit = Integer.MAX_VALUE
		boolean limit = false
		int rowNum = 0;

		while (((row = reader.readNext()) != null) && ((limit == false) || (limit && rowNum < rowLimit))) {
			data << row.collect() { it }
			rowNum++;
		}

		println header
		data.each{ println it }

		DexData dexData = new DexData(header, data)
		DexTaskState state = new DexTaskState(dexData)

		def curDir = new File(".")
		def myMap = [:]
		myMap["state"] = state
		myMap["dexData"] = dexData
		myMap["data"] = data
		myMap["header"] = header
		myMap["basedir"] = curDir.toURI().toURL().toExternalForm()

		def engine = new SimpleTemplateEngine()
		def template = engine.createTemplate(templateCode).make(myMap)
		String output = template.toString()
		// println "Template output: $output" No!!!

		File outputFile = new File("out/Test_groovy/output.html");
		FileUtils.writeStringToFile(outputFile, output)

/*
 * Need to set the JavaFX stage and put the webview in it ...
 *

		WebView wv = new WebView()
		WebEngine we = wv.getEngine()
		//println "Loading: '${outputFile.toURI().toURL().toExternalForm()}'"
		//we?.load(outputFile.toURI().toURL().toExternalForm())
		we?.loadContent(output)
*/


	}

	static void main(def args){
		TestGroovy_02 test = new TestGroovy_02()
	}

}
