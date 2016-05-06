package sandbox.vt.ExecutableHighLiftDevices;

import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Angle;
import org.jscience.physics.amount.Amount;

public class OutputTree {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	private List<Double> deltaCl0FlapList,
						 deltaCL0FlapList,
						 deltaClmaxFlapList,
						 deltaCLmaxFlapList,
						 deltaClmaxSlatList,
						 deltaCLmaxSlatList,
						 deltaCDList,
						 deltaCMC4List;
	private List<Amount<?>> cLalphaNewList;
	private double deltaCl0Flap,
				   deltaCL0Flap,
				   deltaClmaxFlap,
				   deltaCLmaxFlap,
				   deltaClmaxSlat,
				   deltaCLmaxSlat,
				   deltaCD,
				   deltaCMC4,
				   cLmaxFlapSlat,
				   cLStarFlapSlat;
	private Amount<Angle> alphaMaxFlapSlat,
						  alphaStarFlapSlat;
	private Amount<?> cLalphaNew;
	
	//------------------------------------------------------------------------------------------
	// BUILDER:
	
	public OutputTree() {
	
		deltaCl0FlapList = new ArrayList<Double>();
		deltaCL0FlapList = new ArrayList<Double>();
		deltaClmaxFlapList = new ArrayList<Double>();
		deltaCLmaxFlapList = new ArrayList<Double>();
		deltaClmaxSlatList = new ArrayList<Double>();
		deltaCLmaxSlatList = new ArrayList<Double>();
		cLalphaNewList = new ArrayList<Amount<?>>();
		deltaCDList = new ArrayList<Double>();
		deltaCMC4List = new ArrayList<Double>();		
	}

	//------------------------------------------------------------------------------------------
	// GETTERS AND SETTERS:
	
	public List<Double> getDeltaCl0FlapList() {
		return deltaCl0FlapList;
	}

	public void setDeltaCl0FlapList(List<Double> deltaCl0FlapList) {
		this.deltaCl0FlapList = deltaCl0FlapList;
	}

	public List<Double> getDeltaCL0FlapList() {
		return deltaCL0FlapList;
	}

	public void setDeltaCL0FlapList(List<Double> deltaCL0FlapList) {
		this.deltaCL0FlapList = deltaCL0FlapList;
	}

	public List<Double> getDeltaClmaxFlapList() {
		return deltaClmaxFlapList;
	}

	public void setDeltaClmaxFlapList(List<Double> deltaClmaxFlapList) {
		this.deltaClmaxFlapList = deltaClmaxFlapList;
	}

	public List<Double> getDeltaCLmaxFlapList() {
		return deltaCLmaxFlapList;
	}

	public void setDeltaCLmaxFlapList(List<Double> deltaCLmaxFlapList) {
		this.deltaCLmaxFlapList = deltaCLmaxFlapList;
	}

	public List<Double> getDeltaClmaxSlatList() {
		return deltaClmaxSlatList;
	}

	public void setDeltaClmaxSlatList(List<Double> deltaClmaxSlatList) {
		this.deltaClmaxSlatList = deltaClmaxSlatList;
	}

	public List<Double> getDeltaCLmaxSlatList() {
		return deltaCLmaxSlatList;
	}

	public void setDeltaCLmaxSlatList(List<Double> deltaCLmaxSlatList) {
		this.deltaCLmaxSlatList = deltaCLmaxSlatList;
	}

	public List<Amount<?>> getcLalphaNewList() {
		return cLalphaNewList;
	}

	public void setcLalphaNewList(List<Amount<?>> cLalphaNewList) {
		this.cLalphaNewList = cLalphaNewList;
	}

	public List<Double> getDeltaCDList() {
		return deltaCDList;
	}

	public void setDeltaCDList(List<Double> deltaCDList) {
		this.deltaCDList = deltaCDList;
	}

	public List<Double> getDeltaCMC4List() {
		return deltaCMC4List;
	}

	public void setDeltaCMC4List(List<Double> deltaCMC4List) {
		this.deltaCMC4List = deltaCMC4List;
	}

	public double getDeltaCl0Flap() {
		return deltaCl0Flap;
	}

	public void setDeltaCl0Flap(double deltaCl0Flap) {
		this.deltaCl0Flap = deltaCl0Flap;
	}

	public double getDeltaCL0Flap() {
		return deltaCL0Flap;
	}

	public void setDeltaCL0Flap(double deltaCL0Flap) {
		this.deltaCL0Flap = deltaCL0Flap;
	}

	public double getDeltaClmaxFlap() {
		return deltaClmaxFlap;
	}

	public void setDeltaClmaxFlap(double deltaClmaxFlap) {
		this.deltaClmaxFlap = deltaClmaxFlap;
	}

	public double getDeltaCLmaxFlap() {
		return deltaCLmaxFlap;
	}

	public void setDeltaCLmaxFlap(double deltaCLmaxFlap) {
		this.deltaCLmaxFlap = deltaCLmaxFlap;
	}

	public double getDeltaClmaxSlat() {
		return deltaClmaxSlat;
	}

	public void setDeltaClmaxSlat(double deltaClmaxSlat) {
		this.deltaClmaxSlat = deltaClmaxSlat;
	}

	public double getDeltaCLmaxSlat() {
		return deltaCLmaxSlat;
	}

	public void setDeltaCLmaxSlat(double deltaCLmaxSlat) {
		this.deltaCLmaxSlat = deltaCLmaxSlat;
	}

	public Amount<?> getcLalphaNew() {
		return cLalphaNew;
	}

	public void setcLalphaNew(Amount<?> cLalphaNew) {
		this.cLalphaNew = cLalphaNew;
	}

	public double getDeltaCD() {
		return deltaCD;
	}

	public void setDeltaCD(double deltaCD) {
		this.deltaCD = deltaCD;
	}

	public double getDeltaCMC4() {
		return deltaCMC4;
	}

	public void setDeltaCMC4(double deltaCMC4) {
		this.deltaCMC4 = deltaCMC4;
	}

	public double getcLmaxFlapSlat() {
		return cLmaxFlapSlat;
	}

	public void setcLmaxFlapSlat(double cLmaxFlapSlat) {
		this.cLmaxFlapSlat = cLmaxFlapSlat;
	}

	public double getcLStarFlapSlat() {
		return cLStarFlapSlat;
	}

	public void setcLStarFlapSlat(double cLStarFlapSlat) {
		this.cLStarFlapSlat = cLStarFlapSlat;
	}

	public Amount<Angle> getAlphaMaxFlapSlat() {
		return alphaMaxFlapSlat;
	}

	public void setAlphaMaxFlapSlat(Amount<Angle> alphaMaxFlapSlat) {
		this.alphaMaxFlapSlat = alphaMaxFlapSlat;
	}

	public Amount<Angle> getAlphaStarFlapSlat() {
		return alphaStarFlapSlat;
	}

	public void setAlphaStarFlapSlat(Amount<Angle> alphaStarFlapSlat) {
		this.alphaStarFlapSlat = alphaStarFlapSlat;
	}
}
