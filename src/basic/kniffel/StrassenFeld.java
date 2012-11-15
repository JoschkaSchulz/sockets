package basic.kniffel;

class StrassenFeld extends KniffelFeld {
	private int folge = 0;
	private int punkte = 0;
	public StrassenFeld(int folge, int punkte, String type) {
		this.folge = folge;
		this.punkte = punkte;
		this.name = type + " Straﬂe";
		this.desc = punkte + " Punkte";
	}
	public int eval(int[] wuerfel) {
		int[] c = countSeries(1, 6, wuerfel);
		int anzahl = 0;
		for (int i = 0; i < 6; i++) {
			if (c[i] > 0)
				anzahl++;
			else
				anzahl = 0;
			if (anzahl >= folge)
				return punkte;
		}
		return 0;
	}
}