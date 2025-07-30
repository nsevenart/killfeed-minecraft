package nsevenart.killfeed;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.fabricmc.api.ModInitializer;
import nsevenart.killfeed.client.KillFeedClient;


public class KillFeed implements ModInitializer {

    @Override
    public void onInitialize() {
        KillfeedManager.register();
        System.out.println("KillFeed Mod initialized!");
    }

    public static void displayKillfeedMessage(World world, String killerName, String victimName, boolean isPlayer) {
        if (world.isClient) {
            KillFeedClient.addKillfeedMessage(killerName, victimName, isPlayer);
        }
    }
}