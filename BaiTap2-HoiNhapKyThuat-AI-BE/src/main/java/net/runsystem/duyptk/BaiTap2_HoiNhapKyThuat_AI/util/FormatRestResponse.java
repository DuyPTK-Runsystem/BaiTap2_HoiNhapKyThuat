package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.responseDTO.RestResponse;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.util.annotation.ApiMessage;

@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }
        if (body instanceof String || body instanceof Resource || body instanceof RestResponse<?>
                || body instanceof byte[]) {
            return body;
        }

        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = servletResponse.getStatus();
        if (statusCode >= 400) {
            return body;
        }

        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(statusCode);
        restResponse.setData(body);

        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
        restResponse.setMessage(apiMessage == null ? "CALL API SUCCESS" : apiMessage.value());
        return restResponse;
    }
}
