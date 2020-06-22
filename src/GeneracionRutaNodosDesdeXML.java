
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GeneracionRutaNodosDesdeXML {

	public static void main(String[] args) throws Exception {
		generarTrazabilidad();
	}

	private static void generarTrazabilidad()
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		File carpetaRecursos=new File("./recursos");
		File carpetaXSD=new File(carpetaRecursos,"xsd");
		File carpetaJSON=new File(carpetaRecursos,"json");
		File carpetaXML=new File(carpetaRecursos,"xml");
		System.out.println(carpetaRecursos.getAbsolutePath());
		String[] archivos = carpetaXML.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("Req") && name.endsWith(".xml");
			}
		});
		String expression = "//*[not(*)]";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		StringBuilder sb=new StringBuilder();
		XPath xPath =  XPathFactory.newInstance().newXPath();
		for(String archivo:archivos) {
			//Obtiene el request
			String nombreServicio=archivo.replace("Req", "").replace(".xml", "");
			Map<String, String> mapaEntrada=new HashMap<String, String>();
			Map<String, String> mapaSalida =new HashMap<String, String>();
			String archivoResponse= archivo.replace("Req", "Resp");

			File archivoJson=new File(carpetaJSON,nombreServicio+".json");
			if(archivoJson.exists()) {
				StringBuilder request=new StringBuilder();
				StringBuilder response=new StringBuilder();
				leerJSON(archivoJson, request, response);
				mapaEntrada = extraerParametros(xPath, expression, builder, request.toString());
				mapaSalida = extraerParametros(xPath, expression, builder, response.toString());
			}else {
				System.out.println("No existe log Operacional de Ejemplo para el servicio : "+nombreServicio);
			}
			File fileXsd=new File(carpetaXSD,nombreServicio+".xsd");
			Map<String, TipoDato> mapaTipos =new HashMap<String, GeneracionRutaNodosDesdeXML.TipoDato>();
			if(fileXsd.exists()) {
				mapaTipos = leerXSD(xPath, expression, builder, fileXsd);
			}else {
				System.out.println("No se pudo encontro XSD "+nombreServicio);
			}
			

			sb.append(leerXML(xPath, expression, builder,new File(carpetaXML,archivo),true,nombreServicio,mapaEntrada,mapaTipos));

			if(new File(carpetaXML,archivoResponse).exists()) {
				sb.append(leerXML(xPath, expression, builder,new File(carpetaXML,archivoResponse),false,nombreServicio,mapaSalida,mapaTipos));
			}
		}
		File salida=new File("Resultado.txt");
		FileWriter fw=new FileWriter(salida);
		fw.append(sb);
		fw.flush();
		fw.close();
	}

	private static StringBuilder leerXML(XPath xPath, String expression, DocumentBuilder builder,File archivo,
			boolean request,String nombreServicio,Map<String,String> mapaDatos,Map<String,TipoDato> mapaTipo)
					throws SAXException, IOException, XPathExpressionException {
		Document document = builder.parse(archivo);
		document.getDocumentElement().normalize();
		StringBuilder sb=new StringBuilder();
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		String prefijo=null;
		String patron=null;
		for(int i = 0 ; i < nodeList.getLength(); i++) {
			if("ns9:valParametro".equalsIgnoreCase((nodeList.item(i).getNodeName()))) {
				nodeList.item(i).getAttributes();
				sb.append((nodeList.item(i).getNodeName())+":");
				sb.append((nodeList.item(i).getAttributes().item(0))+";");
				sb.append(getXPath(nodeList.item(i))+";");
				sb.append((nodeList.item(i).getAttributes().item(1)));
			}else {
				if(nodeList.item(i).getNodeName().contains("soapenv:Header")) {
					continue;
				}
				sb.append(nombreServicio+"\t");
				if(request) {
					sb.append("IN\t");
				}else {
					sb.append("OUT\t");
				}
				sb.append((nodeList.item(i).getNodeName())+"\t");
				
				String rutaTmp=getXPath(nodeList.item(i));
				rutaTmp=rutaTmp.replace("/", ":");
				if(nombreServicio.contains("EvaluadorLight") ) {
					System.out.println();
					
				}else {
					System.out.println();
				}
				String nombreAtributo=rutaTmp.substring(rutaTmp.lastIndexOf(":")+1);
				TipoDato tipo=mapaTipo.get(nombreAtributo);
				if(tipo!=null) {
					//sb.append(tipo.tipo).append("\t\t").append(tipo.cardinalidad);
					sb.append(tipo.tipo).append("\t\t").append("\t");
				}else {
					sb.append("\t\t");
				}
				sb.append("\t\t\t\t");
				
				if(mapaDatos.get(rutaTmp)!=null) {
					sb.append(rutaTmp);
					sb.append("\t").append(mapaDatos.get(rutaTmp));
				}else {
					String tmp	=rutaTmp.substring(rutaTmp.indexOf(':')+1);
					String nombreCampo=rutaTmp.substring(rutaTmp.lastIndexOf(':')+1);
							
					boolean encontreDato=false;
					for(Entry<String, String> entrada:mapaDatos.entrySet()) {
						String nombreCampoMapa=entrada.getKey().substring(entrada.getKey(). lastIndexOf(':')+1);
//						String tmp2=entrada.getKey().substring(entrada.getKey().indexOf(':')+1);
						if(nombreCampoMapa.equalsIgnoreCase(nombreCampo)) {
							
//						if(tmp.equalsIgnoreCase(tmp2)) {
							prefijo=entrada.getKey().substring(0,entrada.getKey().indexOf(':')+1);
							patron=rutaTmp.substring(0,rutaTmp.indexOf(':')+1);
							sb.append(entrada.getKey());
							sb.append("\t").append(mapaDatos.get(entrada.getKey()));
							encontreDato=true;
						}
					}
					if(!encontreDato) {
						sb.append(rutaTmp);
					}
				}
				//sb.append((nodeList.item(i).getTextContent()));
				sb.append(System.getProperty("line.separator"));
			}
		}
		if(prefijo!=null) {
			return new StringBuilder(		sb.toString().replace(patron, prefijo));
		}else {

			return sb;
		}
	}

	private static Map<String, String> extraerParametros(XPath xPath, String expression, DocumentBuilder builder,
			String contenido)
					throws SAXException, IOException, XPathExpressionException {
		File archivo=File.createTempFile("tmp", ".txt");
		FileWriter fw=new FileWriter(archivo); 
		fw.append(contenido);
		fw.flush();
		fw.close();
		Map<String,String> mapaDatos=new HashMap<String, String>();

		Document document = builder.parse(archivo);
		document.getDocumentElement().normalize();
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
		for(int i = 0 ; i < nodeList.getLength(); i++) {

			if("ns9:valParametro".equalsIgnoreCase((nodeList.item(i).getNodeName()))) {
				nodeList.item(i).getAttributes();
			}else {
				if(nodeList.item(i).getNodeName().contains("soapenv:Header")||nodeList.item(i).getNodeName().contains("soap:Header")) {
					continue;
				}
				String rutaTmp=getXPath(nodeList.item(i));
				rutaTmp=rutaTmp.replace("/", ":");
				String valor = (nodeList.item(i).getTextContent());
				if(valor!=null) {
					mapaDatos.put(rutaTmp,valor);
				}
			}

		}
		return mapaDatos;
	}

	private static String getXPath(Node node) {
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


	private static void leerJSON(File ruta,StringBuilder request,StringBuilder response) {
		JSONParser jsonParser = new JSONParser();

		FileReader reader=null ;
		try{
			//Read JSON file
			reader = new FileReader(ruta);
			JSONObject a=null;
			Object json=jsonParser.parse(reader) ;
			if(json instanceof JSONArray) {
				//				JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);
				a=(JSONObject) (((JSONArray) json).get(0));

			}else {
				a=(JSONObject) json; 
			}

			JSONObject jsonPayload = (JSONObject) a.get("jsonPayload");
			JSONObject dataObject = (JSONObject)jsonPayload.get("dataObject");
			JSONObject mensajes=(JSONObject) dataObject.get("messages");
			request.append((String) mensajes.get("requestService"));
			response.append((String) mensajes.get("responseService"));
		} catch (Exception e) {
			System.out.println(ruta.getAbsoluteFile()); 
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
	}


	private static Map<String, TipoDato> leerXSD(XPath xPath, String expression, DocumentBuilder builder,File archivo){
		Map<String,TipoDato> mapaDatos=new HashMap<String, TipoDato>();

		Document document;
		try {
			document = builder.parse(archivo);
			document.getDocumentElement().normalize();
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			for(int i = 0 ; i < nodeList.getLength(); i++) {
				Node nodo = nodeList.item(i);
				String nombre=obtenerAtributo("name", nodo);
				String cardinalidad=obtenerAtributo("minOccurs", nodo);
				cardinalidad=cardinalidad!=null?cardinalidad:"0";
				String tipo=obtenerAtributo("type", nodo);
				if(tipo==null||tipo.contains("tns:")) {
					continue;
				}
				GeneracionRutaNodosDesdeXML gen = new GeneracionRutaNodosDesdeXML();
				mapaDatos.put(nombre, gen.new TipoDato(nombre,Integer.parseInt(cardinalidad), tipo));
			}
//			for(Entry<String, TipoDato> entrada:mapaDatos.entrySet()) {
//				TipoDato tipo=entrada.getValue();
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapaDatos;
	}
	
	
	private static String obtenerAtributo(String nombreAtributo,Node nodo) {
		Node nodoAtributo=nodo.getAttributes().getNamedItem(nombreAtributo);
		if(nodoAtributo!=null) {
			return nodoAtributo.getNodeValue();
		}
		return null;
			
	}

	class TipoDato{
		
		private String nombre;
		private int cardinalidad;
		private String tipo ;
		
		public TipoDato(String nombre, int cardinalidad, String tipo) {
			super();
			this.nombre = nombre;
			this.cardinalidad = cardinalidad;
			if(tipo!=null) {
				tipo=tipo.replace("xsd:", "");
			}
			this.tipo = tipo;
		}
		
		
		
		
		
		
		
	}

}