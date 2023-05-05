package program;

/**
 * Information about a value.
 */
public class ValueObject {
    private Object value;

    public ValueObject(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (this.value != null) {
            return this.value.toString();
        }
        return "null";
    }
}
