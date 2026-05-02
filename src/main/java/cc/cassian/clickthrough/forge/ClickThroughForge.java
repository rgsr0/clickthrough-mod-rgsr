package cc.cassian.clickthrough.forge;

import cc.cassian.clickthrough.ClickThrough;
import cc.cassian.clickthrough.config.ModConfig;
import cc.cassian.clickthrough.config.ModLists;
import cc.cassian.clickthrough.helpers.ModHelpers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.client.registry.ClientRegistry;

// CLIENT-SIDE ONLY MOD — does not need to be on the server
@Mod(ClickThrough.MOD_ID)
public class ClickThroughForge {

    public ClickThroughForge() {
        ClickThrough.CONFIG = ModConfig.load(FMLPaths.CONFIGDIR.get());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(ClickThrough.TOGGLE_KEY);
        ModLists.loadLists();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ModHelpers.handleKeybind();
        }
    }
}
