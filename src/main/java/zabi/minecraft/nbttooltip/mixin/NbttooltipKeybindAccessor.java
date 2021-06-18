package zabi.minecraft.nbttooltip.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
public interface NbttooltipKeybindAccessor {
	
	@Accessor("boundKey")
	InputUtil.Key getBoundKey();
	
}
