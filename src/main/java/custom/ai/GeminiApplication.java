package custom.ai;

import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Builder;
import org.tinystruct.http.Request;
import org.tinystruct.http.Response;
import org.tinystruct.http.ResponseStatus;
import org.tinystruct.net.URLRequest;
import org.tinystruct.net.URLResponse;
import org.tinystruct.net.handlers.HTTPHandler;
import org.tinystruct.system.annotation.Action;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GeminiApplication — tinystruct-based proxy for the Google Gemini REST API.
 *
 * Endpoints:
 *   POST /api/gemini/generate         → generateContent  (non-streaming)
 *   POST /api/gemini/stream           → streamGenerateContent (SSE pass-through)
 *   POST /api/gemini/embed            → embedContent
 *   GET  /api/gemini/models           → list available models
 *
 * The Gemini API key is read from application.properties as {@code gemini.api.key}.
 * The model name defaults to {@code gemini-flash-latest} but can be overridden per-request
 * by including a {@code "model"} field in the JSON body.
 */
public class GeminiApplication extends AbstractApplication {

    private static final Logger logger = Logger.getLogger(GeminiApplication.class.getName());

    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models";
    private static final String DEFAULT_MODEL = "gemini-flash-latest";
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS    = 60_000;

    private String geminiApiKey;

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    @Override
    public void init() {
        this.geminiApiKey = this.getConfiguration().get("gemini.api.key");
        if (this.geminiApiKey == null || this.geminiApiKey.isEmpty()) {
            logger.warning("gemini.api.key is not configured in application.properties");
        }
        this.setTemplateRequired(false);
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    // -------------------------------------------------------------------------
    // POST /api/gemini/generate  — non-streaming generateContent
    // -------------------------------------------------------------------------

    @Action(value = "api/gemini/generate", mode = Action.Mode.HTTP_POST)
    public Object generate(Request<?, ?> request, Response<?, ?> response) throws ApplicationException {
        try {
            requireApiKey(response);

            String body = requireBody(request, response);
            if (body == null) return null;

            Builder requestJson = new Builder();
            requestJson.parse(body);

            String model = resolveModel(requestJson);

            // Build Gemini request payload (pass-through if body already contains "contents")
            String geminiPayload = buildGeneratePayload(requestJson, body);

            String url = GEMINI_BASE_URL + "/" + model + ":generateContent";
            Builder geminiResponse = callGemini(url, "POST", geminiPayload);

            response.addHeader("Content-Type", "application/json");
            return geminiResponse;

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calling Gemini generateContent", e);
            return errorResponse(response, e.getMessage(), ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/gemini/stream  — streaming generateContent (SSE)
    // -------------------------------------------------------------------------

    @Action(value = "api/gemini/stream", mode = Action.Mode.HTTP_POST)
    public Object stream(Request<?, ?> request, Response<?, ?> response) throws ApplicationException {
        try {
            requireApiKey(response);

            String body = requireBody(request, response);
            if (body == null) return null;

            Builder requestJson = new Builder();
            requestJson.parse(body);

            String model = resolveModel(requestJson);
            String geminiPayload = buildGeneratePayload(requestJson, body);

            String url = GEMINI_BASE_URL + "/" + model + ":streamGenerateContent?alt=sse";

            // Set SSE response headers
            response.addHeader("Content-Type", "text/event-stream");
            response.addHeader("Cache-Control", "no-cache");
            response.addHeader("X-Accel-Buffering", "no");

            // For SSE streams, we cannot parse the response as JSON (Builder) because it's prefixed with "data: ".
            // We use the raw call to forward the plain text.
            String geminiRaw = callGeminiRaw(url, "POST", geminiPayload);

            StringBuilder sseAccumulator = new StringBuilder();
            // Re-emit each SSE data line so callers get proper SSE format
            for (String line : geminiRaw.split("\n")) {
                sseAccumulator.append(line).append("\n");
            }

            return sseAccumulator.toString();
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calling Gemini streamGenerateContent", e);
            return errorResponse(response, e.getMessage(), ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/gemini/embed  — text embedding
    // -------------------------------------------------------------------------

    @Action(value = "api/gemini/embed", mode = Action.Mode.HTTP_POST)
    public Object embed(Request<?, ?> request, Response<?, ?> response) throws ApplicationException {
        try {
            requireApiKey(response);

            String body = requireBody(request, response);
            if (body == null) return null;

            Builder requestJson = new Builder();
            requestJson.parse(body);

            // Use text-embedding-004 by default for embedContent
            String model = requestJson.get("model") != null
                    ? requestJson.get("model").toString()
                    : "text-embedding-004";

            // Validate required "text" field
            if (requestJson.get("text") == null && !body.contains("\"content\"")) {
                return errorResponse(response, "Field 'text' or 'content' is required", ResponseStatus.BAD_REQUEST);
            }

            // Build embedContent payload
            String geminiPayload;
            if (body.contains("\"content\"")) {
                // Already in Gemini format — pass through
                geminiPayload = body;
            } else {
                // Wrap plain text
                String text = requestJson.get("text").toString();
                geminiPayload = "{"
                        + "\"model\":\"models/" + model + "\","
                        + "\"content\":{\"parts\":[{\"text\":" + jsonString(text) + "}]}"
                        + "}";
            }

            String url = GEMINI_BASE_URL + "/" + model + ":embedContent";
            Builder geminiResponse = callGemini(url, "POST", geminiPayload);

            response.addHeader("Content-Type", "application/json");

            return geminiResponse;
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calling Gemini embedContent", e);
            return errorResponse(response, e.getMessage(), ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/gemini/models  — list available models
    // -------------------------------------------------------------------------

    @Action(value = "api/gemini/models", mode = Action.Mode.HTTP_GET)
    public Object listModels(Request<?, ?> request, Response<?, ?> response) throws ApplicationException {
        try {
            requireApiKey(response);

            String url = "https://generativelanguage.googleapis.com/v1beta/models";
            Builder geminiResponse = callGemini(url, "GET", null);

            response.addHeader("Content-Type", "application/json");

            return geminiResponse;
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error listing Gemini models", e);
            return errorResponse(response, e.getMessage(), ResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Helper HTTP call to the Gemini REST API returning a parsed JSON Builder.
     * This validates the response structure implicitly.
     */
    protected Builder callGemini(String urlStr, String method, String jsonBody) throws Exception {
        String rawBody = callGeminiRaw(urlStr, method, jsonBody);
        Builder responseBody = new Builder();
        responseBody.parse(rawBody);
        return responseBody;
    }

    /**
     * Low-level HTTP call to the Gemini REST API returning the raw String.
     * Uses the tinystruct {@link URLRequest} and {@link HTTPHandler}.
     */
    protected String callGeminiRaw(String urlStr, String method, String jsonBody) throws Exception {
        URLRequest req = new URLRequest(new URL(urlStr));
        req.setMethod(method);
        req.setHeader("Content-Type", "application/json");
        req.setHeader("x-goog-api-key", geminiApiKey);

        if (jsonBody != null && !jsonBody.isEmpty()) {
            req.setBody(jsonBody);
        }

        HTTPHandler handler = new HTTPHandler();
        URLResponse urlResponse = handler.handleRequest(req);

        int statusCode = urlResponse.getStatusCode();
        String responseBody = urlResponse.getBody();
        if (statusCode < 200 || statusCode >= 300) {
            throw new Exception("Gemini API error [" + statusCode + "]: " + responseBody);
        }

        return responseBody.trim();
    }

    /**
     * Builds the {@code generateContent} JSON payload.
     * If the incoming body already contains a {@code "contents"} key it is used as-is
     * (Gemini native format). Otherwise a simpler {@code "prompt"} or {@code "message"}
     * field is wrapped appropriately.
     */
    private String buildGeneratePayload(Builder parsed, String rawBody) {
        // Already in Gemini native format — pass through
        if (rawBody.contains("\"contents\"")) {
            return rawBody;
        }

        // Convenience: accept "message" or "prompt"
        String text = null;
        if (parsed.get("message") != null) {
            text = parsed.get("message").toString();
        } else if (parsed.get("prompt") != null) {
            text = parsed.get("prompt").toString();
        }

        if (text == null) {
            // Fallback: forward raw body unchanged
            return rawBody;
        }

        StringBuilder payload = new StringBuilder();
        payload.append("{");
        payload.append("\"contents\":[{\"parts\":[{\"text\":").append(jsonString(text)).append("}]}]");

        // Forward optional generationConfig if present
        if (parsed.get("generationConfig") != null) {
            payload.append(",\"generationConfig\":").append(parsed.get("generationConfig").toString());
        }
        // Forward optional systemInstruction if present
        if (parsed.get("systemInstruction") != null) {
            payload.append(",\"systemInstruction\":").append(parsed.get("systemInstruction").toString());
        }

        payload.append("}");
        return payload.toString();
    }

    /** Resolves the Gemini model name from the request body (defaults to {@value #DEFAULT_MODEL}). */
    private String resolveModel(Builder parsed) {
        return (parsed.get("model") != null) ? parsed.get("model").toString() : DEFAULT_MODEL;
    }

    /** Ensures the API key is configured; returns an error response and throws if missing. */
    private void requireApiKey(Response<?, ?> response) throws ApplicationException {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            errorResponse(response, "Gemini API key is not configured on the server",
                    ResponseStatus.INTERNAL_SERVER_ERROR);
            throw new ApplicationException("gemini.api.key not configured");
        }
    }

    /** Reads and validates the request body; writes a BAD_REQUEST response and returns null if absent. */
    private String requireBody(Request<?, ?> request, Response<?, ?> response) {
        Object body = request.body();
        if (body == null) {
            errorResponse(response, "Missing JSON body", ResponseStatus.BAD_REQUEST);
            return null;
        }
        return body.toString();
    }

    /** Returns a JSON error payload and sets the HTTP status. */
    private String errorResponse(Response<?, ?> response, String message, ResponseStatus status) {
        response.setStatus(status);
        Builder result = new Builder();
        result.put("status", "error");
        result.put("message", message);
        return result.toString();
    }

    /** Escapes a Java string into a JSON string literal (including surrounding quotes). */
    private static String jsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}
