package com.github.fernthedev.light.exceptions;

import com.github.fernthedev.light.api.lines.LightLine;
import lombok.Getter;

public class LightFileParseException extends RuntimeException {

    @Getter
    private LightLine lightLine;



    public LightFileParseException(LightLine lightLine, String message) {
        super(formatErrorMessage(lightLine,message));
        handleLightLine(lightLine);
    }


    public LightFileParseException(LightLine lightLine, Exception exception) {
        super(formatErrorMessage(lightLine),exception);
        handleLightLine(lightLine);
    }

    public LightFileParseException(LightLine lightLine,String message, Exception exception) {
        super(formatErrorMessage(lightLine,message),exception);
        handleLightLine(lightLine);
    }

    public LightFileParseException(LightLine lightLine) {
        super(formatErrorMessage(lightLine));
        handleLightLine(lightLine);
    }

    private void handleLightLine(LightLine lightLine) {
        this.lightLine = lightLine;
    }

    public void printStackTrace() {
        super.printStackTrace();
    }

    private static String formatErrorMessage(LightLine lightLine) {
        return "Error at " + lightLine.getLine() + ":" + lightLine.getLineNumber();
    }

    private static String formatErrorMessage(LightLine lightLine,String message) {
        return "Error at " + lightLine.getLine() + ":" + lightLine.getLineNumber() + "" +
                "\nCause of error: " + message;
    }

}
