/**
 * 
 */
package basic.kniffel;

class AugenFeld extends KniffelFeld {
	private int auge = 0;
	public AugenFeld(int auge) {
		this.auge = auge;
		this.name = auge + "er";
		this.desc = "Nur " + auge + "er zählen";
	}
	public int eval(int[] wuerfel) {
		int summe = 0;
		for (int i : wuerfel)
			if (i == auge)
				summe += i;
		return summe;
	}
}