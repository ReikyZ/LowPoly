package com.reikyz.lowpoly;

/**
 * Created by reikyZ on 16/10/1.
 */

public class Utils {
    public static String getLineNumber(Exception e) {
        StackTraceElement[] trace = e.getStackTrace();
        if (trace == null || trace.length == 0) return "==" + -1 + "=="; //
        return "==" + trace[0].getLineNumber() + "==";
    }
}
