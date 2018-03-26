package aircraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.FuelTank;
import aircraft.components.ISystems;
import aircraft.components.LandingGears;
import aircraft.components.Systems;
import aircraft.components.cabinconfiguration.CabinConfiguration;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.liftingSurface.creator.IEquivalentWing;
import aircraft.components.liftingSurface.creator.ILiftingSurfaceCreator;
import aircraft.components.liftingSurface.creator.ILiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.PowerPlant;
import analyses.ACAnalysisManager;
import calculators.aerodynamics.DragCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
import configuration.enumerations.PrimaryElectricSystemsEnum;
import configuration.enumerations.RegulationsEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

/**
 * This class holds all the data related with the aircraft
 * An aircraft object can be passed to each component
 * in order to make it aware of all the available data
 *
 * @authors Vittorio Trifari,
 * 		    Agostino De Marco,
 *  		Vincenzo Cusati,
 *  	    Manuela Ruocco
 */

public class Aircraft {

	private IAircraft _theAircraftInterface;
	
	//-----------------------------------------------------------------------------------
	// DERIVED DATA
	private List<Object> _componentsList;
	private Amount<Area> _sWetTotal = Amount.valueOf(0.0, SI.SQUARE_METRE);
	private Double _kExcr = 0.0;
	private Amount<Length> _wingACToCGDistance = Amount.valueOf(0.0, SI.METER);
	
	//--------------------------------------------------------------------------
	// COMPONENTS FILE PATHS (GUI)
	private String _fuselageFilePath;
	private String _cabinConfigurationFilePath;
	private String _wingFilePath;
	private String _hTailFilePath;
	private String _vTailFilePath;
	private String _canardFilePath;
	private List<String> _engineFilePathList;
	private List<String> _nacelleFilePathList;
	private String _landingGearsFilePath;
	private String _systemsFilePath;
	
	//-----------------------------------------------------------------------------------
	// ANALYSES
	private ACAnalysisManager _theAnalysisManager;
	
	//-----------------------------------------------------------------------------------
	// BUILDER 
	public Aircraft (IAircraft theAircraftInterface) {
		
		this._theAircraftInterface = theAircraftInterface;
		
		//-------------------------------------------------------------		
		if(_theAircraftInterface.getPowerPlant() != null) {
			if(_theAircraftInterface.getWing() != null) {
				int indexOfEngineUpperWing = 0;
				for(int i=0; i<_theAircraftInterface.getPowerPlant().getEngineNumber(); i++) {
					if(_theAircraftInterface.getPowerPlant().getEngineList().get(i).getMountingPosition() == EngineMountingPositionEnum.WING)
						if(_theAircraftInterface.getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
								> _theAircraftInterface.getWing().getZApexConstructionAxes().doubleValue(SI.METER))
							indexOfEngineUpperWing += 1;
				}
				_theAircraftInterface.getWing().setNumberOfEngineOverTheWing(indexOfEngineUpperWing);
			}
		}
		//-------------------------------------------------------------
		if((_theAircraftInterface.getFuselage() != null) && (_theAircraftInterface.getWing() != null)) { 
			_theAircraftInterface.getWing().setExposedLiftingSurface(
					calculateExposedWing(
							_theAircraftInterface.getWing(), 
							_theAircraftInterface.getFuselage()
							)
					);
			_theAircraftInterface.getWing().getLiftingSurfaceCreator().setSurfaceWettedExposed(
					_theAircraftInterface.getWing().getExposedLiftingSurface().getLiftingSurfaceCreator().getSurfaceWetted()
					);
		}
		else if(_theAircraftInterface.getWing() != null)
			_theAircraftInterface.getWing().getLiftingSurfaceCreator().setSurfaceWettedExposed(
					_theAircraftInterface.getWing().getLiftingSurfaceCreator().getSurfaceWetted()
					);
		if(_theAircraftInterface.getHTail() !=  null) {
			_theAircraftInterface.getHTail().setExposedLiftingSurface(_theAircraftInterface.getHTail());
			_theAircraftInterface.getHTail().getLiftingSurfaceCreator().setTheLiftingSurfaceInterface(
					ILiftingSurfaceCreator.Builder
					.from(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().getTheLiftingSurfaceInterface())
					.setEquivalentWing(
							new IEquivalentWing.Builder()
							.addAllPanels(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().getPanels())
							.setRealWingDimensionlessXOffsetRootChordLE(0.0)
							.setRealWingDimensionlessXOffsetRootChordTE(0.0)
							.setRealWingDimensionlessKinkPosition(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().getEtaBreakPoints().get(0))
							.setRealWingTwistAtKink(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().getTwistsBreakPoints().get(0))
							.setEquivalentWingAirfoilKink(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().getAirfoilList().get(0))
							.build()
							)
					.build()
					);
		}
		if(_theAircraftInterface.getVTail() !=  null) {
			_theAircraftInterface.getVTail().setExposedLiftingSurface(_theAircraftInterface.getVTail());
			_theAircraftInterface.getVTail().getLiftingSurfaceCreator().setTheLiftingSurfaceInterface(
					ILiftingSurfaceCreator.Builder
					.from(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getTheLiftingSurfaceInterface())
					.setEquivalentWing(
							new IEquivalentWing.Builder()
							.addAllPanels(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getPanels())
							.setRealWingDimensionlessXOffsetRootChordLE(0.0)
							.setRealWingDimensionlessXOffsetRootChordTE(0.0)
							.setRealWingDimensionlessKinkPosition(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getEtaBreakPoints().get(0))
							.setRealWingTwistAtKink(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getTwistsBreakPoints().get(0))
							.setEquivalentWingAirfoilKink(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getAirfoilList().get(0))
							.build()
							)
					.build()
					);
		}
		if(_theAircraftInterface.getCanard() !=  null) {
			_theAircraftInterface.getCanard().setExposedLiftingSurface(_theAircraftInterface.getCanard());
			_theAircraftInterface.getCanard().getLiftingSurfaceCreator().setTheLiftingSurfaceInterface(
					ILiftingSurfaceCreator.Builder
					.from(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().getTheLiftingSurfaceInterface())
					.setEquivalentWing(
							new IEquivalentWing.Builder()
							.addAllPanels(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().getPanels())
							.setRealWingDimensionlessXOffsetRootChordLE(0.0)
							.setRealWingDimensionlessXOffsetRootChordTE(0.0)
							.setRealWingDimensionlessKinkPosition(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().getEtaBreakPoints().get(0))
							.setRealWingTwistAtKink(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().getTwistsBreakPoints().get(0))
							.setEquivalentWingAirfoilKink(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().getAirfoilList().get(0))
							.build()
							)
					.build()
					);
		}
		
		// setup the positionRelativeToAttachment variable
		if(_theAircraftInterface.getWing() != null)
			_theAircraftInterface.getWing().setPositionRelativeToAttachment(
					_theAircraftInterface.getWing().getZApexConstructionAxes().doubleValue(SI.METER)
					/(_theAircraftInterface.getFuselage().getSectionCylinderHeight().divide(2).getEstimatedValue())
					);
		
		if(_theAircraftInterface.getHTail() != null) {
			if(_theAircraftInterface.getVTail() != null)
				_theAircraftInterface.getHTail().setPositionRelativeToAttachment(
						(_theAircraftInterface.getHTail().getZApexConstructionAxes()
								.minus(_theAircraftInterface.getVTail()
										.getZApexConstructionAxes().to(SI.METER)
										)
								).divide(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getSpan().to(SI.METER))
						.getEstimatedValue()
						);
			else
				_theAircraftInterface.getHTail().setPositionRelativeToAttachment(0.0);
		}
		
		if(_theAircraftInterface.getVTail() != null)
			_theAircraftInterface.getVTail().setPositionRelativeToAttachment(0.0);
		
		if(_theAircraftInterface.getCanard() != null)
			_theAircraftInterface.getCanard().setPositionRelativeToAttachment(
					_theAircraftInterface.getCanard().getZApexConstructionAxes().doubleValue(SI.METER)
					/(_theAircraftInterface.getFuselage().getSectionCylinderHeight().divide(2).getEstimatedValue())
					);
		
		//----------------------------------------
		if(_theAircraftInterface.getWing() != null)
			calculateLiftingSurfaceACToWingACdistance(_theAircraftInterface.getWing());
		if(_theAircraftInterface.getHTail() != null)
			calculateLiftingSurfaceACToWingACdistance(_theAircraftInterface.getHTail());
		if(_theAircraftInterface.getVTail() != null)
			calculateLiftingSurfaceACToWingACdistance(_theAircraftInterface.getVTail());
		if(_theAircraftInterface.getCanard() != null)
			calculateLiftingSurfaceACToWingACdistance(_theAircraftInterface.getCanard());

		//----------------------------------------
		calculateSWetTotal();
		calculateKExcrescences();
		
	}
	
