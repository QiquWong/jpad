package standaloneutils;

public class MyMiscUtils {

	/** 
	 * Check if string is contained in an enumeration
	 * 
	 * @author Lorenzo Attanasio
	 * @param value
	 * @param enumClass
	 * @return
	 */
	public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
		for (E e : enumClass.getEnumConstants()) {
			if(e.name().equals(value)) { return true; }
		}
		return false;
	}

	/** 
	 * Check if string is an integer
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str) {
		if (str == null) return false;
		
		int length = str.length();
		if (length == 0) {
			return false;
		}
		
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) return false;
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') return false;
		}
		return true;
	}

}
