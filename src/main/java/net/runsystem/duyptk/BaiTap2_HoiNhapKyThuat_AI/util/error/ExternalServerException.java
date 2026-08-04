package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.error;

public class ExternalServerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExternalServerException(String message) {
        super(message);
    }

    public ExternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
