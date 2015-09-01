package lemons.api.walking.map;

public class Flags {

	public static final int FLAG_NORTH_BLOCKED = 1;
	
	public static final int FLAG_EAST_BLOCKED = 2;
	
	public static final int FLAG_SOUTH_BLOCKED = 4;
	
	public static final int FLAG_WEST_BLOCKED = 8;
	
	public static final int FLAG_NORTHWEST_BLOCKED = 16;
	
	public static final int FLAG_NORTHEAST_BLOCKED = 32;
	
	public static final int FLAG_SOUTHWEST_BLOCKED = 64;
	
	public static final int FLAG_SOUTHEAST_BLOCKED = 128;
	
	public static final int FLAG_BLOCKED = 256;
	
	public static final int FLAG_ACCESSED = 512;
	
	public static final int FLAG_OBSTACLE = 2048;
	
}
