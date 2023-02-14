package util;

import nethack.enums.Color;

public final class ColoredStringBuilder {
  StringBuilder sb;
  Color currentColor = Color.RESET;
  /**
   * Constructs a string builder with no characters in it and an initial capacity of 16 characters.
   */
  public ColoredStringBuilder() {
    sb = new StringBuilder(16);
  }

  /**
   * Constructs a string builder with no characters in it and an initial capacity specified by the
   * {@code capacity} argument.
   *
   * @param capacity the initial capacity.
   * @throws NegativeArraySizeException if the {@code capacity} argument is less than {@code 0}.
   */
  public ColoredStringBuilder(int capacity) {
    sb = new StringBuilder(capacity);
  }

  /**
   * Constructs a string builder initialized to the contents of the specified string. The initial
   * capacity of the string builder is {@code 16} plus the length of the string argument.
   *
   * @param str the initial contents of the buffer.
   */
  public ColoredStringBuilder(String str) {
    sb = new StringBuilder(str);
  }

  /**
   * Constructs a string builder that contains the same characters as the specified {@code
   * CharSequence}. The initial capacity of the string builder is {@code 16} plus the length of the
   * {@code CharSequence} argument.
   *
   * @param seq the sequence to copy.
   */
  public ColoredStringBuilder(CharSequence seq) {
    sb = new StringBuilder(seq);
  }

  /**
   * Compares two {@code StringBuilder} instances lexicographically. This method follows the same
   * rules for lexicographical comparison as defined in the {@linkplain
   * java.lang.CharSequence#compare(java.lang.CharSequence, java.lang.CharSequence)
   * CharSequence.compare(this, another)} method.
   *
   * <p>For finer-grained, locale-sensitive String comparison, refer to {@link java.text.Collator}.
   *
   * @param another the {@code StringBuilder} to be compared with
   * @return the value {@code 0} if this {@code StringBuilder} contains the same character sequence
   *     as that of the argument {@code StringBuilder}; a negative integer if this {@code
   *     StringBuilder} is lexicographically less than the {@code StringBuilder} argument; or a
   *     positive integer if this {@code StringBuilder} is lexicographically greater than the {@code
   *     StringBuilder} argument.
   * @since 11
   */
  public int compareTo(ColoredStringBuilder another) {
    return sb.compareTo(another.sb);
  }

  public ColoredStringBuilder append(Object obj) {
    return append(String.valueOf(obj));
  }

  public ColoredStringBuilder append(String str) {
    sb.append(str);
    return this;
  }

  /**
   * Appends the specified {@code StringBuffer} to this sequence.
   *
   * <p>The characters of the {@code StringBuffer} argument are appended, in order, to this
   * sequence, increasing the length of this sequence by the length of the argument. If {@code sb}
   * is {@code null}, then the four characters {@code "null"} are appended to this sequence.
   *
   * <p>Let <i>n</i> be the length of this character sequence just prior to execution of the {@code
   * append} method. Then the character at index <i>k</i> in the new character sequence is equal to
   * the character at index <i>k</i> in the old character sequence, if <i>k</i> is less than
   * <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i> in the argument {@code
   * sb}.
   *
   * @param sb the {@code StringBuffer} to append.
   * @return a reference to this object.
   */
  public ColoredStringBuilder append(StringBuffer sb) {
    sb.append(sb);
    return this;
  }

  public ColoredStringBuilder append(CharSequence s) {
    sb.append(s);
    return this;
  }

  public ColoredStringBuilder append(CharSequence s, int start, int end) {
    sb.append(s, start, end);
    return this;
  }

  public ColoredStringBuilder append(char[] str) {
    sb.append(str);
    return this;
  }

  public ColoredStringBuilder append(char[] str, int offset, int len) {
    sb.append(str, offset, len);
    return this;
  }

  public ColoredStringBuilder append(boolean b) {
    sb.append(b);
    return this;
  }

  public ColoredStringBuilder append(char c) {
    sb.append(c);
    return this;
  }

  public ColoredStringBuilder append(int i) {
    sb.append(i);
    return this;
  }

  public ColoredStringBuilder append(long lng) {
    sb.append(lng);
    return this;
  }

  public ColoredStringBuilder append(float f) {
    sb.append(f);
    return this;
  }

  public ColoredStringBuilder append(double d) {
    sb.append(d);
    return this;
  }

  public ColoredStringBuilder append(Color color, Object obj) {
    Color prevColor = currentColor;
    setColor(color);
    append(String.valueOf(obj));
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, String str) {
    Color prevColor = currentColor;
    setColor(color);
    append(str);
    setColor(prevColor);
    return this;
  }

