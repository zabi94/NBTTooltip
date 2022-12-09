package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import zabi.minecraft.nbttooltip.NBTTooltipClient;
import zabi.minecraft.nbttooltip.config.ClientConfig;

public class BWHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;

	@Override
	public void parseTagToList(List<Component> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(Component.literal("No NBT tag"));
		} else {
			unwrapTag(list, tag, NBTTooltipClient.FORMAT, "", ClientConfig.INSTANCE.compress?"":"  ", split);
		}
	}
	
	private void unwrapTag(List<Component> tooltip, Tag base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof CollectionTag) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Component> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CompoundTag tag = (CompoundTag) base;
		tag.getAllKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof CollectionTag) || (tag.get(s) instanceof CompoundTag);
			if (nested) {
				tooltip.add(Component.literal(pad+s+": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(Component.literal(pad+"}"));
			} else {
				addValueToTooltip(tooltip, tag.get(s), s, pad, splitLongStrings);
			}
		});
	}
	
	private void addListToTooltip(List<Component> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CollectionTag<?> tag = (CollectionTag<?>) base;
		int index = 0;
		for (Tag nbtnext : tag) {
			if (nbtnext instanceof CollectionTag || nbtnext instanceof CompoundTag) {
				tooltip.add(Component.literal(pad + "[" + index + "]: {"));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(Component.literal(pad + "}"));
			} else {
				addValueToTooltip(tooltip, nbtnext, "[" + index + "]", pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Component> tooltip, Tag nbt, String name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(Component.literal(pad+name+": "+ nbt));
		} else {
			int added = 0;
			tooltip.add(Component.literal(pad+name+":"));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				String sb = new StringBuilder()
						.append(ChatFormatting.AQUA).append("|")
						.append(ChatFormatting.RESET).append(pad)
						.append("   ")
						.append(toBeAdded, added, added + nextChunk).toString();
				tooltip.add(Component.literal(sb));
				added += nextChunk;
			}
		}
	}

}
