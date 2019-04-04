package zabi.minecraft.nbttooltip.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
import net.minecraft.util.GameTaskQueue;
import zabi.minecraft.nbttooltip.ModConfig;
import zabi.minecraft.nbttooltip.NBTTooltip;

@Mixin(value = MinecraftClient.class)
public abstract class ClientTicker extends GameTaskQueue<Runnable> {

	public ClientTicker(String string_1) {
		super(string_1);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void injectOnTick(CallbackInfo ci) {
		if (!Screen.hasShiftDown()) {
			NBTTooltip.ticks++;
			int factor = 1;
			if (Screen.hasAltDown()) {
				factor = 4;
			}
			if (NBTTooltip.ticks >= ModConfig.ticksBeforeScroll/factor) {
				NBTTooltip.ticks = 0;
				NBTTooltip.line_scrolled++;
			}
		}
	}

}
