package com.smanzana.nostrumfairies.sound;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public enum NostrumFairiesSounds {
	
	PICKAXE_HIT("ambient.pickaxe.strike", SoundCategory.AMBIENT),
	APPEAR("ambient.appear", SoundCategory.AMBIENT, .6f),
	DWARF_DIE("entity.dwarf.die", SoundCategory.NEUTRAL),
	DWARF_HURT("entity.dwarf.hurt", SoundCategory.NEUTRAL),
	ELF_HURT("entity.elf.hurt", SoundCategory.NEUTRAL),
	ELF_DIE("entity.elf.die", SoundCategory.NEUTRAL),
	ELF_IDLE("entity.elf.idle", SoundCategory.NEUTRAL),
	FAIRY_HURT("entity.fairy.hurt", SoundCategory.NEUTRAL),
	FAIRY_DIE("entity.fairy.die", SoundCategory.NEUTRAL),
	FAIRY_IDLE("entity.fairy.idle", SoundCategory.NEUTRAL),
	GNOME_DIE("entity.gnome.die", SoundCategory.NEUTRAL),
	GNOME_HURT("entity.gnome.hurt", SoundCategory.NEUTRAL),
	GNOME_IDLE("entity.gnome.idle", SoundCategory.NEUTRAL),
	GNOME_WORK("entity.gnome.work", SoundCategory.NEUTRAL),
	SHADOW_FEY_IDLE("entity.shadow_fey.idle", SoundCategory.HOSTILE, .5f),
	SHADOW_FEY_HURT("entity.shadow_fey.hurt", SoundCategory.HOSTILE),
	LYRE("items.instrument.lyre", SoundCategory.PLAYERS),
	FLUTE("items.instrument.flute", SoundCategory.PLAYERS),
	OCARINA("items.instrument.ocarina", SoundCategory.PLAYERS),
	BELL("items.instrument.bell", SoundCategory.PLAYERS);
	
	private ResourceLocation resource;
	private SoundCategory category;
	private SoundEvent event;
	private float volume;
	
	private NostrumFairiesSounds(String suffix, SoundCategory category) {
		this(suffix, category, 2.0f);
	}
	
	private NostrumFairiesSounds(String suffix, SoundCategory category, float volume) {
		this.resource = new ResourceLocation(NostrumFairies.MODID, suffix);
		this.category = category;
		this.event = new SoundEvent(resource);
		this.volume = volume;
	}
	
	public ResourceLocation getLocation() {
		return this.resource;
	}
	
	public void play(Entity at) {
		play(null, at.worldObj, at.getPositionVector());
	}
	
	public void play(EntityPlayer at) {
		play(at, at.worldObj, at.getPositionVector());
	}
	
	public void play(EntityPlayer player, World world, Vec3d at) {
		play(player, world, at.xCoord, at.yCoord, at.zCoord);
	}
	
	public void play(World world, double x, double y, double z) {
		play(null, world, x, y, z);
	}
	
	public void play(EntityPlayer player, World world, double x, double y, double z) {
		world.playSound(player, x, y, z,
				event, category,
				volume, 0.8f + (NostrumMagica.rand.nextFloat() * 0.4f));
	}
	
	public static void registerSounds() {
		int idOffset = SoundEvent.REGISTRY.getKeys().size();
		
		for (NostrumFairiesSounds sound : values()) {
			SoundEvent.REGISTRY.register(idOffset++, sound.resource, sound.event);
		}
		
	}

	public SoundEvent getEvent() {
		return event;
	}
	
}
