package com.kamesuta.mc.signpic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Timer;

import com.kamesuta.mc.signpic.entry.EntryManager;
import com.kamesuta.mc.signpic.entry.EntrySlot;
import com.kamesuta.mc.signpic.entry.content.ContentManager;
import com.kamesuta.mc.signpic.gui.GuiTask;
import com.kamesuta.mc.signpic.gui.OverlayFrame;
import com.kamesuta.mc.signpic.handler.KeyHandler;
import com.kamesuta.mc.signpic.handler.SignHandler;
import com.kamesuta.mc.signpic.http.Communicator;
import com.kamesuta.mc.signpic.http.ICommunicate;
import com.kamesuta.mc.signpic.http.ICommunicateCallback;
import com.kamesuta.mc.signpic.information.Informations;
import com.kamesuta.mc.signpic.render.CustomItemSignRenderer;
import com.kamesuta.mc.signpic.render.SignPicRender;
import com.kamesuta.mc.signpic.state.Progressable;
import com.kamesuta.mc.signpic.state.State;

import net.minecraft.client.resources.model.IBakedModel;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class CoreHandler {
	public final @Nonnull Config configHandler = Config.getConfig();
	public final @Nonnull KeyHandler keyHandler = new KeyHandler();
	public final @Nonnull SignHandler signHandler = new SignHandler();
	public final @Nonnull EntryManager signEntryManager = EntryManager.instance;
	public final @Nonnull ContentManager contentManager = ContentManager.instance;
	public final @Nonnull SignPicRender renderHandler = new SignPicRender();
	public final @Nonnull OverlayFrame overlayHandler = OverlayFrame.instance;
	public final @Nonnull Informations informationHandler = Informations.instance;
	public final @Nonnull Apis apiHandler = Apis.instance;

	public void init() {
		// FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		KeyHandler.init();
		SignHandler.init();
		this.informationHandler.init();
		this.apiHandler.init();
	}

	@SubscribeEvent
	public void onKeyInput(final @Nonnull InputEvent event) {
		this.keyHandler.onKeyInput(event);
	}

	@SubscribeEvent
	public void onRenderTick(final @Nonnull TickEvent.RenderTickEvent event) {
		Timer.tick();
	}

	@SubscribeEvent
	public void onSign(final @Nonnull GuiOpenEvent event) {
		this.signHandler.onSign(event);
	}

	@SubscribeEvent
	public void onClick(final @Nonnull MouseEvent event) {
		this.signHandler.onClick(event);
	}

	@SubscribeEvent
	public void onTooltip(final @Nonnull ItemTooltipEvent event) {
		this.signHandler.onTooltip(event);
	}

	@SubscribeEvent
	public void onRender(final @Nonnull RenderWorldLastEvent event) {
		this.renderHandler.onRender(event);
	}

	@SubscribeEvent()
	public void onDraw(final @Nonnull RenderGameOverlayEvent.Post event) {
		this.renderHandler.onDraw(event);
		this.overlayHandler.onDraw(event);
	}

	@SubscribeEvent()
	public void onDraw(final @Nonnull GuiScreenEvent.DrawScreenEvent.Post event) {
		this.overlayHandler.onDraw(event);
		this.signHandler.onDraw(event);
	}

	@SubscribeEvent
	public void onText(final @Nonnull RenderGameOverlayEvent.Text event) {
		this.renderHandler.onText(event);
	}

	@SubscribeEvent
	public void onConfigChanged(final @Nonnull ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		this.configHandler.onConfigChanged(eventArgs);
	}

	@SubscribeEvent
	public void onTick(final @Nonnull ClientTickEvent event) {
		if (event.phase==Phase.END) {
			Client.startSection("signpic_load");
			debugKey();
			this.signEntryManager.onTick();
			this.signHandler.onTick();
			this.contentManager.onTick();
			this.overlayHandler.onTick(event);
			this.informationHandler.onTick(event);
			EntrySlot.Tick();
			Client.endSection();
		}
	}

	@SubscribeEvent
	public void onModelBakeEvent(final @Nonnull ModelBakeEvent event) {
		final IBakedModel object = event.modelRegistry.getObject(CustomItemSignRenderer.modelResourceLocation);
		final CustomItemSignRenderer atlas6 = new CustomItemSignRenderer(object);
		event.modelRegistry.putObject(CustomItemSignRenderer.modelResourceLocation, atlas6);
	}

	private boolean debugKey;

	private void debugKey() {
		if (Keyboard.isKeyDown(Keyboard.KEY_I)&&Keyboard.isKeyDown(Keyboard.KEY_O)&&Keyboard.isKeyDown(Keyboard.KEY_P)) {
			if (!this.debugKey)
				debug();
			this.debugKey = true;
		} else
			this.debugKey = false;
	}

	private void debug() {
		// Client.openEditor();
		// Reference.logger.info("try to delete: "+Client.location.modFile.getName());
		// Client.deleteMod();
		// Client.notice("Debug Message!", 3f);
		final DebugCommunicate debug = new DebugCommunicate();
		debug.getState().getMeta().put(GuiTask.HighlightPanel, true);
		Communicator.instance.communicate(debug);
	}

	private static class DebugCommunicate implements ICommunicate, Progressable {
		private @Nonnull State state = new State();
		{
			this.state.setName("Debug Progress").getProgress().setOverall(10);
		}

		@Override
		public void cancel() {
		}

		@Override
		public @Nonnull State getState() {
			return this.state;
		}

		@Override
		public void communicate() {
			try {
				for (int i = 0; i<10; i++) {
					Thread.sleep(100);
					this.state.getProgress().setDone(i+1);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void setCallback(final @Nullable ICommunicateCallback callback) {
		}
	}
}
