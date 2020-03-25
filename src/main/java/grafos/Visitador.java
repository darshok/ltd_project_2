package grafos;
	
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


public class Visitador extends VoidVisitorAdapter<CFG>
{
	/********************************************************/
	/********************** Atributos ***********************/
	/********************************************************/
	
	// Usamos un contador para numerar las instrucciones
	int contador=1;
	String nodoActual = "";
/*	String nodoAnterior = "Start";
	String nodoIni = "";
	List<String> listaTiposNodos = new ArrayList<>();
	List<String> listaNodosControl = new ArrayList<>();*/
	List<String> listaNodos = new ArrayList<>();
	Expression ifCondition;
	Expression whileCondition;
	Expression doCondition;
	List<Expression> forIni = new ArrayList<>();
	Optional forCondition;
	List<Expression> forUpd = new ArrayList<>();

	/********************************************************/
	/*********************** Metodos ************************/
	/********************************************************/

		// Visitador de métodos
	// Este visitador añade el nodo final al CFG	
	@Override	
	public void visit(MethodDeclaration methodDeclaration, CFG cfg)
	{
		listaNodos.add("Start");
	    // Visitamos el método
		super.visit(methodDeclaration, cfg);
		// Añadimos el nodo final al CFG
		for(String nodo : listaNodos) {
			cfg.arcos.add(nodo + "-> Stop;");
		}
	}
	
	// Visitador de expresiones
	// Cada expresión encontrada genera un nodo en el CFG	
	@Override
	public void visit(ExpressionStmt es, CFG cfg)
	{
		// Creamos el nodo actual
		nodoActual = crearNodo(es);
		crearArcos(cfg);
//		checkLastNode(cfg);
//		nodoAnterior = nodoActual;

		listaNodos.clear();
		listaNodos.add(nodoActual);

		// Seguimos visitando...
		super.visit(es, cfg);
	}

	@Override
	public void visit(IfStmt ifStmt, CFG cfg){
		ifCondition = ifStmt.getCondition();
		nodoActual = crearNodo(" if (" + ifCondition + ")");

		String ifNode =  nodoActual;
//		checkLastNode(cfg);
		crearArcos(cfg);
//		nodoAnterior = nodoActual;
		listaNodos.clear();
		listaNodos.add(nodoActual);
		ifStmt.getThenStmt().accept(this,cfg);

//		String thenLastNode = nodoAnterior;
		// auxiliar que guarda los nodos de antes del then
		List<String> listThen = new ArrayList<>(listaNodos);

		if(ifStmt.getElseStmt().isPresent()) {
//			nodoAnterior = ifNode;
			listaNodos.clear();
			listaNodos.add(ifNode);
			ifStmt.getElseStmt().get().accept(this,cfg);

/*			listaTiposNodos.add("ifElse");
			listaNodosControl.add(thenLastNode);*/
			listaNodos.addAll(listThen);
		} else {
			/*listaTiposNodos.add("ifThen");
			listaNodosControl.add(ifNode);*/
			listaNodos.add(ifNode);
		}
	}

	@Override
	public void visit(WhileStmt whileStmt, CFG cfg){
		whileCondition = whileStmt.getCondition();
		nodoActual = crearNodo("while (" + whileCondition + ")");
		String whileNode = nodoActual;

/*		listaTiposNodos.add("while");
		listaNodosControl.add(whileNode);
		checkLastNode(cfg);*/

		crearArcos(cfg);
		listaNodos.clear();
		listaNodos.add(nodoActual);
//		nodoAnterior = nodoActual;

		whileStmt.getBody().accept(this,cfg);
//		nodoAnterior = nodoActual;
		nodoActual = whileNode;
		crearArcos(cfg);
//		nodoAnterior = whileNode;
		listaNodos.clear();
		listaNodos.add(whileNode);
	}

	@Override
	public void visit(DoStmt doStmt, CFG cfg){
		doCondition = doStmt.getCondition();
		String doNode = crearNodo("do");
		nodoActual = doNode;
		crearArcos(cfg);

/*		listaTiposNodos.add("doWhile");
		listaNodosControl.add(crearNodo(doStmt));*/
		listaNodos.clear();
		listaNodos.add(doNode);

		doStmt.getBody().accept(this, cfg);

		/*String endNode = nodoAnterior;
		String whileNode = crearNodo("while (" + doCondition + ")");*/
		List<String> finalNodes = new ArrayList<>(listaNodos);
		String whileNode = crearNodo("while (" + doCondition + ")");

		listaNodos.clear();
		listaNodos.add(whileNode);
		nodoActual = doNode;
		crearArcos(cfg);

		for(String nodo : finalNodes){
			añadirArco(cfg, nodo, whileNode);
		}

		/*nodoAnterior = whileNode;
		nodoActual = endNode;
		añadirArcoSecuencialCFG(cfg);
		añadirArco(cfg, whileNode, nodoIni);*/
	}

