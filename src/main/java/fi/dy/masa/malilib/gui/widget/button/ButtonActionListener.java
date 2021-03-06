package fi.dy.masa.malilib.gui.widget.button;

public interface ButtonActionListener
{
    /**
     * Called when a button is clicked with the mouse
     * @param button
     * @param mouseButton
     */
    void actionPerformedWithButton(BaseButton button, int mouseButton);
}
