package net.sf.rails.game.state;

/**
 * A stateful version of a boolean variable
 */
public final class BooleanState extends State {

    private boolean value;

    public BooleanState(Item parent, String id, boolean value) {
        super(parent, id);

        this.value = value;
    }

    public BooleanState(Item parent, String id) {
        this(parent, id, false);
    }

    /**
     * @param value set state to this value
     */
    public void set(boolean value) {
        if (value != this.value) new BooleanChange(this, value);
    }

    /**
     * @return current value of state variable
     */
    public boolean value() {
        return value;
    }

    @Override
    public String toText() {
        return Boolean.toString(value);
    }

    public void change(boolean value) {
        this.value = value;
    }
}
