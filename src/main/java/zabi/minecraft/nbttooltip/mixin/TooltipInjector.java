package zabi.minecraft.nbttooltip.mixin;

import static zabi.minecraft.nbttooltip.NBTTooltip.FORMAT;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.StringTextComponent;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import net.minecraft.world.World;
import zabi.minecraft.nbttooltip.ModConfig;
import zabi.minecraft.nbttooltip.NBTTooltip;

@Mixin(Item.class)
public abstract class TooltipInjector {
	
	@Inject(at = @At("RETURN"), method = "buildTooltip")
	public void onInjectTooltip(ItemStack stack, World world, List<TextComponent> list, TooltipContext context, CallbackInfo ci) {
		if (!ModConfig.requiresf3 || context.isAdvanced()) {
			CompoundTag tag = stack.getTag();
			ArrayList<TextComponent> ttip = new ArrayList<TextComponent>(ModConfig.maxLinesShown);
			if (tag!=null) {
				list.add(new StringTextComponent(""));

				if (ModConfig.showDelimiters) {
					ttip.add(new StringTextComponent(TextFormat.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.compress) {
					ttip.add(new StringTextComponent(FORMAT+tag.toString()));
				} else {
					NBTTooltip.unwrapTag(ttip, tag, FORMAT, "", ModConfig.compress?"":"  ");
				}
				if (ModConfig.showDelimiters) {
					ttip.add(new StringTextComponent(TextFormat.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltip.transformTtip(ttip);

				list.addAll(ttip);
			} else {
				list.add(new StringTextComponent(FORMAT+"No NBT tag"));
			}
		}
	}
}
