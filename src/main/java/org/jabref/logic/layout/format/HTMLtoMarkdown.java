package org.jabref.logic.layout.format;

import java.util.Objects;
import org.jabref.logic.layout.LayoutFormatter;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;

public class HTMLtoMarkdown implements LayoutFormatter {

    @Override
    public String format(final String fieldText) {
        Objects.requireNonNull(fieldText, "Field Text should not be null, when handed to formatter");
        return FlexmarkHtmlConverter.builder().build().convert(fieldText);
    }
}