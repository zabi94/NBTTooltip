package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import zabi.minecraft.nbttooltip.config.ClientConfig;

public class ColoredHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;
	
	private static final ChatFormatting LISTINDEX = ChatFormatting.GREEN;
	private static final ChatFormatting STRING = ChatFormatting.LIGHT_PURPLE;
	private static final ChatFormatting STRUCTURE = ChatFormatting.GRAY;
	private static final ChatFormatting TAGNAME = ChatFormatting.GOLD;

	@Override
	public void parseTagToList(List<Component> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(Component.literal("No NBT tag").withStyle(ChatFormatting.DARK_GRAY));
		} else {
			unwrapTag(list, tag, "", "", ClientConfig.INSTANCE.compress?"":"  ", split);
		}
	}
	
	private void unwrapTag(List<Component> tooltip, Tag base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof CollectionTag) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, Component.literal(tagName).withStyle(TAGNAME), pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Component> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CompoundTag tag = (CompoundTag) base;
		tag.getAllKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof CollectionTag) || (tag.get(s) instanceof CompoundTag);
			if (nested) {
				Component subtreeName = Component.literal(s).withStyle(TAGNAME);
				Component intro = Component.translatable("%s%s%s", pad, subtreeName, Component.literal(": {").withStyle(STRUCTURE));
				tooltip.add(intro);
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(Component.literal(pad+"}").withStyle(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, tag.get(s), Component.literal(s).withStyle(TAGNAME), pad, splitLongStrings);
			}
		});
	}
	
	private void addListToTooltip(List<Component> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CollectionTag<?> tag = (CollectionTag<?>) base;
		int index = 0;
		for (Tag nbtnext : tag) {
			if (nbtnext instanceof CollectionTag || nbtnext instanceof CompoundTag) {
				tooltip.add(Component.translatable("%s [%s]: {", pad, Component.literal("" + index).withStyle(LISTINDEX)).withStyle(STRUCTURE));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(Component.literal(pad + "}").withStyle(STRUCTURE));
			} else {
				addValueToTooltip(tooltip, nbtnext, Component.translatable("[%s]", Component.literal("" + index).withStyle(LISTINDEX))
						.withStyle(STRUCTURE), pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Component> tooltip, Tag nbt, Component name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(Component.translatable(pad+"%s: %s", name, Component.literal(nbt.toString()).withStyle(STRING)));
		} else {
			Component separator = Component.literal("|").withStyle(ChatFormatting.AQUA);
			int added = 0;
			tooltip.add(Component.translatable(pad+"%s:", name));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				Component chunk = Component.literal(toBeAdded.substring(added, added+nextChunk)).withStyle(STRING);
				tooltip.add(Component.translatable("%s"+pad+"   %s", separator, chunk));
				added += nextChunk;
			}
		}
	}

}
