package fi.dy.masa.malilib.config.gui;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.lwjgl.input.Keyboard;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase.ConfigOptionWrapper;
import fi.dy.masa.malilib.gui.interfaces.IDialogHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public abstract class ConfigPanelBase extends AbstractConfigPanel
{
    private final List<GuiConfigsBase> subPanels = new ArrayList<>();
    private GuiConfigsBase selectedSubPanel;
    protected int subPanelButtonWidth = 300;
    protected int subPanelButtonHeight = 20;
    protected int subPanelButtonsStartY = 10;

    protected abstract String getPanelTitlePrefix();

    protected abstract void createSubPanels();

    @Override
    public String getPanelTitle()
    {
        if (this.selectedSubPanel != null)
        {
            return this.getPanelTitlePrefix() + " => " + this.selectedSubPanel.getTitle();
        }

        return this.getPanelTitlePrefix();
    }

    @Override
    public void onPanelHidden()
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.onGuiClosed();
        }
    }

    @Override
    protected void addOptions(ConfigPanelHost host)
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.initGui();
            return;
        }

        this.createSubPanels();

        int buttonWidth = this.subPanelButtonWidth;
        int buttonHeight = this.subPanelButtonHeight;
        int x = host.getWidth() / 2 - buttonWidth / 2;
        int y = this.subPanelButtonsStartY;

        for (int i = 0; i < this.subPanels.size(); i++)
        {
            GuiConfigsBase subPanel = this.subPanels.get(i);
            ButtonListenerPanelSelection<GuiButton> listener = new ButtonListenerPanelSelection<>(subPanel);
            this.addControl(new GuiButton(i, x, y, buttonWidth, buttonHeight, subPanel.getTitle()), listener);
            y += this.subPanelButtonHeight + 1;
        }
    }

    @Override
    public void drawPanel(ConfigPanelHost host, int mouseX, int mouseY, float partialTicks)
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.drawScreen(mouseX, mouseY, partialTicks);
        }
        else
        {
            super.drawPanel(host, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public int getContentHeight()
    {
        if (this.selectedSubPanel != null)
        {
            return -1;
        }
        else
        {
            return super.getContentHeight();
        }
    }

    @Override
    public void keyPressed(ConfigPanelHost host, char keyChar, int keyCode)
    {
        if (this.selectedSubPanel != null)
        {
            if (this.selectedSubPanel.onKeyTyped(keyChar, keyCode) == false && keyCode == Keyboard.KEY_ESCAPE)
            {
                this.setSelectedSubPanel(null);
            }
        }
        else
        {
            super.keyPressed(host, keyChar, keyCode);
        }
    }

    @Override
    public void mouseMoved(ConfigPanelHost host, int mouseX, int mouseY)
    {
        if (this.selectedSubPanel != null)
        {
        }
        else
        {
            super.mouseMoved(host, mouseX, mouseY);
        }
    }

    @Override
    public void mousePressed(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton)
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.onMouseClicked(mouseX, mouseY, mouseButton);
        }
        else
        {
            super.mousePressed(host, mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(ConfigPanelHost host, int mouseX, int mouseY, int mouseButton)
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.onMouseReleased(mouseX, mouseY, mouseButton);
        }
        else
        {
            super.mouseReleased(host, mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onPanelResize(ConfigPanelHost host)
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.initGui();
        }
        else
        {
            super.onPanelResize(host);
        }
    }

    @Override
    protected void clearOptions()
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.clearOptions();
        }
        else
        {
            super.clearOptions();
        }
    }

    protected void addSubPanel(GuiConfigsBase panel)
    {
        panel.setWorldAndResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        this.subPanels.add(panel);
    }

    public void setSelectedSubPanel(@Nullable GuiConfigsBase panel)
    {
        if (this.selectedSubPanel != null)
        {
            this.selectedSubPanel.onGuiClosed();
        }

        if (panel != null)
        {
            this.selectedSubPanel = panel;
            this.selectedSubPanel.setParentGui(this.mc.currentScreen);
            this.selectedSubPanel.setDialogHandler(new DialogHandler(this.selectedSubPanel));
            this.selectedSubPanel.initGui();
        }
        else
        {
            this.selectedSubPanel = null;
        }
    }

    private class ButtonListenerPanelSelection<T extends GuiButton> implements ConfigOptionListener<T>
    {
        private final GuiConfigsBase panel;

        public ButtonListenerPanelSelection(GuiConfigsBase panel)
        {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(T control)
        {
            ConfigPanelBase.this.setSelectedSubPanel(this.panel);
        }
    }

    private class DialogHandler implements IDialogHandler
    {
        @Nullable private final GuiConfigsBase selectedPanel;

        private DialogHandler(@Nullable GuiConfigsBase selectedPanel)
        {
            this.selectedPanel = selectedPanel;
        }

        @Override
        public void openDialog(GuiBase gui)
        {
            String modId = this.selectedPanel.getModId();
            String title = this.selectedPanel.getTitle();
            List<ConfigOptionWrapper> wrappers = this.selectedPanel.getConfigs();

            ConfigPanelBase.this.setSelectedSubPanel(new GuiConfigsWrapper(modId, title, wrappers, this.selectedPanel, gui));
        }

        @Override
        public void closeDialog()
        {
            ConfigPanelBase.this.setSelectedSubPanel(this.selectedPanel);
        }
    }

    public static class GuiConfigsWrapper extends GuiModConfigs
    {
        protected final GuiScreen backgroundGui;
        protected final GuiBase foregroundGui;

        public GuiConfigsWrapper(String modId, String title, List<ConfigOptionWrapper> wrappers,
                GuiScreen backgroundGui, GuiBase foregroundGui)
        {
            super(modId, title, wrappers, false);

            this.backgroundGui = backgroundGui;
            this.foregroundGui = foregroundGui;
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks)
        {
            this.backgroundGui.drawScreen(mouseX, mouseY, partialTicks);
            this.foregroundGui.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public void updateScreen()
        {
            this.foregroundGui.updateScreen();
        }

        @Override
        public void onGuiClosed()
        {
            this.foregroundGui.onGuiClosed();
        }

        @Override
        public boolean onKeyTyped(char typedChar, int keyCode)
        {
            return this.foregroundGui.onKeyTyped(typedChar, keyCode);
        }

        @Override
        public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton)
        {
            return this.foregroundGui.onMouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public boolean onMouseScrolled(int mouseX, int mouseY, int mouseWheelDelta)
        {
            return this.foregroundGui.onMouseScrolled(mouseX, mouseY, mouseWheelDelta);
        }
    }
}
