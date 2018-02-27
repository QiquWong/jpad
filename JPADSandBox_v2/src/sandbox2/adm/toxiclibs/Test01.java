package sandbox2.adm.toxiclibs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import standaloneutils.JPADXmlReader;
import toxi.geom.Line3D;
import toxi.geom.mesh.STLReader;
import toxi.geom.mesh.WEFace;
import toxi.geom.mesh.WETriangleMesh;
import toxi.geom.mesh.WingedEdge;

class MyArgumentTest01 {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}

}

public class Test01 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	public static void main(String[] args) {

		System.out.println("----------------------");
		System.out.println("Testing toxiclibs");
		System.out.println("----------------------");

		MyArgumentTest01 va = new MyArgumentTest01();
		Test01.theCmdLineParser = new CmdLineParser(va);

		try {
			Test01.theCmdLineParser.parseArgument(args);
			String pathToSTL = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToSTL);
			System.out.println("--------------");

			System.out.println("importing the binary STL into a TriangleMesh...");

			toxi.geom.mesh.TriangleMesh mesh =
					(toxi.geom.mesh.TriangleMesh)new STLReader().loadBinary(
							pathToSTL,STLReader.TRIANGLEMESH);
			System.out.println("Report - " + mesh.toString());

			System.out.println("Faces:");
			mesh.getFaces().stream()
			.forEach(f -> System.out.println(f));

			System.out.println("Building the WETriangleMesh...");

			WETriangleMesh wemesh = new WETriangleMesh("Agodemar Winged-Edge mesh");
			wemesh.addMesh(mesh);
			System.out.println("Report - " + wemesh.toString());

			System.out.println("Edges:");
			// a lambda function taking 2 arguments
			BiConsumer<? super Line3D, ? super WingedEdge> actionEdge = (line, we) -> {
				//System.out.println(line);
				System.out.println(we);
				System.out.println("  Faces: "); 
				we.faces.forEach(f -> {
					StringBuilder sb = new StringBuilder();
					sb.append("  Face: ");
					sb.append("  "+ f.a +", "+ f.b +", "+ f.c);
					System.out.println(sb.toString());
				});
			};
			wemesh.edges.forEach(actionEdge);


		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			Test01.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

	}

}