  /**
   * Appends the specified {@code StringBuffer} to this sequence.
   *
   * <p>The characters of the {@code StringBuffer} argument are appended, in order, to this
   * sequence, increasing the length of this sequence by the length of the argument. If {@code sb}
   * is {@code null}, then the four characters {@code "null"} are appended to this sequence.
   *
   * <p>Let <i>n</i> be the length of this character sequence just prior to execution of the {@code
   * append} method. Then the character at index <i>k</i> in the new character sequence is equal to
   * the character at index <i>k</i> in the old character sequence, if <i>k</i> is less than
   * <i>n</i>; otherwise, it is equal to the character at index <i>k-n</i> in the argument {@code
   * sb}.
   *
   * @param sb the {@code StringBuffer} to append.
   * @return a reference to this object.
   */
  public ColoredStringBuilder append(Color color, StringBuffer sb) {
    Color prevColor = currentColor;
    setColor(color);
    append(sb);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, CharSequence s) {
    Color prevColor = currentColor;
    setColor(color);
    append(s);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, CharSequence s, int start, int end) {
    Color prevColor = currentColor;
    setColor(color);
    append(s, start, end);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, char[] str) {
    Color prevColor = currentColor;
    setColor(color);
    append(str);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, char[] str, int offset, int len) {
    Color prevColor = currentColor;
    setColor(color);
    append(str, offset, len);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, boolean b) {
    Color prevColor = currentColor;
    setColor(color);
    append(b);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, char c) {
    Color prevColor = currentColor;
    setColor(color);
    append(c);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, int i) {
    Color prevColor = currentColor;
    setColor(color);
    append(i);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, long lng) {
    Color prevColor = currentColor;
    setColor(color);
    append(lng);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, float f) {
    Color prevColor = currentColor;
    setColor(color);
    append(f);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder append(Color color, double d) {
    Color prevColor = currentColor;
    setColor(color);
    append(d);
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder setColor(Color color) {
    if (currentColor == color) {
      return this;
    }
    sb.append(color.stringCode());
    currentColor = color;
    return this;
  }

  public ColoredStringBuilder resetColor() {
    return setColor(Color.RESET);
  }

  public ColoredStringBuilder appendf(String format, Object... args) {
    sb.append(String.format(format, args));
    return this;
  }

  public ColoredStringBuilder appendf(Color color, String format, Object... args) {
    Color prevColor = currentColor;
    setColor(color);
    sb.append(String.format(format, args));
    setColor(prevColor);
    return this;
  }

  public ColoredStringBuilder newLine() {
    sb.append(System.lineSeparator());
    return this;
  }

  public ColoredStringBuilder appendCodePoint(int codePoint) {
    sb.appendCodePoint(codePoint);
    return this;
  }

  public ColoredStringBuilder delete(int start, int end) {
    sb.delete(start, end);
    return this;
  }

  public ColoredStringBuilder deleteCharAt(int index) {
    sb.deleteCharAt(index);
    return this;
  }

  public ColoredStringBuilder replace(int start, int end, String str) {
    sb.replace(start, end, str);
    return this;
  }

  public ColoredStringBuilder insert(int index, char[] str, int offset, int len) {
    sb.insert(index, str, offset, len);
    return this;
  }

  public ColoredStringBuilder insert(int offset, Object obj) {
    sb.insert(offset, obj);
    return this;
  }

  public ColoredStringBuilder insert(int offset, String str) {
    sb.insert(offset, str);
    return this;
  }

  public ColoredStringBuilder insert(int offset, char[] str) {
    sb.insert(offset, str);
    return this;
  }

  public ColoredStringBuilder insert(int dstOffset, CharSequence s) {
    sb.insert(dstOffset, s);
    return this;
  }

  public ColoredStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
    sb.insert(dstOffset, s, start, end);
    return this;
  }

  public ColoredStringBuilder insert(int offset, boolean b) {
    sb.insert(offset, b);
    return this;
  }

  public ColoredStringBuilder insert(int offset, char c) {
    sb.insert(offset, c);
    return this;
  }

  public ColoredStringBuilder insert(int offset, int i) {
    sb.insert(offset, i);
    return this;
  }

  public ColoredStringBuilder insert(int offset, long l) {
    sb.insert(offset, l);
    return this;
  }

  public ColoredStringBuilder insert(int offset, float f) {
    sb.insert(offset, f);
    return this;
  }

  public ColoredStringBuilder insert(int offset, double d) {
    sb.insert(offset, d);
    return this;
  }

  public int indexOf(String str) {
    return sb.indexOf(str);
  }

  public int indexOf(String str, int fromIndex) {
    return sb.indexOf(str, fromIndex);
  }

  public int lastIndexOf(String str) {
    return sb.lastIndexOf(str);
  }

  public int lastIndexOf(String str, int fromIndex) {
    return sb.lastIndexOf(str, fromIndex);
  }

  public ColoredStringBuilder reverse() {
    sb.reverse();
    return this;
  }

  public String toString() {
    // Create a copy, don't share the array
    setColor(Color.RESET);
    return sb.toString();
  }
}
