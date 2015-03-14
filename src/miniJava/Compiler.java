package miniJava;

import mJAM.Disassembler;
import mJAM.ObjectFile;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.SourceFile;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.CodeGenerator.Generator;
import miniJava.ContextualAnalyzer.Checker;
import miniJava.ContextualAnalyzer.Identify;


public class Compiler {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SourceFile source = new SourceFile(args[0]);
        Scanner scanner = new Scanner(source);
        Parser parser = new Parser(scanner);
        ErrorReporter eReport = new ErrorReporter();
        Identify ider = new Identify(eReport);
        
        AST ast = parser.parse();
        ider.id(ast);
        Generator codG = new Generator();
        codG.generate(ast);
        String filename = args[0];
        String newfname = filename.replaceAll(".java", ".mJAM");
        ObjectFile obj = new ObjectFile(newfname);
        obj.write();
        Disassembler d = new Disassembler(newfname);
		if (d.disassemble()) {
			System.out.println("FAILED!");
			return;
		}
        System.exit(0);
        
        
        
        
	}

}
