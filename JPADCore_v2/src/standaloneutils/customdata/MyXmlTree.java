package standaloneutils.customdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Build an object which links each relevant object
 * (e.g., the fuselage, the wing, airfoils etc.)
 * with a description and a level. Be careful that
 * if an object is added each list must be populated
 * with a value in order to keep all the following
 * methods valid
 * 
 * @author Lorenzo Attanasio
 */
public class MyXmlTree {

	private List<Object> objList = new ArrayList<Object>();
	private List<Class> classList = new ArrayList<Class>();
	private List<Integer> idList = new ArrayList<Integer>();
	private List<Enum> enumList = new ArrayList<Enum>(); 
	private List<Integer> levelList = new ArrayList<Integer>(); 
	private List<String> descriptionList = new ArrayList<String>();
	private int i = 0;

	public MyXmlTree() {
	}

	/**
	 * @param ob the object to map
	 * @param enu the object type
	 * 
	 * @param level the indentation level at which the object is in the xml tree:
	<ADOpT> : level 0
    	<Operating_Conditions> : level 1
        	<Altitude from="input" unit="m" varName="_altitude">6000.00000</Altitude>
        	<Mach_number from="input" unit="" varName="_machCurrent">0.4300</Mach_number>
        	<Pressure from="output" unit="Pa" varName="_pressure">47217.60476</Pressure>
        	<Pressure_differential from="output" unit="Pa" varName="_maxDeltaP">32283.79971</Pressure_differential>
        	<Density from="output" unit="kg/m³" varName="_density">0.66011</Density>
        	<Temperature from="output" unit="K" varName="_temperature">249.18678</Temperature>
        	<DynamicViscosity from="output" unit="Pa·s" varName="_mu">0.000016610284600104726</DynamicViscosity>
        	<Speed from="output" unit="m/s" varName="_speed">136.07423</Speed>
    	</Operating_Conditions>
    	<AIRCRAFT> : level 1
        	<Configuration> : level 2
			...

	 * @param description a description of the object
	 */
	public void add(Object ob, Enum enu, Integer level, String description) {
		objList.add(ob);
		enumList.add(enu);
		levelList.add(level);
		descriptionList.add(description);
		idList.add(i);
		i++;
	}

	public void add(Object ob, Integer level, String description) {
		objList.add(ob);
		classList.add(ob.getClass());
		levelList.add(level);
		descriptionList.add(description);
		//		idList.add(i);
		//		i++;
	}

	public void add(Class<?> clazz, Integer level, String description) {
		classList.add(clazz);
		levelList.add(level);
		descriptionList.add(description);
		//		idList.add(i);
		//		i++;
	}

	public void add(Object ob, Integer level, String description, String id) {
		if (ob != null) {
			add(ob, level, description);
			idList.add(Integer.parseInt(id));
		} else {
			System.out.println("WARNING: The object " + description + " has not been initialized");
		}
	}

	public void add(Class<?> clazz, Integer level, String description, String id) {
		if (clazz != null) {
			add(clazz, level, description);
			idList.add(Integer.parseInt(id));
		} else {
			System.out.println("WARNING: The object " + description + " has not been initialized");
		}
	}

	public Object getPrevious(Object ob) {
		return objList.get(objList.indexOf(ob) - 1);
	}

	public Object getNext(Object ob) {
		return objList.get(objList.indexOf(ob) + 1);
	}

	public Integer getLevel(Object ob){
		return levelList.get(objList.indexOf(ob));
	}

	public Integer getLevel(Class cl){
		return levelList.get(classList.indexOf(cl));
	}

	public String getDescription(Class cl){
		return descriptionList.get(classList.indexOf(cl));
	}

	public String getDescription(Object ob){
		return descriptionList.get(objList.indexOf(ob));
	}

	public Object getObject(String des) {
		return objList.get(descriptionList.indexOf(des));
	}

	public Object getObject(Enum enu) {
		return objList.get(enumList.indexOf(enu));
	}

	public int getId(Object ob) {
		return idList.get(objList.indexOf(ob));
	}

	public String getIdAsString(Object ob) {
		return idList.get(objList.indexOf(ob)).toString();
	}

	public String getIdAsString(Class<?> clazz) {
		return idList.get(classList.indexOf(clazz)).toString();
	}

}