	@Override
	public void visit(ForStmt forStmt, CFG cfg){
		forIni = forStmt.getInitialization();
		String iniNode = crearNodo(forIni);
		nodoActual = iniNode;
		crearArcos(cfg);
		listaNodos.clear();
		listaNodos.add(nodoActual);

		forCondition = forStmt.getCompare();
		String condNode = crearNodo("for (" + forCondition.get() + ")");
		nodoActual = condNode;
		crearArcos(cfg);
		listaNodos.clear();
		listaNodos.add(nodoActual);

		forUpd = forStmt.getUpdate();
		String updNode = crearNodo(forUpd);
		forStmt.getBody().accept(this, cfg);
		nodoActual = updNode;
		crearArcos(cfg);
		añadirArco(cfg,updNode,condNode);
		listaNodos.clear();
		listaNodos.add(condNode);
	}

	/*//Comprueba si salimos de un if else y crea el arco
	private void checkLastNode(CFG cfg){
		while (!listaNodosControl.isEmpty()) {
			int lastTipo = listaTiposNodos.size()-1;
			int lastNodo = listaNodosControl.size()-1;
			if(listaTiposNodos.get(lastTipo) == "ifElse"){
				añadirArco(cfg, listaNodosControl.get(lastNodo));
				listaTiposNodos.remove(lastTipo);
				listaNodosControl.remove(lastNodo);
			} else if (listaTiposNodos.get(lastTipo) == "ifThen") {
				añadirArco(cfg, listaNodosControl.get(lastNodo));
				listaTiposNodos.remove(lastTipo);
				listaNodosControl.remove(lastNodo);
			} else if(listaTiposNodos.get(lastTipo) == "doWhile"){
				nodoIni = nodoActual;
				listaNodosControl.remove(lastNodo);
				listaTiposNodos.remove(lastTipo);
			} else if(listaTiposNodos.get(lastTipo) == "while"){
				añadirArco(cfg, listaNodosControl.get(lastNodo));
				listaNodosControl.remove(lastNodo);
				listaTiposNodos.remove(lastTipo);
			} else break;
		}
	}*/
	
	// Añade un arco desde el último nodo hasta el nodo actual (se le pasa como parametro)
	private void añadirArcoSecuencialCFG(CFG cfg)
	{
		System.out.println("NODO: " + nodoActual);
		
		for(String nodo : listaNodos){
			String arco = nodo + "->" + nodoActual + ";";
			cfg.arcos.add(arco);
		}
	}

	/*private void añadirArco(CFG cfg, String nodo)
	{
		System.out.println("NODO: " + nodoActual);

		String arco = nodo + "->" + nodoActual + ";";
		cfg.arcos.add(arco);
	}*/

	// Añade un arco de un nodo1 a un nodo2
	private void añadirArco(CFG cfg, String nodo1, String nodo2)
	{
		System.out.println("NODO: " + nodo2);

		String arco = nodo1 + "->" + nodo2 + ";";
		cfg.arcos.add(arco);
	}
	
	// Crear arcos
	private void crearArcos(CFG cfg)
	{
			añadirArcoSecuencialCFG(cfg);
	}

	// Crear nodo
	// Añade un arco desde el nodo actual hasta el último control
	private String crearNodo(Object objeto)
	{
		return "\"("+ contador++ +") "+quitarComillas(objeto.toString())+"\"";
	}
	
	// Sustituye " por \" en un string: Sirve para eliminar comillas.
	private static String quitarComillas(String texto)
	{
	    return texto.replace("\"", "\\\"");
	}
	
	// Dada una sentencia, 
	// Si es una �nica instrucci�n, devuelve un bloque equivalente 
	// Si es un bloque, lo devuelve
	private BlockStmt convertirEnBloque(Statement statement)
	{
		if (statement instanceof BlockStmt)
			return (BlockStmt) statement;

		BlockStmt block = new BlockStmt();
		NodeList<Statement> blockStmts = new NodeList<Statement>();
		blockStmts.add(statement);

		block.setStatements(blockStmts);

		return block;
	}
	
}
