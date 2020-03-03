package grafos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class Transformador {

	public static void main(String[] args) throws Exception {
		// Ruta del fichero con el programa que vamos a transformar
		String ruta = "./src/main/java/ejemplos/Bucles_6";
		
		// Abrimos el fichero original (un ".java")
		File original = new File(ruta + ".java");
		        
		// Parseamos el fichero original. Se crea una unidad de compilación (un AST).
		CompilationUnit cu = JavaParser.parse(original);
		
		quitarComentarios(cu);
		
		// Recorremos el AST
		CFG cfg = new CFG();
		VoidVisitor<CFG> visitador = new Visitador();
		visitador.visit(cu,cfg);

		// Imprimimos el CFG del programa 
		String dotInfo = imprimirGrafo(cfg.arcos);
		
		// Generamos un PDF con el CFG del programa
		System.out.print("\nGenerando PDF...");
	    GraphViz gv=new GraphViz();
	    gv.addln(gv.start_graph());
	    gv.add(dotInfo);
	    gv.addln(gv.end_graph());
	    String type = "pdf";   // String type = "gif";
	  // gv.increaseDpi();
	    gv.decreaseDpi();
	    gv.decreaseDpi();
	    gv.decreaseDpi();
	    gv.decreaseDpi();
	    File destino_CFG = new File(ruta + "_CFG."+ type);
	    gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), destino_CFG);
	    System.out.println("     PDF generado!");
	}

	// Imprime el grafo en la pantalla
	private static String imprimirGrafo(List<String> arcos)
	{
		String dotInfo="";
		for(String arco:arcos) {
			dotInfo += arco;	
			System.out.println("ARCO: "+arco);
		}
		System.out.println("\nCFG:");
		System.out.println(dotInfo);

		return dotInfo;
	}
	
	// Elimina todos los comentarios de un nodo y sus hijos
	static void quitarComentarios(Node node){
		node.removeComment();
		for (Comment comment : node.getOrphanComments())
		{
			node.removeOrphanComment(comment);
		}
	    // Do something with the node
	    for (Node child : node.getChildNodes()){
	    	quitarComentarios(child);
	    }
	}
	
}


////////////////////////////////////////////////////////////////
// COMO CONFIGURAR GRAPHVIZ:
////////////////////////////////////////////////////////////////
//
//Update config.properties file. Copy paste the following:
//
//##############################################################
//#                    Linux Configurations                    #
//##############################################################
//# The dir. where temporary files will be created.
//tempDirForLinux = /tmp
//# Where is your dot program located? It will be called externally.
//dotForLinux = /usr/bin/dot
//
//##############################################################
//#                   Windows Configurations                   #
//##############################################################
//# The dir. where temporary files will be created.
//tempDirForWindows = c:/temp
//# Where is your dot program located? It will be called externally.
//dotForWindows = "c:/Program Files (x86)/Graphviz 2.28/bin/dot.exe"
//
//##############################################################
//#                    Mac Configurations                      #
//##############################################################
//# The dir. where temporary files will be created.
//tempDirForMacOSX = /tmp
//# Where is your dot program located? It will be called externally.
//dotForMacOSX = /usr/local/bin/dot





