package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.math.BlockPos;

public interface IFeyWorker {

	public static enum FairyGeneralStatus {
		WANDERING, // Not attached to a home, and therefore incapable of working
		IDLE, // Able to work, but with no work to do
		WORKING, // Working
		REVOLTING; // Refusing to work
		
		public final static class FairyStatusSerializer implements DataSerializer<FairyGeneralStatus> {
			
			private FairyStatusSerializer() {
				DataSerializers.registerSerializer(this);
			}
			
			@Override
			public void write(PacketBuffer buf, FairyGeneralStatus value) {
				buf.writeEnumValue(value);
			}

			@Override
			public FairyGeneralStatus read(PacketBuffer buf) throws IOException {
				return buf.readEnumValue(FairyGeneralStatus.class);
			}

			@Override
			public DataParameter<FairyGeneralStatus> createKey(int id) {
				return new DataParameter<>(id, this);
			}

			@Override
			public FairyGeneralStatus copyValue(FairyGeneralStatus value) {
				return value;
			}
		}
		
		public static FairyStatusSerializer Serializer = null;
		public static void Init() {
			 Serializer = new FairyStatusSerializer();
		}
	}
	
	// Suggested maximum fairy work distance.
	// Note: If this is larger, path finding starts to break down since MC limits
	// the amount of iterations in patch finding code to 200, which things start to bump into
	// the further away they are.
	public static final double MAX_FAIRY_DISTANCE_SQ = 24 * 24;
	
	/**
	 * Get current fairy worker status
	 * @return
	 */
	public FairyGeneralStatus getStatus();
	
	/**
	 * Get the fairy's home block.
	 * @return
	 */
	@Nullable
	public BlockPos getHome();
	
	/**
	 * Return the current task a fairy is working on.
	 * It's expected that a status of 'WORKING' means this will return something,
	 * while calling while IDLE would not.
	 * @return The current task if there is one, or null.
	 */
	public @Nullable ILogisticsTask getCurrentTask();
	
	/**
	 * A task claimed by this fairy is no longer needed. Drop it.
	 */
	public void dropTask(ILogisticsTask task);
	
	public @Nullable LogisticsNetwork getLogisticsNetwork();
	
}