	private LiftingSurface calculateExposedWing(LiftingSurface theWing, Fuselage theFuselage) {
		
		Amount<Length> sectionWidthAtZ = Amount.valueOf(
				0.5 * theFuselage.getSectionWidthAtZ(
						theWing.getZApexConstructionAxes()
						.doubleValue(SI.METER)),
				SI.METER);

		Amount<Length> chordRootExposed = Amount.valueOf(
				theWing.getLiftingSurfaceCreator().getChordAtYActual(sectionWidthAtZ.doubleValue(SI.METER)),
				SI.METER
				);
		Airfoil exposedWingRootAirfoil = LSGeometryCalc.calculateAirfoilAtY(
				theWing,
				sectionWidthAtZ.doubleValue(SI.METER)
				);
		
		Amount<Length> exposedWingFirstPanelSpan = theWing.getLiftingSurfaceCreator()
				.getPanels().get(0)
				.getSpan()
				.minus(sectionWidthAtZ);	

		LiftingSurfacePanelCreator exposedWingFirstPanel = new LiftingSurfacePanelCreator(
				new ILiftingSurfacePanelCreator.Builder()
				.setId("Exposed wing first panel")
				.setLinkedTo(false)
				.setChordRoot(chordRootExposed)
				.setChordTip(theWing.getLiftingSurfaceCreator().getPanels().get(0).getChordTip())
				.setAirfoilRoot(exposedWingRootAirfoil)
				.setAirfoilTip(theWing.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilTip())
				.setTwistGeometricAtRoot(theWing.getLiftingSurfaceCreator().getPanels().get(0).getTwistGeometricRoot())
				.setTwistGeometricAtTip(theWing.getLiftingSurfaceCreator().getPanels().get(0).getTwistGeometricAtTip())
				.setSpan(exposedWingFirstPanelSpan)
				.setSweepLeadingEdge(theWing.getLiftingSurfaceCreator().getPanels().get(0).getSweepLeadingEdge())
				.setDihedral(theWing.getLiftingSurfaceCreator().getPanels().get(0).getDihedral())
				.buildPartial()
				);

		List<LiftingSurfacePanelCreator> exposedWingPanels = new ArrayList<LiftingSurfacePanelCreator>();

		exposedWingPanels.add(exposedWingFirstPanel);

		for(int i=1; i<theWing.getLiftingSurfaceCreator().getPanels().size(); i++)
			exposedWingPanels.add(theWing.getLiftingSurfaceCreator().getPanels().get(i));

		LiftingSurface theExposedWing = new LiftingSurface(
				new LiftingSurfaceCreator(
						new ILiftingSurfaceCreator.Builder()
						.setId("Exposed Wing")
						.setType(ComponentEnum.WING)
						.setMirrored(true)
						.setEquivalentWingFlag(false)
						.addAllPanels(exposedWingPanels)
						.setMainSparDimensionlessPosition(theWing.getLiftingSurfaceCreator().getMainSparDimensionlessPosition())
						.setSecondarySparDimensionlessPosition(theWing.getLiftingSurfaceCreator().getSecondarySparDimensionlessPosition())
						.setRoughness(theWing.getLiftingSurfaceCreator().getRoughness())
						.setWingletHeight(theWing.getLiftingSurfaceCreator().getWingletHeight())
						.buildPartial()
						)
				);
				
		theExposedWing.setAeroDatabaseReader(theWing.getAeroDatabaseReader());
		theExposedWing.setHighLiftDatabaseReader(theWing.getHighLiftDatabaseReader());
		theExposedWing.setVeDSCDatabaseReader(theWing.getVeDSCDatabaseReader());
		
		theExposedWing.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.WING, true);
		theExposedWing.getLiftingSurfaceCreator().populateAirfoilList(false);
		theExposedWing.setXApexConstructionAxes(theWing.getXApexConstructionAxes());
		theExposedWing.setYApexConstructionAxes(Amount.valueOf(
				0.5 * theFuselage.getSectionWidthAtZ(
						theWing.getZApexConstructionAxes().doubleValue(SI.METER)),
				SI.METER)
				);
		theExposedWing.setZApexConstructionAxes(theWing.getZApexConstructionAxes());
		theExposedWing.setRiggingAngle(theWing.getRiggingAngle());

