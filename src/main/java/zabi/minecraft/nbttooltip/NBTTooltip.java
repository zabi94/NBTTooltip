package zabi.minecraft.nbttooltip;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import de.klotzi111.ktig.api.KTIG;
import de.klotzi111.ktig.api.KeyBindingTriggerPoints;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.SystemToast.Type;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import zabi.minecraft.nbttooltip.config.ModConfig;
import zabi.minecraft.nbttooltip.parse_engine.NbtTagParser;

public class NBTTooltip implements ClientModInitializer {

	public static int ticks = 0;
	public static int line_scrolled = 0;

	public static final String FORMAT = Formatting.ITALIC.toString()+Formatting.DARK_GRAY;

	public static KeyBinding COPY_TO_CLIPBOARD = new KeyBinding("key.nbttooltip.copy", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.category.nbttooltip");
	public static KeyBinding TOGGLE_NBT = new KeyBinding("key.nbttooltip.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.category.nbttooltip");
	public static KeyBinding SCROLL_UP = new KeyBinding("key.nbttooltip.scroll_up", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.category.nbttooltip");
	public static KeyBinding SCROLL_DOWN = new KeyBinding("key.nbttooltip.scroll_down", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.category.nbttooltip");

	public static boolean nbtKeyToggled = false;
	public static boolean nbtKeyPressed = false;

	public static boolean manual_scroll = false;
	public static ItemStack last_tooltip_stack = null;
	
	private static void registerKeyBinding(KeyBinding keyBinding) {
		KeyBindingHelper.registerKeyBinding(keyBinding);
		KTIG.registerKeyBindingForTriggerPoints(keyBinding, KeyBindingTriggerPoints.MAIN_WINDOW_BIT | KeyBindingTriggerPoints.NO_VANILLA_BIT);
	}
	
	@Override
	public void onInitializeClient() {
		ModConfig.init();
		ClientTickEvents.END_CLIENT_TICK.register(NBTTooltip::clientTick);
		ItemTooltipCallback.EVENT.register(NBTTooltip::onInjectTooltip);
		registerKeyBinding(COPY_TO_CLIPBOARD);
		registerKeyBinding(TOGGLE_NBT);
		registerKeyBinding(SCROLL_DOWN);
		registerKeyBinding(SCROLL_UP);
	}

	public static void clientTick(MinecraftClient mc) {
		
		if(manual_scroll && Screen.hasShiftDown()) {
			// activate the auto scroll again when pressing shift
			manual_scroll = false;
		}
		
		int scroll_down_count = KTIG.getPressedCount(SCROLL_DOWN);
		int scroll_up_count = KTIG.getPressedCount(SCROLL_UP);
		
		//for toggle
		if (KTIG.wasKeyBindingPressed(TOGGLE_NBT)) {
			nbtKeyToggled = !nbtKeyToggled;
		}
		//for press
		nbtKeyPressed = isPressed(mc, TOGGLE_NBT);

		//fast scroll now comes from the repeat events of the keys
		int scroll_count = scroll_down_count - scroll_up_count;
		
		int line_scrolled_before = line_scrolled;
		//add scroll amount but make sure we never go negativ
		line_scrolled = Math.max(line_scrolled + scroll_count, 0);

		if (line_scrolled_before != line_scrolled) {
			//we did scroll
			manual_scroll = true;
		}

		
		if (!manual_scroll && !Screen.hasShiftDown() && scroll_down_count == 0 && scroll_up_count == 0) {
			ticks++;
			int factor = 1;
			if (Screen.hasAltDown()) {
				factor = 4;
			}
			if (ticks >= ModConfig.INSTANCE.ticksBeforeScroll/factor) {
				ticks = 0;
				if (ModConfig.INSTANCE.ticksBeforeScroll > 0) {
					line_scrolled++;
				}
			}
		}
		
	}

	private static boolean isPressed(MinecraftClient mc, KeyBinding key) {
		//mc is now unused can could be removed from the method signature
		//or isPressed() could be called directly on the keybinding instead of calling this methods
		
		//this is not only less code and easier to read but most importantly it works as it should
		//the old code caused GL ERRORs every tick when the keybinding was bound to a mouse button (or to something else from other mods)
		//the keybinding normally only gets triggered when the key is pressed while NO gui is currently active (so this would not work)...
		//... But we use KTIG. This will allow the keybindings to be triggered even while having a screen (gui) open
		return key.isPressed();
	}

	public static ArrayList<Text> transformTtip(ArrayList<Text> ttip, int lines) {
		ArrayList<Text> newttip = new ArrayList<>(lines);
		if (ModConfig.INSTANCE.showSeparator) {
			newttip.add(new LiteralText("- NBTTooltip -"));
		}
		if (ttip.size()>lines) {
			if (lines+line_scrolled>ttip.size()) {
				if (manual_scroll) {
					line_scrolled = ttip.size() - lines;
				} else {
					line_scrolled = 0;
				}
			}
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

	public static void onInjectTooltip(ItemStack stack, TooltipContext context, List<Text> list) {
		handleClipboardCopy(stack);
		if (ModConfig.INSTANCE.triggerType.shouldShowTooltip(context)) {
			if(last_tooltip_stack != stack) {
				//we now show the tooltip of a different item
				manual_scroll = false;
				last_tooltip_stack = stack;
			}
			
			int lines = ModConfig.INSTANCE.maxLinesShown;
			if (ModConfig.INSTANCE.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(new LiteralText(""));
			}
			NbtCompound tag = stack.getTag();
			ArrayList<Text> ttip = new ArrayList<>(lines);
			if (tag!=null) {
				if (ModConfig.INSTANCE.showDelimiters) {
					ttip.add(new LiteralText(Formatting.DARK_PURPLE+" - nbt start -"));
				}
				if (ModConfig.INSTANCE.compress) {
					ttip.add(new LiteralText(FORMAT+ tag));
				} else {
					getRenderingEngine().parseTagToList(ttip, tag, ModConfig.INSTANCE.splitLongLines);
				}
				if (ModConfig.INSTANCE.showDelimiters) {
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
			if (KTIG.wasKeyBindingPressed(COPY_TO_CLIPBOARD)) {
				copyToClipboard(stack, mc);
			}
		}
	}

	private static void copyToClipboard(ItemStack stack, MinecraftClient mc) {
		StringBuilder sb = new StringBuilder();
		String name = I18n.translate(stack.getTranslationKey());
		ArrayList<Text> nbtData = new ArrayList<>();
		getCopyingEngine().parseTagToList(nbtData, stack.getTag(), false);
		nbtData.forEach(t -> {
			sb.append(t.asString().replaceAll("§[0-9a-gk-or]", ""));
			sb.append("\n");
		});
		try {
			mc.keyboard.setClipboard(sb.toString());
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, new TranslatableText("nbttooltip.copied_to_clipboard"), new TranslatableText("nbttooltip.object_details", name)));
		} catch (Exception e) {
			mc.getToastManager().add(new SystemToast(Type.TUTORIAL_HINT, new TranslatableText("nbttooltip.copy_failed"), new LiteralText(e.getMessage())));
			e.printStackTrace();
		}
	}
	
	private static NbtTagParser getRenderingEngine() {
		return ModConfig.INSTANCE.tooltipEngine.getEngine();
	}
	
	private static NbtTagParser getCopyingEngine() {
		return ModConfig.INSTANCE.copyingEngine.getEngine();
	}

}
