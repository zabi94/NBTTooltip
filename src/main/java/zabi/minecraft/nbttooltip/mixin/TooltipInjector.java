package zabi.minecraft.nbttooltip.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import zabi.minecraft.nbttooltip.NBTTooltip;

@Mixin(ItemStack.class)
public abstract class TooltipInjector {
	
	@Inject(at = @At("RETURN"), method = "getTooltipText")
	public void onInjectTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Component>> ci) {
		NBTTooltip.onInjectTooltip(this, player == null?null:player.world, ci.getReturnValue(), context);
	}
	
}
