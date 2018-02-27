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
import toxi.geom.Vec3D;
import toxi.geom.mesh.BezierPatch;
import toxi.geom.mesh.Mesh3D;
import toxi.geom.mesh.STLReader;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.WETriangleMesh;
import toxi.geom.mesh.WingedEdge;
import toxi.math.noise.SimplexNoise;

class MyArgumentTest02 {
	@Option(name = "-i", aliases = { "--input" }, required = true,
			usage = "my input file")
	private File _inputFile;

	@Option(name = "-o", aliases = { "--output-dir" }, required = true,
			usage = "output dir")
	private File _outputDir;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	public File getOutputDir() {
		return _outputDir;
	}

}

public class Test02 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;
	public static JPADXmlReader reader;

	public static void main(String[] args) {

		System.out.println("----------------------");
		System.out.println("Testing toxiclibs");
		System.out.println("----------------------");

		MyArgumentTest02 va = new MyArgumentTest02();
		Test02.theCmdLineParser = new CmdLineParser(va);

		try {
			Test02.theCmdLineParser.parseArgument(args);
			String pathToSTL = va.getInputFile().getAbsolutePath();
			System.out.println("INPUT ===> " + pathToSTL);
			String pathToOutDir = va.getOutputDir().getAbsolutePath();
			System.out.println("OUTPUT DIR ===> " + pathToOutDir);
			System.out.println("--------------");


			// define the patch
			// 4x4 bezier patch implementation with tesselation support (dynamic resolution) 
			// for generating triangle mesh representations. 
			
//			float NS = 0.05f;
//			float SIZE = 100;
//			float AMP = SIZE*4;
			Mesh3D mesh;
//
//			float phase = NS*0.1f;
			BezierPatch patch = new BezierPatch();
//			for (int y = 0; y < 4; y++) {
//				for (int x = 0; x < 4; x++) {
//					float xx = x * SIZE;
//					float yy = y * SIZE;
//					float zz = (float) (SimplexNoise.noise(xx * NS, yy * NS,phase) * AMP);
//					patch.set(x, y, new Vec3D(xx, yy, zz));
//				}
//			}
			patch.set(0, 0, new Vec3D(0.0f, 0.0f, 1.0f));
			patch.set(1, 0, new Vec3D(1.0f, 0.0f, 0.0f));
			patch.set(2, 0, new Vec3D(2.0f, 0.0f, 0.0f));
			patch.set(3, 0, new Vec3D(3.0f, 0.0f, 0.0f));
			patch.set(0, 1, new Vec3D(0.0f, 1.0f, 0.0f));
			patch.set(1, 1, new Vec3D(1.0f, 1.0f, 0.0f));
			patch.set(2, 1, new Vec3D(2.0f, 1.0f, 0.0f));
			patch.set(3, 1, new Vec3D(3.0f, 1.0f, 0.0f));
			patch.set(0, 2, new Vec3D(0.0f, 2.0f, 0.0f));
			patch.set(1, 2, new Vec3D(1.0f, 2.0f,-1.0f));
			patch.set(2, 2, new Vec3D(2.0f, 2.0f,-1.0f));
			patch.set(3, 2, new Vec3D(3.0f, 2.0f, 0.0f));
			patch.set(0, 3, new Vec3D(0.0f, 3.0f, 0.0f));
			patch.set(1, 3, new Vec3D(1.0f, 3.0f, 0.0f));
			patch.set(2, 3, new Vec3D(2.0f, 3.0f, 0.0f));
			patch.set(3, 3, new Vec3D(3.0f, 3.0f, 1.0f));
			mesh=patch.toMesh(16);
			
			String pathToOutputSTL = pathToOutDir + File.separator + "pippo.stl";
			
			((TriangleMesh) mesh).saveAsSTL(pathToOutputSTL);
			
//
//			System.out.println("importing the binary STL into a TriangleMesh...");
//
//			toxi.geom.mesh.TriangleMesh mesh =
//					(toxi.geom.mesh.TriangleMesh)new STLReader().loadBinary(
//							pathToSTL,STLReader.TRIANGLEMESH);
//			System.out.println("Report - " + mesh.toString());
//
//			System.out.println("Faces:");
//			mesh.getFaces().stream()
//			.forEach(f -> System.out.println(f));
//
//			System.out.println("Building the WETriangleMesh...");
//
//			WETriangleMesh wemesh = new WETriangleMesh("Agodemar Winged-Edge mesh");
//			wemesh.addMesh(mesh);
//			System.out.println("Report - " + wemesh.toString());
//
//			System.out.println("Edges:");
//			// a lambda function taking 2 arguments
//			BiConsumer<? super Line3D, ? super WingedEdge> actionEdge = (line, we) -> {
//				//System.out.println(line);
//				System.out.println(we);
//				System.out.println("  Faces: "); 
//				we.faces.forEach(f -> {
//					StringBuilder sb = new StringBuilder();
//					sb.append("  Face: ");
//					sb.append("  "+ f.a +", "+ f.b +", "+ f.c);
//					System.out.println(sb.toString());
//				});
//			};
//			wemesh.edges.forEach(actionEdge);


		} catch (CmdLineException e) {
			System.err.println("Error: " + e.getMessage());
			Test02.theCmdLineParser.printUsage(System.err);
			System.err.println();
			System.err.println("  Must launch this app with proper command line arguments.");
			return;
		}	    

	}

}
