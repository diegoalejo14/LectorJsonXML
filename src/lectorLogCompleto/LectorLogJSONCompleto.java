package lectorLogCompleto;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LectorLogJSONCompleto {
	
	public static void main(String[] args) {
		new LectorLogJSONCompleto().leerArchivo();
	}
	
	
	public void leerArchivo() {
		File carpeta=new File("lectorjson");
		File carpetaGuardar=new File("recursos");
//		carpetaGuardar.delete();
//		carpetaGuardar.mkdir();
		System.out.println(carpeta.getAbsoluteFile()); 
		System.out.println(carpeta.exists());
		File rutaLog=new File(carpeta,"LogCompleto.json");
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader=null ;
		try{
			Properties properties=new Properties();
			properties.load(new FileReader(new File(carpeta,"logxServicio.properties")));
			reader = new FileReader(rutaLog);
			JSONArray a=null;
			Object json=jsonParser.parse(reader) ;
			a=((JSONArray) json);
			for (int i = 0; i < a.size(); i++) {
				JSONObject objeto = (JSONObject) a.get(i);
				JSONObject jsonPayload = (JSONObject) objeto.get("jsonPayload");
				JSONObject dataObject = (JSONObject)jsonPayload.get("dataObject");
				JSONObject mensajes=(JSONObject) dataObject.get("messages");
				String nombreServicio=(String) mensajes.get("idService");
				if(nombreServicio!=null) {
					String nombreServicioTmp=nombreServicio;
					nombreServicio=(String) properties.get(nombreServicio);
					if(nombreServicio==null) {
						nombreServicio=nombreServicioTmp;
					}
					if(nombreServicio!=null && nombreServicio.contains("srvScnTokenQrService")
							&& objeto.toJSONString().contains("msjSolOpValidarOTPQR")) {
						nombreServicio="validarOTPQR";
					}
				}
				File archivoGuardar=new File(carpetaGuardar,nombreServicio+".json");
				if(archivoGuardar.exists()) {
					archivoGuardar.delete();
					System.out.println("Eliminando "+archivoGuardar.getAbsolutePath());
				}
				FileWriter fw=new FileWriter(archivoGuardar);
				fw.write(objeto.toJSONString());
				fw.flush();
				fw.close();
				
				
				 
				//				JSONObject jsonPayload = (JSONObject) ((ArrayList) a.get(i)).get("jsonPayload");
//				JSONObject dataObject = (JSONObject)jsonPayload.get("dataObject");
//				JSONObject mensajes=(JSONObject) dataObject.get("messages");
//				System.out.println(a.get(i));
			}
			
//			if(json instanceof JSONArray) {
//				a=(JSONObject) (((JSONArray) json).get(0));
//			}else {
//				a=(JSONObject) json; 
//			}
//			JSONObject jsonPayload = (JSONObject) a.get("jsonPayload");
//			JSONObject dataObject = (JSONObject)jsonPayload.get("dataObject");
//			JSONObject mensajes=(JSONObject) dataObject.get("messages");
//			return (String) mensajes.get(mensajeObtener);
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
	}

}
