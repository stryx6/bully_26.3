package net.kraftattacke.bully;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	public static final Identifier MUSIC_DISC_ONRYO_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.onryo");
	public static final SoundEvent MUSIC_DISC_ONRYO = register(MUSIC_DISC_ONRYO_ID);
	public static final Identifier MUSIC_DISC_ATTACK_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.attack");
	public static final SoundEvent MUSIC_DISC_ATTACK = register(MUSIC_DISC_ATTACK_ID);
	public static final Identifier MUSIC_DISC_ELDERWOOD_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.elderwood");
	public static final SoundEvent MUSIC_DISC_ELDERWOOD = register(MUSIC_DISC_ELDERWOOD_ID);
	public static final Identifier MUSIC_DISC_EVERMERE_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.evermere");
	public static final SoundEvent MUSIC_DISC_EVERMERE = register(MUSIC_DISC_EVERMERE_ID);
	public static final Identifier MUSIC_DISC_FORGOTTEN_TALE_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.forgotten_tale");
	public static final SoundEvent MUSIC_DISC_FORGOTTEN_TALE = register(MUSIC_DISC_FORGOTTEN_TALE_ID);
	public static final Identifier MUSIC_DISC_SHADOWROOT_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.shadowroot");
	public static final SoundEvent MUSIC_DISC_SHADOWROOT = register(MUSIC_DISC_SHADOWROOT_ID);
	public static final Identifier MUSIC_DISC_SILVERROAD_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.silverroad");
	public static final SoundEvent MUSIC_DISC_SILVERROAD = register(MUSIC_DISC_SILVERROAD_ID);
	public static final Identifier MUSIC_DISC_UNDERTALE_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "music_disc.undertale");
	public static final SoundEvent MUSIC_DISC_UNDERTALE = register(MUSIC_DISC_UNDERTALE_ID);

	private ModSounds() {
	}

	private static SoundEvent register(Identifier id) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void register() {
		Bully.LOGGER.info("Registering sounds for {}", Bully.MOD_ID);
	}
}
