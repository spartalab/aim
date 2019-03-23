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

import java.awt.Font;

import javax.swing.JLabel;

/**
 * A specialized label that formats an update value according to a format
 * string given on construction.
 */
public class FormattedLabel extends JLabel {

  private static final long serialVersionUID = 1L;

  // ///////////////////////////////
  // PUBLIC FIELDS
  // ///////////////////////////////

  /**
   * The font in which to display this information. It is monospaced so that
   * numeric values will display in an easily readable format if their values
   * change rapidly.
   */
  public static final Font FONT = new Font("Monospaced", Font.PLAIN, 10);

  // ///////////////////////////////
  // PRIVATE FIELDS
  // ///////////////////////////////

  /** The string to put before the value. */
  private final String prefix;
  /** The format string to format the value, including any units. */
  private final String format;
  /**
   * The string of blanks to hold the space for the true value, to preserve
   * formatting in case formatting is dynamic.
   */
  private final String blank;

  // ///////////////////////////////
  // CLASS CONSTRUCTORS
  // ///////////////////////////////

  /**
   * Construct a new FormattedLabel with the given prefix string, format
   * string, and width.
   *
   * @param prefix
   *          the string introducing the value
   * @param format
   *          the way to format the value (a la <code>printf</code>),
   *          including any units
   * @param width
   *          the desired maximum width of the label, in characters
   */
  public FormattedLabel(String prefix, String format, int width) {
    super(prefix);
    setFont(FONT);
    this.prefix = prefix;
    this.format = format;
    blank = String.format("%" + width + "s", "");
    clear();
  }

  // ///////////////////////////////
  // PUBLIC METHODS
  // ///////////////////////////////

  /** Set the FormattedLabel to show no value. */
  public void clear() {
    setText(prefix + blank);
  }

  /**
   * Update the FormattedLabel to display the given value. This version is
   * designed for double values.
   *
   * @param param
   *          the value to display.
   */
  public void update(int param) {
    setText(prefix + String.format(format, param));
  }

  /**
   * Update the FormattedLabel to display the given value. This version is
   * designed for double values.
   *
   * @param param
   *          the value to display.
   */
  public void update(long param) {
    setText(prefix + String.format(format, param));
  }

  /**
   * Update the FormattedLabel to display the given value. This version is
   * designed for double values.
   *
   * @param param
   *          the value to display.
   */
  public void update(double param) {
    setText(prefix + String.format(format, param));
  }

  /**
   * Update the FormattedLabel to display the given value. This version is
   * designed for objects that define their own <code>toString()</code> method
   * (e.g. <code>Enum</code>s).
   *
   * @param param
   *          the value to display.
   */
  public void update(Object param) {
    setText(prefix + String.format(format, param));
  }
}

