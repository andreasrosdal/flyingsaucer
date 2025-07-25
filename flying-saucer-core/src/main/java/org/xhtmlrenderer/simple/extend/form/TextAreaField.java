/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.simple.extend.form;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.LengthValue;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.plaf.basic.BasicTextUI;
import java.awt.*;

import static org.xhtmlrenderer.util.GeneralUtil.parseIntRelaxed;

class TextAreaField extends FormField<JScrollPane> {
    @Nullable
    private TextAreaFieldJTextArea _textarea;

    TextAreaField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JScrollPane create() {
        int rows = parseIntRelaxed(getAttribute("rows"), 4);
        int cols = parseIntRelaxed(getAttribute("cols"), 10);

        _textarea = new TextAreaFieldJTextArea(rows, cols);

        _textarea.setWrapStyleWord(true);
        _textarea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(_textarea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        applyComponentStyle(_textarea, scrollPane);

        return scrollPane;
    }

    private void applyComponentStyle(TextAreaFieldJTextArea textArea, JScrollPane scrollPane) {
        applyComponentStyle(textArea);

        CalculatedStyle style = getBox().getStyle();
        RectPropertySet padding = style.getCachedPadding();
        Insets margin = style.padding().withDefaults(new Insets(2, 3, 2, 3));

        //if a border is set or a background color is set, then use a special JButton with the BasicButtonUI.
        if (style.disableOSBorder()) {
            //when background color is set, need to use the BasicButtonUI, certainly when using XP l&f
            BasicTextUI ui = new BasicTextAreaUI();
            textArea.setUI(ui);
            scrollPane.setBorder(null);
        }

        textArea.setMargin(margin);

        padding.reset();

        FSDerivedValue widthValue = style.valueByName(CSSName.WIDTH);
        if (widthValue instanceof LengthValue) {
            intrinsicWidth = getBox().getContentWidth() + margin.left + margin.right;
        }

        FSDerivedValue heightValue = style.valueByName(CSSName.HEIGHT);
        if (heightValue instanceof LengthValue) {
            intrinsicHeight = getBox().getHeight() + margin.top + margin.bottom;
        }
    }


    @Override
    protected FormFieldState loadOriginalState() {
        return FormFieldState.fromString(
                XhtmlForm.collectText(getElement()));
    }

    @Override
    protected void applyOriginalState() {
        _textarea.setText(getOriginalState().getValue());
    }

    @Override
    protected String[] getFieldValues() {
        JTextArea textarea = (JTextArea) component().getViewport().getView();

        return new String[] {
                textarea.getText()
        };
    }


    private static class TextAreaFieldJTextArea extends JTextArea {
        private int columnWidth;

        private TextAreaFieldJTextArea(int rows, int columns) {
            super(rows, columns);
        }
        //override getColumnWidth to base on 'o' instead of 'm'.  more like other browsers
        @Override
        protected int getColumnWidth() {
            if (columnWidth == 0) {
                FontMetrics metrics = getFontMetrics(getFont());
                columnWidth = metrics.charWidth('o');
            }
            return columnWidth;
        }

        //Avoid Swing bug #5042886.   This bug was fixed in java6
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            Dimension size = super.getPreferredScrollableViewportSize();
            size = (size == null) ? new Dimension(400,400) : size;
            Insets insets = getInsets();

            size.width = (getColumns() == 0) ? size.width : getColumns() * getColumnWidth() + insets.left + insets.right;
            size.height = (getRows() == 0) ? size.height : getRows() * getRowHeight() + insets.top + insets.bottom;
            return size;
        }
    }
}
