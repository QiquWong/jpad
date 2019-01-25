package it.unina.daf.jpadcadsandbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.WingAdjustCriteriaEnum;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcadsandbox.Test26mds.AeroComponents;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.DataWriter;
import it.unina.daf.jpadcadsandbox.utils.GeometricData;
import it.unina.daf.jpadcadsandbox.utils.GeometricData.GeometricDataBuilder;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions;
import it.unina.daf.jpadcadsandbox.utils.OperatingConditions.OperatingConditionsBuilder;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters;
import it.unina.daf.jpadcadsandbox.utils.SimulationParameters.SimulationParametersBuilder;
import standaloneutils.aerotools.aero.StdAtmos1976;
import standaloneutils.atmosphere.AtmosphereCalc;

public class PostProcessingGB {
	public static BufferedWriter bwoutu = null; 
	public static BufferedWriter bwoutr = null;
	public static BufferedWriter bwoutd = null;

	//TODO
	//	public static final String filesFolderPath = "C:\\Users\\giord\\Desktop\\STAR_WORKING_FOLDER";
	public static final String casesFolderPath = "C:\\Users\\giord\\Desktop\\STAR_WORKING_FOLDER";
	public static final String macroPath = " D:\\eclipse\\STAR_MACRO\\src\\macro";
	public static final String macroName = "Post_Process_Sim.java";
	public static final String starExePath = "C:\\Program Files\\CD-adapco\\12.04.011-R8\\STAR-CCM+12.04.011-R8\\star\\bin\\starccm+.exe";
	public static final String starOptions = "-cpubind -power -podkey 2jHU+QkwqexqrAOdVZ6ZzQ -licpath 1999@flex.cd-adapco.com -np 8 -rsh ssh";


