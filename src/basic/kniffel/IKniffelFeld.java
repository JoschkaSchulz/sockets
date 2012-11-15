/**
 * 
 */
package basic.kniffel;

interface IKniffelFeld {
	public String getName();
	public String getDesc();
	public int eval(int[] wuerfel);
}