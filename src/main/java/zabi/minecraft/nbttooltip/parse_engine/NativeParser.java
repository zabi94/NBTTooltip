package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class NativeParser implements NbtTagParser {
	
	@Override
	public void parseTagToList(List<Component> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(Component.literal("{}"));
		} else {
			list.add(Component.literal(unwrap(tag)));
		}
	}

	public String unwrap(Tag tag) {
		if (tag instanceof NumericTag) {
			return stripTypeIdentifier(tag.toString());
		} else if (tag instanceof StringTag) {
			return tag.toString();
		} else if (tag instanceof CollectionTag) {
			return String.format("[%s%s]", listIdentifier((CollectionTag<?>) tag), unwrapList((CollectionTag<?>) tag));
		} else if (tag instanceof CompoundTag) {
			return String.format("{%s}", unwrapCompound((CompoundTag) tag));
		}
		return "";
	}

	private String unwrapCompound(CompoundTag tag) {
		if (tag.getAllKeys().size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s:tag.getAllKeys()) {
			sb.append(s);
			sb.append(":");
			sb.append(unwrap(tag.get(s)));
			sb.append(",");
		}
		return sb.substring(0, sb.toString().length() - 1); //Remove last comma
	}

	private String unwrapList(CollectionTag<?> tag) {
		if (tag.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tag.size() - 1; i++) {
			sb.append(unwrap(tag.get(i)));
			sb.append(',');
		}
		sb.append(unwrap(tag.get(tag.size() - 1)));
		return sb.toString();
	}
	
	private String listIdentifier(CollectionTag<?> tag) {
		if (tag instanceof ByteArrayTag) {
			return "B;";
		} else if (tag instanceof IntArrayTag) {
			return "I;";
		} else if (tag instanceof LongArrayTag) {
			return "L;";
		} else return "";
	}

	private String stripTypeIdentifier(String string) {
		char last = string.charAt(string.length() - 1);
		if (last >= '0' && last <= '9') return string;
		return string.substring(0, string.length() - 1);
	}


}
