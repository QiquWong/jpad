package standaloneutils.launchers.avl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.measure.quantity.Length;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.inferred.freebuilder.FreeBuilder;
import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import standaloneutils.MyArrayUtils;


@FreeBuilder
public interface AVLBody {
	/*
	 * A data class defining the parameters of a fuselage or other body modeled
	 * by side and planform projections arranged in a plus (+) shape (when
	 * viewed from the front).
	 */
	
	String getDescription();
	
	Integer getNBody();
	Double getBSpace();
	
	boolean isDuplicated();
	Double getYDupl();
	
	Double[] getOrigin();
	Double[] getScale();
	
	Optional<File> getBodyCoordFile();
	Optional<Fuselage> getFuselageObject();

	
	Optional<RealMatrix> getBodySectionInline();

	/** Builder of AVLBody instances. */
	class Builder extends AVLBody_Builder {
		
		public Builder() {
			// Set defaults in the builder constructor.
			setDescription("BODY");
			
			setNBody(15);
			setBSpace(1.0);
			
			setDuplicated(false);
			setYDupl(0.0);
			
			setOrigin(new Double[]{0.0, 0.0, 0.0});
			setScale(new Double[]{1.0, 1.0, 1.0});
			
		}
		@Override
		public Builder setBodyCoordFile(File file) {
			// custom action
			if (super.getFuselageObject().isPresent()) {
				Fuselage fuselage = super.getFuselageObject().get();
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
					writer.println("x z ! " + fuselage.getFuselageCreator().getId());
					// writer.println("The second line");


					// TODO: calculate the equivalent diameter of each fuselage section
					//       and center each circle on camberline z's
					
					//--------------------------------------------------
					// get data vectors from fuselage discretization
					//--------------------------------------------------
					// upper curve, sideview
					List<Amount<Length>> vXUpper = fuselage.getFuselageCreator().getOutlineXZUpperCurveAmountX();
					int nXUpper = vXUpper.size();
					List<Amount<Length>> vZUpper = fuselage.getFuselageCreator().getOutlineXZUpperCurveAmountZ();
		
					// lower curve, sideview
					List<Amount<Length>> vXLower = fuselage.getFuselageCreator().getOutlineXZLowerCurveAmountX();
					int nXLower = vXLower.size();
					List<Amount<Length>> vZLower = fuselage.getFuselageCreator().getOutlineXZLowerCurveAmountZ();

					vXLower.remove(0);
					vZLower.remove(0);
					
					Collections.reverse(vXUpper); 
					
					Double[] xs = (Double[])ArrayUtils.addAll(
						MyArrayUtils.convertListOfAmountToDoubleArray(vXUpper), 
						MyArrayUtils.convertListOfAmountToDoubleArray(vXLower)
					);
					
					Collections.reverse(vZUpper);
					
					
					
					Double[] zs = (Double[])ArrayUtils.addAll(
							MyArrayUtils.convertListOfAmountToDoubleArray(vZUpper), 
							MyArrayUtils.convertListOfAmountToDoubleArray(vZLower));
					
					
					
					for (int i = 0; i < xs.length; i++)
						writer.println(String.format(Locale.ROOT, "%1$11.4f %2$11.4f", xs[i], zs[i]));					    
					writer.close();
				} catch (Exception e) {
					System.err.format("Unable to write file %1$s\n", file.getAbsolutePath());
				}
			}
			// then, super function call
			return super.setBodyCoordFile(file);
		}
		
		
	}
	

}
