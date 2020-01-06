/**
 *
 */
package io.sinistral.proteus.server.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.sinistral.proteus.server.exceptions.ServerException;
import io.sinistral.proteus.server.predicates.ServerPredicates;
import io.undertow.server.DefaultResponseListener;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ExceptionHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jbauer
 *
 */
@Singleton
public class ServerDefaultResponseListener implements DefaultResponseListener
{
    private static Logger log = LoggerFactory.getLogger(ServerDefaultResponseListener.class.getCanonicalName());

    @Inject
    protected XmlMapper xmlMapper;

    @Inject
    protected ObjectMapper objectMapper;

    @Override
    public boolean handleDefaultResponse(HttpServerExchange exchange)
    {
        if (!exchange.isResponseChannelAvailable()) {
            return false;
        }

        int statusCode = exchange.getStatusCode();

        Throwable throwable = exchange.getAttachment(ExceptionHandler.THROWABLE);

        if(throwable == null)
        {
            throwable = exchange.getAttachment(DefaultResponseListener.EXCEPTION);
        }

        if (statusCode >= 400 || throwable != null) {

            final Map<String, String> errorMap = new HashMap<>();

            final String path = exchange.getRelativePath();

            if (throwable == null) {
                final String reason = StatusCodes.getReason(statusCode);

                throwable = new Exception(reason);
            } else if(throwable instanceof ServerException)
            {
                ServerException serverException = (ServerException) throwable;
                exchange.setStatusCode(serverException.getStatus());
                statusCode = serverException.getStatus();
            }

            errorMap.put("exceptionClass", throwable.getClass().getName());
            errorMap.put("message", throwable.getMessage());
            errorMap.put("path", path);
            errorMap.put("code", Integer.toString(statusCode));

            log.error("\n\tmessage: " + throwable.getMessage() + "\n\tpath: " + path, throwable);

            if (throwable.getStackTrace() != null) {
                if (throwable.getStackTrace().length > 0) {
                    errorMap.put("className", throwable.getStackTrace()[0].getClassName());
                }

                StringWriter sw = new StringWriter();

                throwable.printStackTrace(new PrintWriter(sw));

                String exceptionAsString = sw.toString();
                List<String> stringList = Arrays.stream(exceptionAsString.split("\n")).collect(Collectors.toList());

                try {
                    errorMap.put("stackTrace", objectMapper.writeValueAsString(stringList));
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
            }

            if (throwable instanceof IllegalArgumentException) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            }

            if (ServerPredicates.ACCEPT_XML_EXCLUSIVE_PREDICATE.resolve(exchange)) {
                try {

                    final String xmlBody = xmlMapper.writeValueAsString(errorMap);

                    exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, xmlBody.length());
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MediaType.APPLICATION_XML);
                    exchange.getResponseSender().send(xmlBody);

                } catch (JsonProcessingException e) {
                    log.warn("Unable to create XML from error...");
                }
            } else {
                String jsonBody;

                try {
                    jsonBody = objectMapper.writeValueAsString(errorMap);
                } catch (Exception e) {
                    jsonBody = errorMap.toString();
                }

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, jsonBody.length());
                exchange.getResponseSender().send(jsonBody);
            }

            return true;
        }

        return false;
    }
}



