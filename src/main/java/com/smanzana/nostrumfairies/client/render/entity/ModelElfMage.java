package com.smanzana.nostrumfairies.client.render.entity;

import com.smanzana.nostrumfairies.entity.fey.EntityElf;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelElfMage<T extends EntityElf> extends ModelElf<T> {

	public static LayerDefinition createLayer(boolean leftHanded) {
		MeshDefinition mesh = ModelElf.createMesh(leftHanded);
		PartDefinition root = mesh.getRoot();
		
		root.getChild("body").getChild("armMain").addOrReplaceChild("wand",
				makeWand(),
				PartPose.offset(0, (10f / 16f), 0)
		);
		
		//render.offsetY = (10f / 16f); // height of arm, - a bit
		
		return LayerDefinition.create(mesh, 64, 32);
	}
	
	protected static CubeListBuilder makeWand() {
		return CubeListBuilder.create()
				.texOffs(48, 25).addBox(-.5f, -6, -.5f, 1, 6, 1)
				.texOffs(44, 28).addBox(-1.5f, -4, -.5f, 1, 1, 1)
				.texOffs(52, 27).addBox(0.5f, -8, 0, 1, 4, 1)
				.texOffs(52, 25).addBox(1.5f, -8, 0, 1, 1, 1)
				.texOffs(60, 29).addBox(-.5f, -9, 0, 1, 2, 1)
				.texOffs(56, 28).addBox(-1.5f, -11, -.5f, 1, 3, 1)
		;
	}
	
	protected ModelPart wand;
	
	public ModelElfMage(ModelPart part) {
		super(part);
		
		wand = part.getChild("body").getChild("armMain").getChild("wand");
	}

	@Override
	protected ModelPart getHeldMainHand() {
		return wand;
	}
}
