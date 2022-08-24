package impl.com.calendarfx.view;

import javafx.util.StringConverter;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

public class ZoneIdStringConverter extends StringConverter<ZoneId> {

    public ZoneIdStringConverter() {
    }

    @Override
    public String toString(ZoneId id) {
        if (id != null) {
            return id.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) + " (" + id.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault()) + ")";
        }
        return "";
    }

    @Override
    public ZoneId fromString(String id) {
        return ZoneId.of(id);
    }
}