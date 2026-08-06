package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error;

public class IdInvalidException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IdInvalidException(String message) {
        super(message);
    }

    public IdInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
