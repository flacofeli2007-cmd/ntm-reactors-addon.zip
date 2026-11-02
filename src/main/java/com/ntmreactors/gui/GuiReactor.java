package com.ntmreactors.gui;

import com.ntmreactors.ReactorType;
import com.ntmreactors.tile.TileEntityReactorBase;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;

/** GUI dibujado por código (sin textura PNG): barras de energía, calor y progreso. */
@SideOnly(Side.CLIENT)
public class GuiReactor extends GuiContainer {

    private static final int ENERGY_X = 8, HEAT_X = 28, BAR_Y = 17, BAR_W = 14, BAR_H = 52;

    private final TileEntityReactorBase tile;

    public GuiReactor(ContainerReactor container) {
        super(container);
        this.tile = container.getTile();
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        if (inArea(mouseX, mouseY, guiLeft + ENERGY_X, guiTop + BAR_Y, BAR_W, BAR_H)) {
            drawHoveringText(Collections.singletonList(
                    tile.guiEnergy() + " / " + tile.type.capacity + " FE"), mouseX, mouseY);
        }
        if (tile.type.usesHeat && inArea(mouseX, mouseY, guiLeft + HEAT_X, guiTop + BAR_Y, BAR_W, BAR_H)) {
            drawHoveringText(Collections.singletonList(
                    I18n.format("gui.ntmreactors.heat") + ": " + tile.getSyncData()[2] + " / "
                            + TileEntityReactorBase.HEAT_SCRAM), mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = guiLeft, y = guiTop;
        int[] d = tile.getSyncData();

        // panel
        drawRect(x, y, x + xSize, y + ySize, 0xFF2B2F36);
        drawRect(x + 1, y + 1, x + xSize - 1, y + ySize - 1, 0xFFC6C6C6);

        // ranuras
        for (Slot s : inventorySlots.inventorySlots) {
            drawRect(x + s.xPos - 1, y + s.yPos - 1, x + s.xPos + 17, y + s.yPos + 17, 0xFF8B8B8B);
            drawRect(x + s.xPos, y + s.yPos, x + s.xPos + 16, y + s.yPos + 16, 0xFF373737);
        }

        // energía
        drawBar(x + ENERGY_X, y + BAR_Y, BAR_W, BAR_H, tile.guiEnergy(), tile.type.capacity, 0xFFD83A3A);

        // calor (solo reactor rápido)
        if (tile.type.usesHeat) {
            int color = d[2] > 700 ? 0xFFFF2020 : 0xFFFFA020;
            drawBar(x + HEAT_X, y + BAR_Y, BAR_W, BAR_H, d[2], TileEntityReactorBase.HEAT_SCRAM, color);
        }

        // combustible restante y refrigerante / moderador restante (barras horizontales)
        drawHBar(x + 78, y + 31, 48, 6, d[3], d[4], 0xFF4CAF50);
        if (tile.type != ReactorType.THORIUM) {
            drawHBar(x + 78, y + 61, 48, 6, d[5], d[6], 0xFF3FA7E0);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int[] d = tile.getSyncData();
        String title = I18n.format("tile.ntmreactors." + tile.type.id + ".name");
        fontRenderer.drawString(title, 8, 6, 0x404040);

        fontRenderer.drawString(I18n.format("gui.ntmreactors.fuel"), 56, 16, 0x404040);
        fontRenderer.drawString(I18n.format("gui.ntmreactors.aux." + tile.type.id), 56, 46, 0x404040);
        fontRenderer.drawString(I18n.format("gui.ntmreactors.output"), 132, 16, 0x404040);

        String status;
        int color;
        if ((d[7] & 2) != 0) {
            status = I18n.format("gui.ntmreactors.status.scram");
            color = 0xCC0000;
        } else if ((d[7] & 1) != 0) {
            status = I18n.format("gui.ntmreactors.status.running") + "  +" + d[8] + " FE/t";
            color = 0x1B7F1B;
        } else {
            status = I18n.format("gui.ntmreactors.status.idle");
            color = 0x606060;
        }
        fontRenderer.drawString(status, 56, 74, color);
    }

    private void drawBar(int x, int y, int w, int h, int value, int max, int color) {
        drawRect(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF2B2F36);
        drawRect(x, y, x + w, y + h, 0xFF1B1B1B);
        if (max > 0 && value > 0) {
            int fill = Math.min(h, (int) ((long) h * value / max));
            drawRect(x, y + h - fill, x + w, y + h, color);
        }
    }

    private void drawHBar(int x, int y, int w, int h, int value, int max, int color) {
        drawRect(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF2B2F36);
        drawRect(x, y, x + w, y + h, 0xFF1B1B1B);
        if (max > 0 && value > 0) {
            int fill = Math.min(w, (int) ((long) w * value / max));
            drawRect(x, y, x + fill, y + h, color);
        }
    }

    private static boolean inArea(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
