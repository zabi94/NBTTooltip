package zabi.minecraft.nbttooltip.config;

import java.util.function.BiFunction;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import zabi.minecraft.nbttooltip.NBTTooltip;

public enum TriggerType {

	F3H((ctx, type) -> type.isAdvanced()),
	ALWAYS_ON((ctx, type) -> true),
	TOGGLE_ON_KEY((ctx, type) -> NBTTooltip.nbtKeyToggled),
	SHOW_ON_KEY((ctx, type) -> NBTTooltip.nbtKeyPressed);

	private BiFunction<Item.TooltipContext, TooltipFlag, Boolean> test;

	TriggerType(BiFunction<Item.TooltipContext, TooltipFlag, Boolean> check) {
		this.test = check;
	}

	public boolean shouldShowTooltip(Item.TooltipContext context, TooltipFlag type) {
		return this.test.apply(context, type);
	}

}
