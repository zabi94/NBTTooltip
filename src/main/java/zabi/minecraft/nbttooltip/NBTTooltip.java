package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.SystemToast.Type;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.mixin.NbttooltipKeybindAccessor;

public class NBTTooltip implements ClientModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;
	public static int line_split_threshold = 30;

	public static final String FORMAT = Formatting.ITALIC.toString()+Formatting.DARK_GRAY;

	public static KeyBinding COPY_TO_CLIPBOARD = new KeyBinding("key.nbttooltip.copy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.category.nbttooltip");
	public static boolean flipflop_key_copy = false;

	@Override
	public void onInitializeClient() {
		ModConfig.init();
		ClientTickEvents.END_CLIENT_TICK.register(NBTTooltip::clientTick);
		ItemTooltipCallback.EVENT.register(NBTTooltip::onInjectTooltip);
		KeyBindingHelper.registerKeyBinding(COPY_TO_CLIPBOARD);
	}

	public static void clientTick(MinecraftClient mc) {
		if (!Screen.hasShiftDown()) {
			NBTTooltip.ticks++;
			int factor = 1;
			if (Screen.hasAltDown()) {
				factor = 4;
			}
			if (NBTTooltip.ticks >= ModConfig.ticksBeforeScroll/factor) {
				NBTTooltip.ticks = 0;
				NBTTooltip.line_scrolled++;
			}
		}
	}

	public static ArrayList<Text> transformTtip(ArrayList<Text> ttip, int lines) {
		ArrayList<Text> newttip = new ArrayList<Text>(lines);
		if (ModConfig.showSeparator) {
			newttip.add(new LiteralText("- NBTTooltip -"));
		}
		if (ttip.size()>lines) {
			if (lines+line_scrolled>ttip.size()) line_scrolled = 0;
			for (int i = 0; i < lines; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
			return newttip;
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
			return newttip;
		}
	}

	public static void unwrapTag(List<Text> tooltip, Tag base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof AbstractListTag) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, splitLongStrings);
		}
	}

	private static void addCompoundToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CompoundTag tag = (CompoundTag) base;
		tag.getKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof AbstractListTag) || (tag.get(s) instanceof CompoundTag);
			if (nested) {
				tooltip.add(new LiteralText(pad+s+": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad+"}"));
			} else {
				addValueToTooltip(tooltip, tag.get(s), s, pad, splitLongStrings);
			}
		});
	}

	private static void addListToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractListTag<?> tag = (AbstractListTag<?>) base;
		int index = 0;
		Iterator<? extends Tag> iter = tag.iterator();
		while (iter.hasNext()) {
			Tag nbtnext = iter.next();
			if (nbtnext instanceof AbstractListTag || nbtnext instanceof CompoundTag) {
				tooltip.add(new LiteralText(pad + "["+index+"]: {"));
				unwrapTag(tooltip, nbtnext, pad+padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad+"}"));
			} else {
				addValueToTooltip(tooltip, nbtnext, "["+index+"]", pad, splitLongStrings);
			}
			index++;
		}
	}

	private static void addValueToTooltip(List<Text> tooltip, Tag nbt, String name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(new LiteralText(pad+name+": "+nbt.toString()));
		} else {
			int added = 0;
			tooltip.add(new LiteralText(pad+name+":"));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				StringBuilder sb = new StringBuilder(Formatting.AQUA.toString())
					.append("|")
					.append(Formatting.RESET.toString())
					.append(pad)
					.append("   ")
					.append(toBeAdded.substring(added, added+nextChunk));
				tooltip.add(new LiteralText(sb.toString()));
				added += nextChunk;
			}
		}
	}

	public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
		handleClipboardCopy(stack);
		if (!ModConfig.requiresf3 || context.isAdvanced()) {
			int lines = ModConfig.maxLinesShown;
			if (ModConfig.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(new LiteralText(""));
			}
			CompoundTag tag = stack.getTag();
			ArrayList<Text> ttip = new ArrayList<Text>(lines);
			if (tag!=null) {
				if (ModConfig.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.compress) {
					ttip.add(new LiteralText(FORMAT+tag.toString()));
				} else {
					NBTTooltip.unwrapTag(ttip, tag, FORMAT, "", ModConfig.compress?"":"  ", true);
				}
				if (ModConfig.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltip.transformTtip(ttip, lines);
				list.addAll(ttip);
			} else {
				list.add(new LiteralText(FORMAT+"No NBT tag"));
			}
		}
	}

	private static void handleClipboardCopy(ItemStack stack) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.currentScreen != null) {
			boolean pressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), ((NbttooltipKeybindAccessor) COPY_TO_CLIPBOARD).getBoundKey().getCode());
			if (pressed) {
				if (!flipflop_key_copy) {
					flipflop_key_copy = true;
					copyToClipboard(stack, mc);
				}
			} else {
				flipflop_key_copy = false;
			}
		}
	}

	private static void copyToClipboard(ItemStack stack, MinecraftClient mc) {
		StringBuilder sb = new StringBuilder();
		String name = I18n.translate(stack.getTranslationKey());
		if (stack.getTag() == null) {
			sb.append("{}\n");
		} else {
			ArrayList<Text> nbtData = new ArrayList<>();
			unwrapTag(nbtData, stack.getTag(), "", "Item NBT", "\t", false);
			nbtData.forEach(t -> {
				sb.append(t.asString());
				sb.append("\n");
			});
		}
		try {
			mc.keyboard.setClipboard(sb.toString());
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, new TranslatableText("nbttooltip.copied_to_clipboard"), new TranslatableText("nbttooltip.object_details", name)));
		} catch (Exception e) {
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, new TranslatableText("nbttooltip.copy_failed"), new LiteralText(e.getMessage())));
			e.printStackTrace();
		}
	}

}
