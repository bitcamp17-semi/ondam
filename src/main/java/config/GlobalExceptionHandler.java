package config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception ex, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int code = 404; // 기본값

        if (status != null) {
            try {
                code = Integer.parseInt(status.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        String message;
        switch (code) {
            case 400 -> message = "잘못된 요청입니다.";
            case 403 -> message = "접근이 거부되었습니다.";
            case 404 -> message = "페이지를 찾을 수 없습니다.";
            case 500 -> message = "서버 내부 오류가 발생했습니다.";
            default -> message = "예상치 못한 오류가 발생했습니다.";
        }
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "error"; // templates/error/error.html
    }
}