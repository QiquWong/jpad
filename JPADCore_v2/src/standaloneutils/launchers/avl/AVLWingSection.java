package standaloneutils.launchers.avl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.math3.linear.RealMatrix;
import org.inferred.freebuilder.FreeBuilder;

import aircraft.auxiliary.airfoil.Airfoil;

@FreeBuilder
public interface AVLWingSection {

	String getDescription();
	Double[] getOrigin();
	Double getChord();
	Double getTwist();
	Optional<File> getAirfoilCoordFile();
	Optional<Airfoil> getAirfoilObject();
	Optional<RealMatrix> getAirfoilSectionInline();

	
	List<AVLWingSectionControlSurface> getControlSurfaces();

	/** Builder of AVLWingSection instances. */
	class Builder extends AVLWingSection_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("SECTION");
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			setChord(0.0);
			setTwist(0.0);
			clearControlSurfaces();
			addAllControlSurfaces(new ArrayList<AVLWingSectionControlSurface>());
		}
		@Override
		public Builder setAirfoilCoordFile(File file) {
			// custom action
			if (super.getAirfoilObject().isPresent()) {
				Airfoil airfoil = super.getAirfoilObject().get();
				if (file.exists()) {
					try {
						Files.delete(file.toPath());
					} 
					catch (IOException e) {
						System.err.println(e + " (Unable to delete file)");
					}
				}
				try{
					PrintWriter writer = new PrintWriter(file.getAbsolutePath(), "UTF-8");
					writer.println("x z ! " + airfoil.getName());
					// writer.println("The second line");

					Double[] xs = airfoil.getAirfoilCreator().getXCoords();
					Double[] zs = airfoil.getAirfoilCreator().getZCoords();
					for (int i = 0; i < xs.length; i++)
						writer.println(String.format(Locale.ROOT, "%1$11.4f %2$11.4f", xs[i], zs[i]));					    
					writer.close();
				} catch (Exception e) {
					System.err.format("Unable to write file %1$s\n", file.getAbsolutePath());
				}
			}
			// then, super function call
			return super.setAirfoilCoordFile(file);
		}
		Builder appendControlSurface(AVLWingSectionControlSurface controlSurface) {
			addControlSurfaces(controlSurface);
			return this;
		}
	}
}
