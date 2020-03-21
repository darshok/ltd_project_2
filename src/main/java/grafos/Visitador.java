package grafos;
	
import java.util.ArrayList;
import java.util.List;

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
	String nodoAnterior = "Start";
	String nodoActual = "";
	List<String> listaNodosControl = new ArrayList<>();
	List<String> listaTiposNodos = new ArrayList<>();
	Expression ifCondition;
	Expression whileCondition;

	/********************************************************/
	/*********************** Metodos ************************/
	/********************************************************/

		// Visitador de métodos
	// Este visitador añade el nodo final al CFG	
	@Override	
	public void visit(MethodDeclaration methodDeclaration, CFG cfg)
	{
	    // Visitamos el método
		super.visit(methodDeclaration, cfg);
		
		// Añadimos el nodo final al CFG
		cfg.arcos.add(nodoAnterior+"-> Stop;");
	}
	
	// Visitador de expresiones
	// Cada expresión encontrada genera un nodo en el CFG	
	@Override
	public void visit(ExpressionStmt es, CFG cfg)
	{
		// Creamos el nodo actual
		nodoActual = crearNodo(es);

		crearArcos(cfg);

		checkLastNode(cfg);

		nodoAnterior = nodoActual;
		
		// Seguimos visitando...
		super.visit(es, cfg);
	}

	@Override
	public void visit(IfStmt ifStmt, CFG cfg){
		ifCondition = ifStmt.getCondition();
		nodoActual = crearNodo(" if (" + ifCondition + ")");
		final String ifNode =  nodoActual;
		checkLastNode(cfg);
		crearArcos(cfg);
		nodoAnterior = nodoActual;

		ifStmt.getThenStmt().accept(this,cfg);

		final String thenLastNode = nodoAnterior;

		if(ifStmt.getElseStmt().isPresent()) {
			nodoAnterior = ifNode;
			ifStmt.getElseStmt().get().accept(this,cfg);
			listaTiposNodos.add("ifElse");
			listaNodosControl.add(thenLastNode);
		} else {
			listaTiposNodos.add("ifThen");
			listaNodosControl.add(ifNode);
		}
	}

	@Override
	public void visit(WhileStmt whileStmt, CFG cfg){
		whileCondition = whileStmt.getCondition();
		nodoActual = crearNodo("while (" + whileCondition + ")");
		final String whileNode = nodoActual;

		checkLastNode(cfg);

		añadirArcoSecuencialCFG(cfg);
		nodoAnterior = nodoActual;

		whileStmt.getBody().accept(this,cfg);
		nodoAnterior = nodoActual;
		nodoActual = whileNode;
		añadirArcoSecuencialCFG(cfg);
		nodoAnterior = whileNode;
	}

	//Comprueba si salimos de un if else y crea el arco
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
			} else break;
		}
	}
	
	// Añade un arco desde el último nodo hasta el nodo actual (se le pasa como parametro)
	private void añadirArcoSecuencialCFG(CFG cfg)
	{
		System.out.println("NODO: " + nodoActual);
		
		String arco = nodoAnterior + "->" + nodoActual + ";";
		cfg.arcos.add(arco);
	}

	private void añadirArco(CFG cfg, String nodo)
	{
		System.out.println("NODO: " + nodoActual);

		String arco = nodo + "->" + nodoActual + ";";
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
