package ru.hh.vsplitter.stem;

abstract class SnowballProgram {
  static class Among {
    public final int s_size;      // search string
    public final String s;        // search string
    public final int substring_i; // index to longest matching substring
    public final int result;      // result of the lookup

    public Among(String s, int substring_i, int result) {
      this.s_size = s.length();
      this.s = s;
      this.substring_i = substring_i;
      this.result = result;
    }
  }

  private StringBuilder current;

  protected int cursor;
  protected int limit;
  protected int limit_backward;
  protected int bra;
  protected int ket;

  private boolean modified;

  public boolean stem(StringBuilder string) {
    current = string;
    cursor = 0;
    limit = current.length();
    limit_backward = 0;
    bra = cursor;
    ket = limit;
    modified = false;

    stem();

    return modified;
  }

  protected abstract boolean stem();

  protected boolean in_grouping(char[] s, int min, int max) {
    if (cursor >= limit) {
      return false;
    }
    char ch = current.charAt(cursor);
    if (ch > max || ch < min) {
      return false;
    }
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
      return false;
    }
    cursor++;
    return true;
  }

  protected boolean in_grouping_b(char[] s, int min, int max) {
    if (cursor <= limit_backward) {
      return false;
    }
    char ch = current.charAt(cursor - 1);
    if (ch > max || ch < min) {
      return false;
    }
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
      return false;
    }
    cursor--;
    return true;
  }

  protected boolean out_grouping(char[] s, int min, int max) {
    if (cursor >= limit) {
      return false;
    }
    char ch = current.charAt(cursor);
    if (ch > max || ch < min) {
      cursor++;
      return true;
    }
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
      cursor++;
      return true;
    }
    return false;
  }

  protected boolean out_grouping_b(char[] s, int min, int max) {
    if (cursor <= limit_backward) {
      return false;
    }
    char ch = current.charAt(cursor - 1);
    if (ch > max || ch < min) {
      cursor--;
      return true;
    }
    ch -= min;
    if ((s[ch >> 3] & (0X1 << (ch & 0X7))) == 0) {
      cursor--;
      return true;
    }
    return false;
  }

  protected boolean in_range(int min, int max) {
    if (cursor >= limit) {
      return false;
    }
    char ch = current.charAt(cursor);
    if (ch > max || ch < min) {
      return false;
    }
    cursor++;
    return true;
  }

  protected boolean in_range_b(int min, int max) {
    if (cursor <= limit_backward) {
      return false;
    }
    char ch = current.charAt(cursor - 1);
    if (ch > max || ch < min) {
      return false;
    }
    cursor--;
    return true;
  }

  protected boolean out_range(int min, int max) {
    if (cursor >= limit) {
      return false;
    }
    char ch = current.charAt(cursor);
    if (!(ch > max || ch < min)) {
      return false;
    }
    cursor++;
    return true;
  }

  protected boolean out_range_b(int min, int max) {
    if (cursor <= limit_backward) {
      return false;
    }
    char ch = current.charAt(cursor - 1);
    if (!(ch > max || ch < min)) {
      return false;
    }
    cursor--;
    return true;
  }

  protected boolean eq_s(int s_size, String s) {
    if (limit - cursor < s_size) {
      return false;
    }
    int i;
    for (i = 0; i != s_size; i++) {
      if (current.charAt(cursor + i) != s.charAt(i)) {
        return false;
      }
    }
    cursor += s_size;
    return true;
  }

  protected boolean eq_s_b(int s_size, String s) {
    if (cursor - limit_backward < s_size) {
      return false;
    }
    int i;
    for (i = 0; i != s_size; i++) {
      if (current.charAt(cursor - s_size + i) != s.charAt(i)) {
        return false;
      }
    }
    cursor -= s_size;
    return true;
  }

  protected boolean eq_v(StringBuilder s) {
    return eq_s(s.length(), s.toString());
  }

  protected boolean eq_v_b(StringBuilder s) {
    return eq_s_b(s.length(), s.toString());
  }

  protected int find_among(Among v[], int v_size) {
    int i = 0;
    int j = v_size;

    int c = cursor;
    int l = limit;

    int common_i = 0;
    int common_j = 0;

    boolean first_key_inspected = false;

    while (true) {
      int k = i + ((j - i) >> 1);
      int diff = 0;
      int common = common_i < common_j ? common_i : common_j; // smaller
      Among w = v[k];
      int i2;
      for (i2 = common; i2 < w.s_size; i2++) {
        if (c + common == l) {
          diff = -1;
          break;
        }
        diff = current.charAt(c + common) - w.s.charAt(i2);
        if (diff != 0) {
          break;
        }
        common++;
      }
      if (diff < 0) {
        j = k;
        common_j = common;
      } else {
        i = k;
        common_i = common;
      }
      if (j - i <= 1) {
        if (i > 0) {
          break; // v->s has been inspected
        }
        if (j == i) {
          break; // only one item in v
        }

        // - but now we need to go round once more to get
        // v->s inspected. This looks messy, but is actually
        // the optimal approach.

        if (first_key_inspected) {
          break;
        }
        first_key_inspected = true;
      }
    }
    while (true) {
      Among w = v[i];
      if (common_i >= w.s_size) {
        cursor = c + w.s_size;
        return w.result;
      }
      i = w.substring_i;
      if (i < 0) {
        return 0;
      }
    }
  }

  protected int find_among_b(Among v[], int v_size) {
    int i = 0;
    int j = v_size;

    int c = cursor;
    int lb = limit_backward;

    int common_i = 0;
    int common_j = 0;

    boolean first_key_inspected = false;

    while (true) {
      int k = i + ((j - i) >> 1);
      int diff = 0;
      int common = common_i < common_j ? common_i : common_j;
      Among w = v[k];
      int i2;
      for (i2 = w.s_size - 1 - common; i2 >= 0; i2--) {
        if (c - common == lb) {
          diff = -1;
          break;
        }
        diff = current.charAt(c - 1 - common) - w.s.charAt(i2);
        if (diff != 0) {
          break;
        }
        common++;
      }
      if (diff < 0) {
        j = k;
        common_j = common;
      } else {
        i = k;
        common_i = common;
      }
      if (j - i <= 1) {
        if (i > 0) {
          break;
        }
        if (j == i) {
          break;
        }
        if (first_key_inspected) {
          break;
        }
        first_key_inspected = true;
      }
    }
    while (true) {
      Among w = v[i];
      if (common_i >= w.s_size) {
        cursor = c - w.s_size;
        return w.result;
      }
      i = w.substring_i;
      if (i < 0) {
        return 0;
      }
    }
  }

  protected int replace_s(int c_bra, int c_ket, String s) {
    int adjustment = s.length() - (c_ket - c_bra);
    current.replace(c_bra, c_ket, s);
    modified = true;
    limit += adjustment;
    if (cursor >= c_ket) {
      cursor += adjustment;
    } else if (cursor > c_bra) {
      cursor = c_bra;
    }
    return adjustment;
  }

  protected void slice_from(String s) {
    replace_s(bra, ket, s);
  }

  protected void slice_from(StringBuilder s) {
    slice_from(s.toString());
  }

  protected void slice_del() {
    slice_from("");
  }

  protected void insert(int c_bra, int c_ket, String s) {
    int adjustment = replace_s(c_bra, c_ket, s);
    if (c_bra <= bra) {
      bra += adjustment;
    }
    if (c_bra <= ket) {
      ket += adjustment;
    }
  }

  protected void insert(int c_bra, int c_ket, StringBuilder s) {
    insert(c_bra, c_ket, s.toString());
  }
}
