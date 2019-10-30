package com.github.fernthedev.light.api.lines;

import com.github.fernthedev.light.api.annotations.LineArgument;
import com.github.fernthedev.light.api.annotations.LineData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
@Data
@LineData(name = "print")
public class LightPrintLine extends ILightLine {
    @LineArgument(name = "message")
    private String print;

    public LightPrintLine(@NonNull String line, int lineNumber,@NonNull String print) {
        super(line, lineNumber);
        this.print = print;
    }

    public LightPrintLine(ILightLine lightLine, @NonNull String print) {
        super(lightLine);
        this.print = print;
    }

    @Override
    public @NonNull ILightLine constructLightLine(ILightLine lightLine, String[] args) {
        if(args.length > 0) {
            StringBuilder st = new StringBuilder();
            int t = 0;
            for (String se : args) {
                if (t > 0) st.append(" ");
                st.append(se);

                t++;
            }

            return new LightPrintLine(lightLine, st.toString());
        } else {
            throwException("Not enough arguments. \nFound: " + getArguments() + "\nRequired:" + getArguments(getClass()));
        }
        return null;
    }
}
