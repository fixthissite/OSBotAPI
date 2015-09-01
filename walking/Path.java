package lemons.api.walking;

public interface Path {

	public boolean walk();
	
	public boolean loop();
	
	public boolean loop(int count);
	
	public int length();
	
}
