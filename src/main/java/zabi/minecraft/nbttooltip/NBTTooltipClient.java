package zabi.minecraft.nbttooltip;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.lwjgl.glfw.GLFW;
import zabi.minecraft.nbttooltip.config.ClientConfig;
import zabi.minecraft.nbttooltip.parse_engine.NbtTagParser;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "nbttooltip", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NBTTooltipClient {

	public static int ticks = 0;
	public static int line_scrolled = 0;

	public static final String FORMAT = ChatFormatting.ITALIC.toString()+ChatFormatting.DARK_GRAY;

	public static final int WAITTIME_BEFORE_FAST_SCROLL = 10;

	public static KeyMapping COPY_TO_CLIPBOARD = new KeyMapping("key.nbttooltip.copy", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.category.nbttooltip");
	public static KeyMapping TOGGLE_NBT = new KeyMapping("key.nbttooltip.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, "key.category.nbttooltip");
	public static KeyMapping SCROLL_UP = new KeyMapping("key.nbttooltip.scroll_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, "key.category.nbttooltip");
	public static KeyMapping SCROLL_DOWN = new KeyMapping("key.nbttooltip.scroll_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "key.category.nbttooltip");

	public static boolean flipflop_key_copy = false;
	public static boolean flipflop_key_toggle = false;
	
	public static boolean nbtKeyToggled = false;
	public static boolean nbtKeyPressed = false;

	public static int fast_scroll_warmup = 0;
	public static int autoscroll_locks = 0;

	@SubscribeEvent
	public static void onConstructMod(final FMLConstructModEvent evt) {
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.INSTANCE.buildSpec());
		registerHandlers();
	}

	private static void registerHandlers() {
		MinecraftForge.EVENT_BUS.addListener((final TickEvent.ClientTickEvent evt) -> {
			if (evt.phase == TickEvent.Phase.END) {
				clientTick(Minecraft.getInstance());
			}
		});
		MinecraftForge.EVENT_BUS.addListener((final ItemTooltipEvent evt) -> {
			onInjectTooltip(evt.getItemStack(), evt.getFlags(), evt.getToolTip());
		});
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent evt) {
		evt.register(COPY_TO_CLIPBOARD);
		evt.register(TOGGLE_NBT);
		evt.register(SCROLL_DOWN);
		evt.register(SCROLL_UP);
	}

	@SubscribeEvent
	public static void onModConfig(final ModConfigEvent evt) {
		if (evt.getConfig().getModId().equals("nbttooltip")) {
			if (evt.getConfig().getType() == ModConfig.Type.CLIENT) {
				ClientConfig.INSTANCE.sync();
			}
		}
	}

	public static void clientTick(Minecraft mc) {
		
		if (autoscroll_locks > 0) autoscroll_locks--;
		
		if (!Screen.hasShiftDown() && !isPressed(mc, SCROLL_DOWN) && !isPressed(mc, SCROLL_UP) && autoscroll_locks == 0) {
			NBTTooltipClient.ticks++;
			int factor = 1;
			if (Screen.hasAltDown()) {
				factor = 4;
			}
			if (NBTTooltipClient.ticks >= ClientConfig.INSTANCE.ticksBeforeScroll/factor) {
				NBTTooltipClient.ticks = 0;
				if (ClientConfig.INSTANCE.ticksBeforeScroll > 0) {
					NBTTooltipClient.line_scrolled++;
				}
			}
		}
		
		if (isPressed(mc, TOGGLE_NBT)) {
			if (!flipflop_key_toggle) {
				nbtKeyToggled = !nbtKeyToggled;
			}
			flipflop_key_toggle = true;
			nbtKeyPressed = true;
		} else {
			flipflop_key_toggle = false;
			nbtKeyPressed = false;
		}


		if (!isPressed(mc, SCROLL_DOWN) && isPressed(mc, SCROLL_UP) && line_scrolled > 0 && cooldownTimeAcceptable()) {
			line_scrolled--;
		}

		if (isPressed(mc, SCROLL_DOWN) && !isPressed(mc, SCROLL_UP) && cooldownTimeAcceptable()) {
			line_scrolled++;
		}

		if (isPressed(mc, SCROLL_DOWN) || isPressed(mc, SCROLL_UP)) {
			if (fast_scroll_warmup < WAITTIME_BEFORE_FAST_SCROLL) fast_scroll_warmup++;
			autoscroll_locks = 2;
		} else {
			fast_scroll_warmup = 0;
		}
	}

	private static boolean cooldownTimeAcceptable() {
		return fast_scroll_warmup == 0 || fast_scroll_warmup >= WAITTIME_BEFORE_FAST_SCROLL;
	}

	private static boolean isPressed(Minecraft mc, KeyMapping key) {
		return !key.isUnbound() && InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.getKey(key.saveString()).getValue());
	}

	public static ArrayList<Component> transformTtip(ArrayList<Component> ttip, int lines) {
		ArrayList<Component> newttip = new ArrayList<>(lines);
		if (ClientConfig.INSTANCE.showSeparator) {
			newttip.add(Component.literal("- NBTTooltip -"));
		}
		if (ttip.size()>lines) {
			if (lines+line_scrolled>ttip.size()) {
				if (isPressed(Minecraft.getInstance(), SCROLL_DOWN)) {
					line_scrolled = ttip.size() - lines;
				} else {
					line_scrolled = 0;
				}
			}
			for (int i = 0; i < lines; i++) {
				newttip.add(ttip.get(i+line_scrolled));
			}
		} else {
			line_scrolled = 0;
			newttip.addAll(ttip);
		}
		return newttip;
	}

	public static void onInjectTooltip(ItemStack stack, TooltipFlag context, List<Component> list) {
		handleClipboardCopy(stack);
		if (ClientConfig.INSTANCE.triggerType.shouldShowTooltip(context)) {
			if (autoscroll_locks > 0) autoscroll_locks = 2;
			int lines = ClientConfig.INSTANCE.maxLinesShown;
			if (ClientConfig.INSTANCE.ctrlSuppressesRest && Screen.hasControlDown()) {
				lines += list.size();
				list.clear();
			} else {
				list.add(Component.literal(""));
			}
			CompoundTag tag = stack.getTag();
			ArrayList<Component> ttip = new ArrayList<>(lines);
			if (tag!=null) {
				if (ClientConfig.INSTANCE.showDelimiters) {
					ttip.add(Component.literal(ChatFormatting.DARK_PURPLE+" - nbt start -"));
				}
				if (ClientConfig.INSTANCE.compress) {
					ttip.add(Component.literal(FORMAT+ tag));
				} else {
					getRenderingEngine().parseTagToList(ttip, tag, ClientConfig.INSTANCE.splitLongLines);
				}
				if (ClientConfig.INSTANCE.showDelimiters) {
					ttip.add(Component.literal(ChatFormatting.DARK_PURPLE+" - nbt end -"));
				}
				ttip = NBTTooltipClient.transformTtip(ttip, lines);
				list.addAll(ttip);
			} else {
				list.add(Component.literal(FORMAT+"No NBT tag"));
			}
		}
	}

	private static void handleClipboardCopy(ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (isPressed(mc, COPY_TO_CLIPBOARD)) {
				if (!flipflop_key_copy) {
					flipflop_key_copy = true;
					copyToClipboard(stack, mc);
				}
			} else {
				flipflop_key_copy = false;
			}
		}
	}

	private static void copyToClipboard(ItemStack stack, Minecraft mc) {
		StringBuilder sb = new StringBuilder();
		String name = I18n.get(stack.getDescriptionId());
		ArrayList<Component> nbtData = new ArrayList<>();
		getCopyingEngine().parseTagToList(nbtData, stack.getTag(), false);
		nbtData.forEach(t -> {
			sb.append(t.getString().replaceAll("§[0-9a-gk-or]", ""));
			sb.append("\n");
		});
		try {
			mc.keyboardHandler.setClipboard(sb.toString());
			mc.getToasts().addToast(new SystemToast(SystemToastIds.TUTORIAL_HINT, Component.translatable("nbttooltip.copied_to_clipboard"), Component.translatable("nbttooltip.object_details", name)));
		} catch (Exception e) {
			mc.getToasts().addToast(new SystemToast(SystemToastIds.TUTORIAL_HINT, Component.translatable("nbttooltip.copy_failed"), Component.literal(e.getMessage())));
			e.printStackTrace();
		}
	}
	
	private static NbtTagParser getRenderingEngine() {
		return ClientConfig.INSTANCE.tooltipEngine.getEngine();
	}
	
	private static NbtTagParser getCopyingEngine() {
		return ClientConfig.INSTANCE.copyingEngine.getEngine();
	}

}
