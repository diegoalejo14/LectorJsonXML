package lectorLogCompleto;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ManejadorJSON {
	
	public ManejadorJSON() {
		// TODO Auto-generated constructor stub
	}
	
	
	public  String leerJSON(File ruta,String mensajeObtener) {
		JSONParser jsonParser = new JSONParser();

		FileReader reader=null ;
		try{
			reader = new FileReader(ruta);
			JSONObject a=null;
			Object json=jsonParser.parse(reader) ;
			if(json instanceof JSONArray) {
				a=(JSONObject) (((JSONArray) json).get(0));

			}else {
				a=(JSONObject) json; 
			}
			JSONObject jsonPayload = (JSONObject) a.get("jsonPayload");
			JSONObject dataObject = (JSONObject)jsonPayload.get("dataObject");
			JSONObject mensajes=(JSONObject) dataObject.get("messages");
			return (String) mensajes.get(mensajeObtener);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	public  List<String> cargarXMLDesdeJSON(File rutaArchivo,String campoJSON) throws Exception {
		StringBuilder request=new StringBuilder(leerJSON(rutaArchivo,campoJSON));
		File archivoRequest=escribirContenido(request);
		return  cargarXML(archivoRequest);
	}
	
	private File escribirContenido(StringBuilder contenido) throws IOException {
		File archivo=File.createTempFile("tmp", ".txt");
		FileWriter fw=new FileWriter(archivo); 
		fw.append(contenido);
		fw.flush();
		fw.close();
		return archivo;
	}
	
	private List<String> cargarXML(File rutaXML) throws Exception{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(rutaXML);
		XPath xPath =  XPathFactory.newInstance().newXPath();
		document.getDocumentElement().normalize();
		String expression = "//*[not(*)]";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		List<String> rutas=new ArrayList<String>();
		for(int i = 0 ; i < nodeList.getLength(); i++) {
			if("ns9:valParametro".equalsIgnoreCase((nodeList.item(i).getNodeName()))) {
				nodeList.item(i).getAttributes();
			}else {
				if(nodeList.item(i).getNodeName().contains("soapenv:Header")) {
					continue;
				}
				String rutaTmp=getXPath(nodeList.item(i));
				if(rutaTmp.contains("soapenv:Header")) {
					continue;
				}
				String a="#document:S:Envelope:S:Body:";
						
				rutaTmp=rutaTmp.replace("/", ":");
				if(rutaTmp.contains(a)) {
					rutaTmp=rutaTmp.replace(a, "");
				}
				rutas.add(rutaTmp);
			}
		}
		return rutas;
	}
	

	private  String getXPath(Node node) {
		Node parent = node.getParentNode();
		if (parent == null) {
			return node.getNodeName();
		}
		String ruta=getXPath(parent) + "/" + node.getNodeName();
		if(ruta.contains("#document/soapenv:Envelope/soapenv:Body/")) {
			ruta=ruta.replace("#document/soapenv:Envelope/soapenv:Body/", "");

		}
		return ruta;
	}


	

}