		return theExposedWing;
	}

	public void calculateSWetTotal() {
		
		if(this._theAircraftInterface.getFuselage() != null)
			this._sWetTotal = this._sWetTotal.plus(_theAircraftInterface.getFuselage().getSWetTotal());
		
		if(this._theAircraftInterface.getWing() != null)
			this._sWetTotal = this._sWetTotal.plus(_theAircraftInterface.getWing().getExposedLiftingSurface().getLiftingSurfaceCreator().getSurfaceWetted());
			
		if(_theAircraftInterface.getHTail() != null)
			this._sWetTotal = this._sWetTotal.plus(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().getSurfaceWetted());
		
		if(_theAircraftInterface.getVTail() != null)
			this._sWetTotal = this._sWetTotal.plus(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().getSurfaceWetted());
		
		if(_theAircraftInterface.getCanard() != null)
			this._sWetTotal = this._sWetTotal.plus(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().getSurfaceWetted());
			
		if(_theAircraftInterface.getNacelles() != null)
			this._sWetTotal = this._sWetTotal.plus(_theAircraftInterface.getNacelles().getSurfaceWetted());
		
	}
	
	public void calculateKExcrescences() {
		_kExcr = DragCalc.calculateKExcrescences(getSWetTotal().doubleValue(SI.SQUARE_METRE));
		
		if(_theAircraftInterface.getFuselage() != null)
			_theAircraftInterface.getFuselage().setKExcr(_kExcr);
		
		if(_theAircraftInterface.getWing() != null)
			_theAircraftInterface.getWing().getLiftingSurfaceCreator().setKExcr(_kExcr);
		
		if(_theAircraftInterface.getHTail() != null)
			_theAircraftInterface.getHTail().getLiftingSurfaceCreator().setKExcr(_kExcr);
		
		if(_theAircraftInterface.getVTail() != null)
			_theAircraftInterface.getVTail().getLiftingSurfaceCreator().setKExcr(_kExcr);
		
		if(_theAircraftInterface.getCanard() != null)
			_theAircraftInterface.getCanard().getLiftingSurfaceCreator().setKExcr(_kExcr);
		
		if(_theAircraftInterface.getNacelles() != null)
			_theAircraftInterface.getNacelles().setKExcr(_kExcr);
	}
	
	public void calculateArms(LiftingSurface theLiftingSurface, Amount<Length> xcgMTOM){
		
		if(theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.WING) {
			calculateAircraftCGToWingACdistance(xcgMTOM.to(SI.METER));
			theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
					getWingACToCGDistance().to(SI.METER)
					);
		}
		else if( // case CG behind AC wing
				xcgMTOM.doubleValue(SI.METER) > 
				(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theAircraftInterface.getWing().getXApexConstructionAxes().to(SI.METER)).getEstimatedValue() + 
						_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)*0.25)
				) {
			
			if((theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.HORIZONTAL_TAIL)
					|| (theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.VERTICAL_TAIL)) {
			
				calculateAircraftCGToWingACdistance(xcgMTOM.to(SI.METER));
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(SI.METER)
							.minus(getWingACToCGDistance().to(SI.METER))
						);
			}
			else if (theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.CANARD) {
				calculateAircraftCGToWingACdistance(xcgMTOM.to(SI.METER));
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(SI.METER)
							.plus(getWingACToCGDistance().to(SI.METER))
						);
			}
		}
		else if( // case AC wing behind CG
				xcgMTOM.doubleValue(SI.METER) <= 
				(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theAircraftInterface.getWing().getXApexConstructionAxes().to(SI.METER)).getEstimatedValue() + 
						_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)*0.25)
				) {
			if((theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.HORIZONTAL_TAIL)
					|| (theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.VERTICAL_TAIL)) {
			
				calculateAircraftCGToWingACdistance(xcgMTOM.to(SI.METER));
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(SI.METER)
							.plus(getWingACToCGDistance().to(SI.METER))
						);
			}
			else if (theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.CANARD) {
				calculateAircraftCGToWingACdistance(xcgMTOM.to(SI.METER));
				calculateLiftingSurfaceACToWingACdistance(theLiftingSurface);
				calculateVolumetricRatio(theLiftingSurface);
				theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceArm(
						theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(SI.METER)
							.minus(getWingACToCGDistance().to(SI.METER))
						);
			}
		}
	}

	private void calculateAircraftCGToWingACdistance(Amount<Length> xCGMTOM){
		_wingACToCGDistance = Amount.valueOf(
				Math.abs(
						xCGMTOM.doubleValue(SI.METER) -
						(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
								.plus(_theAircraftInterface.getWing().getXApexConstructionAxes().to(SI.METER)).getEstimatedValue() + 
								_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER)*0.25)
						), 
				SI.METER);
	}

	private void calculateLiftingSurfaceACToWingACdistance(LiftingSurface theLiftingSurface) {
		theLiftingSurface.getLiftingSurfaceCreator().setLiftingSurfaceACTOWingACDistance(
				Amount.valueOf(
						Math.abs(
								(theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
										.plus(theLiftingSurface.getXApexConstructionAxes().to(SI.METER))
										.plus(theLiftingSurface.getLiftingSurfaceCreator().getMeanAerodynamicChord().to(SI.METER).times(0.25))
										.getEstimatedValue()
										) 
								- (_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
												.plus(_theAircraftInterface.getWing().getXApexConstructionAxes().to(SI.METER)) 
												.plus(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().to(SI.METER).times(0.25))
												.getEstimatedValue()
												)
								),
						SI.METER)
				);
	}

	private void calculateVolumetricRatio(LiftingSurface theLiftingSurface) {
		
		if ((theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.HORIZONTAL_TAIL)
				|| (theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.CANARD)) {
			theLiftingSurface.getLiftingSurfaceCreator().setVolumetricRatio(
					(theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().to(SI.SQUARE_METRE)
							.divide(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getSurfacePlanform().to(SI.SQUARE_METRE)))
					.times(theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(SI.METER)
							.divide(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().to(SI.METER)))
			.getEstimatedValue()
			);
		} 
		else if(theLiftingSurface.getLiftingSurfaceCreator().getType() == ComponentEnum.VERTICAL_TAIL) {
			theLiftingSurface.getLiftingSurfaceCreator().setVolumetricRatio(
					(theLiftingSurface.getLiftingSurfaceCreator().getSurfacePlanform().to(SI.SQUARE_METRE)
							.divide(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getSurfacePlanform().to(SI.SQUARE_METRE)))
					.times(theLiftingSurface.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(SI.METER)
							.divide(_theAircraftInterface.getWing().getLiftingSurfaceCreator().getSpan().to(SI.METER)))
					.getEstimatedValue()
					);
		}
	}
	
	public static Aircraft importFromXML (String pathToXML,
									      String liftingSurfacesDir,
									      String fuselagesDir,
									      String engineDir,
									      String nacelleDir,
									      String landingGearsDir,
									      String cabinConfigurationDir,
									      String airfoilsDir,
									      AerodynamicDatabaseReader aeroDatabaseReader,
									      HighLiftDatabaseReader highLiftDatabaseReader,
									      FusDesDatabaseReader fusDesDatabaseReader,
									      VeDSCDatabaseReader veDSCDatabaseReader) {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading aircraft data from file ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		AircraftTypeEnum type;
		String typeProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		if(typeProperty.equalsIgnoreCase("TURBOPROP"))
			type = AircraftTypeEnum.TURBOPROP;
		else if(typeProperty.equalsIgnoreCase("BUSINESS_JET"))
			type = AircraftTypeEnum.BUSINESS_JET;
		else if(typeProperty.equalsIgnoreCase("JET"))
			type = AircraftTypeEnum.JET;
		else if(typeProperty.equalsIgnoreCase("GENERAL_AVIATION"))
			type = AircraftTypeEnum.GENERAL_AVIATION;
		else if(typeProperty.equalsIgnoreCase("FIGHTER"))
			type = AircraftTypeEnum.FIGHTER;
		else if(typeProperty.equalsIgnoreCase("ACROBATIC"))
			type = AircraftTypeEnum.ACROBATIC;
		else if(typeProperty.equalsIgnoreCase("COMMUTER"))
			type = AircraftTypeEnum.COMMUTER;
		else {
			System.err.println("INVALID AIRCRAFT TYPE !!!");
			return null;
		}
		
		RegulationsEnum regulations;
		String regulationsProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@regulations");
		if(regulationsProperty.equalsIgnoreCase("FAR_23"))
			regulations = RegulationsEnum.FAR_23;
		else if(regulationsProperty.equalsIgnoreCase("FAR_25"))
			regulations = RegulationsEnum.FAR_25;
		else {
			System.err.println("INVALID AIRCRAFT REGULATIONS TYPE !!!");
			return null;
		}
		
		//---------------------------------------------------------------------------------
		// FUSELAGE
		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		
		Fuselage theFuselage = null;
		Amount<Length> xApexFuselage = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexFuselage = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexFuselage = Amount.valueOf(0.0, SI.METER);
		
		if(fuselageFileName != null) {
			String fuselagePath = fuselagesDir + File.separator + fuselageFileName;
			theFuselage = Fuselage.importFromXML(fuselagePath);
			
			theFuselage.calculateGeometry();
			theFuselage.setFusDesDatabaseReader(fusDesDatabaseReader);
			
			xApexFuselage = reader.getXMLAmountLengthByPath("//fuselage/position/x");
			yApexFuselage = reader.getXMLAmountLengthByPath("//fuselage/position/y");
			zApexFuselage = reader.getXMLAmountLengthByPath("//fuselage/position/z");
			theFuselage.setXApexConstructionAxes(xApexFuselage);
			theFuselage.setYApexConstructionAxes(yApexFuselage);
			theFuselage.setZApexConstructionAxes(zApexFuselage);
		}
		
		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION
		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");
		
		CabinConfiguration theCabinConfiguration = null;
		if(cabinConfigrationFileName != null) {
			String cabinConfigurationPath = cabinConfigurationDir + File.separator + cabinConfigrationFileName;
			theCabinConfiguration = CabinConfiguration.importFromXML(cabinConfigurationPath);
			theCabinConfiguration.setCabinConfigurationPath(new File(cabinConfigurationPath));
		}
		
		//---------------------------------------------------------------------------------
		// WING
		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		
		LiftingSurface theWing = null;
		Amount<Length> xApexWing = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexWing = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexWing = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleWing = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(wingFileName != null) {
			String wingPath = liftingSurfacesDir + File.separator + wingFileName;
			LiftingSurfaceCreator wingCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.WING, wingPath, airfoilsDir);
			theWing = new LiftingSurface(wingCreator);

			theWing.setAeroDatabaseReader(aeroDatabaseReader);
			theWing.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theWing.setVeDSCDatabaseReader(veDSCDatabaseReader);
			theWing.getLiftingSurfaceCreator().populateAirfoilList(false);
			
			xApexWing = reader.getXMLAmountLengthByPath("//wing/position/x");
			yApexWing = reader.getXMLAmountLengthByPath("//wing/position/y");
			zApexWing = reader.getXMLAmountLengthByPath("//wing/position/z");
			riggingAngleWing = reader.getXMLAmountAngleByPath("//wing/rigging_angle");
			theWing.setXApexConstructionAxes(xApexWing);
			theWing.setYApexConstructionAxes(yApexWing);
			theWing.setZApexConstructionAxes(zApexWing);
			theWing.setRiggingAngle(riggingAngleWing);
		}
		
		//---------------------------------------------------------------------------------
		// FUEL TANK
		FuelTank theFuelTank = null;
		Amount<Length> xApexFuelTank = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexFuelTank = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexFuelTank = Amount.valueOf(0.0, SI.METER);
		
		if(theWing != null) {
			
			theFuelTank = new FuelTank("Fuel Tank", theWing);
			
			xApexFuelTank = xApexWing
					.plus(theWing.getLiftingSurfaceCreator().getPanels().get(0).getChordRoot()
							.times(theWing.getLiftingSurfaceCreator()
									.getMainSparDimensionlessPosition()
									)
							);
			yApexFuelTank = yApexWing;
			zApexFuelTank = zApexWing;			
			theFuelTank.setXApexConstructionAxes(xApexFuelTank);
			theFuelTank.setYApexConstructionAxes(yApexFuelTank);
			theFuelTank.setZApexConstructionAxes(zApexFuelTank);
		}
		
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL
		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		
		LiftingSurface theHorizontalTail = null;
		Amount<Length> xApexHTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexHTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexHTail = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleHTail = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(hTailFileName != null) {
			String hTailPath = liftingSurfacesDir + File.separator + hTailFileName;
			LiftingSurfaceCreator hTailCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.HORIZONTAL_TAIL, hTailPath, airfoilsDir);
			theHorizontalTail = new LiftingSurface(hTailCreator);
			
			theHorizontalTail.setAeroDatabaseReader(aeroDatabaseReader);
			theHorizontalTail.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theHorizontalTail.setVeDSCDatabaseReader(veDSCDatabaseReader);
			theHorizontalTail.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.HORIZONTAL_TAIL, true);
			theHorizontalTail.getLiftingSurfaceCreator().populateAirfoilList(false);
			
			xApexHTail = reader.getXMLAmountLengthByPath("//horizontal_tail/position/x");
			yApexHTail = reader.getXMLAmountLengthByPath("//horizontal_tail/position/y");
			zApexHTail = reader.getXMLAmountLengthByPath("//horizontal_tail/position/z");
			riggingAngleHTail = reader.getXMLAmountAngleByPath("//horizontal_tail/rigging_angle");
			theHorizontalTail.setXApexConstructionAxes(xApexHTail);
			theHorizontalTail.setYApexConstructionAxes(yApexHTail);
			theHorizontalTail.setZApexConstructionAxes(zApexHTail);
			theHorizontalTail.setRiggingAngle(riggingAngleHTail);
		}
		
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL
		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		
		LiftingSurface theVerticalTail = null;
		Amount<Length> xApexVTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexVTail = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexVTail = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleVTail = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(vTailFileName != null) {
			String vTailPath = liftingSurfacesDir + File.separator + vTailFileName;
			LiftingSurfaceCreator vTailCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.VERTICAL_TAIL, vTailPath, airfoilsDir);
			theVerticalTail = new LiftingSurface(vTailCreator);

			theVerticalTail.setAeroDatabaseReader(aeroDatabaseReader);
			theVerticalTail.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theVerticalTail.setVeDSCDatabaseReader(veDSCDatabaseReader);
			theVerticalTail.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.VERTICAL_TAIL, false);
			theVerticalTail.getLiftingSurfaceCreator().populateAirfoilList(false);
			
			xApexVTail = reader.getXMLAmountLengthByPath("//vertical_tail/position/x");
			yApexVTail = reader.getXMLAmountLengthByPath("//vertical_tail/position/y");
			zApexVTail = reader.getXMLAmountLengthByPath("//vertical_tail/position/z");
			riggingAngleVTail = reader.getXMLAmountAngleByPath("//vertical_tail/rigging_angle");
			theVerticalTail.setXApexConstructionAxes(xApexVTail);
			theVerticalTail.setYApexConstructionAxes(yApexVTail);
			theVerticalTail.setZApexConstructionAxes(zApexVTail);
			theVerticalTail.setRiggingAngle(riggingAngleVTail);
		}
		
		//---------------------------------------------------------------------------------
		// CANARD
		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		
		LiftingSurface theCanard = null;
		Amount<Length> xApexCanard = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexCanard = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexCanard = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> riggingAngleCanard = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		
		if(canardFileName != null) {
			String canardPath = liftingSurfacesDir + File.separator + canardFileName;
			LiftingSurfaceCreator canardCreator = LiftingSurfaceCreator.importFromXML(ComponentEnum.CANARD, canardPath, airfoilsDir);
			theCanard = new LiftingSurface(canardCreator);
			
			theCanard.setAeroDatabaseReader(aeroDatabaseReader);
			theCanard.setHighLiftDatabaseReader(highLiftDatabaseReader);
			theCanard.setVeDSCDatabaseReader(veDSCDatabaseReader);
			theCanard.getLiftingSurfaceCreator().calculateGeometry(ComponentEnum.CANARD, true);
			theCanard.getLiftingSurfaceCreator().populateAirfoilList(false);
			
			xApexCanard = reader.getXMLAmountLengthByPath("//canard/position/x");
			yApexCanard = reader.getXMLAmountLengthByPath("//canard/position/y");
			zApexCanard = reader.getXMLAmountLengthByPath("//canard/position/z");
			riggingAngleCanard = reader.getXMLAmountAngleByPath("//canard/rigging_angle");
			theCanard.setXApexConstructionAxes(xApexCanard);
			theCanard.setYApexConstructionAxes(yApexCanard);
			theCanard.setZApexConstructionAxes(zApexCanard);
			theCanard.setRiggingAngle(riggingAngleCanard);
		}
		
		//---------------------------------------------------------------------------------
		// POWER PLANT
		List<Engine> engineList = new ArrayList<Engine>();
		PowerPlant thePowerPlant = null;
		
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");

		if(nodelistEngines.getLength() > 0) {		
			List<String> xApexPowerPlantListProperties = reader.getXMLPropertiesByPath("//engine/position/x");
			List<String> yApexPowerPlantListProperties = reader.getXMLPropertiesByPath("//engine/position/y");
			List<String> zApexPowerPlantListProperties = reader.getXMLPropertiesByPath("//engine/position/z");		
			List<String> tiltingAngleListProperties = reader.getXMLPropertiesByPath("//engine/tilting_angle");
			List<String> mountingPointListProperties = reader.getXMLPropertiesByPath("//engine/mounting_point");

			List<Amount<Length>> xApexPowerPlantList = new ArrayList<>();
			List<Amount<Length>> yApexPowerPlantList = new ArrayList<>();
			List<Amount<Length>> zApexPowerPlantList = new ArrayList<>();
			List<Amount<Angle>> tiltingAngleList = new ArrayList<>();
			List<EngineMountingPositionEnum> mountingPointList = new ArrayList<>();

			for(int i=0; i<mountingPointListProperties.size(); i++) {
				if(mountingPointListProperties.get(i).equalsIgnoreCase("AFT_FUSELAGE"))
					mountingPointList.add(EngineMountingPositionEnum.AFT_FUSELAGE);
				else if(mountingPointListProperties.get(i).equalsIgnoreCase("BURIED"))
					mountingPointList.add(EngineMountingPositionEnum.BURIED);
				else if(mountingPointListProperties.get(i).equalsIgnoreCase("REAR_FUSELAGE"))
					mountingPointList.add(EngineMountingPositionEnum.REAR_FUSELAGE);
				else if(mountingPointListProperties.get(i).equalsIgnoreCase("WING"))
					mountingPointList.add(EngineMountingPositionEnum.WING);
				else if(mountingPointListProperties.get(i).equalsIgnoreCase("HTAIL"))
					mountingPointList.add(EngineMountingPositionEnum.HTAIL);
				else {
					System.err.println("INVALID ENGINE MOUNTING POSITION !!! ");
					return null;
				}

				xApexPowerPlantList.add(
						Amount.valueOf(
								Double.valueOf(
										xApexPowerPlantListProperties.get(i)
										),
								SI.METER)
						);
				yApexPowerPlantList.add(
						Amount.valueOf(
								Double.valueOf(
										yApexPowerPlantListProperties.get(i)
										),
								SI.METER)
						);
				zApexPowerPlantList.add(
						Amount.valueOf(
								Double.valueOf(
										zApexPowerPlantListProperties.get(i)
										),
								SI.METER)
						);
				tiltingAngleList.add(
						Amount.valueOf(
								Double.valueOf(
										tiltingAngleListProperties.get(i)
										),
								NonSI.DEGREE_ANGLE)
						);
			}

			System.out.println("Engines found: " + nodelistEngines.getLength());
			for (int i = 0; i < nodelistEngines.getLength(); i++) {
				Node nodeEngine  = nodelistEngines.item(i); // .getNodeValue();
				Element elementEngine = (Element) nodeEngine;
				String engineFileName = elementEngine.getAttribute("file");
				System.out.println("[" + i + "]\nEngine file: " + elementEngine.getAttribute("file"));

				String enginePath = engineDir + File.separator + engineFileName;
				engineList.add(Engine.importFromXML(enginePath));

				engineList.get(i).setXApexConstructionAxes(xApexPowerPlantList.get(i));
				engineList.get(i).setYApexConstructionAxes(yApexPowerPlantList.get(i));
				engineList.get(i).setZApexConstructionAxes(zApexPowerPlantList.get(i));
				engineList.get(i).setTiltingAngle(tiltingAngleList.get(i));
				engineList.get(i).setMountingPosition(mountingPointList.get(i));

			}

			thePowerPlant = new PowerPlant(engineList);

		}
		//---------------------------------------------------------------------------------
		// NACELLES
		List<NacelleCreator> nacelleList = new ArrayList<NacelleCreator>();
		Nacelles theNacelles = null;
		
		NodeList nodelistNacelle = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");

		if(nodelistNacelle.getLength() > 0) {
			List<String> xApexNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/position/x");
			List<String> yApexNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/position/y");
			List<String> zApexNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/position/z");		
			List<String> mountingPointNacellesListProperties = reader.getXMLPropertiesByPath("//nacelle/mounting_point");

			List<Amount<Length>> xApexNacellesList = new ArrayList<>();
			List<Amount<Length>> yApexNacellesList = new ArrayList<>();
			List<Amount<Length>> zApexNacellesList = new ArrayList<>();
			List<NacelleMountingPositionEnum> mountingPointNacellesList = new ArrayList<>();

			for(int i=0; i<mountingPointNacellesListProperties.size(); i++) {
				if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("WING"))
					mountingPointNacellesList.add(NacelleMountingPositionEnum.WING);
				else if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("FUSELAGE"))
					mountingPointNacellesList.add(NacelleMountingPositionEnum.FUSELAGE);
				else if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("UNDERCARRIAGE_HOUSING"))
					mountingPointNacellesList.add(NacelleMountingPositionEnum.UNDERCARRIAGE_HOUSING);
				else if(mountingPointNacellesListProperties.get(i).equalsIgnoreCase("HTAIL"))
					mountingPointNacellesList.add(NacelleMountingPositionEnum.HTAIL);
				else {
					System.err.println("INVALID NACELLE MOUNTING POSITION !!! ");
					return null;
				}

				xApexNacellesList.add(
						Amount.valueOf(
								Double.valueOf(
										xApexNacellesListProperties.get(i)
										),
								SI.METER)
						);
				yApexNacellesList.add(
						Amount.valueOf(
								Double.valueOf(
										yApexNacellesListProperties.get(i)
										),
								SI.METER)
						);
				zApexNacellesList.add(
						Amount.valueOf(
								Double.valueOf(
										zApexNacellesListProperties.get(i)
										),
								SI.METER)
						);
			}

			System.out.println("Nacelles found: " + nodelistNacelle.getLength());
			for (int i = 0; i < nodelistNacelle.getLength(); i++) {
				Node nodeNacelle  = nodelistNacelle.item(i); // .getNodeValue();
				Element elementNacelle = (Element) nodeNacelle;
				String nacelleFileName = elementNacelle.getAttribute("file");
				System.out.println("[" + i + "]\nNacelle file: " + elementNacelle.getAttribute("file"));

				String nacellePath = nacelleDir + File.separator + nacelleFileName;
				nacelleList.add(NacelleCreator.importFromXML(nacellePath, engineDir));

				nacelleList.get(i).setXApexConstructionAxes(xApexNacellesList.get(i));
				nacelleList.get(i).setYApexConstructionAxes(yApexNacellesList.get(i));
				nacelleList.get(i).setZApexConstructionAxes(zApexNacellesList.get(i));
				nacelleList.get(i).setMountingPosition(mountingPointNacellesList.get(i));

			}

			theNacelles = new Nacelles(nacelleList);
		}
		//---------------------------------------------------------------------------------
		// LANDING GEARS
		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		
		LandingGearsMountingPositionEnum mountingPosition = null;
		LandingGears theLandingGears = null;
		Amount<Length> xApexNoseLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexNoseLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexNoseLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> xApexMainLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> yApexMainLandingGears = Amount.valueOf(0.0, SI.METER);
		Amount<Length> zApexMainLandingGears = Amount.valueOf(0.0, SI.METER);
		
		if(landingGearsFileName != null) {
			String landingGearsPath = landingGearsDir + File.separator + landingGearsFileName;
			theLandingGears = LandingGears.importFromXML(landingGearsPath);
			
			xApexNoseLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/nose_gear/x");
			yApexNoseLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/nose_gear/y");
			zApexNoseLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/nose_gear/z");
			theLandingGears.setXApexConstructionAxesMainGear(xApexMainLandingGears);
			theLandingGears.setYApexConstructionAxesMainGear(yApexMainLandingGears);
			theLandingGears.setZApexConstructionAxesMainGear(zApexMainLandingGears);
			
			xApexMainLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/main_gear/x");
			yApexMainLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/main_gear/y");
			zApexMainLandingGears = reader.getXMLAmountLengthByPath("//landing_gears/position/main_gear/z");
			theLandingGears.setXApexConstructionAxesNoseGear(xApexNoseLandingGears);
			theLandingGears.setYApexConstructionAxesNoseGear(yApexNoseLandingGears);
			theLandingGears.setZApexConstructionAxesNoseGear(zApexNoseLandingGears);
			
			String mountingPositionProperty = reader.getXMLPropertyByPath("//landing_gears/mounting_point");
			if(mountingPositionProperty.equalsIgnoreCase("FUSELAGE"))
				mountingPosition = LandingGearsMountingPositionEnum.FUSELAGE;
			else if(mountingPositionProperty.equalsIgnoreCase("WING"))
				mountingPosition = LandingGearsMountingPositionEnum.WING;
			else if(mountingPositionProperty.equalsIgnoreCase("NACELLE"))
				mountingPosition = LandingGearsMountingPositionEnum.NACELLE;
			else {
				System.err.println("INVALID LANDING GEARS MOUNTING POSITION !!! ");
				return null;
			}
				
			theLandingGears.setMountingPosition(mountingPosition);
			
		}
		
		//---------------------------------------------------------------------------------
		// SYSTEMS
		String systemsPrimaryElectricalSystemsTypeString =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//systems/@primary_electrical_systems_type");
		
		// default choice
		PrimaryElectricSystemsEnum electricalSystemsType = PrimaryElectricSystemsEnum.AC;
		
		if(systemsPrimaryElectricalSystemsTypeString != null) {
			
			if(systemsPrimaryElectricalSystemsTypeString.equalsIgnoreCase("AC"))
				electricalSystemsType = PrimaryElectricSystemsEnum.AC;
			else if(systemsPrimaryElectricalSystemsTypeString.equalsIgnoreCase("DC"))
				electricalSystemsType = PrimaryElectricSystemsEnum.DC;
				
		}
		
		ISystems theSystemsInterface = new ISystems.Builder()
				.setPrimaryElectricSystemsType(electricalSystemsType)
				.buildPartial();
		Systems theSystems = new Systems(theSystemsInterface);
		
		//---------------------------------------------------------------------------------
		// COMPONENT LIST:
		List<Object> componentList = new ArrayList<>();
		if(theFuselage != null)
			componentList.add(theFuselage);
		if(theCabinConfiguration != null)
			componentList.add(theCabinConfiguration);
		if(theWing != null)
			componentList.add(theWing);
		if(theFuelTank != null)
			componentList.add(theFuelTank);
		if(theHorizontalTail != null)
			componentList.add(theHorizontalTail);
		if(theVerticalTail != null)
			componentList.add(theVerticalTail);
		if(theCanard != null)
			componentList.add(theCanard);
		if(thePowerPlant != null)
			componentList.add(thePowerPlant);
		if(theNacelles != null)
			componentList.add(theNacelles);
		if(theLandingGears != null)
			componentList.add(theLandingGears);
		if(theSystems != null)
			componentList.add(theSystems);


		//---------------------------------------------------------------------------------
		Aircraft theAircraft = new Aircraft(
				new IAircraft.Builder()
				.setId(id)
				.setTypeVehicle(type)
				.setRegulations(regulations)
				.setPrimaryElectricSystemsType(electricalSystemsType)
				.setCabinConfiguration(theCabinConfiguration)
				.setFuselage(theFuselage)
				.setXApexFuselage(xApexFuselage)
				.setYApexFuselage(yApexFuselage)
				.setZApexFuselage(zApexFuselage)
				.setWing(theWing)
				.setXApexWing(xApexWing)
				.setYApexWing(yApexWing)
				.setZApexWing(zApexWing)
				.setRiggingAngleWing(riggingAngleWing)
				.setHTail(theHorizontalTail)
				.setXApexHTail(xApexHTail)
				.setYApexHTail(yApexHTail)
				.setZApexHTail(zApexHTail)
				.setRiggingAngleHTail(riggingAngleHTail)
				.setVTail(theVerticalTail)
				.setXApexVTail(xApexVTail)
				.setYApexVTail(yApexVTail)
				.setZApexVTail(zApexVTail)
				.setRiggingAngleVTail(riggingAngleVTail)
				.setCanard(theCanard)
				.setXApexCanard(xApexCanard)
				.setYApexCanard(yApexCanard)
				.setZApexCanard(zApexCanard)
				.setRiggingAngleCanard(riggingAngleCanard)
				.setFuelTank(theFuelTank)
				.setXApexFuelTank(xApexFuelTank)
				.setYApexFuelTank(yApexFuelTank)
				.setZApexFuelTank(zApexFuelTank)
				.setPowerPlant(thePowerPlant)
				.setNacelles(theNacelles)
				.setLandingGears(theLandingGears)
				.setXApexNoseGear(xApexNoseLandingGears)
				.setYApexNoseGear(yApexNoseLandingGears)
				.setZApexNoseGear(zApexNoseLandingGears)
				.setXApexMainGear(xApexMainLandingGears)
				.setYApexMainGear(yApexMainLandingGears)
				.setZApexMainGear(zApexMainLandingGears)
				.setLandingGearsMountingPositionEnum(mountingPosition)
				.setSystems(theSystems)
				.build()
				);
		
		return theAircraft;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder();

		sb.append("\t-------------------------------------\n")
		  .append("\tThe Aircraft\n")
		  .append("\t-------------------------------------\n")
		  .append("\tId: '" + _theAircraftInterface.getId() + "'\n")
		  .append("\tType: '" + _theAircraftInterface.getTypeVehicle() + "'\n");
		
		if(_theAircraftInterface.getFuselage() != null)
			sb.append(_theAircraftInterface.getFuselage().toString());
		
		if(_theAircraftInterface.getWing() != null)
			sb.append(_theAircraftInterface.getWing().getLiftingSurfaceCreator().toString());
		
		if(_theAircraftInterface.getWing().getExposedLiftingSurface() != null)
			sb.append(_theAircraftInterface.getWing().getExposedLiftingSurface().toString());
		
		if(_theAircraftInterface.getHTail() != null)
			sb.append(_theAircraftInterface.getHTail().getLiftingSurfaceCreator().toString());
		
		if(_theAircraftInterface.getVTail() != null)
			sb.append(_theAircraftInterface.getVTail().getLiftingSurfaceCreator().toString());
		
		if(_theAircraftInterface.getCanard() != null)
			sb.append(_theAircraftInterface.getCanard().getLiftingSurfaceCreator().toString());
		
		if(_theAircraftInterface.getCabinConfiguration() != null)
			sb.append(_theAircraftInterface.getCabinConfiguration().toString());
		
		if(_theAircraftInterface.getFuelTank() != null)
			sb.append(_theAircraftInterface.getFuelTank().toString());
		
		if(_theAircraftInterface.getPowerPlant() != null)
			sb.append(_theAircraftInterface.getPowerPlant().toString());
		
		if(_theAircraftInterface.getNacelles() != null)
			sb.append(_theAircraftInterface.getNacelles().toString());
		
		if(_theAircraftInterface.getLandingGears() != null)
			sb.append(_theAircraftInterface.getLandingGears().toString());
		
		return sb.toString();
		
	}

	//----------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public IAircraft getTheAircraftInterface() {
		return _theAircraftInterface;
	}
	
	public void setTheAircraftInterface(IAircraft theAircraftInterface) {
		this._theAircraftInterface = theAircraftInterface;
		
	}
	
	public AircraftTypeEnum getTypeVehicle() {
		return _theAircraftInterface.getTypeVehicle();
	}

	public void setTypeVehicle(AircraftTypeEnum _typeVehicle) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setTypeVehicle(_typeVehicle).build());
	}

	public RegulationsEnum getRegulations() {
		return _theAircraftInterface.getRegulations();
	}

	public void setRegulations(RegulationsEnum _regulations) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setRegulations(_regulations).build());
	}

	public String getId() {
		return _theAircraftInterface.getId();
	}

	public void setId(String _name) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setId(_name).build());
	}
	
	public Amount<Area> getSWetTotal() {
		return _sWetTotal;
	}
	
	public void setSWetTotal(Amount<Area> _sWetTotal) {
		this._sWetTotal = _sWetTotal;
	}

	public Double getKExcr() {
		return _kExcr;
	}

	public void setKExcr(Double kExcr) {
		this._kExcr = kExcr;
	}

	public List<Object> getComponentsList() {
		return _componentsList;
	}
	
	public ACAnalysisManager getTheAnalysisManager() {
		return this._theAnalysisManager;
	}
	
	public void setTheAnalysisManager(ACAnalysisManager theAnalysisManager) {
		this._theAnalysisManager = theAnalysisManager;
	}
	
	public Fuselage getFuselage() {
		return _theAircraftInterface.getFuselage();
	}
	
	public void setFuselage(Fuselage fuselage) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setFuselage(fuselage).build());
	}
	
	public LiftingSurface getWing() {
		return _theAircraftInterface.getWing();
	}
	
	public void setWing(LiftingSurface wing) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setWing(wing).build());
	}
	
	public LiftingSurface getExposedWing() {
		return _theAircraftInterface.getWing().getExposedLiftingSurface();
	}
	
	public void setExposedWing(LiftingSurface exposedWing) {
		_theAircraftInterface.getWing().setExposedLiftingSurface(exposedWing);
	}
	
	public LiftingSurface getHTail() {
		return _theAircraftInterface.getHTail();
	}

	public void setHTail(LiftingSurface hTail) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setHTail(hTail).build());
	}
	
	public LiftingSurface getVTail() {
		return _theAircraftInterface.getVTail();
	}

	public void setVTail(LiftingSurface vTail) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setVTail(vTail).build());
	}
	
	public LiftingSurface getCanard() {
		return _theAircraftInterface.getCanard();
	}

	public void setCanard(LiftingSurface canard) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setCanard(canard).build());
	}
	
	public PowerPlant getPowerPlant() {
		return _theAircraftInterface.getPowerPlant();
	}
	
	public void setPowerPlant(PowerPlant powerPlant) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setPowerPlant(powerPlant).build());
	}
	
	public Nacelles getNacelles() {
		return _theAircraftInterface.getNacelles();
	}
	
	public void setNacelles(Nacelles nacelles) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setNacelles(nacelles).build());
	}
	
	public FuelTank getFuelTank() {
		return _theAircraftInterface.getFuelTank();
	}
	
	public void setFuelTank(FuelTank fuelTank) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setFuelTank(fuelTank).build());
	}
	
	public LandingGears getLandingGears() {
		return _theAircraftInterface.getLandingGears();
	}
	
	public void setLandingGears(LandingGears landingGears) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setLandingGears(landingGears).build());
	}
	
	public Systems getSystems() {
		return _theAircraftInterface.getSystems();
	}
	
	public void setSystems(Systems systems) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setSystems(systems).build());
	}
	
	public CabinConfiguration getCabinConfiguration() {
		return _theAircraftInterface.getCabinConfiguration();
	}
	
	public void setCabinConfiguration(CabinConfiguration theCabinConfiguration) {
		setTheAircraftInterface(IAircraft.Builder.from(_theAircraftInterface).setCabinConfiguration(theCabinConfiguration).build());
	}
	
	public Amount<Length> getWingACToCGDistance() {
		return _wingACToCGDistance;
	}

	public void setWingACToCGDistance(Amount<Length> _wingACToCGDistance) {
		this._wingACToCGDistance = _wingACToCGDistance;
	}

	public String getFuselageFilePath() {
		return _fuselageFilePath;
	}

	public void setFuselageFilePath(String _fuselageFilePath) {
		this._fuselageFilePath = _fuselageFilePath;
	}

	public String getCabinConfigurationFilePath() {
		return _cabinConfigurationFilePath;
	}

	public void setCabinConfigurationFilePath(String _cabinConfigurationFilePath) {
		this._cabinConfigurationFilePath = _cabinConfigurationFilePath;
	}

	public String getWingFilePath() {
		return _wingFilePath;
	}

	public void setWingFilePath(String _wingFilePath) {
		this._wingFilePath = _wingFilePath;
	}

	public String getHTailFilePath() {
		return _hTailFilePath;
	}

	public void setHTailFilePath(String _hTailFilePath) {
		this._hTailFilePath = _hTailFilePath;
	}

	public String getVTailFilePath() {
		return _vTailFilePath;
	}

	public void setVTailFilePath(String _vTailFilePath) {
		this._vTailFilePath = _vTailFilePath;
	}

	public String getCanardFilePath() {
		return _canardFilePath;
	}

	public void setCanardFilePath(String _canardFilePath) {
		this._canardFilePath = _canardFilePath;
	}

	public List<String> getEngineFilePathList() {
		return _engineFilePathList;
	}

	public void setEngineFilePathList(List<String> _engineFilePathList) {
		this._engineFilePathList = _engineFilePathList;
	}

	public List<String> getNacelleFilePathList() {
		return _nacelleFilePathList;
	}

	public void setNacelleFilePathList(List<String> _nacelleFilePathList) {
		this._nacelleFilePathList = _nacelleFilePathList;
	}

	public String getLandingGearsFilePath() {
		return _landingGearsFilePath;
	}

	public void setLandingGearsFilePath(String _landingGearsFilePath) {
		this._landingGearsFilePath = _landingGearsFilePath;
	}

	public String getSystemsFilePath() {
		return _systemsFilePath;
	}

	public void setSystemsFilePath(String _systemsFilePath) {
		this._systemsFilePath = _systemsFilePath;
	}
	
} 
