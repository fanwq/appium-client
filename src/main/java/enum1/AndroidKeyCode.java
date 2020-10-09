package enum1;

public enum AndroidKeyCode {
    /**
     * Key code constant: Unknown key code.
     */
    UNKNOWN(0),
    /**
     * Key code constant: Soft Left key.
     * Usually situated below the display on phones and used as a multi-function
     * feature key for selecting a software defined function shown on the bottom left
     * of the display.
     */
    SOFT_LEFT(1),
    /**
     * Key code constant: Soft Right key.
     * Usually situated below the display on phones and used as a multi-function
     * feature key for selecting a software defined function shown on the bottom right
     * of the display.
     */
    SOFT_RIGHT(2),
    /**
     * Key code constant: Home key.
     * This key is handled by the framework and is never delivered to applications.
     */
    HOME(3),
    /**
     * Key code constant: Back key.
     */
    BACK(4),
    /**
     * Key code constant: Call key.
     */
    CALL(5),
    /**
     * Key code constant: End Call key.
     */
    ENDCALL(6),;
    /**
     * Key code constant: '0' key.
     */

    private final int code;

    AndroidKeyCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
