/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.gui.component;

import java.awt.Component;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import aim4.util.Util;
import java.util.Hashtable;

/**
 * The labeled slider.
 */
public class LabeledSlider extends JPanel implements ChangeListener {

  private static final long serialVersionUID = 1L;

  /** The minimum value */
  private double minValue;
  /** The maximum value */
  private double maxValue;
  /** The default value */
  private double defaultValue;
  /** The major tick */
  private double majorTick;
  /** The minor tick */
  private double minorTick;
  /** The lable format */
  private String labelFormat;
  /** The tick label format */
  private String tickLabelFormat;
  /** Special tick label table */
  private Map<Integer, String> specialTickLabelTable;
  /** Special tick label format table */
  private Map<Integer, String> specialTickLabelFormatTable;
  /** The change listener */
  private ChangeListener changeListener;
  /** The label of the slider */
  JLabel label;
  /** The slider */
  JSlider slider;

  /**
   * Create a labeled slider.
   *
   * @param minValue        the minimum value
   * @param maxValue        the maximum value
   * @param defaultValue    the default value
   * @param majorTick       the major tick
   * @param minorTick       the minor tick
   * @param labelFormat     the label format
   * @param tickLabelFormat the tick label format
   * @param changeListener  the change listener
   */
  public LabeledSlider(double minValue,
                       double maxValue,
                       double defaultValue,
                       double majorTick,
                       double minorTick,
                       String labelFormat,
                       String tickLabelFormat,
                       ChangeListener changeListener) {
    this(minValue, maxValue, defaultValue, majorTick, minorTick,
         labelFormat, tickLabelFormat);
    this.changeListener = changeListener;
  }

  /**
   * Create a labeled slider.
   *
   * @param minValue        the minimum value
   * @param maxValue        the maximum value
   * @param defaultValue    the default value
   * @param majorTick       the major tick
   * @param minorTick       the minor tick
   * @param labelFormat     the label format
   * @param tickLabelFormat the tick label format
   */
  public LabeledSlider(double minValue,
                       double maxValue,
                       double defaultValue,
                       double majorTick,
                       double minorTick,
                       String labelFormat,
                       String tickLabelFormat) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.defaultValue = defaultValue;
    this.majorTick = majorTick;
    this.minorTick = minorTick;
    this.labelFormat = labelFormat;
    this.tickLabelFormat = tickLabelFormat;

    specialTickLabelTable = new HashMap<Integer,String>();
    specialTickLabelFormatTable = new HashMap<Integer,String>();

    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

    label = new JLabel();
    setLabel((int)(defaultValue/minorTick));
    label.setAlignmentX(Component.CENTER_ALIGNMENT);
    add(label);

    slider = new JSlider(JSlider.HORIZONTAL,
                         (int)(minValue/minorTick),
                         (int)(maxValue/minorTick),
                         (int)(defaultValue/minorTick));

    slider.setMajorTickSpacing((int) (majorTick / minorTick));
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.addChangeListener(this);
    slider.setSnapToTicks(true);

    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    for(double r = minValue;
        Util.isDoubleEqualOrLess(r, maxValue);
        r += majorTick) {
      labelTable.put((int)(r / minorTick),
                     new JLabel(String.format(tickLabelFormat, r)));
    }
    slider.setLabelTable(labelTable);

    add(slider);

    changeListener = null;
  }

  /**
   * Get the value of the slider.
   *
   * @return the value of the slider
   */
  public double getValue() {
    return slider.getValue() * minorTick;
  }

  /**
   * Set the tick label.
   *
   * @param value  the value
   * @param l      the label
   * @param fmt    the format of the label
   */
  @SuppressWarnings("unchecked")
  public void setTickLabel(double value, String l, String fmt) {
    int vid = (int)(value/minorTick);

    Dictionary<Integer, JLabel> labelTable =
      (Hashtable<Integer, JLabel>)slider.getLabelTable();
//    Hashtable<Integer, JLabel> labelTable =
//      (Hashtable<Integer, JLabel>) slider.getLabelTable();
    labelTable.put(vid, new JLabel(l));
    slider.setLabelTable(labelTable);

    specialTickLabelTable.put(vid, l);
    specialTickLabelFormatTable.put(vid, fmt);

    setLabel(slider.getValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    JSlider source = (JSlider)e.getSource();
    setLabel(source.getValue());

    if (changeListener != null) {
      changeListener.stateChanged(new ChangeEvent(this));
    }
  }

  /**
   * Update the label of the current value.
   *
   * @param vid the value
   */
  private void setLabel(int vid) {
    double value = vid * minorTick;
    if (specialTickLabelFormatTable.containsKey(vid)) {
      label.setText(String
        .format(specialTickLabelFormatTable.get(vid),
                specialTickLabelTable.get(vid)));
    } else {
      label.setText(String.format(labelFormat, value));
    }
  }

}

