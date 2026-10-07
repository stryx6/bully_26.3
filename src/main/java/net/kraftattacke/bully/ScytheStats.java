package net.kraftattacke.bully;

public record ScytheStats(
		int durability,
		int enchantability,
		double attackDamage,
		double attackSpeed
) {
	public static ScytheStats defaults() {
		// Mirrors vanilla mace baseline.
		// Note: the final in-game attack damage is (base 1.0 + this modifier).
		return new ScytheStats(3500, 15, 8.0, -2.8);
	}
}
