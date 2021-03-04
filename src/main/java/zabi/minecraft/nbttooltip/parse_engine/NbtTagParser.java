package zabi.minecraft.nbttooltip.parse_engine;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.Tag;
import net.minecraft.text.Text;

public interface NbtTagParser {

	public void parseTagToList(List<Text> list, @Nullable Tag tag, boolean splitlines);
	
}
