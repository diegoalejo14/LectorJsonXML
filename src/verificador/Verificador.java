package verificador;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import lectorLogCompleto.ManejadorJSON;

public class Verificador {


	public static void main(String[] args) {
		new Verificador().validarArchivo();

	}


	private String validarArchivo() {
		File f=new File("./verificador/archivoOrigen.txt");
		try {
			ManejadorJSON manejadorJSON=new ManejadorJSON();
			FileReader fr=new FileReader(f);
			BufferedReader br=new BufferedReader(fr);
			String linea=null;
			int numeroLinea=0;
			List<LineaGenerada> lineasGeneradas=new ArrayList<LineaGenerada>();
			Map<String,String> mapaRutasXServicioEntrada=new HashMap<String, String>();
			Map<String,String> mapaRutasXServicioSalida=new HashMap<String, String>();
			Map<String,Map<String,String>> mapaServiciosEntrada = new HashMap<String, Map<String,String>>();
			Map<String,Map<String,String>> mapaServiciosSalida = new HashMap<String, Map<String,String>>();
			StringBuilder resultado=new StringBuilder();
			while((linea=br.readLine())!=null) {
				numeroLinea++;
				String[] valores=linea.split("\t");
				LineaGenerada lineaG=new LineaGenerada(valores);
				lineasGeneradas.add(lineaG);
				mapaRutasXServicioEntrada=estaEnMapa(mapaServiciosEntrada, lineaG.getServicio());
				mapaRutasXServicioSalida=estaEnMapa(mapaServiciosSalida, lineaG.getServicio());
				if(lineaG.isEntrada()) {
					mapaRutasXServicioEntrada.put(lineaG.getPath(), lineaG.getPath());
					mapaServiciosEntrada.put(lineaG.getServicio(), mapaRutasXServicioEntrada);
				}else {
					mapaRutasXServicioSalida.put(lineaG.getPath(), lineaG.getPath());
					mapaServiciosSalida.put(lineaG.getServicio(), mapaRutasXServicioSalida);
				}
			}
			
			File carpeta=new File("recursos");
			Iterator<Entry<String, Map<String, String>>> it=mapaServiciosEntrada.entrySet().iterator();
			while(it.hasNext()) {
				String nombreServicio=it.next().getKey();
				File archivoJSON=new File(carpeta,nombreServicio+".json");
				if(archivoJSON.exists()) {
					List<String> pathsRequest = manejadorJSON.cargarXMLDesdeJSON(archivoJSON,"requestService");
					String valorEntrada=validarMapa(pathsRequest, mapaServiciosEntrada.get(nombreServicio));
					List<String> pathsResponse = manejadorJSON.cargarXMLDesdeJSON(archivoJSON,"responseService");
					String valorSalida=validarMapa(pathsResponse, mapaServiciosSalida.get(nombreServicio));
					if(valorEntrada!=null) {
						resultado.append("Error validando el servicio: "+nombreServicio+" en path "+valorEntrada).append("\n");
					}
					if(valorSalida!=null) {
						resultado.append("Error validando el servicio: "+nombreServicio+" en path Salida "+valorSalida).append("\n");
					}
				}else {
					System.out.println("Archivo no encontrado : "+nombreServicio);
				}
			}
			System.out.println(resultado);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	private Map<String, String> estaEnMapa(Map<String,Map<String,String>> mapaServicio,String nombreServicio) {
		Map<String, String> mapaRes = mapaServicio.get(nombreServicio);
		if(mapaRes==null) {
			mapaRes=new HashMap<String, String>();
		}
		return mapaRes;
	}
	
	private String validarMapa(List<String> paths,Map<String,String> pathsJSON) {
		StringBuilder sb=new StringBuilder();
		for(String path:paths) {
			if(path.contains("soapenv:Header")||path.contains("Header")) {
				continue;
			}
			if(path.contains("S:Body")) {
				System.out.println();
				
			}
			if(pathsJSON.get(path)==null) {
				sb.append(path).append("\n");
			}
		}
		if(sb.length()==0) {
			return null;
		}
		return sb.toString();
	}
	
	
	
	
	
	
	
	


	
	
	
	
	
	

	
	
}
