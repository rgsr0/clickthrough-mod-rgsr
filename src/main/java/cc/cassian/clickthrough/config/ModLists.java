package cc.cassian.clickthrough.config;

import cc.cassian.clickthrough.ClickThrough;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class ModLists {
    public static ArrayList<Block> containers = new ArrayList<>();

    public static void loadLists() {
        containers = new ArrayList<>();
        for (String entry : ClickThrough.CONFIG.containers) {
            ResourceLocation rl = ResourceLocation.tryParse(entry);
            if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) {
                Block block = ForgeRegistries.BLOCKS.getValue(rl);
                if (block != null) {
                    containers.add(block);
                }
            }
        }
    }
}
