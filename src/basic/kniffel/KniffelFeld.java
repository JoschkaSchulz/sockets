package basic.kniffel;

public class KniffelFeld implements IKniffelFeld {
	protected String name = "Chance"; 
	protected String desc = "Alle Augen zählen"; 
	public String getName() {
		return name;
	}
	public String getDesc() {
		return desc;
	}
	public int eval(int[] wuerfel) {
		int summe = 0;
		for (int i : wuerfel)
			summe += i;
		return summe;
	}
	protected static boolean contains(int needle, int[] haystack) {
		for (int i : haystack)
			if (i == needle)
				return true;
		return false;
	}
	protected static int count(int needle, int[] haystack) {
		int anzahl = 0;
		for (int i : haystack)
			if (i == needle)
				anzahl++;
		return anzahl;
	}
	protected static int[] countSeries(int start, int stop, int[] haystack) {
		int[] anzahl = new int[stop - start + 1];
		for (int i = start; i <= stop; i++) {
			anzahl[i - start] = count(i, haystack);
		}
		return anzahl;
	}
}
