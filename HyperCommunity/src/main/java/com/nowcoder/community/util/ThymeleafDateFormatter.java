package com.nowcoder.community.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ThymeleafDateFormatter {
    public String format(Date date, String pattern) {
        if (date == null) return "";
        return new SimpleDateFormat(pattern).format(date);
    }
}
