package es.uma.lcc.caesium.grasp.util;

import java.math.BigDecimal;

import com.github.cliftonlabs.json_simple.JsonObject;

/**
 * Utility functions to handle JSON
 * @author ccottap
 * @version 1.0
 *
 */
public class JsonUtil {
	
	/**
	 * Convenience method to obtain an integer from the JSON object
	 * @param obj the JSON object
	 * @param key the key whose value is sought
	 * @return the value of the key as an integer
	 */
	public static int getInt (JsonObject obj, String key) {
		return ((BigDecimal)obj.get(key)).intValue();
	}
	
	/**
	 * Convenience method to obtain a long from the JSON object
	 * @param obj the JSON object
	 * @param key the key whose value is sought
	 * @return the value of the key as a long
	 */
	public static long getLong (JsonObject obj, String key) {
		return ((BigDecimal)obj.get(key)).longValue();
	}
	
	/**
	 * Convenience method to obtain a double from the JSON object
	 * @param obj the JSON object
	 * @param key the key whose value is sought
	 * @return the value of the key as a double
	 */
	public static double getDouble (JsonObject obj, String key) {
		return ((BigDecimal)obj.get(key)).doubleValue();
	}

}
