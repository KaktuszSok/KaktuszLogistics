package kaktusz.kaktuszlogistics.util;

public class CastingUtils {

	/**
	 * Allows casting without needing to suppress unchecked warning.
	 * Use this responsibly! (e.g. when deserialising generics)
	 */
	@SuppressWarnings("unchecked")
	public static <T> T confidentCast(Object object) {
		return (T)object;
	}

}
