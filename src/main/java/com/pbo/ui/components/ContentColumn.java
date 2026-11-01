package com.pbo.ui.components;

import com.pbo.ui.Theme;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;

/** Kolom isi maksimal 1140px yang selalu di tengah halaman. */
public class ContentColumn extends JPanel {

    public ContentColumn() {
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension preferred = getPreferredSize();
        return new Dimension(Theme.CONTENT_WIDTH, preferred.height);
    }
}
