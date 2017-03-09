package sandbox2.bs.javafx.test2Read;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Read extends Application{
	private static Stage primaryStage;
	private static Pane mainLayout; 
	private String percorso;
	private Document newDocument;
	
	@FXML
	private TextField pathField;
	@FXML
	private TextField nomeField;
	@FXML
	private TextField cognomeField;
	@FXML
	private TextField altezzaField;
	@FXML
	private TextField unitField;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Read File XML");		
		showHome();
	}
	private void showHome() throws IOException{
		FXMLLoader loader = new FXMLLoader();  //Loads an object hierarchy from an XML document
		loader.setLocation(Read.class.getResource("Home.fxml"));
		mainLayout = loader.load();
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	@FXML
	public void chooserWindow(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("File XML");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML File","*.xml"));
		this.percorso = fileChooser.showOpenDialog(primaryStage).getAbsolutePath();
		pathField.appendText(percorso);
	}
	@FXML
	public void load() throws ParserConfigurationException, Exception, IOException{
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentFactory.newDocumentBuilder();
		Document document = builder.parse(new File(percorso));
		NodeList persone = document.getElementsByTagName("persona");
		String nome = persone.item(0).getChildNodes().item(1).getTextContent();
		String cognome = persone.item(0).getChildNodes().item(3).getTextContent();
		String altezza = persone.item(0).getChildNodes().item(5).getTextContent();
		String unit = persone.item(0).getChildNodes().item(5).getAttributes().getNamedItem("unit").getNodeValue();
		this.nomeField.appendText(nome);
		this.cognomeField.appendText(cognome);
		this.altezzaField.appendText(altezza);
		this.unitField.appendText(unit);
	}
	@FXML
	public void update() throws Exception{
		
		String newNome = nomeField.getText();
		String newCognome = cognomeField.getText();
		String newAltezza = altezzaField.getText();
		String newUnit = unitField.getText();
		
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentFactory.newDocumentBuilder();
		Document document = builder.parse(new File(percorso));
		NodeList persone = document.getElementsByTagName("persona");
		
		persone.item(0).getChildNodes().item(1).setTextContent(newNome);
		persone.item(0).getChildNodes().item(3).setTextContent(newCognome);
		persone.item(0).getChildNodes().item(5).setTextContent(newAltezza);
		persone.item(0).getChildNodes().item(5).getAttributes().getNamedItem("unit").setNodeValue(newUnit);
		
		newDocument = document;
	}
	@FXML
	public void save() throws Exception{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("File XML");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XML File","*.xml"));
		String saveIn = fileChooser.showSaveDialog(primaryStage).getAbsolutePath();		
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(newDocument);
		StreamResult result = new StreamResult(new File(saveIn));
		transformer.transform(source, result);		
		
	}
	public static void main(String[] args) {
		launch(args);
	}

}

