package com.wjc.codetest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice(value = {"com.wjc.codetest.product.controller"})
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> runTimeException(Exception e) {
        log.error("status :: {}, errorType :: {}, errorCause :: {}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "runtimeException",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
/*
- 문제 1: 모든 RuntimeException을 무조건 서버에러(상태코드 500)으로 처리 하고 있습니다.
- 원인 : 코드에서 runtimeException을 직접 던지고, http상태 코드와 메세지를 구분하지 않고 있습니다.
- 개선안 : CustomException을 만들어 사용합니다.
예시
CodeTestException.java
public class CodeTestException extends RuntimeException {
    private ErrorCode errorCode;
	private String message;

	public CodeTestException(ErrorCode errorCode) {
		this.errorCode = errorCode;
		this.message = errorCode.getDescription();
	}
}

ErrorCode.java
public enum ErrorCode {
    INVALID_REQUEST(BAD_REQUEST, "잘못 된 요청"),
	NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "상품을 찾을 수 없음");

	private final HttpStatus httpStatus;
	private final String description;
}

ErrorResponse.java
public class ErrorResponse {
	private ErrorCode errorCode;
	private String message;
}

GlobalExceptionHandler.java
public class GlobalExceptionHandler {

	@ExceptionHandler(CodeTestException.class)
	public ResponseEntity<ErrorResponse> handleAuctionException(CodeTestException e) {
		return ResponseEntity.status(e.getErrorCode().getHttpStatus())
			.body(new ErrorResponse(errorCode, errorCode.getDescription()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {
		log.error("MethodArgumentNotValidException is occurred.", e);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorResponse(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDescription()));
	}
	.
	.
}
-선택 근거 :
예외를 구체화 하면 log로 남겨 확인하기 쉽고, 의미 있는 오류 메세지를 전달할 수 있습니다.
ProductService.java 의 getProductById 에서
throw new CodeTestException(ErrorCode.NOT_FOUND_PRODUCT); 처럼 사용하거나
클라이언트가 잘못 된 요청을 했을 때 보다 확실하고 빠르게 문제지점을 파악할 수 있습니다.

- 문제2 : GlobalExceptionHandler 가 특정 컨트롤러 패키지만 지정,
         가독성 문제
- 원인 : @ControllerAdvice(value = {"com.wjc.codetest.product.controller"})로 해당 패키지를 지정했고,
        @ControllerAdvice 와 @ResponseBody를 함께 사용 중
- 개선안 : value 부분을 삭제 하거나 GlobalExceptionHandler에서 ProductExceptionHandler로 이름을 변경하고 product패키지 안으로 이동,
          @ControllerAdvice + @ResponseBody를 한번에 @RestControllerAdvice로 변경
- 선택근거 :
해당 프로젝트에서는 뷰를 렌더링 하지 않기 때문에 RestApi server라고 판단 했습니다.
또, GlobalExceptionHandler는 모든 예외 처리를 담당하기 때문에 value 부분은 필요 없고,
만일 특정 모듈에만 적용하기 위함이라면 클래스 이름과 위치를 바꿔야 한다고 생각 했습니다.

 */
