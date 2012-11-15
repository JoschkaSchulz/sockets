package basic.kniffel;

class PaschFeld extends KniffelFeld {
	private int wert1 = 0;
	private int anzahl1 = 0;
	private int wert2 = 0;
	private int anzahl2 = 0;
	private int punkte = 0;
	public PaschFeld(int wert1, int anzahl1, int wert2, int anzahl2, int punkte, String type) {
		assert(wert1 >= wert2);
		this.wert1 = wert1;
		this.anzahl1 = anzahl1;
		this.wert2 = wert2;
		this.anzahl2 = anzahl2;
		this.punkte = punkte;
		this.name = type;
		this.desc = punkte == 0 ? "Alle Augen zählen" : punkte + " Punkte";
	}
	public int eval(int[] wuerfel) {
		int[] c = countSeries(1, 6, wuerfel);
		int anzahl1 = this.anzahl1;
		int anzahl2 = this.anzahl2;
		for (int i = 0; i < 6; i++)
			if (c[i] >= wert1 && anzahl1 > 0)
				anzahl1--;
			else if (c[i] >= wert2 && anzahl2 > 0)
				anzahl2--;
		if (anzahl1 == 0 && anzahl2 == 0)
			return punkte == 0 ? super.eval(wuerfel) : punkte;
		return 0;
	}
}