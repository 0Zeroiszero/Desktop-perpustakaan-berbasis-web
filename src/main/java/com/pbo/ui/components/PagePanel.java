package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.LayoutManager;

/** Halaman scroll: lebarnya mengikuti viewport, tingginya mengikuti isi. */
public class PagePanel extends JPanel implements Scrollable {

    public PagePanel() {
        super();
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setBackground(Theme.BACKGROUND);
    }

    protected PagePanel(LayoutManager layout) {
        super(layout);
        setBackground(Theme.BACKGROUND);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 22;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(100, visibleRect.height - 40);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