	public static void main(String[] args) throws IOException {
		System.out.println("-------------------");
		System.out.println("Post-Processing: Downwash and Upwash calculation");
		System.out.println("-------------------");




		//TODO number of cases
		int n_cases=3;


		List<Double> alphaVec = new ArrayList<>();
		List<String> caseFolderPaths = new ArrayList<>();



		//canard_wing
		List<Double> cd_canard_w = new ArrayList<>();
		List<Double> cy_canard_w = new ArrayList<>();
		List<Double> cl_canard_w = new ArrayList<>();
		List<Double> croll_canard_w = new ArrayList<>();
		List<Double> cm_canard_w = new ArrayList<>();
		List<Double> cn_canard_w = new ArrayList<>();

		List<List<Double>> cCl_y_canard_w = new ArrayList<>(n_cases);
		List<List<Double>> cl_y_canard_w = new ArrayList<>(n_cases);


		//wing_canard
		List<Double> cd_wing_c = new ArrayList<>();
		List<Double> cy_wing_c = new ArrayList<>();
		List<Double> cl_wing_c = new ArrayList<>();
		List<Double> croll_wing_c = new ArrayList<>();
		List<Double> cm_wing_c = new ArrayList<>();
		List<Double> cn_wing_c = new ArrayList<>();

		List<List<Double>> cCl_y_wing_c = new ArrayList<>(n_cases);
		List<List<Double>> cl_y_wing_c = new ArrayList<>(n_cases);


		//canard alone
		List<Double> cd_canard = new ArrayList<>();
		List<Double> cy_canard = new ArrayList<>();
		List<Double> cl_canard = new ArrayList<>();
		List<Double> croll_canard = new ArrayList<>();
		List<Double> cm_canard = new ArrayList<>();
		List<Double> cn_canard = new ArrayList<>();

		List<List<Double>> cCl_y_canard = new ArrayList<>(n_cases);
		List<List<Double>> cl_y_canard = new ArrayList<>(n_cases);


		//wing alone
		List<Double> cd_wing = new ArrayList<>();
		List<Double> cy_wing = new ArrayList<>();
		List<Double> cl_wing = new ArrayList<>();
		List<Double> croll_wing = new ArrayList<>();
		List<Double> cm_wing = new ArrayList<>();
		List<Double> cn_wing = new ArrayList<>();

		List<List<Double>> cCl_y_wing = new ArrayList<>(n_cases);
		List<List<Double>> cl_y_wing = new ArrayList<>(n_cases);

		//total
		List<Double> cd_total = new ArrayList<>();
		List<Double> cy_total = new ArrayList<>();
		List<Double> cl_total = new ArrayList<>();
		List<Double> croll_total = new ArrayList<>();
		List<Double> cm_total = new ArrayList<>();
		List<Double> cn_total = new ArrayList<>();

		//wing geometric parameters
		List<Double> y_wing = new ArrayList<>();
		List<Double> eta_wing = new ArrayList<>();
		List<Double> c_y_wing = new ArrayList<>();

		//canard geometric parameters
		List<Double> y_canard = new ArrayList<>();
		List<Double> eta_canard = new ArrayList<>();
		List<Double> c_y_canard = new ArrayList<>();



		//initialize 2D-array lists
		for(int j = 0; j < n_cases; j++) {
			cCl_y_wing.add(new ArrayList());
		}


		for(int j = 0; j < n_cases; j++) {
			cl_y_wing.add(new ArrayList());
		}

		for(int j = 0; j < n_cases; j++) {
			cCl_y_canard.add(new ArrayList());
		}

		for(int j = 0; j < n_cases; j++) {
			cl_y_canard.add(new ArrayList());
		}

		for(int j = 0; j < n_cases; j++) {
			cCl_y_wing_c.add(new ArrayList());
		}


		for(int j = 0; j < n_cases; j++) {
			cl_y_wing_c.add(new ArrayList());
		}

		for(int j = 0; j < n_cases; j++) {
			cCl_y_canard_w.add(new ArrayList());
		}

		for(int j = 0; j < n_cases; j++) {
			cl_y_canard_w.add(new ArrayList());
		}


		Double downwash_int = 0.0;
		Double upwash_int = 0.0;

		for(int countm=0; countm<n_cases-2; countm++) {
			for(int c1=0; c1<n_cases-2; c1++) {
				for(int c2=0; c2<n_cases-2; c2++) {
					for(int c3=0; c3<n_cases-2; c3++) {
						for(int c4=0; c4<n_cases-2; c4++) {
							for(int c5=0; c5<n_cases-2; c5++) {
								for(int c6=0; c6<n_cases-2; c6++) {
									for(int counta=0; counta<n_cases; counta++) {

										//Post Process the Simulation
										String runsimName = "CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 +"_"+ c4 +"_"+ c5 +"_"+ c6 +"_"+ countm +"_"+ counta+"_run.sim";

										try {
											Runtime runtime = Runtime.getRuntime();

											Process runTheMacro = runtime.exec(
													"cmd /c cd\\ && cd " + macroPath + " && dir && " + // change directory
															"\"" + starExePath + "\" " +               // run the application
															starOptions + " " +                        // set license and settings
															runsimName+" -batch " + macroName            // load simulation in batch mode
													);
											//get starccm+ log messages
											BufferedReader input = new BufferedReader(new InputStreamReader(runTheMacro.getInputStream()));

											String line = null;		
											while((line = input.readLine()) != null) System.out.println(line);

											int exitVal = runTheMacro.waitFor();
											System.out.println("Exited with error code " + exitVal);
										}

										catch(Exception e) {
											System.out.println(e.toString());
											e.printStackTrace();
										}


										String coefficient = "";
										String type = "";
										Double value = 0.0;


										//IMPORT CANARD_WING AERODYNAMIC COEFFICIENTS

										//TODO put the correct case's path
										try(BufferedReader br = new BufferedReader(new FileReader(casesFolderPath+"\\Case_"+c1+"_"+c2+"_"+c3+"_"+c4+"_"+c5+"_"+c6+"_"+countm+"_"+counta
												+ File.separator +
												"/CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm + "_" + counta +"_run_report.csv"))){
											String line;
											while ((line = br.readLine()) != null) {

												// get coefficient
												String[] result0 = line.split("_");

												if (1 == result0.length) 
													continue;


												if (result0[0].equals("Angle")) 
													type = "Angle";

												else {
													if (result0[1].contains("CANARD")) 
														type = "CANARD";
													coefficient = result0[0];

													if (result0[1].contains("WING"))
														type = "WING";
													coefficient = result0[0];

//													if (result0[1].equals("total"))
//														type = "Total";
//													coefficient = result0[0];
												}

												switch(type) {

												case "Angle":
													String[] resulta = result0[2].split(",");
													value = Double.valueOf(resulta[resulta.length - 1]);
													alphaVec.add(value);
													break;

												case "CANARD":
													String[] resultc = result0[1].split(",");
													value = Double.valueOf(resultc[resultc.length - 1]);


													if(coefficient.equals("CD"))
														cd_canard_w.add(value);


													if(coefficient.equals("CY"))
														cy_canard_w.add(value);


													if(coefficient.equals("CL"))
														cl_canard_w.add(value);


													if(coefficient.equals("CRoll"))
														croll_canard_w.add(value);


													if(coefficient.equals("CM"))
														cm_canard_w.add(value);


													if(coefficient.equals("CN"))
														cn_canard_w.add(value);

													break;


												case "WING":

													String[] resultw = result0[1].split(",");
													value = Double.valueOf(resultw[resultw.length - 1]);

													if(coefficient.equals("CD"))
														cd_wing_c.add(value);

													if(coefficient.equals("CY"))
														cy_wing_c.add(value);


													if(coefficient.equals("CL"))
														cl_wing_c.add(value);


													if(coefficient.equals("CRoll"))
														croll_wing_c.add(value);


													if(coefficient.equals("CM"))
														cm_wing_c.add(value);


													if(coefficient.equals("CN"))
														cn_wing_c.add(value);


													break;


												case "Total":

													String[] result = result0[1].split(",");
													value = Double.valueOf(result[result.length - 1]);

													if(coefficient.equals("CD"))
														cd_total.add(value);

													if(coefficient.equals("CY"))
														cy_total.add(value);


													if(coefficient.equals("CL"))
														cl_total.add(value);


													if(coefficient.equals("CRoll"))
														croll_total.add(value);


													if(coefficient.equals("CM"))
														cm_total.add(value);


													if(coefficient.equals("CN"))
														cn_total.add(value);


													break;


												default:
													break;


												}
											}

										}



										//IMPORT CANARD AND WING ALONE AERODYNAMIC COEFFICIENTS

										String[] alone = new String[] {"CANARD","WING"};

										for(int name=0; name < alone.length; name++) {


											try(BufferedReader br= new BufferedReader(new FileReader(casesFolderPath
													+"\\"+alone[name]+"_ALONE"+File.separator+
													"/"+alone[name]+ "_" + c1 +"_"+ c2 +"_"+ c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm + "_" + counta +"_report.csv"))){
												String line;
												while ((line = br.readLine()) != null) {


													// get coefficient
													String[] result0 = line.split("_");

													if (1 == result0.length) 
														continue;


													if (result0[0].equals("Angle")) 
														type = "Angle";

													else {
														if (result0[1].contains("CANARD")) 
															type = "CANARD";
														coefficient = result0[0];

														if (result0[1].contains("WING"))
															type = "WING";
														coefficient = result0[0];


													}


													switch(type) {

													case "CANARD":

														String[] resultc = result0[1].split(",");
														value = Double.valueOf(resultc[resultc.length - 1]);

														if(coefficient.equals("CD"))
															cd_canard.add(value);


														if(coefficient.equals("CY"))
															cy_canard.add(value);


														if(coefficient.equals("CL"))
															cl_canard.add(value);


														if(coefficient.equals("CRoll"))
															croll_canard.add(value);


														if(coefficient.equals("CM"))
															cm_canard.add(value);


														if(coefficient.equals("CN"))
															cn_canard.add(value);

														break;


													case "WING":

														String[] resultw = result0[1].split(",");
														value = Double.valueOf(resultw[resultw.length - 1]);

														if(coefficient.equals("CD"))
															cd_wing.add(value);

														if(coefficient.equals("CY"))
															cy_wing.add(value);


														if(coefficient.equals("CL"))
															cl_wing.add(value);


														if(coefficient.equals("CRoll"))
															croll_wing.add(value);


														if(coefficient.equals("CM"))
															cm_wing.add(value);


														if(coefficient.equals("CN"))
															cn_wing.add(value);


														break;


													default:
														break;

													}
												}
											}
										}




										//																			System.out.println(alphaVec + "\n"+
										//																					           cd_canard_w + "\n"+
										//																			                   cy_canard_w + "\n"+
										//																			                   cl_canard_w + "\n"+
										//																			                   croll_canard_w + "\n"+
										//																			                   cm_canard_w + "\n"+
										//																			                   cn_canard_w + "\n"+
										//																			                   cd_wing_c + "\n"+
										//																			                   cy_wing_c + "\n"+
										//																			                   cl_wing_c + "\n"+
										//																			                   croll_wing_c + "\n"+
										//																			                   cm_wing_c + "\n"+
										//																			                   cn_wing_c + "\n"+
										//																			                   cd_canard + "\n"+
										//																			                   cy_canard + "\n"+
										//																			                   cl_canard + "\n"+
										//																			                   croll_canard + "\n"+
										//																			                   cm_canard + "\n"+
										//																			                   cn_canard + "\n"+
										//																			                   cd_wing + "\n"+
										//																			                   cy_wing+ "\n"+
										//																			                   cl_wing+ "\n"+
										//																			                   croll_wing+ "\n"+
										//																			                   cm_wing+ "\n"+
										//																			                   cn_wing+ "\n");


										//IMPORT CANARD AND WING LOADS IN CANARD_WING CASE

										for(int name=0; name < alone.length; name++) {


											try(BufferedReader br = new BufferedReader(new FileReader(casesFolderPath+"\\Case_"+c1+"_"+c2+"_"+c3+"_"+c4+"_"+c5+"_"+c6+"_"+countm+"_"+counta
													+File.separator+
													"/CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm + "_" + counta +"_run_"+alone[name]+"_loads.csv"))){
												String line;
												while ((line = br.readLine()) != null) {

													String[] result = line.split(",");



													if (result[0].equals("y"))
														continue;

													switch(alone[name]) {

													case "WING":

														if(counta==0) {

															y_wing.add(Double.valueOf(result[0]));

															eta_wing.add(Double.valueOf(result[1]));

															c_y_wing.add(Double.valueOf(result[2]));
														}

														cCl_y_wing_c.get(counta).add(Double.valueOf(result[3]));

														cl_y_wing_c.get(counta).add(Double.valueOf(result[result.length - 1]));

														break;

													case "CANARD":

														if(counta==0) {

															y_canard.add(Double.valueOf(result[0]));

															eta_canard.add(Double.valueOf(result[1]));

															c_y_canard.add(Double.valueOf(result[2]));

														}

														cCl_y_canard_w.get(counta).add(Double.valueOf(result[3]));

														cl_y_canard_w.get(counta).add(Double.valueOf(result[result.length - 1]));

														break;

													default:
														break;
													}
												}
											}
										}



										//IMPORT CANARD AND WING LOADS IN CANARD AND WING ALONE CASE


										for(int name=0; name<alone.length; name++) {


											try(BufferedReader br = new BufferedReader(new FileReader(casesFolderPath
													+"\\"+alone[name]+"_ALONE"+File.separator+
													"/"+ alone[name] + "_"+ c1 + "_" + c2 + "_" + c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm + "_" + counta +"_"+ alone[name]+"_loads.csv"))){
												String line;
												while ((line = br.readLine()) != null) {


													String[] result = line.split(",");
													if (result[0].equals("y"))
														continue;

													switch(alone[name]) {


													case "CANARD":

														cCl_y_canard.get(counta).add(Double.valueOf(result[3]));

														cl_y_canard.get(counta).add(Double.valueOf(result[result.length - 1]));

														break;

													case "WING":


														cCl_y_wing.get(counta).add(Double.valueOf(result[3]));

														cl_y_wing.get(counta).add(Double.valueOf(result[result.length - 1]));

														break;



													default:
														break;



													}

												}
											}
										}
									}


									//									System.out.println(cl_y_wing+ "\n"
									//											);	

									//INTEGRAL DOWNWASH AND UPWASH CALCULATION

									double[] regression_coefficients = linearRegression(alphaVec.size(), cl_wing, alphaVec);
									Double cla_wing = regression_coefficients[1];
									regression_coefficients = linearRegression(alphaVec.size(), cl_canard, alphaVec);
									Double cla_canard = regression_coefficients[1];
									regression_coefficients = linearRegression(alphaVec.size(), cl_wing_c, alphaVec);
									Double cla_wing_c = regression_coefficients[1];
									regression_coefficients = linearRegression(alphaVec.size(), cl_canard_w, alphaVec);
									Double cla_canard_w = regression_coefficients[1];

									//TODO
									downwash_int =1- (cla_wing_c/cla_wing);  //de/da integrale positivo
									upwash_int = (cla_canard_w/cla_canard)-1; 

									//SPANWISE DOWNWASH AND UPWASH CALCULATION

									List<List<Double>> downwash_y = new ArrayList<>(n_cases);
									List<List<Double>> upwash_y = new ArrayList<>(n_cases);


									for(int j = 0; j < n_cases; j++) {
										downwash_y.add(new ArrayList());
									}

									for(int j = 0; j < n_cases; j++) {
										upwash_y.add(new ArrayList());
									}



									for(int a=0; a < n_cases; a++) {
										for(int i=0; i < y_wing.size(); i++) {

											downwash_y.get(a).add((cl_y_wing_c.get(a).get(i)-cl_y_wing.get(a).get(i))/cla_wing_c);

										}



										for(int i=0; i < y_canard.size(); i++) {
											upwash_y.get(a).add((cl_y_canard_w.get(a).get(i)-cl_y_canard.get(a).get(i))/cla_canard_w);
										}


									}


									List<Double> downwash_da = new ArrayList<>();
									List<Double> upwash_da = new ArrayList<>();

									List<Double> downwash_i = new ArrayList<>();
									List<Double> upwash_i = new ArrayList<>();

									for(int i=0; i < y_wing.size(); i++) {

										for(int a =0; a < alphaVec.size(); a++) {


											downwash_i.add(downwash_y.get(a).get(i)); 
										}

										regression_coefficients = linearRegression(alphaVec.size(), downwash_i, alphaVec);
										downwash_da.add(regression_coefficients[1]);

									}

									for(int i=0; i < y_canard.size(); i++) {

										for(int a =0; a < alphaVec.size(); a++) {


											upwash_i.add(upwash_y.get(a).get(i)); 
										}

										regression_coefficients = linearRegression(alphaVec.size(), upwash_i, alphaVec);
										upwash_da.add(regression_coefficients[1]);
									}


									//WRITE RESULTS


									String destFolder = casesFolderPath+"\\RESULTS"+ File.separator + "Case_" + c1 + "_" + c2 + "_" + c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm ;
									caseFolderPaths.add(destFolder);
									File directory = new File(destFolder);
									directory.mkdir();

									bwoutr = new BufferedWriter(new FileWriter((destFolder + File.separator+
											"/CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm +"_resume.csv")));
									bwoutr.write("Aerodynamic Coefficients \n");

									String[] aerodynamicCoeff = new String[] {"CD","CL","CM"};

									for(String coeff : aerodynamicCoeff) {

										bwoutr.write(coeff + "\n");
										bwoutr.write("Alpha,Wing,Wing(Canard),Canard,Canard(Wing),Total \n");

										switch(coeff) {

										case "CD":

											for(int a =0; a < alphaVec.size(); a++) {
												bwoutr.write(alphaVec.get(a)+","+cd_wing.get(a)+","+cd_wing_c.get(a)+","+cd_canard.get(a)+","+cd_canard_w.get(a)+/*","+cd_total.get(a)+*/"\n");
											}
											break;

										case "CL":
											for(int a =0; a < alphaVec.size(); a++) {
												bwoutr.write(alphaVec.get(a)+","+cl_wing.get(a)+","+cl_wing_c.get(a)+","+cl_canard.get(a)+","+cl_canard_w.get(a)+/*","+cl_total.get(a)+*/"\n");
											}
											break;

										case "CM":
											for(int a =0; a < alphaVec.size(); a++) {
												bwoutr.write(alphaVec.get(a)+","+cm_wing.get(a)+","+cm_wing_c.get(a)+","+cm_canard.get(a)+","+cm_canard_w.get(a)+/*","+cm_total.get(a)+*/"\n");
											}
											break;
										}

									}

									bwoutr.close();

									bwoutd = new BufferedWriter(new FileWriter((destFolder + File.separator +
											"/CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm +"_Downwash.csv")));
									bwoutd.write("Integral Downwash \n");
									bwoutd.write(downwash_int+ "\n");
									bwoutd.write(" \n");


									bwoutu = new BufferedWriter(new FileWriter((destFolder + File.separator +
											"/CANARD_WING_"+ c1 +"_"+ c2 +"_"+ c3 + "_" + c4 + "_" + c5 + "_" + c6 + "_" + countm +"_Upwash.csv")));
									bwoutu.write("Integral Upwash \n");
									bwoutu.write(upwash_int+ "\n");
									bwoutu.write(" \n");

									for(int a=0; a < n_cases; a++) {

										bwoutd.write("Wing Spanwise Downwash at Alpha = "+alphaVec.get(a)+ "\n");
										bwoutd.write("eta,Downwash(eta) \n");

										bwoutu.write("Canard Spanwise Upwash at Alpha = "+alphaVec.get(a)+ "\n");
										bwoutu.write("eta,Upwash(eta) \n");


										for(int i=0; i < y_wing.size(); i++) {

											bwoutd.write(eta_wing.get(i)+","+downwash_y.get(a).get(i)+"\n");

										}

										for(int i=0; i < y_canard.size(); i++) {

											bwoutu.write(eta_canard.get(i)+","+upwash_y.get(a).get(i)+"\n");

										}
									}

									bwoutd.write("Wing Spanwise Downwash Derivative \n");

									for(int i=0; i < y_wing.size(); i++) {

										bwoutd.write(eta_wing.get(i)+","+downwash_da.get(i)+"\n");

									}

									bwoutu.write("Canard Spanwise Upwash Derivative \n");

									for(int i=0; i < y_canard.size(); i++) {

										bwoutu.write(eta_canard.get(i)+","+upwash_da.get(i)+"\n");

									}

									bwoutd.close();
									bwoutu.close();



								}
							}
						}
					}
				}
			}
		}
	}



	private static double[] linearRegression (int n, List<Double> y, List<Double> x) {

		double sum_y = 0.0;
		double sum_x = 0.0;
		double sum_xy = 0.0;
		double sum_x_square = 0.0;
		double b = 0.0;
		double a = 0.0;

		for(int i=0; i<n; i++) {

			sum_y += y.get(i);
			sum_x += x.get(i);
			sum_xy += y.get(i)*x.get(i);
			sum_x_square += Math.pow((x.get(i)), 2);

		}

		//y=a+bx

		b=((n*sum_xy)-(sum_x*sum_y))/((n*sum_x_square) - Math.pow(sum_x, 2));

		a=((sum_y*sum_x_square)-(sum_x*sum_xy))/((n*sum_x_square) -Math.pow(sum_x, 2));	

		double[] coefficients = new double[] {a,b};

		return coefficients;

	}



}







