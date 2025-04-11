package com.smanzana.nostrumfairies.sound;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum NostrumFairiesSounds {
	
	PICKAXE_HIT("ambient.pickaxe.strike", SoundSource.AMBIENT),
	APPEAR("ambient.appear", SoundSource.AMBIENT, .6f),
	DWARF_DIE("entity.dwarf.die", SoundSource.NEUTRAL),
	DWARF_HURT("entity.dwarf.hurt", SoundSource.NEUTRAL),
	ELF_HURT("entity.elf.hurt", SoundSource.NEUTRAL),
	ELF_DIE("entity.elf.die", SoundSource.NEUTRAL),
	ELF_IDLE("entity.elf.idle", SoundSource.NEUTRAL, .75f),
	FAIRY_HURT("entity.fairy.hurt", SoundSource.NEUTRAL),
	FAIRY_DIE("entity.fairy.die", SoundSource.NEUTRAL),
	FAIRY_IDLE("entity.fairy.idle", SoundSource.NEUTRAL, .75f),
	GNOME_DIE("entity.gnome.die", SoundSource.NEUTRAL),
	GNOME_HURT("entity.gnome.hurt", SoundSource.NEUTRAL),
	GNOME_IDLE("entity.gnome.idle", SoundSource.NEUTRAL, .75f),
	GNOME_WORK("entity.gnome.work", SoundSource.NEUTRAL, .75f),
	SHADOW_FEY_IDLE("entity.shadow_fey.idle", SoundSource.HOSTILE, .5f),
	SHADOW_FEY_HURT("entity.shadow_fey.hurt", SoundSource.HOSTILE),
	LYRE("items.instrument.lyre", SoundSource.PLAYERS),
	FLUTE("items.instrument.flute", SoundSource.PLAYERS),
	OCARINA("items.instrument.ocarina", SoundSource.PLAYERS),
	BELL("items.instrument.bell", SoundSource.PLAYERS);
	
	private ResourceLocation resource;
	private SoundSource category;
	private SoundEvent event;
	private float volume;
	
	private NostrumFairiesSounds(String suffix, SoundSource category) {
		this(suffix, category, 2.0f);
	}
	
	private NostrumFairiesSounds(String suffix, SoundSource category, float volume) {
		this.resource = new ResourceLocation(NostrumFairies.MODID, suffix);
		this.category = category;
		this.event = new SoundEvent(resource);
		event.setRegistryName(resource);
		this.volume = volume;
	}
	
	public ResourceLocation getLocation() {
		return this.resource;
	}
	
	public void play(Entity at) {
		play(null, at.level, at.position());
	}
	
	public void play(Player at) {
		play(at, at.level, at.position());
	}
	
	public void play(Player player, Level world, Vec3 at) {
		play(player, world, at.x, at.y, at.z);
	}
	
	public void play(Level world, double x, double y, double z) {
		play(null, world, x, y, z);
	}
	
	public void play(Player player, Level world, double x, double y, double z) {
		world.playSound(player, x, y, z,
				event, category,
				volume, 0.8f + (NostrumMagica.rand.nextFloat() * 0.4f));
	}
	
	public static void registerSounds(IForgeRegistry<SoundEvent> registry) {
		for (NostrumFairiesSounds sound : values()) {
			registry.register(sound.event);
		}
	}

	public SoundEvent getEvent() {
		return event;
	}
	
	@SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
    	NostrumFairiesSounds.registerSounds(event.getRegistry());
    }
}
