package io.pivotal.errorHandling;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import retrofit.RetrofitError;

@ControllerAdvice
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {
    @Override
    public String getErrorPath() {
        return ErrorPathConstants.ERROR_PATH;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @RequestMapping(ErrorPathConstants.ERROR_PATH)
    public @ResponseBody String unknownError() {
        return new ErrorPresenter(ErrorMessages.UNKNOWN.getErrorMessage()).toJson();
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @RequestMapping(ErrorPathConstants.ERROR_NO_PARAMS_PATH)
    public @ResponseBody String errorNoParams() {
        return new ErrorPresenter(ErrorMessages.MISSING_PARAM.getErrorMessage()).toJson();
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @RequestMapping(ErrorPathConstants.ERROR_PARAM_OUT_OF_RANGE_PATH)
    public @ResponseBody String errorOutOfRangeQueryParams() {
        return new ErrorPresenter(ErrorMessages.PARAM_OUT_OF_RANGE.getErrorMessage()).toJson();
    }

    @ExceptionHandler({RetrofitError.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @RequestMapping(ErrorPathConstants.ERROR_RETROFIT_CONFIG_PATH)
    public @ResponseBody String errorRetrofitConfig() {
        return new ErrorPresenter(ErrorMessages.RETROFIT.getErrorMessage()).toJson();
    }
}