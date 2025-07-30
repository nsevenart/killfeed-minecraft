package nsevenart.killfeed.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KillFeedClient implements ClientModInitializer {

    private static final List<KillFeedMessage> killfeedMessages = new ArrayList<>();
    private static final int DISPLAY_TIME = 500; // 100 тиков = 5 секунд (20 тиков/сек)
    private static final Identifier KILLFEED_PACKET_ID = new Identifier("killfeed", "killfeed_packet");

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::onHudRender);

        // Регистрация обработчика на клиенте для получения данных с сервера
        ClientPlayNetworking.registerGlobalReceiver(KILLFEED_PACKET_ID, (client, handler, buf, responseSender) -> {
            String killerName = buf.readString(32767);
            String victimName = buf.readString(32767);
            boolean isRelevantToPlayer = buf.readBoolean(); // Килл связан с текущим игроком?

            client.execute(() -> addKillfeedMessage(killerName, victimName, isRelevantToPlayer));
        });
    }

    public static void addKillfeedMessage(String killer, String victim, boolean isRelevantToPlayer) {
        String messageText = killer + " ✖ " + victim;
        int color = 0xFFFFFF; // Белый цвет текста
        killfeedMessages.add(new KillFeedMessage(Text.literal(messageText), DISPLAY_TIME, color, isRelevantToPlayer));
    }

    private void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int y = 10; // Положение по оси Y

        Iterator<KillFeedMessage> iterator = killfeedMessages.iterator();
        while (iterator.hasNext()) {
            KillFeedMessage message = iterator.next();

            int textWidth = client.textRenderer.getWidth(message.text);
            int x = client.getWindow().getScaledWidth() - 10 - textWidth;

            // Рисуем задний фон
            int backgroundColor = MathHelper.packRgb(0, 0, 0) | (int)(0.6 * 255) << 24; // Черный фон с прозрачностью 60%
            context.fill(x - 5, y - 2, x + textWidth + 5, y + 10, backgroundColor);

            // Если событие связано с игроком, рисуем красную рамку
            if (message.isRelevantToPlayer) {
                drawRect(context, x - 6, y - 3, x + textWidth + 6, y + 12, 0xFFFF5555);
            }

            // Рисуем текст
            context.drawTextWithShadow(client.textRenderer, message.text, x, y, message.color);
            y += 20; // Увеличиваем отступ для следующего сообщения

            // Уменьшаем таймер и удаляем сообщение, если время истекло
            message.timeLeft--;
            if (message.timeLeft <= 0) {
                iterator.remove();
            }
        }
    }

    private void drawRect(DrawContext context, int left, int top, int right, int bottom, int color) {
        // Рисуем рамку по краям текста
        context.fill(left, top, right, top + 1, color); // Верхняя линия
        context.fill(left, bottom - 1, right, bottom, color); // Нижняя линия
        context.fill(left, top, left + 1, bottom, color); // Левая линия
        context.fill(right - 1, top, right, bottom, color); // Правая линия
    }

    private static class KillFeedMessage {
        private final Text text;
        private int timeLeft;
        private final int color;
        private final boolean isRelevantToPlayer;

        public KillFeedMessage(Text text, int timeLeft, int color, boolean isRelevantToPlayer) {
            this.text = text;
            this.timeLeft = timeLeft;
            this.color = color;
            this.isRelevantToPlayer = isRelevantToPlayer;
        }
    }
}
