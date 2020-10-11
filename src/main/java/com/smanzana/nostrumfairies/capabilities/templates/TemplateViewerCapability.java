package com.smanzana.nostrumfairies.capabilities.templates;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Default implementation of the ITemplateViewerCapability interface. Always enabled.
 * @author Skyler
 *
 */
public class TemplateViewerCapability implements ITemplateViewerCapability {

	@CapabilityInject(ITemplateViewerCapability.class)
	public static Capability<ITemplateViewerCapability> CAPABILITY = null;
	
	@Override
	public boolean isEnabled() {
		return true;
	}
}
