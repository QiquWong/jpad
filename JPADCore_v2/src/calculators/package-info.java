/**
 * This package is a collection of classes useful for evaluating the
 * parameters needed to analyze an aircraft aerodynamics, balance, performance, costs.
 * 
 * Some classes provide a way to execute the analysis in an Object Oriented fashion:
 * the user developer can create an object initializing all the parameters needed for
 * the calculations. Using the method calculateAll() he/she can then execute all the
 * analyses included in the corresponding class; the estimated parameters can then be
 * accessed from the object itself.
 * 
 * The classes in this package do not depend on the Aircraft class; this means that the
 * parameters of the aircraft have to be passed one by one. Although this requires to
 * pass a lot of parameters to each static method/class, it also increases the portability
 * and the re-usability of the code. Also, it enables the developer and the user developer
 * to have under control the parameters actually needed to perform the computations. 
 * 
 * NOTICE: UNLESS SPECIFIED ALL METHODS REQUIRE SI UNITS
 * 
 * @author Lorenzo Attanasio
 *
 */
package calculators;