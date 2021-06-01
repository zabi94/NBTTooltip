package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class NativeParser implements NbtTagParser {
	
	@Override
	public void parseTagToList(List<Text> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(new LiteralText("{}"));
		} else {
			list.add(new LiteralText(unwrap(tag)));
		}
	}
	
	public String unwrap(Tag tag) {
		if (tag instanceof AbstractNumberTag) {
			return stripTypeIdentifier(tag.toString());
		} else if (tag instanceof StringTag) {
			return tag.toString();
		} else if (tag instanceof AbstractListTag) {
			return String.format("[%s%s]", listIdentifier((AbstractListTag<?>) tag), unwrapList((AbstractListTag<?>) tag));
		} else if (tag instanceof CompoundTag) {
			return String.format("{%s}", unwrapCompound((CompoundTag) tag));
		}
		return "";
	}

	private String unwrapCompound(CompoundTag tag) {
		if (tag.getKeys().size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String s:tag.getKeys()) {
			sb.append(s);
			sb.append(":");
			sb.append(unwrap(tag.get(s)));
			sb.append(",");
		}
		return sb.toString().substring(0, sb.toString().length() - 1); //Remove last comma
	}

	private String unwrapList(AbstractListTag<?> tag) {
		if (tag.size() == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tag.size() - 1; i++) {
			sb.append(unwrap((Tag) tag.get(i)));
			sb.append(',');
		}
		sb.append(unwrap((Tag) tag.get(tag.size() - 1)));
		return sb.toString();
	}
	
	private String listIdentifier(AbstractListTag<?> tag) {
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
