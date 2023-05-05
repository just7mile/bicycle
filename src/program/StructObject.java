package program;

import lexer.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * Information about a struct instance.
 */
public class StructObject {
    /**
     * Name of the struct.
     */
    private String label;

    /**
     * Fields of the struct. It is map of field names to their values.
     */
    private Map<String, Object> fields;

    public StructObject(String label) {
        this.label = label;
        this.fields = new HashMap<>();
    }

    public void addField(String label, Object value) {
        this.fields.put(label, value);
    }

    public Object getField(String name) {
        return this.fields.get(name);
    }

    public void setField(String name, Object value) {
        this.fields.put(name, value);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(Token.getLabelValue(Token.LP));
        str.append(" ");
        boolean isFirst = true;
        for (String k : this.fields.keySet()) {
            if (!isFirst) {
                str.append(Token.getLabelValue(Token.COMMA));
                str.append(" ");
            } else {
                isFirst = false;
            }
            str.append(k);
            str.append(" ");
            str.append(Token.getLabelValue(Token.ASSIGNMENT));
            str.append(" ");
            if (this.fields.get(k) != null) {
                str.append(this.fields.get(k));
            } else {
                str.append("null");
            }
        }
        if (!this.fields.isEmpty()) {
            str.append(" ");
            str.append(Token.getLabelValue(Token.RP));
        }

        return str.toString();
    }
}
