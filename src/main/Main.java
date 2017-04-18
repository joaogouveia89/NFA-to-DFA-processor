package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Main {
	
	private static String[] alfabeto;
	private static HashMap<String, HashMap<String, String>> estados = new HashMap<String, HashMap<String, String>>();
	private static HashMap<Integer, String> codigosEstados = new HashMap<Integer, String>();

	public static void main(String[] args) throws IOException{
		String estadoAtual = null;
		String estadoInicial = null;
		String estadoFinal = null;
		HashMap<String, String> transicoesEstadoAtual = new HashMap<String, String>();
		
		String estadosConcatenados = "";
		String line;
		int codEstadoAtual = 0;
		int codTransicaoAtual = 0;
		String transicaoAtual;
		alfabeto = null;
		ArrayList<String> alfabetoArr = null;
		String[] linhaAtualAfn = null;
		
		FileReader fr = new FileReader("io/input.txt");
		FileWriter fw = new FileWriter("io/output.txt");
		
		BufferedReader textReader = new BufferedReader(fr);
		BufferedWriter textWriter = new BufferedWriter(fw);
		
		/*
		 * leitura do arquivo input.txt na pasta io.
		 * mapeamento dos dados serializados nas estruturas declaradas acima.
		 * o tratamento dos dados em memória é mais viável do que em disco, em termos
		 * de performance e facilidade de manipulação dos dados
		 */
		while((line = textReader.readLine())!= null){
			linhaAtualAfn = line.split(":");			
			if(linhaAtualAfn.length > 1){
				if(linhaAtualAfn[0].equals("i")){
					estadoInicial = linhaAtualAfn[1];
				}else if(linhaAtualAfn[0].equals("f")){
					estadoFinal = linhaAtualAfn[1];
				}else if(linhaAtualAfn[0].equals("AB")){
					alfabeto = linhaAtualAfn[1].trim().split(" ");
					alfabetoArr = new ArrayList<String>(Arrays.asList(alfabeto));
				}							
			}else{
				//colocando todas as transições do estado atual num hashmap
				linhaAtualAfn = linhaAtualAfn[0].trim().split(" ");
				if(estadoAtual != null && !estadoAtual.equals(linhaAtualAfn[0].trim())){
					transicoesEstadoAtual.clear();
				}
				estadoAtual = linhaAtualAfn[0].trim();
				estadosConcatenados = "";
				if(linhaAtualAfn.length == 3){
					if(alfabetoArr != null && alfabetoArr.contains(linhaAtualAfn[1].trim())){
						transicoesEstadoAtual.put(linhaAtualAfn[1].trim(), linhaAtualAfn[2]);
					}					
				}else{						
					for(int m = 2; m < linhaAtualAfn.length; m++){
						estadosConcatenados = estadosConcatenados + linhaAtualAfn[m];
						estadosConcatenados = estadosConcatenados + " ";
					}
					if(alfabetoArr != null && alfabetoArr.contains(linhaAtualAfn[1].trim())){
						transicoesEstadoAtual.put(linhaAtualAfn[1].trim(), estadosConcatenados);
					}					
				}
			}			
			
			if(estadoAtual != null){
				if(linhaAtualAfn[1].trim().equals("1")){
					estados.put(estadoAtual.trim(), getMudancasDeEstado(transicoesEstadoAtual));
				}				
			}
			
		}
		textReader.close();
		// fim da leitura e mapeamento do arquivo
		
		//inicio tratamento dos dados e escrita no arquivo io/output.txt
		textWriter.write("AB: ");
		for(int n = 0; n < alfabeto.length; n++){
			textWriter.write(alfabeto[n] + " ");
		}
		textWriter.newLine();
		textWriter.write("i: " + estadoInicial.trim());
		textWriter.newLine();
		textWriter.write("f: " + estadoFinal.trim());
		textWriter.newLine();
		
		//repetindo as transicoes do primeiro estado da tabela de AFN
		HashMap.Entry<String, HashMap<String, String>> firstState = estados.entrySet().iterator().next();
		for(int n = 0; n < alfabeto.length; n++){
			cadastrarEstado(estados.get(estados.keySet().iterator().next()).get(alfabeto[n]));
			codEstadoAtual = getCodigoEstado(firstState.getKey());
			textWriter.write(firstState.getKey()); //primeiro estado
			textWriter.write(" ");
			textWriter.write(alfabeto[n]); // simbolo do alfabeto
			textWriter.write(" ");
			codTransicaoAtual = getCodigoEstado(estados.get(estados.keySet().iterator().next()).get(alfabeto[n]));
			textWriter.write(Integer.toString(codTransicaoAtual));
			textWriter.newLine();			
		}
		
		while(codEstadoAtual < codigosEstados.size()){
			codEstadoAtual++;
			for(int n = 0; n < alfabeto.length; n++){
				textWriter.write(Integer.toString(codEstadoAtual));
				textWriter.write(" ");
				textWriter.write(alfabeto[n]); // simbolo do alfabeto
				textWriter.write(" ");
				transicaoAtual = calculateTransicao(codEstadoAtual, alfabeto[n]);
				cadastrarEstado(transicaoAtual);
				textWriter.write(Integer.toString(getCodigoEstado(transicaoAtual)));
				textWriter.newLine();
			}
		}
		System.out.println("AFD gerado com sucesso! Confira o resultado em io/output.txt");
		textWriter.close();
	}
	
	/**
	 * @description Método criado para resolver o problema de alocação dinamica do Hashmap, forçando a criação
	 * de do objeto para cada estado do AFN
	 * @return HashMap<String,String>
	 * @params HashMap<String, String> 
	 */
	private static HashMap<String, String> getMudancasDeEstado(HashMap<String, String> arrayMudancas){
		HashMap<String, String> mudancasEstadoAtual = new HashMap<String, String>();
		
		for(HashMap.Entry<String, String> entry : arrayMudancas.entrySet()){
			mudancasEstadoAtual.put(entry.getKey(), entry.getValue());
		}
		return mudancasEstadoAtual;
	}
	
	/**
	 * @description Método para cadastrar um estado novo na memória, caso o estado ja exista,
	 *  não é feito nada
	 * @return void
	 * @params String
	 */
	private static void cadastrarEstado(String estado){
		if(!estado.isEmpty()){
			if(codigosEstados.isEmpty()){
				codigosEstados.put(1, estado.trim());
			}else{
				if(!estadoJaExiste(estado)){
					int newVal = Collections.max(codigosEstados.keySet()) + 1; 
					codigosEstados.put(newVal , estado.trim());
				}
			}
		}
	}
	
	/**
	 * @description Método para verificar se um estado ja existe na memória
	 * @return boolean
	 * @param String
	 */
	
	private static boolean estadoJaExiste(String estado){
		for(HashMap.Entry<Integer, String> actual : codigosEstados.entrySet()){
			if(actual.getValue().equals(estado)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @description Método para buscar na memória(Hashmap) o código do estado
	 * @return int
	 * @param String
	 */
	private static int getCodigoEstado(String estado){
		if(codigosEstados.isEmpty()){
			return -1;
		}else{
			for(HashMap.Entry<Integer, String> actual : codigosEstados.entrySet()){
				if(actual.getValue().equals(estado.trim())){
					return actual.getKey();
				}
			}
		}
		return -1;
	}
	
	/**
	 * @description Método para buscar o estado pelo código
	 * @param int
	 * @return String
	 */
	private static String getEstado(int codigo){
		return codigosEstados.get(codigo);
	}
	
	/**
	 * @description Método para calcular a transicao de estados
	 * @param codEstAtual
	 * @param simbolo
	 * @return String
	 */
	private static String calculateTransicao(int codEstAtual, String simbolo){
		String estado = getEstado(codEstAtual);
		String[] estadosSplitados = estado.split(" ");
		String result = "";
		String transicoesAfn;
		for(int n = 0; n < estadosSplitados.length; n++){
			transicoesAfn = estados.get(estadosSplitados[n].trim()).get(simbolo.trim());
			result = result + " ";
			result = result + transicoesAfn;
		}
		return result.trim().replaceAll(" +", " ");
	}
}