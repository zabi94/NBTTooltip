package zabi.minecraft.nbttooltip.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
public interface NbttooltipKeybindAccessor {
	
	@Accessor("boundKey")
	public InputUtil.Key getBoundKey();
	
}
