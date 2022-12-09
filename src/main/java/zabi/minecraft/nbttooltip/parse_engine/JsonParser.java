package zabi.minecraft.nbttooltip.parse_engine;

import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import zabi.minecraft.nbttooltip.config.ClientConfig;

public class JsonParser implements NbtTagParser {
	
	@Override
	public void parseTagToList(List<Component> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(Component.literal("{}"));
		} else {
			list.add(Component.literal("{"));
			unwrapTag(list, tag, ClientConfig.INSTANCE.compress?"":"  ", "", ClientConfig.INSTANCE.compress?"":"  ");
			list.add(Component.literal("}"));
		}
	}
	
	private void unwrapTag(List<Component> tooltip, Tag base, String pad, String tagName, String padIncrement) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement);
		} else if (base instanceof CollectionTag) {
			addListToTooltip(tooltip, base, pad, padIncrement);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, false);
		}
	}
	
	private void addCompoundToTooltip(List<Component> tooltip, Tag base, String pad, String padIncrement) {
		CompoundTag tag = (CompoundTag) base;
		Iterator<String> iter = tag.getAllKeys().iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			if (tag.get(s) instanceof CompoundTag) {
				tooltip.add(Component.literal(pad+'"'+s+"\": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
				tooltip.add(Component.literal(pad+"}"+(iter.hasNext()?",":"")));
			} else if (tag.get(s) instanceof CollectionTag) {
				tooltip.add(Component.literal(pad+'"'+s+"\": ["));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement);
				tooltip.add(Component.literal(pad+"]"+(iter.hasNext()?",":"")));
			} else {
				addNamedValueToTooltip(tooltip, tag.get(s), s, pad, iter.hasNext());
			}
		}
	}
	
	private void addListToTooltip(List<Component> tooltip, Tag base, String pad, String padIncrement) {
		CollectionTag<?> tag = (CollectionTag<?>) base;
		Iterator<? extends Tag> iter = tag.iterator();
		while (iter.hasNext()) {
			Tag nbtnext = iter.next();
			if (nbtnext instanceof CompoundTag) {
				tooltip.add(Component.literal(pad + "{"));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
				tooltip.add(Component.literal(pad+"}"+(iter.hasNext()?",":"")));
			} else if (nbtnext instanceof CollectionTag) {
				tooltip.add(Component.literal(pad + "["));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement);
				tooltip.add(Component.literal(pad+"]"+(iter.hasNext()?",":"")));
			} else {
				addValueToTooltip(tooltip, nbtnext, "", pad, iter.hasNext());
			}
		}
	}
	
	private static void addNamedValueToTooltip(List<Component> tooltip, Tag nbt, String name, String pad, boolean addComma) {
		String cleanString = nbt instanceof StringTag ? escapeChars(nbt.toString()) : stripTypeIdentifiers(nbt.toString());
		tooltip.add(Component.literal(pad+'"'+name+"\": "+cleanString+(addComma ? "," : "")));
	}
	
	private static void addValueToTooltip(List<Component> tooltip, Tag nbt, String name, String pad, boolean addComma) {
		String cleanString = nbt instanceof StringTag ? escapeChars(nbt.toString()) : stripTypeIdentifiers(nbt.toString());
		tooltip.add(Component.literal(pad+cleanString+(addComma ? "," : "")));
	}
	
	private static String stripTypeIdentifiers(String string) {
		char last = string.charAt(string.length() - 1);
		if (last >= '0' && last <= '9') return string;
		return string.substring(0, string.length() - 1);
	}

	private static String escapeChars(String in) {
		if ((in.charAt(0) == '"' && in.charAt(in.length()-1) == '"') || (in.charAt(0) == '\'' && in.charAt(in.length()-1) == '\'')) {
			in = in.substring(1, in.length() - 1);
		}
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (c == '"') {
				sb.append('\\');
				sb.append('"');
			} else {
				sb.append(c);
			}
		}
		sb.append('"');
		return sb.toString();
	}

}
