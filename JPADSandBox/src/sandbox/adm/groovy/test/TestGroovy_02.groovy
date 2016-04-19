package sandbox.adm.groovy.test

import groovy.text.SimpleTemplateEngine

import javafx.scene.web.WebEngine
import javafx.scene.web.WebView

import com.dexvis.dex.wf.DexTaskState
import com.dexvis.javafx.scene.control.DexFileChooser
import org.apache.commons.io.FileUtils

import com.thoughtworks.xstream.annotations.XStreamOmitField

class TestGroovy_02 {

	// !!!!! see WebTask.groovy

	@XStreamOmitField
	private String templatePath = ""
	@XStreamOmitField
	private String templateCode = "Insert template code here..."
	@XStreamOmitField
	private String output = ""

	//	@XStreamOmitField
	//	private WebView wv = new WebView()
	//	@XStreamOmitField
	//	protected WebEngine we = wv.getEngine()
	@XStreamOmitField
	private boolean saveDynamic = false;

	@XStreamOmitField
	private static DexFileChooser htmlChooser = null

	// State of this task, initialize to start.
	@XStreamOmitField
	private DexTaskState dexTaskState = new DexTaskState();

	public TestGroovy_02() {

		this.templatePath = "web/c3/LineChart.gtmpl"

		println "Template: ${templatePath}"

		templateCode = FileUtils.readFileToString(new File(templatePath))

		//println "Template Code: '$templateCode'"

		def binding = getBinding(dexTaskState)
		def engine = new SimpleTemplateEngine()
		def template = engine.createTemplate(templateCode).make(binding)
		output = template.toString()

		println "Template output: '$output'"
		// TODO: add libraries jboss.netty.* ...


	}

	public Map getBinding(DexTaskState state)
	{
		def curDir = new File(".")

		return [
			"state":state,
			"dexData":state.dexData,
			"data":state.dexData.data,
			"header":state.dexData.header,
			"basedir" : curDir.toURI().toURL().toExternalForm()
		]
	}

	static void main(def args){
		TestGroovy_02 test = new TestGroovy_02()
	}

}
