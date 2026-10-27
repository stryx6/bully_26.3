package net.kraftattacke.bully.portable;

/** Marker for a workbench menu opened from the inventory (not a placed block). */
public interface PortableStation {
	/** Position of this station in the carried-stations list, for left/right cycling. */
	int bully$index();
}
