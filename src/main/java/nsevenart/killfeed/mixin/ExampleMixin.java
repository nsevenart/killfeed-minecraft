package nsevenart.killfeed.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ExampleMixin {

    // Перехват метода "tick" из класса MinecraftClient
    @Inject(at = @At("HEAD"), method = "tick()V")
    private void onTick(CallbackInfo info) {
        System.out.println("This line is printed by an example mixin during the tick event!");
    }
}
