package verificador;

public class LineaGenerada {

		private String servicio;
		private boolean entrada;
		private String variable;
		private String tipo;
		private String longitud;
		private String cardinalidad;
		private String path;
		private String valor;
		
		
		public LineaGenerada(String [] valores) {
			this.servicio=valores[0];
			this.entrada=valores[1].equalsIgnoreCase("in");
			this.variable=valores[2];
			this.tipo=valores[3];
			this.longitud=valores[4];
			this.cardinalidad=valores[5];
			this.path=valores[8];
			if(valores.length>9) {
				this.valor=valores[9];
			}
			
		}
		
		
		public boolean validar() {
			return tieneValorCadena(servicio)
					&& tieneValorCadena(tipo)
					&& tieneValorCadena(path);
		}

		private boolean tieneValorCadena(String valor) {
			return valor!=null && valor.replace(" ", "").length()>0;
			
		}


		public String getServicio() {
			return servicio;
		}


		public void setServicio(String servicio) {
			this.servicio = servicio;
		}


		public boolean isEntrada() {
			return entrada;
		}


		public void setEntrada(boolean entrada) {
			this.entrada = entrada;
		}


		public String getVariable() {
			return variable;
		}


		public void setVariable(String variable) {
			this.variable = variable;
		}


		public String getTipo() {
			return tipo;
		}


		public void setTipo(String tipo) {
			this.tipo = tipo;
		}


		public String getLongitud() {
			return longitud;
		}


		public void setLongitud(String longitud) {
			this.longitud = longitud;
		}


		public String getCardinalidad() {
			return cardinalidad;
		}


		public void setCardinalidad(String cardinalidad) {
			this.cardinalidad = cardinalidad;
		}


		public String getPath() {
			return path;
		}


		public void setPath(String path) {
			this.path = path;
		}


		public String getValor() {
			return valor;
		}


		public void setValor(String valor) {
			this.valor = valor;
		}

		
		
		
}
